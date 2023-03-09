// Updated 10 June 2015

package bryanWilliams;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.list.FixedSizeList;

import com.google.common.primitives.Doubles;

import bryanWilliams.Coherence.CauseGraph;
import bryanWilliams.Coherence.CauseNode;
import bryanWilliams.Coherence.OrganizationalCoherenceEvaluator;
import bryanWilliams.Learning.PredictedRule;
import bryanWilliams.Learning.RelatedRankingComparator;
import bryanWilliams.Learning.ReasoningNode;
import bryanWilliams.Learning.ScoreSimilarityComparator;
import bryanWilliams.Learning.SelfRuleTeacher;
import conceptNet.conceptNetModel.ConceptNetAssertion;
import conceptNet.conceptNetModel.ConceptNetFeature;
import conceptNet.conceptNetModel.ConceptNetJustification;
import conceptNet.conceptNetModel.ConceptNetFeature.FeatureType;
import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.ConceptNetQueryResult;
import conceptNet.conceptNetNetwork.ConceptNetScoredAssertion;
import bryanWilliams.goalAnalysis.AspireEngine;
import connections.AbstractWiredBox;
import connections.Connections;
import constants.Markers;
import dictionary.PennTag;
import dictionary.WordNet;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import gui.Spider;
import gui.TextViewer;
import matchers.EntityMatcher;
import matchers.StandardMatcher;
import matchers.StructureMapper;
import matchers.representations.BindingsWithProperties;
import start.Start;
import storyProcessor.StoryProcessor;
import translator.BasicTranslator;
import utils.Mark;
import utils.tools.Constructors;
import utils.tools.Predicates;

/**
 * A local processor class that just receives a complete story description, takes apart the wrapper object to fetch
 * various parts of the complete story description, and prints them so you can see what is in there.
 */
public class LocalProcessor extends AbstractWiredBox {

	// EXamples of how ports are named, not used here
	public final String MY_INPUT_PORT = "my input port";

	public final String MY_OUTPUT_PORT = "my output port";
	
	private final boolean SHOW_COMMONSENSE = true;
	private final boolean DEBUG = true;
	private final boolean READING_MACBETH = false;

	/**
	 */
	public LocalProcessor() {
		super("Local story processor");
		// Receives story processor when story has been processed
		Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT, this::reset);

		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::processStoryProcessor);
	}

	/**
	 * You have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 * <p>
	 * Writes information extracted from the story processor received on the port.
	 */
	public void processStoryProcessor(Object signal) {
		// Make sure it is what was expected
		Mark.say("Entering processStoryProcessor");

		if (signal instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) signal;   

			Sequence story = processor.getStory();
			Sequence explicitElements = processor.getExplicitElements();
			Sequence inferences = processor.getInferredElements();
			Sequence concepts = processor.getInstantiatedConceptPatterns();
									
			Generator generator = Generator.getGenerator();
			story.stream().forEach(e -> Mark.say(DEBUG, generator.generate(e)));
			int storySize = story.getElements().size();
			Mark.say("Number of events:", storySize);
			
			for (Entity ent : story.getElements()) {
			    Mark.say(ent);
//			    Mark.say("Proper names", Util.getAllDeepComponents(ent)
//			            .stream()
//			            .filter(e -> e.hasProperty(Markers.PROPER))
//			            .collect(Collectors.toList()));
//			    Mark.say(ent.getFeatures());
//			    Mark.say(ent.getPropertyList());
			    Mark.say(ent.hash());
//			    if (ent.toEnglish().contains("eyes")) {
//			        Entity goal = ent;//BasicTranslator.getTranslator().translate("Matt wants to close his eyes").get(0);
//			        Mark.say(goal);
//			        Mark.say(goal.getFeatures());
//			        Entity wantedAction = goal.getObject().getElement(0).getSubject();
//			        Mark.say(wantedAction);
//			        Mark.say(wantedAction.getFeatures());
//			        Mark.say(wantedAction.toEnglish());
//			    }
			    Mark.say("\n");
			}
            
            for (Entity rule : processor.getCommonsenseRules().getElements()) {
//                System.out.println(Util.toEnglishAndInnerese(rule));
                //System.out.println(rule.toEnglish());
//                for (Entity e : Util.getAllDeepComponents(rule)) {
//                    //System.out.println(e + " " + e.getBundle());
//                    System.out.println(e + " " +e.getTypes());
//                }
//                System.out.println("\n");
            }
            System.out.println(processor.getCommonsenseRules().size()+" total cs rules");
            
            if (READING_MACBETH) {
                Entity killBeth = null;
                Entity murderLMD = null;
                List<Entity> storyElements = story.getElements();
                for (int i = 0; i < storyElements.size(); i++) {
                    Entity event = storyElements.get(i);
                    if (event.toEnglish().equals("Macbeth murders Lady Macduff.")) {
                        murderLMD = event;
                        System.out.println("murderLMD index: "+i);
                    } else if (event.toEnglish().equals("Macduff kills Macbeth.")) {
                        killBeth = event;
                        System.out.println("killBeth index: "+i);
                    }
                }
                assert killBeth != null;
                assert murderLMD != null;

                System.out.println("killBeth: "+killBeth.toEnglish());
                System.out.println("killBeth inner: "+killBeth);
//                System.out.println("killBeth obj: "+killBeth.getObject().toEnglish());
//                System.out.println("killBeth subj: "+killBeth.getSubject().toEnglish());
//                System.out.println("kilBeth deep comps: "+Util.getAllDeepComponents(killBeth));

                System.out.println("murderLMD: "+murderLMD.toEnglish());
                System.out.println("murderLMD inner: "+murderLMD);
//                System.out.println("murderLMD obj: "+murderLMD.getObject().toEnglish());
//                System.out.println("murderLMD subj: "+murderLMD.getSubject().toEnglish());
//                System.out.println("murderLMD  deep comps: "+Util.getAllDeepComponents(murderLMD));
                SelfRuleTeacher ruleTeacher = new SelfRuleTeacher(story, processor.getCommonsenseRules());
                
                ReasoningNode explanations = ruleTeacher.generateExplanations(killBeth, false);
                System.out.println();
                System.out.println("---generated "+explanations.numNodesInTree()+" explanations---");
                generateReasoningTreeView(explanations.getVisualGraph());
                
                ReasoningNode predictions = ruleTeacher.generatePredictions(murderLMD, false);
                System.out.println();
                System.out.println("---generated "+predictions.numNodesInTree()+" predictions---");
                generateReasoningTreeView(predictions.getVisualGraph());
                
                List<PredictedRule> predictedRules = ruleTeacher.generatePredictedRules(explanations, predictions);
                int similarRuleCount = 0;
                for (PredictedRule rule : predictedRules) {
//                    System.out.println();
//                    System.out.println("Possible Cause: "+rule.getExplicitCause().toEnglish() + " (from "+rule.getCauseNode().chainFromRoot()+")");
//                    System.out.println("Possible Effect: "+rule.getEffect().toEnglish()+ " (from "+rule.getEffectNode().chainFromRoot()+")");
//                    for (Entity bridge : rule.getBridgeCauses()) {
//                        System.out.println("Possible Bridge: "+bridge.toEnglish());
//                    }
                    for (Entry<Relation, Entity> rulePair : rule.getSimilarRules(processor.getCommonsenseRules(), 5).entrySet()) {
                        similarRuleCount++;
                        Relation similarRule = rulePair.getKey();
                        Entity csRule = rulePair.getValue();
                        Relation generalizedRule = Util.withPlaceholders(similarRule);
                        System.out.println("similarRule: "+similarRule);
                        System.out.println("Full eng: "+similarRule.toEnglish());
                        System.out.print("I predict "+similarRule.getObject().toEnglish().replace(".", "")+" because "+similarRule.getSubject().toEnglish()+" ");
                        System.out.println("I predict this because the rule "+csRule.toEnglish()+" is quite similar.");
                        System.out.println("From this, we can generalize "+generalizedRule.getObject().toEnglish().replace(".", "")+" because "+generalizedRule.getSubject().toEnglish());
                        System.out.println();
                    }
                }
                System.out.println();
                System.out.println("Total num predicted rules: " + predictedRules.size());
                System.out.println("Total num similar rules: "+similarRuleCount);
            }
                
            CauseGraph causeGraph = new CauseGraph(processor.getStory());
//            Mark.say("Number of graph nodes:", causeGraph.numNodes());
//            Mark.say("Number of roots:", causeGraph.numRoots());
//            Mark.say("Greatest cause chain length:", causeGraph.maxChainLength());
//            Mark.say("Maximum branching factor:", causeGraph.maxBranchingFactor());
//            Mark.say("Number of weakly connected components:", causeGraph.numWeaklyConnectedComponents());
                        
			//generateCausationGraphView(causeGraph.getVisualGraph());
			//generateSpiderDiagram(causeGraph);
			System.out.println("Time spent talking to CN: "+Util.time/1000.0);
			
			//showConceptNetView(story);
			
			if (READING_MACBETH) {
			    //OrganizationalCoherenceEvaluator oce = new OrganizationalCoherenceEvaluator(explicitElements, story, causeGraph);
			    //oce.generateActionGraph();
			    //Mark.say("Organizational coherence: ", oce.commentOnCoherence());
			}
			
//			GoalAnalyzer ga = new GoalAnalyzer(processor.getCommonsenseRules());
//			Mark.say("About to scan elements");
//			for (Entity e : story.getElements()) {
//			    Mark.say("Scanned element");
//			    ga.processEvent(e);
//			}
            
//			processor.getMentalModel().getCauseGraphViewer().process(story);
//			processor.getMentalModel().getViewerWrapper().repaint();
			
            /*
            ConflictEvaluator ce = new ConflictEvaluator(story, processor.getCommonsenseRules(), causeGraph);
            ce.sayConflicts();
            Mark.say(ce.commentOnPrimaryConflict());
            */
			
//	        Entity ang = BasicTranslator.getTranslator().translateToEntity("XX doesn't anger YY.");
//	        Entity noAng = BasicTranslator.getTranslator().translateToEntity("YY angers XX.");
//	        System.out.println(ang+"; "+noAng);
//	        System.out.println(new EntityMatcher().match(noAng, ang).semanticMatch);
//	        System.out.println(new EntityMatcher().match(ang, noAng).semanticMatch);
//	        
//	        Entity noAng2 = BasicTranslator.getTranslator().translateToEntity("ZZ angers YY.");
//	        System.out.println(new EntityMatcher().match(noAng, noAng2).semanticMatch);
//	        System.out.println(new EntityMatcher().match(noAng2, noAng).semanticMatch);
		}
	}

	public void reset(Object signal) {
		// Does nothing right now
	}
//	
//	public void showConceptNetView(Sequence story) {
//	    JPanel view = new JPanel();
//	    TextViewer topView = new TextViewer();
//	    JPanel bottomView = new JPanel();
//	    view.setLayout(new BorderLayout());
//	    view.add(topView, BorderLayout.NORTH);
//	    
//	    String text = "";
//	    for (Entity entity : story.getElements()) {
//	        Mark.say("CN view got entity", entity.toEnglish(), "with properties", entity.getPropertyList());
//	        if (entity.hasProperty(Markers.CONCEPTNET_JUSTIFICATION)) {
//	            @SuppressWarnings("unchecked")
//                List<ConceptNetJustification> justification = (List<ConceptNetJustification>) entity.getProperty(Markers.CONCEPTNET_JUSTIFICATION);
//                String justificationStr = ConceptNetJustification.toJustificationString(justification);
//                if (entity.relationP()) {
//                    // remove periods (most likely trailing)
//                    String entEnglish = entity.toEnglish().replaceAll("\\.", "");
//                    text += "Justification for "+entEnglish+": "+justificationStr+"\n";
//                }
//               // view.addText("Connection between "+entity.getProperty(Markers.CONCEPTNET_JUSTIFICATION).toString());
//	        }
//	    }
//	    
//	    topView.addText(text);
//	    
//	    GroupLayout layout = new GroupLayout(bottomView);
//	    bottomView.setLayout(layout);
//	    layout.setAutoCreateContainerGaps(true);
//	    layout.setAutoCreateGaps(true);
//	    JLabel result = new JLabel("blank for now");
//	    JButton similarityButton = new JButton("Push me!");
//	    similarityButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                result.setText(Math.random()+"");
//            }
//	    });
//	    layout.setHorizontalGroup(
//	            layout.createSequentialGroup()
//	                .addComponent(similarityButton)
//	                .addComponent(result)
//	            );
//	    layout.setVerticalGroup(
//	            layout.createParallelGroup(Alignment.CENTER)
//	                .addComponent(similarityButton)
//	                .addComponent(result)
//	            );
//	    bottomView.repaint();
//	    view.add(bottomView, BorderLayout.SOUTH);
//	    System.out.println("component count: "+bottomView.getComponentCount());
//	    
//	    JFrame frame = new JFrame("CN view");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.getContentPane().add(view); 
//        frame.pack();
//        frame.setVisible(true); 
//	}
//	
	private void generateCausationGraphView(DirectedGraph<CauseNode, String> g) {
        Layout<CauseNode, String> kklayout = new KKLayout<CauseNode, String>(g); //KKLayout is best, FRLayout is also cool
        kklayout.setSize(new Dimension(650, 650));
        VisualizationViewer<CauseNode,String> v = new VisualizationViewer<CauseNode,String>(kklayout);
        v.setPreferredSize(new Dimension(700,700)); //Sets the viewing area size
        v.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<CauseNode>());
        DefaultModalGraphMouse<CauseNode, String> gm = new DefaultModalGraphMouse<CauseNode, String>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        v.setGraphMouse(gm); 
        
        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(v); 
        frame.pack();
        frame.setVisible(true); 
	}
	
	private void generateReasoningTreeView(Tree<ReasoningNode, String> t) {
	    Layout<ReasoningNode, String> treeLayout = new TreeLayout<ReasoningNode, String>(t);
	    VisualizationViewer<ReasoningNode, String> v = new VisualizationViewer<>(treeLayout);
	    v.setPreferredSize(new Dimension(700, 700));
	    v.getRenderContext().setVertexLabelTransformer(new Transformer<ReasoningNode, String>() {
            @Override
            public String transform(ReasoningNode node) {
                return node.getSubstitutedEntity().toEnglish();
                //return Util.toEnglishAndInnerese(node.getSubstitutedEntity());
            }
	    });
        DefaultModalGraphMouse<ReasoningNode, String> gm = new DefaultModalGraphMouse<ReasoningNode, String>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        v.setGraphMouse(gm); 
        
        JFrame frame = new JFrame("Simple Tree View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(v); 
        frame.pack();
        frame.setVisible(true); 
	}
	
	private void generateSpiderDiagram(CauseGraph causeGraph) {
	    Spider spider = new Spider();
	    String [] axisLabels = {"# Total Nodes / # Roots", "Max Chain Length / # Total Nodes", "# Total Nodes / Max Branching Factor",
	            "# Total Nodes / # Connected Components"};
	    double numNodes = causeGraph.numNodes();
	    // Numbers chosen based on maximum out of Caesar, Hamlet, and Macbeth demos
	    double [] spiderData = {(numNodes/causeGraph.numRoots())/1.85, (causeGraph.maxChainLength()/numNodes)/0.444,
	            (numNodes/causeGraph.maxBranchingFactor())/12.05, (numNodes/causeGraph.numWeaklyConnectedComponents())/4.8};
	    spider.setData(spiderData);
	    spider.setAxisLabels(axisLabels);
	    JFrame frame = new JFrame();
	    frame.getContentPane().add(spider);
	    frame.setBounds(100, 100, 500, 700);
	    frame.addWindowListener(new WindowAdapter () { 
	                       public void windowClosing(WindowEvent e) { 
	                       System.exit(0); 
	                      }} 
	                     ); 
	    spider.setDataColor(Color.RED);
	    spider.setAreaColor(Color.LIGHT_GRAY);
	    frame.show();
	    spider.setConnectDots(true);
	    spider.setFillArea(true);
	}

	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//	    
//      BasicTranslator.getTranslator().translate("XX and YY are persons.");
//      Mark.say(BasicTranslator.getTranslator().translate("If XX is relaxing and YY disturbs XX, then XX feels anger towards YY.").get(0));

	    
//      Mark.say(BasicTranslator.getTranslator().translate("Matt and Jackie are people."));
//    Entity kiss = BasicTranslator.getTranslator().translate("Matt kisses Jackie and Helen.");
////    Entity lips = BasicTranslator.getTranslator().translate("Matt kisses Jackie.").get(0);
//    Mark.say(kiss);
////    Mark.say(lips);
////    Mark.say(new EntityMatcher().match(kiss, lips));
//    
//    
//    Mark.say(BasicTranslator.getTranslator().translate("Matt wants to slap Jackie."));
	    
//	    Mark.say(BasicTranslator.getTranslator().translate("XX is a person"));
//	       Mark.say(BasicTranslator.getTranslator().translate("YY is a person"));
//
//	    
//	    Entity e = BasicTranslator.getTranslator().translate("If XX wants to harm YY, XX is a bully.").get(0);
//	    Mark.say(e);
//	    Mark.say(Util.getAllDeepComponents(e).stream().map(Entity::getBundle).collect(Collectors.toList()));
    
//	    Mark.say(true, Util.getAllDeepComponents(BasicTranslator.getTranslator().translate("Mexico is a country").get(0)).get(1).hasProperty(Markers.PROPER)
//	            );
//	    
//	    Entity goal = BasicTranslator.getTranslator().translate("Matt wants to close his eyes").get(0);
//	    Mark.say(goal);
//	    Mark.say(goal.getFeatures());
//	    Entity wantedAction = goal.getObject().getElement(0).getSubject();
//	    Mark.say(wantedAction);
//	    Mark.say(wantedAction.getFeatures());
//	    Mark.say(wantedAction.toEnglish());
//	    Entity matt = goal.getSubject();
//	    Mark.say(matt.toEnglish());
//	    Entity josh = BasicTranslator.getTranslator().translate("Josh").get(0);
//	    Mark.say(josh);
//	    Mark.say(josh.toEnglish());

	    
//      BasicTranslator.getTranslator().translate("Fred is a person.");
//      Entity work = BasicTranslator.getTranslator().translate("Fred works out.").get(0);
//
//      Entity ex = BasicTranslator.getTranslator().translate("Fred exercises.").get(0);
//
//     Mark.say(work.getBundle().size());
//      Mark.say(work);
//      Mark.say(ex);
//      Mark.say(new EntityMatcher().match(ex, work));
      
     // Mark.say(BasicTranslator.getTranslator().translate("Fred has a drink and watches television.").get(0));

//      Mark.say(BasicTranslator.getTranslator().translate("Fred leaves the apartment and takes a walk.").get(1));
//
//      Mark.say(BasicTranslator.getTranslator().translate("Fred thinks about work and makes a breakthrough.").get(1));
//
//      Mark.say(BasicTranslator.getTranslator().translate("Fred calms down and heads home.").get(0));
//	    
//      BasicTranslator.getTranslator().translate("Fred is a person.");
//      BasicTranslator.getTranslator().translate("Matt is a person.");
//      Mark.say(BasicTranslator.getTranslator().translate("Fred fights with John over his music.").get(0));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred feels anger towards John.").get(0));
//      
//      Mark.say(ConceptNetClient.getSimilarityScore("wake up", "disturb").getResult());

	    
//	    Start start = new Start();
//	    boolean debug = true;
//	    // Mark.say("Processing name", request);
//	    String header = "server=" + start.getServer() + "&action=add-word&query=";
//	    String content = "(noun \"" + "angry" + "\")";
//	    String trailer = "";
//	    String encodedString = "";
//	    try {
//	        encodedString = URLEncoder.encode(content, "UTF-8");
//	    }
//	    catch (UnsupportedEncodingException e) {
//	        e.printStackTrace();
//	    }
//	    String probe = header + encodedString + trailer;
//	    // Mark.say("Probe is", probe);
//	    StringBuffer answer = start.processProbe(probe);
//	    Mark.say(debug, "Name probe is:", probe);
//	    Mark.say("ANSWER: "+answer);
	    
//	       BasicTranslator.getTranslator().translate("Matt is a person.");
//	        BasicTranslator.getTranslator().translate("Josh is a person.");
//	    Entity matt = BasicTranslator.getTranslator().translate("Matt is angry.").get(0);
//	    Mark.say(Util.getAllDeepComponents(matt).get(1).getAllTypes());
//	    WordNet wn = new WordNet();
//	    Mark.say(wn.getSynsetsForWord("anger", PennTag.convert("adverb")));
	    
		// MentalModelDemo.main(args);
	    
	    // CANNOT test matching using below code
	    // below works for StructureMapper testing
	    //Entity bryKill = BasicTranslator.getTranslator().translate("AA and BB are persons and AA harms BB.");
//	    Entity bryKill = BasicTranslator.getTranslator().translate("YY harms WW because YY kills WW.");
	    //Util.getAllDeepComponents(bryKill).stream().forEach(e -> System.out.println(e + " " + e.getBundle()));
//	    Entity aaronMurder = BasicTranslator.getTranslator().translate("YY angers WW because YY fights WW.");
//	    System.out.println(bryKill);
//	    System.out.println(aaronMurder);
	    
	    //System.out.println(Arrays.toString(new String[10]));
	    
	    //System.out.println(BasicTranslator.getTranslator().translate("Matt steals lunch money from Josh."));
	    //System.out.println(BasicTranslator.getTranslator().translate("Matt cheats off Josh by taking his homework and turning it in."));
        //System.out.println(BasicTranslator.getTranslator().translate("Josh tricks Matt by giving him the wrong assignment."));
	    // note - If XX is a bully, XX may want give a gift to YY works!
	   //System.out.println(BasicTranslator.getTranslator().translate("If XX is a bully, XX may want to punch YY."));
	    //System.out.println(BasicTranslator.getTranslator().translate("Matt harms Josh."));
	    
//	    Entity rule = BasicTranslator.getTranslator().translate("If XX is a bully, XX may want to harm YY.").getElement(0);
//	    Entity want = rule.getObject();
//	    Entity wantObj = want.getObject();
//	    Entity wantObjFcn = want.getObject().getElement(0);
//	    Entity wantObjFcnSubj = want.getObject().getElement(0).getSubject();
//	    Entity harm = BasicTranslator.getTranslator().translate("XX harms YY.").getElement(0);
//	    System.out.println("harm: "+harm);
//	    EntityMatcher em = new EntityMatcher();
//	    System.out.println("want: "+want);
//	    System.out.println("want obj: "+wantObj);
//	    System.out.println("want obj fcn: "+wantObjFcn);
//	    System.out.println("want obj fcn subj: "+wantObjFcnSubj);
//	    System.out.println("match want obj fcn subj? "+em.match(wantObjFcnSubj, harm));
//	    System.out.println("\n\nmatch want obj fcn? "+em.match(wantObjFcn, harm));
//	    System.out.println("\n\nmatch want obj? "+em.match(wantObj, harm));
//	    System.out.println("\n\nmatch want? "+em.match(want, harm));
//
//	    
//	    System.out.println(Util.getAllDeepComponents(rule).stream().map(e -> e.getPrimedThread()).collect(Collectors.toList()));
//        System.out.println(Util.getAllDeepComponents(harm).stream().map(e -> e.getPrimedThread()).collect(Collectors.toList()));
	   
//	   Entity hw = BasicTranslator.getTranslator().translate("Matt finished his homework.").getElement(0);
//	   Entity lieDown = BasicTranslator.getTranslator().translate("Matt lies down.").getElement(0);
//	   Entity tv = BasicTranslator.getTranslator().translate("Matt watches television.").getElement(0);
//	   System.out.println(lieDown.getType());
//	   System.out.println();
//	   System.out.println(hw);
//	   System.out.println(BasicTranslator.getTranslator().translate("If XX finishes his or her homework, XX may want to relax."));
	    
//	   System.out.println(hw);
//	   System.out.println(hw.getType() + " " + hw.getObject().getElement(0).getSubject().getType());
//       System.out.println(Util.getAllDeepComponents(hw.getObject()).stream().map(Entity::getPrimedThread).collect(Collectors.toList())); 
//	   
//       Entity watch = BasicTranslator.getTranslator().translate("Matt watched television and Josh.");
//       System.out.println(watch);
//       
//       Entity relax = BasicTranslator.getTranslator().translate("Matt relaxes").getElement(0);
//       System.out.println(relax);
//       System.out.println(Util.getAllDeepComponents(relax.getObject()));
//       System.out.println(relax.getObject().getAllComponents());
	    
//	    Entity punch = BasicTranslator.getTranslator().translate("Charlie wants to punch XX").get(0);
//	    Mark.say(punch.toEnglish());
//	    Mark.say(punch.getObject().get(0).getSubject());
//	    Mark.say(punch.getObject().get(0).getSubject().toEnglish());
	    
//	    Entity watch = BasicTranslator.getTranslator().translate("Charlie watches a tv show.").get(0);
//	    Mark.say(watch);
//	    
//	    Entity work = BasicTranslator.getTranslator().translate("Fred works hard.").get(0);
//        Mark.say(work);
//        
//        Entity read = BasicTranslator.getTranslator().translate("Fred has a drink and reads a book.").get(1);
//        Mark.say(read);
        
//        Entity eat = BasicTranslator.getTranslator().translate("If XX disturbs YY and YY is relaxing, then YY becomes angry.").get(0);
//        Mark.say(eat);
        
//        Entity first = BasicTranslator.getTranslator().translate("Josh is a person.").get(0);
//        Entity popcorn = BasicTranslator.getTranslator().translate("Josh eats an apple").get(0);
//        Entity apple = BasicTranslator.getTranslator().translate("Josh eats an apple").get(0);
//        //Mark.say(true, Util.isTransitiveRelation(eat));
//        //Mark.say(eat.getPrimedThread());
//        Mark.say(popcorn);
//        Mark.say(apple);
//
//        EntityMatcher em = new EntityMatcher();
//        Mark.say(em.match(popcorn, apple));
//        Mark.say(em.match(popcorn.getObject().get(0).getSubject(), apple.getObject().get(0).getSubject()));
        
//        Entity love = BasicTranslator.getTranslator().translate("Matt is artistic.").get(0);
//	    Mark.say(love);
//        
//      Entity person = BasicTranslator.getTranslator().translate("Jackie is a person.").get(0);
//      Entity friend = BasicTranslator.getTranslator().translate("Jackie finds a friend.").get(0);
//      Entity friendObj = friend.getObject().get(0).getSubject();
//      boolean isPerson = false;
//      Bundle friendBundle = friendObj.getBundle();
//      for (int i = 0; i < friendBundle.size(); i++) {
//          bridge.reps.entities.Thread curThread = friendBundle.get(i);
//          Mark.say("cur thread", curThread);
//          Mark.say("cur thread type", curThread.getThreadType());
//          if (!curThread.contains("name")) {
//              isPerson = true;
//              //break;
//          }
//      }
//      if (isPerson) {
//          Mark.say("There is a sense in which person is not a name");
//      } else {
//          Mark.say("Person always a name!");
//      }
	    
//	    BasicTranslator.getTranslator().translate("George is a person.");
//	    Mark.say(BasicTranslator.getTranslator().translate("George overeats.").get(0));
//	    Mark.say(BasicTranslator.getTranslator().translate("George gains weight.").get(0));
//        Mark.say(BasicTranslator.getTranslator().translate("George rides a bike to his office.").get(0));
//	    	    
//	    Object i = Integer.valueOf(1);
//	    Double scoreNum = Doubles.tryParse(i);
//	    Sy
//	    
	     // BasicTranslator.getTranslator().translate("Josh is a person.");
//	     BasicTranslator.getTranslator().translate("Matt is a person.");
//	     Entity josh = BasicTranslator.getTranslator().translate("popcorn.").get(0);
//	     Mark.say(josh);
//	     Entity matt = BasicTranslator.getTranslator().translate("apple.").get(0);
//       Mark.say(true, //new EntityMatcher().match(matt, josh));
//               new StandardMatcher().match(matt, josh));
//	    char pChar = 'A';
//	    Mark.say(BasicTranslator.getTranslator().translateToEntity("Someone is a person"));
//	    Mark.say(String.format("%.2f", "abc123"));
//	    for (int i = 0; i < 20; i++) {
//	        Mark.say(Util.getPlaceholder(i));
//	    }
	    
	    /*
        Mark.say(true, Util.getTransitiveRelationDirectObjects(BasicTranslator.getTranslator().translate("Bryan conquered his opponent").get(0))
                .get(0).isAlwaysA("name"));
                */
        
//        Mark.say(BasicTranslator.getTranslator().translate("If XX harms YY, then YY becomes angry.").get(0));
//        Mark.say(BasicTranslator.getTranslator().translate("Matt embarrasses Josh.").get(0));


//	    Mark.say(Util.getAllDeepComponents(BasicTranslator.getTranslator().translate("Matt plays hockey").get(0)).stream()
//	            .map(Entity::getPrimedThread)
//	            .collect(Collectors.toList()));
//	    BasicTranslator.getTranslator().translate("XX is a person.");
//	    Entity hockey = BasicTranslator.getTranslator().translate("Matt plays hockey.").get(0);
//	    Entity xxSport = BasicTranslator.getTranslator().translate("XX plays a sport.").get(0);
//
//	    Mark.say(new EntityMatcher().match(xxSport, hockey));
//
//	    Entity athletic = BasicTranslator.getTranslator().translate("Matt is athletic.").get(0);
//	    Entity artistic = BasicTranslator.getTranslator().translate("Matt is artistic.").get(0);
//	    Mark.say(new EntityMatcher().match(athletic,  artistic));
//
//	    Entity mattSport = BasicTranslator.getTranslator().translate("Matt wants to play a sport.").get(0);
//	    Entity music = BasicTranslator.getTranslator().translate("Matt wants to make music.").get(0);
//
//	    Mark.say(new EntityMatcher().match(mattSport,  music));
//	    
//	    Entity bully = BasicTranslator.getTranslator().translate("Matt bullies Josh.").get(0);
//	    Entity annoy = BasicTranslator.getTranslator().translate("Matt angers Josh.").get(0);
//	    Mark.say(new EntityMatcher().match(bully, annoy));
//	    
//	       Entity loathe = BasicTranslator.getTranslator().translate("Matt loathes Josh.").get(0);
//           Entity hate = BasicTranslator.getTranslator().translate("Matt hates Josh.").get(0);
//
//           Mark.say(new StandardMatcher().similarityMatch(loathe, hate, new BindingsWithProperties()));



//      //Mark.say(BasicTranslator.getTranslator().translate("Fred overeats.").get(0));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred works hard at the office.").get(0));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred buys beer.").get(0));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred has a drink and reads a book.").get(0));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred has a drink and reads a book.").get(1));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred lies down and rests.").get(0));
//      Mark.say(BasicTranslator.getTranslator().translate("Fred lies down and rests.").get(1));


//      List<String> concepts = Arrays.asList("work hard");
//      List<String> causedGoalConcepts = concepts.stream()
//              .map(conceptStr -> new ConceptNetFeature(conceptStr, "CausesDesire", FeatureType.LEFT))
//              .map(ConceptNetClient::featureToAssertions)
//              .map(ConceptNetQueryResult::getResult)
//              .flatMap(List::stream)
//              .filter(scoredAssertion -> scoredAssertion.getScore() >= 0.1)
//              .map(ConceptNetAssertion::getConcept2String)
//              .collect(Collectors.toList());
//      Mark.say(causedGoalConcepts);
      
      

       // Mark.say(BasicTranslator.getTranslator().translate("George rides a bike to his office.").get(0));
	    
	    
//	    ConceptNetQueryResult<List<ConceptNetAssertion>> filledInAssertions = 
//	            ConceptNetClient.featureToAssertions(new ConceptNetFeature("relax", "CausesDesire", FeatureType.RIGHT));
//	    Mark.say(filledInAssertions);
//	    Mark.say(true, filledInAssertions.getResult() instanceof List);
//	    //ConceptNetAssertion a = filledInAssertions.getResult();
//	    
//	    ConceptNetQueryResult<Double> similar = ConceptNetClient.getSimilarityScore("cat", "shovel");
//	    Mark.say(similar);
//	    
//	    ConceptNetQueryResult<Double> tru = ConceptNetClient.howTrueIs(new ConceptNetAssertion("watch television", "MotivatedByGoal", "relax"));
//	    Mark.say(tru);
//	    Double result = tru.getResult();
//	    Mark.say(result);
//	    
//	    similar = ConceptNetClient.getSimilarityScore("shovel", "adsfda"); 
//	    Mark.say(similar);
//      
//      Mark.say(Util.getAllDeepComponents(friend).stream().map(Entity::getBundle)
//              .map(mapper));

	    
	   // Mark.say(punch.getObject().toEnglish());

//	    Entity goalRule = BasicTranslator.getTranslator().translate("If XX is hungry, XX may want to eat popcorn").get(0);
//	    System.out.println(Predicates.isExplictCauseOrLeadsTo(goalRule));
//	    System.out.println(goalRule.getObject().hasProperty(Markers.CERTAINTY, Markers.TENTATIVE));
       
	    //System.out.println(ConceptNetClient.howTrueIsAssertion("lie down", "MotivatedByGoal", "relax"));//"relax"));
	    
//	   System.out.println(Util.getAllDeepComponents(hungry));
//	   System.out.println(Util.getAllNameComponents(hungry));
//	   System.out.println(Util.getAllDeepComponents(hungry).stream().map(e -> e.getPrimedThread()).collect(Collectors.toList()));

	    
//	    Entity torment = BasicTranslator.getTranslator().translate("Josh embarrasses Matt at school later.").get(0);
//	    System.out.println(torment);
//	    System.out.println(torment.getPrimedThread());
//	    System.out.println(torment.getType());
//	    System.out.println(torment.relationP());
//	    System.out.println(torment.getObject());
//	    System.out.println(torment.getSubject());
	    
//	    Entity harass = BasicTranslator.getTranslator().translate("XX harasses YY.").get(0);
//	    Entity annoy = BasicTranslator.getTranslator().translate("XX annoys YY.").get(0);
//	    Entity pester = BasicTranslator.getTranslator().translate("XX pesters YY.").get(0);
//	    Entity mental = BasicTranslator.getTranslator().translate("XX is distraught.").get(0);
//	    System.out.println(harass);
//	    System.out.println(annoy);
//	    System.out.println(pester);
//	    Comparator<Entity> comp = new ScoreSimilarityComparator(Entity::getType, 0.67);
//	    System.out.println(comp.compare(harass, annoy));
//	    System.out.println(new RankingSimilarityComparator(Entity::getType, 5, true).compare(harass, annoy));
//	    System.out.println(new RankingSimilarityComparator(Entity::getType, 2, false).compare(pester, harass));
//	    System.out.println(new RankingSimilarityComparator(Entity::getType, 2, false).compare(harass, pester));
//	    System.out.println(comp.compare(mental, pester));
	    //System.out.println(x);
	    
//	    EntityMatcher matcher = new EntityMatcher();
	    //System.out.println(bryKill);
//	    System.out.println(aaronMurder);
//	    System.out.println(matcher.match(aaronMurder, bryKill).semanticMatch);
	    
//      Entity ang = BasicTranslator.getTranslator().translateToEntity("XX doesn't anger YY.");
//      Entity noAng = BasicTranslator.getTranslator().translateToEntity("YY angers XX.");
//      System.out.println(ang+"; "+noAng);
//      System.out.println(new EntityMatcher().match(noAng, ang).semanticMatch);
//      System.out.println(new EntityMatcher().match(ang, noAng).semanticMatch);
//      
//      Entity noAng2 = BasicTranslator.getTranslator().translateToEntity("ZZ angers YY.");
//      System.out.println(new EntityMatcher().match(noAng, noAng2).semanticMatch);
//      System.out.println(new EntityMatcher().match(noAng2, noAng).semanticMatch);
	    	    
	    /*
	    BasicTranslator.getTranslator().translateToEntity("John is a person");
	    BasicTranslator.getTranslator().translateToEntity("Mary is a person");
	    BasicTranslator.getTranslator().translateToEntity("Macbeth is a person");
	    BasicTranslator.getTranslator().translateToEntity("Duncan is a person");
	    */
	    
	    /*
	    Entity goal = BasicTranslator.getTranslator().translate("The Union wanted the Confederacy to stay in the United States.");
	    System.out.println(goal.getTypes());
	    */
//	    
//        Entity goal2 = BasicTranslator.getTranslator().translate("Lady Macbeth wants Macbeth to become king.");
//        System.out.println(goal2.getTypes());
//        
//        Entity aa = BasicTranslator.getTranslator().translateToEntity("AA");
//        System.out.println(aa);
//        
//        Entity ang = BasicTranslator.getTranslator().translateToEntity("XX doesn't anger YY.");
//        Entity noAng = BasicTranslator.getTranslator().translateToEntity("YY angers XX.");
//        System.out.println(ang+"; "+noAng);
//        System.out.println(new EntityMatcher().match(noAng, ang).semanticMatch);
//        System.out.println(new EntityMatcher().match(ang, noAng).semanticMatch);
//        
//        Entity noAng2 = BasicTranslator.getTranslator().translateToEntity("ZZ angers YY.");
//        System.out.println(new EntityMatcher().match(noAng, noAng2).semanticMatch);
//        System.out.println(new EntityMatcher().match(noAng2, noAng).semanticMatch);

	    /*
		Entity seq = BasicTranslator.getTranslator().translate("I am smart. I am loyal.");
	    
		Mark.say("Seq: ", seq.toEnglish());
		Mark.say("One: ", seq.get(0).toEnglish());
		Mark.say("Two: ", seq.get(1).toEnglish());
		*/
		
	    //Entity result = Translator.getTranslator().translate("John loves Mary because Mary is smart.").get(0);

		/*
		Mark.say("Result:", result);

		Mark.say("Cause?", Predicates.isCause(result));

		Mark.say("Antecedents", result.getSubject());

		result.getSubject().stream().forEachOrdered(e -> Mark.say("Antecedent", e));

		Mark.say("Consequent", result.getObject());
		*/
//	    BasicTranslator.getTranslator().translate("Matt is a person");
//	    Entity friend = BasicTranslator.getTranslator().translate("Matt kisses his friend").get(0);
//	    Entity hit = BasicTranslator.getTranslator().translate("Matt kisses").get(0);
//	    Mark.say(friend);
//	    Mark.say(hit);
//	    Entity friendCopy = friend.deepClone();
//	    friendCopy.setObject(new Sequence());
//	    Mark.say(friendCopy);
//	    Mark.say(true, friendCopy.isDeepEqual(hit));
//	    Mark.say(friend);
//	    Mark.say(Util.getAllDeepComponents(friend)
//            .stream()
//            .filter(e -> e.hasProperty(Markers.PROPER))
//            .collect(Collectors.toList())
//	    );
//	    Mark.say(Util.getAllDeepComponents(friend)
//	            .stream()
//	            .filter(e -> Util.isProperName(e))
//	            .collect(Collectors.toList())
//	            );
//	    Mark.say(Util.getAllDeepComponents(friend)
//	            .stream()
//	            .map(Entity::getBundle)
//	            .collect(Collectors.toList())
//	            );
//
//	    Mark.say(BasicTranslator.getTranslator().translate("play hockey"));
	    
//	    Mark.say(BasicTranslator.getTranslator().translate("Matt wants to kiss Josh").get(0));
//	    Mark.say(BasicTranslator.getTranslator().translate("Matt loves baseball").get(0));
//	    Mark.say(BasicTranslator.getTranslator().translate("Josh loves basketball").get(0));
//	    
//	    Mark.say(ConceptNetClient.getSimilarityScore("phone", "computer"));
//	    Mark.say(ConceptNetClient.getSimilarityScore("song", "music"));
//	    Mark.say(ConceptNetClient.getSimilarityScore("harm", "harass"));
//	       Mark.say(ConceptNetClient.getSimilarityScore("harm", "attack"));
	       
//           Mark.say(ConceptNetClient.getSimilarityScore("annoy", "tease"));
//           Mark.say(ConceptNetClient.getSimilarityScore("pester", "harm"));
//           Mark.say(ConceptNetClient.getSimilarityScore("provoke", "harm"));
//           Mark.say(ConceptNetClient.getSimilarityScore("disturb", "harm"));
//           Mark.say(ConceptNetClient.getSimilarityScore("hassle", "harm"));
//           Mark.say(ConceptNetClient.getSimilarityScore("bother", "harm"));
//           Mark.say(ConceptNetClient.getSimilarityScore("badger", "harm"));

//
//        Mark.say(ConceptNetClient.getSimilarityScore("bully", "annoy"));
//
//        Mark.say(ConceptNetClient.getRelatedGroup("abcde"));
//        Mark.say(ConceptNetClient.featureToAssertions(new ConceptNetFeature("acdsa", "Causes Desire", FeatureType.LEFT)));
        
      Mark.say(BasicTranslator.getTranslator().translate("Sean rides his bike to the office"));

	}
}