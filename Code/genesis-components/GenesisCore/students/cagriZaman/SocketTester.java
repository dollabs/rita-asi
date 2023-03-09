package cagriZaman;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import conceptNet.conceptNetModel.*;
import conceptNet.conceptNetModel.ConceptNetFeature.FeatureType;
import conceptNet.conceptNetNetwork.*;
import connections.*;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import start.Start;
import storyProcessor.StoryProcessor;
import utils.Mark;

public class SocketTester extends AbstractWiredBox implements Runnable {

	private final String hostName= "128.30.31.153";
	private final int portNumber = 1755;
	
	private static Gson gson = new Gson();

	public static String WORLD = "world";
	
	
	public SocketTester(){
        Connections.getPorts(this).addSignalProcessor(StoryProcessor.INCREMENT_PORT, this::processAction);
        //Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT,this::processStory);
        //Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT,this::doNothing);
        //Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT,this::processStoryProcessor);
       
        try{
        Connections.publish(this, "Cagri's socket tester");
        }catch(NetWireException e){
        	Mark.err(e);
        }
	}
	
	
	private ArrayList<Entity> things;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean closeSession=false;
		things= new ArrayList<Entity>();
		try{
			Socket echoSocket = new Socket(hostName,portNumber);
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		    
			out.println("Hello from Genesis");
			
			
			// Connect to the remote server. 
			while(!closeSession){
				if(in.ready()){
					//This method assumes every message terminate with with "\n". Easy for testing. Not good for the future.
					String msg=in.readLine();
					//Every json field in message should match a field in a predefined class. 
					//TODO 
					//We can write a reader that first checks the type of message, and wire it to appropriate decoder.i.e. Perception Message
					//Perception Message has a Type field that can be used for this purpose.
					
					//Close the connection and terminate the app when Server sends termination message.
					if(msg.equals("So Long!")){
						closeSession=true;
					}
					else{
					PerceptionMessage obj =gson.fromJson(msg,PerceptionMessage.class);
					
					if(obj.Type.equals("RoleFrame")){
						Entity e= PerceptionMessage.toRoleFrame(obj.Subject, obj.Verb,obj.Adverb,obj.Object);
				
						Mark.say(obj.Object);
						Mark.say(obj.Subject);
						Mark.say(obj.Verb);
						Entity thing = Entity.getClassifiedThing(obj.Object);
						if(!things.contains(thing)){
							things.add(thing);
							//System.out.println(thing);
							Mark.say(thing+" added to the things list");
						} 
						
//						Entity e2=RoleFrames.makeRoleFrame(e, currentDirection);
//						Mark.say(e2.toEnglish());
						Connections.getPorts(this).transmit(WORLD, e);
						
//						if(getLocationRelation(Entity.getClassifiedThing(obj.Subject).toEnglish(), Entity.getClassifiedThing(obj.Object).toEnglish())){
//							Entity injectLocation=PerceptionMessage.decodeMessage("RoleFrame", obj.Subject+" is at the "+obj.Object);
//							injectLocation.addProperty(Markers.GOAL_ANALYSIS,true,true);
//							Connections.getPorts(this).transmit(WORLD, injectLocation);
//							
//						}
						
						
					}
					
					else if(obj.Type.equals("Action")){
						Entity e = PerceptionMessage.toRoleFrame(obj.Subject, obj.Verb,obj.Adverb,obj.Object);
						Mark.say(e.toEnglish());
							Connections.getPorts(this).transmit(WORLD,e);
							
						
						
					}
					
					//Not used anymore. Relations received in a role frame.
//					else if(obj.Type.equals("Relation")){
//						Relation r =PerceptionMessage.toRelation(obj.Subject,obj.Object, obj.Adverb);
//						Mark.say(r.getSubject().toEnglish(),"is",obj.Adverb,r.getObject().toEnglish());
//					}
					else if(obj.Type.equals("Feature")){
						Entity e = Entity.getClassifiedThing(obj.Subject);
						e.addFeature(obj.Feature);
					}
					}
					Thread.sleep(100);				
				}
			}
			out.close();
			echoSocket.close();
			Mark.say("Socket Closed... Terminating");
		}catch(IOException e){
			Mark.say(e.toString());
		}catch(InterruptedException e){
			Mark.say(e.toString());
		}
	}
	
	public boolean getLocationRelation(String s, String o ){
		
		ConceptNetFeature causeGoalFeature = new ConceptNetFeature(s, "AtLocation", FeatureType.LEFT);
        ConceptNetQueryResult<List<ConceptNetScoredAssertion>> unflattenedResult = 
                ConceptNetClient.featureToAssertions(causeGoalFeature);	
        List<ConceptNetQueryResult<Double>> flattenedResults = ConceptNetQueryResult.flattenResult(unflattenedResult);
        Set<ConceptNetQueryResult<Double>> locationResults = flattenedResults.stream()
                
                .collect(Collectors.toSet());
        
        for (ConceptNetQueryResult< Double> location:locationResults){
        	if(o.toLowerCase().equals(location.getQuery().getComponentConcepts().get(1).getConceptString())){
        		Mark.say("Concept net AtLocation match!");
        		return true;
        	}
        }
		
		
		return false;
	}
	
	
	public Entity getObjectOfAction(String action){
		
		ConceptNetFeature usedForFeature = new ConceptNetFeature(action, "UsedFor", FeatureType.RIGHT);
        ConceptNetQueryResult<List<ConceptNetScoredAssertion>> unflattenedResult = 
                ConceptNetClient.featureToAssertions(usedForFeature);	
        List<ConceptNetQueryResult<Double>> flattenedResults = ConceptNetQueryResult.flattenResult(unflattenedResult);
        Set<ConceptNetQueryResult<Double>> actionResults = flattenedResults.stream()
                .collect(Collectors.toSet());
        
        for (ConceptNetQueryResult< Double> actionResult:actionResults){
        	Mark.say("ConceptNet got something to work on for UsedFor relations");
        	
        	String source = actionResult.getQuery().getComponentConcepts().get(0).getConceptString();
        	for(Entity thing:things){
            	String target = thing.getType();
        	if(target.equals(source)){
        		Mark.say("Concept net UsedFor match!");
        		return thing;
        	}
        	else{
        		Mark.say(target + " is not a match for " + source);
        	}
        	}
        }
		
		
		return null;
		
	}
	
	
	public void doNothing(Object signal){
		
	}
	// Listen to the story and see if there is any explicit goal sentence. 
	public void processAction(Object signal){
		if(signal instanceof BetterSignal){
			Mark.say("Is there an action?");
		}	
		
		else{
			Mark.say(signal.getClass());
			Sequence s = (Sequence)signal;
			Entity e = s.get(0);
			e.getPrimedThread().stream().forEachOrdered(c -> {
			
				if(c.equals("desire")){					
					Mark.say("An action found!");
					String wantsTo=e.getObject().getElement(0).getSubject().getType();
					Entity result=getObjectOfAction(wantsTo);
					if(result!=null){
						Entity actionReasoning=RoleFrames.makeRoleFrame("John",wantsTo,"at",result);
							actionReasoning.addProperty(Markers.GOAL_ANALYSIS,true,true);

							Connections.getPorts(this).transmit(WORLD,actionReasoning);
					}
					else
					{
						Mark.say("No match from ConceptNet for UsedFor relation for action "+ wantsTo);
					}
				
				}
			});
		}
	}
	
	
	public void processStoryProcessor(Object signal){
		Mark.say("PROCESS STORY PROCESSOR HAS BEEN CALLED");
	}
	public void processStory(Object signal){
		boolean debug = true;
		Mark.say(debug,"COMPLETE STORY ANALYSIS");
		Entity last =PerceptionMessage.decodeMessage("Role Frame", "John is in the room");
		Connections.getPorts(this).transmit(WORLD,last);
		if(signal instanceof BetterSignal){
			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
			Sequence explicitElements = s.get(1, Sequence.class);
			Sequence inferences = s.get(2, Sequence.class);
			Sequence concepts = s.get(3, Sequence.class);
			
			
			Mark.say(debug, "\n\n\nStory elements");
			story.getElements().stream().forEach(f -> Mark.say(debug, f));
			// for (Entity e : story.getElements()) {
			// // Print story element in human-readable, parenthesized form
			// Mark.say(debug, e);
			// }
			Mark.say(debug, "\n\n\nExplicit story elements");
			for (Entity e : explicitElements.getElements()) {
				Mark.say(debug, e);
			}
			Mark.say(debug, "\n\n\nInstantiated commonsense rules");
			for (Entity e : inferences.getElements()) {
				Mark.say(debug, e);
			}
			Mark.say(debug, "\n\n\nInstantiated concept patterns");
			concepts.getElements().stream().forEach(e -> Mark.say(e));

			Mark.say(debug, "\n\n\nAll story elements, in English");
			Generator generator = Generator.getGenerator();

			story.getElements().stream().forEach(f -> Mark.say(generator.generate(f)));
		}
	}
	
	public static void main(String[] args) {
		// // TODO Auto-generated method stub
		// String[] testMessage = {"{'type':'Entity',Object:'Simon',Verb:'sees',Subject:'A Tree'}",
		// "{'type':'Entity',Object:'Simon',Verb:'sees',Subject:'Blue House'}",
		// "{'type':'Entity',Object:'Simon',Verb:'sees',Subject:'Green House'}",
		// "{'type':'Entity',Object:'Simon',Verb:'sees',Subject:'Blue House'}"};
		//
		// for(String message:testMessage){
		// PerceptionMessage obj =gson.fromJson(message,PerceptionMessage.class);
		//
		// Mark.say(PerceptionMessage.toRoleFrame(obj.Subject, obj.Verb, obj.Object));
		// Entity x = RoleFrames.makeRoleFrame("Simon+4", "see", "house","in","woods");
		// Mark.say(x);
		//
		// }
		// Entity ss=RoleFrames.makeRoleFrame("Simon+5", "sees", "house+43");
		// Entity e= RoleFrames.makeRoleFrame("Simon+5", "sees","tree+3","near","house+43","in","afternoon");
		// Entity b= RoleFrames.makeRoleFrame("house+43", "appears", "near","simon+5");
		// e.getSubject().addFeature("red");
		// Mark.say("Entity",e);
		// Mark.say(e.getObject());
		// Mark.say(e.toEnglish());
		// Mark.say(b.toEnglish());
//
//			Entity action = RoleFrames.makeRoleFrame("John", "wants", "to","sit");
//			Mark.say(action.getObject().getElement(0).getSubject());
		//Uncomment below to test the socket connection to the server.
		SocketTester test = new SocketTester();
		new Thread(test).start();
	}

}
