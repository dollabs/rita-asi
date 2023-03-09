package conceptNet.conceptNetNetwork;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import conceptNet.conceptNetModel.ConceptNetAssertion;
import conceptNet.conceptNetModel.ConceptNetConcept;
import conceptNet.conceptNetModel.ConceptNetFeature;
import constants.Switch;
import utils.Mark;
import utils.NewTimer;

/**
 * ConceptNetClient provides convenient queries that allow users to easily get data from ConceptNet without
 * worrying about any of the networking details. ConceptNet sits between the networking code and the user.
 * Users just need to create instances of the ConceptNet ideas they wish to model (see conceptNetModel package)
 * and submit these objects to any of ConceptNetClient's public methods.
 * 
 * Note that ConceptNetClient primarily uses ConceptNet 4, not ConceptNet 5. Currently, the only place where ConceptNet 5
 * is used is in the getSimilarityScore() method, where the relatedness score from ConceptNet 5 is combined with the similarity
 * score from ConceptNet 4 to compute an overall similarity score.
 * 
 * If you're interested in using this class, and especially if you're interested in *extending* this class, the following guide
 * is hopefully helpful: https://goo.gl/4PpNOL
 * 
 * @author bryanwilliams
 *
 */
public class ConceptNetClient {
    private static final boolean debug = false;
    private static final String CN5_RELATEDNESS_URL = "http://api.conceptnet.io/related/c/en/%s?filter=/c/en%s";
    private static final String CACHE_FILENAME = "conceptnet.data";
    private static Map<ConceptNetQuery<?>, ConceptNetQueryResult<?>> cache = new HashMap<>();
    private static final String CN4_URL = "http://d4d.media.mit.edu:5678/d4d";

    private static RelatedGroup relatedGroupAtURL(String url) throws IOException {
        NewTimer.conceptNetTimer.turnOn();
        HttpURLConnection connection;
        while (true) {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36");
            connection.addRequestProperty("Accept", "application/json");
            // too many requests
            if (connection.getResponseCode() == 429) {
                try {
                    Mark.say("Pausing similarity analysis due to ConceptNet5's rate limit...");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
            } else {
                break;
            }
        }
            
        InputStream response = connection.getInputStream();
        Scanner scanner = new Scanner(response);
        String responseBody = scanner.useDelimiter("\\A").next();
        ObjectMapper mapper = new ObjectMapper();
        RelatedGroup sg = mapper.readValue(responseBody, RelatedGroup.class);
        scanner.close();
        NewTimer.conceptNetTimer.turnOff();
        return sg;
    }
    
    /**
     * Returns the CN5 RelatedGroup for the base word.
     * A "RelatedGroup" is a group of concepts CN5 has identified as related to the base word. Every
     * related concept is paired with a score. If CN5 does not have this word as a concept, an empty RelatedGroup
     * will be returned.
     */
    public static RelatedGroup getRelatedGroup(String baseWord) throws IOException {
        String url = String.format(CN5_RELATEDNESS_URL, baseWord, "");
        RelatedGroup rg =  relatedGroupAtURL(url);
        return rg;
    }
    
    /**
     * Returns the geometric mean of the CN5 relatedness score and the CN4 similarity score for the two concepts.
     * We combine these two scores because it seems to be more robust than using either one alone.
     * If the two concepts are not present in both CN4 and CN5, returns a score of zero.
     */
    public static ConceptNetQueryResult<Double> getSimilarityScore(String s1, String s2) {
        return getSimilarityScore(new ConceptNetConcept(s1), new ConceptNetConcept(s2));
    }
    
    /**
     * Returns the geometric mean of the CN5 relatedness score and the CN4 similarity score for the two concepts.
     * We combine these two scores because it seems to be more robust than using either one alone.
     * If the two concepts are not present in both CN4 and CN5, returns a score of zero.
     * 
     * This is modified by ZhijingJin in May 2018, because conceptNet4's host server does not respond.
     * Therefore, only the score of ConceptNet5 is returned and cached
     */
    public static ConceptNetQueryResult<Double> getSimilarityScore(ConceptNetConcept c1, ConceptNetConcept c2) {
        ConceptNetSimilarityQuery simQuery = new ConceptNetSimilarityQuery(c1, c2);
        
        if (isCached(simQuery)) {
            Mark.say(debug, "Using cached result for", simQuery);
            return cachedResult(simQuery);
        }
//        // if the two concepts aren't present in both CN4 and CN5, will return score of 0
//        // otherwise, return geometric mean of the two scores
        ConceptNetQueryResult<Double> result;
//        if (!simQuery.meetsPrerequisites()) {
//            // don't cache the fact that prereqs are not met because it could be due to transient error like 
//            // server being down. if prereqs truly are not met, the results of the calls checking the prereqs
//            // are cached anyways, so not losing much performance
//            return simQuery.defaultResult();
//        } else {
//            ConceptNetQueryResult<Double> cn4Result = conceptNet4SimilarityScore(simQuery);
            ConceptNetQueryResult<Double> cn5Result = conceptNet5RelatednessScore(simQuery);
            double geometricMean = Math.sqrt(Math.max(0, cn5Result.getResult())
                                            *Math.max(0, cn5Result.getResult()));
//            double geometricMean = Math.sqrt(Math.max(0, cn4Result.getResult())
//                    *Math.max(0, cn5Result.getResult()));

            result = new ConceptNetQueryResult<Double>(simQuery, geometricMean);
//        }
        
        // overwrites what CN4 put into cache for this query when conceptNet4SimilarityScore was called
        putInCache(simQuery, result);
        return result;
    }
    
    private static String formatConceptNet5Concept(String concept) {
        return concept.replaceAll("(\\s+|-+)", "_");
    }
    
    private static ConceptNetQueryResult<Double> conceptNet5RelatednessScore(ConceptNetSimilarityQuery simQuery) {
        List<ConceptNetConcept> concepts = simQuery.getComponentConcepts();
        String s1 = formatConceptNet5Concept(concepts.get(0).getConceptString());
        String s2 = formatConceptNet5Concept(concepts.get(1).getConceptString());
        try {
            String url = String.format(CN5_RELATEDNESS_URL, s1, "/"+s2);
            List<RelatedGroupElement> groupResult = relatedGroupAtURL(url).getSimilarWords();
            double score;
            if (groupResult.isEmpty()) {
                score = -1;
            } else {
                score = groupResult.get(0).getScore();   
            }
            Mark.say(debug, "CN5 Score for relatedness", s1, s2, score);
            return new ConceptNetQueryResult<Double>(simQuery, score);
        } catch(IOException e) {
            Mark.say("CN5 relatedness error", e);
            return simQuery.defaultResult();
        }
    }
    
    // surround with quotes
    static String stringify(String s) {
        return "\"" + s + "\"";
    }
    
    static List<String> stringify(Collection<String> strings) {
        return strings.stream()
                .map(ConceptNetClient::stringify)
                .collect(Collectors.toList());
    }
    
    private static <T> ConceptNetQueryResult<T> fireConceptNet4Query(ConceptNetQuery<T> query) throws IOException {
		boolean debug = true;

        if (isCached(query)) {
            Mark.say(debug, "Using cached result for", query);
            return cachedResult(query);
        }
        
        if (!query.meetsPrerequisites()) {
            Mark.say(debug, query, "did not meet its prereqs, returning default result");
            // don't cache the fact that prereqs are not met because it could be due to transient error like 
            // server being down. if prereqs truly are not met, the results of the calls checking the prereqs
            // are cached anyways, so not losing much performance
            ConceptNetQueryResult<T> result = query.defaultResult();
            return result;
        }
        
        NewTimer.conceptNetTimer.turnOn();
        Mark.say(debug, query, "beginning to fire!");
        
        String methodName = query.getMethodName();
        List<String> args = query.getArguments();
        Map<String, String> params = new HashMap<>();
        params.put("class_name", "d4d");
        params.put("instance_name", "c4");
        params.put("method_name", methodName);
        for (int i = 0; i < args.size(); i++) {
            params.put("arg"+i, args.get(i));
        }
        String dataString = getDataString(params);

		Mark.say("xxx");
                
        URLConnection connection = new URL(CN4_URL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.addRequestProperty("Accept", "application/json");
        try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(dataString.getBytes(StandardCharsets.UTF_8));
         }
        
		Mark.say("yyy");

        connection.setReadTimeout(15*1000);
        InputStream responseStream = connection.getInputStream();
        Scanner scanner = new Scanner(responseStream);
        String responseBody = scanner.useDelimiter("\\A").next();
        scanner.close();
        // uses Jackson to read JSON into ConceptNet4Respones instance
        ObjectMapper mapper = new ObjectMapper();
        ConceptNet4Response response = mapper.readValue(responseBody, ConceptNet4Response.class);

		Mark.say("zzz");

        if (response.error()) {
            Mark.err("ConceptNet4 responded with error: "+response.getErrorMessage());
        }
        T resultObj = query.parseResult(response.getResult());
        if (resultObj == null) {
            Mark.err("Unable to parse result from server: ", response.getResult(), "for query", query);
            return query.defaultResult();
        }
        ConceptNetQueryResult<T> result = new ConceptNetQueryResult<T>(query, resultObj);
        NewTimer.conceptNetTimer.turnOff();
		Mark.say("+++");
        putInCache(query, result);
        return result;
    }
    
    // credit to http://stackoverflow.com/questions/40574892/how-to-send-post-request-with-x-www-form-urlencoded-body
    private static String getDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");    
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }    
        return result.toString();
    }
    
    // "Safe" because reports the error if there was an exception
    private static <T> ConceptNetQueryResult<T> fireConceptNet4QuerySafe(ConceptNetQuery<T> query) {
        try {
            return fireConceptNet4Query(query);
        } catch (IOException e) {
            ConceptNetQueryResult<T> defaultResult = query.defaultResult();
            Mark.err("Error firing ConceptNet4 query", e, "\nusing default result", defaultResult);
            return defaultResult;
        }
    }
    
    public static ConceptNetQueryResult<Boolean> conceptExists(ConceptNetConcept concept) {
        return fireConceptNet4QuerySafe(new ConceptNetConceptExistsQuery(concept));
    }
    
    public static ConceptNetQueryResult<Boolean> conceptExists(String concept) {
        return conceptExists(new ConceptNetConcept(concept));
    }
    
    private static ConceptNetQueryResult<Double> conceptNet4SimilarityScore(ConceptNetSimilarityQuery simQuery) {
        return fireConceptNet4QuerySafe(simQuery);
    }
    
    /**
     * Note that if ConceptNet does not contain either of the concepts in the assertion, a score of 0 will be returned.
     */
    public static ConceptNetQueryResult<Double> howTrueIs(ConceptNetAssertion assertion) {
        ConceptNetAssertionQuery sparseQuery = new ConceptNetAssertionQuery(assertion);
        ConceptNetAssertionQuery analogySpaceQuery = new ConceptNetAssertionQuery(assertion, true);
        List<ConceptNetQueryResult<Double>> results = new ArrayList<>();
        // try to use both sparse matrix and AnalogySpace. If both queries fail, returns default result (0).
        // If both succeed, return the maximum result. Otherwise, return the result of the query that succeeded.
        try {
            results.add(fireConceptNet4Query(sparseQuery));
        } catch (IOException e) {
            Mark.err("Error firing ConceptNet4 how_true_is sparse query", e);
        }
        try {
            results.add(fireConceptNet4Query(analogySpaceQuery));
        } catch (IOException e) {
            Mark.err("Error firing ConceptNet4 analogy space query", e);
        }
        if (results.size() == 0) {
            return sparseQuery.defaultResult();
        }
        return ConceptNetQueryResult.max(results);
    }
    
    /**
     * Returns all the ways a ConceptNet feature can be completed into a full ConceptNet assertion and associated score.
     */
    public static ConceptNetQueryResult<List<ConceptNetScoredAssertion>> featureToAssertions(ConceptNetFeature feature) {
        return fireConceptNet4QuerySafe(new ConceptNetFeatureQuery(feature));
    }
    
    public static boolean isCached(ConceptNetQuery<?> query) {
       if (!Switch.useConceptNetCache.isSelected()) {
            return false;
        }
        return cache.containsKey(query);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> ConceptNetQueryResult<T> cachedResult(ConceptNetQuery<T> query) {
        return (ConceptNetQueryResult<T>) cache.get(query);
    }
    
    private static <T> void putInCache(ConceptNetQuery<T> query, ConceptNetQueryResult<T> result) {
        if (Switch.useConceptNetCache.isSelected()) {
            cache.put(query, result);
        }
    }
    
    private static File getCacheFile() {
        return new File(System.getProperty("user.home") + File.separator + 
                CACHE_FILENAME);
    }
    
    private static int cacheSize() {
        return cache.size();
    }
    
    // WARNING: the map must have the property that every key's type (e.g. Double query)
    // and value's type (e.g. Double result) must match. If the cache is only modified through
    // putInCache() method, this will be automatically enforced
    private static void setCache(Map<ConceptNetQuery<?>, ConceptNetQueryResult<?>> newCache) {
        cache = new HashMap<>(newCache);
    }
    
    public static void purgeCache() {
        Mark.say("Purging concept net cache,", getCacheFile(), "of", cacheSize(), "items");
        cache.clear();
        writeCache();
    }

    @SuppressWarnings("unchecked")
    public static void readCache() {
        File inputFile = getCacheFile();
        if (!inputFile.exists()) {
            Mark.say("No concept net cache,", inputFile, "to load");
            return;
        }
        Mark.say("Loading concept net cache");
        try {
            FileInputStream fileInputStream = new FileInputStream(getCacheFile());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            if (object != null) {
                setCache((Map<ConceptNetQuery<?>, ConceptNetQueryResult<?>>) object);
                Mark.say(true, "Number of items read: " + cacheSize());
            }
            objectInputStream.close();
            fileInputStream.close();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Mark.say("Concept net cache could not be read, sticking with existing cache");
        }
    }
    
    public static void writeCache() {
        Mark.say("Writing ConceptNet cache");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getCacheFile());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(cache);
            objectOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Mark.say("Number of concept net items written:", cacheSize());
    }
}
