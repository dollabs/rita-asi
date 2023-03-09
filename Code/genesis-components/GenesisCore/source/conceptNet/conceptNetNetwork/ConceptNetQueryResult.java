package conceptNet.conceptNetNetwork;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the result of a query. The result if of type T. This class also tracks
 * which query this result is for.
 * 
 * @author bryanwilliams
 *
 * @param <T> the type of the result
 */
public class ConceptNetQueryResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ConceptNetQuery<T> query;
    private final T result;

    public ConceptNetQueryResult(ConceptNetQuery<T> query, T result) {
        this.query = query;
        this.result = result;
    }

    public ConceptNetQuery<T> getQuery() {
        return query;
    }

    public T getResult() {
        return result;
    }

    /**
     * returns the maximum result from a collection of a results. Requires the result type to be comparable.
     */
    public static <C extends Comparable<C>> ConceptNetQueryResult<C> max(Collection<ConceptNetQueryResult<C>> args) {
        return Collections.max(args, (arg1, arg2) -> arg1.getResult().compareTo(arg2.getResult()));
    }
    
    /**
     * "Flattens" a single result of a list of scored assertions into a list of individual results
     */
    public static List<ConceptNetQueryResult<Double>> flattenResult(
            ConceptNetQueryResult<List<ConceptNetScoredAssertion>> resultList) {
        return resultList.getResult().stream()
                .map(result -> new ConceptNetQueryResult<>(new ConceptNetAssertionQuery(result), result.getScore()))
                .collect(Collectors.toList());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConceptNetQueryResult<?> other = (ConceptNetQueryResult<?>) obj;
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConceptNetQueryResult [query=" + query + ", result=" + result + "]";
    }  

}
