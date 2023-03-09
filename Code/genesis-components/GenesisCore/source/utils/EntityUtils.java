package utils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import constants.*;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import memory.distancemetrics.Operations;
/**
 * @author adk
 * Provides some functionality that I found lacking in the Thing class and its descendants.  When/if Thing is
 * ported to Gauntlet, it may be helpful to add these to the Things as methods.
 *
 */
public class EntityUtils {
 
	public static boolean containsByReference(Entity t, Object pointer){
		if(t==pointer){
			return true;
		}
		for(Entity c:t.getChildren()){
			if(containsByReference(c,pointer)){
				return true;
			}
		}
		return false;
	}
	/**
	 * toString that can print Things with cycles, in a format that my
	 * BetterThingParser can read in.
	 * @param timer
	 * @return
	 */
	private static String xmlIndent = "   ";
	public static String adamsPrettyPrint(Entity t){
		//toString that can print Things with cycles, in a format that my
		//BetterThingParser can read in.
		Set<String> visited = new HashSet<String>();
		return ppHelp(t,visited,0);
	}
	private static String ppHelp(Entity t, Set<String> visited, int tabLevel){
		String markup = "";
		if(t instanceof Relation){
			markup += javaIsBusted(xmlIndent,tabLevel)+"<RELATION>\n";
			markup += javaIsBusted(xmlIndent,tabLevel+1)+"<NAME>"+t.getName()+"</NAME>\n";
			if(!visited.contains(t.getName())){
				visited.add(t.getName());
				markup+=ppHelp(((Relation)t).getObject(),visited,tabLevel+1);
				markup+=ppHelp(((Relation)t).getSubject(),visited,tabLevel+1);
			}
			else{
				markup += javaIsBusted(xmlIndent,tabLevel+1)+"...body defined previously...\n";
			}
			markup += ppBundle(t.getBundle(),tabLevel+1);
			markup += javaIsBusted(xmlIndent,tabLevel)+"</RELATION>\n";
		}
		else if(t instanceof Function){
			markup += javaIsBusted(xmlIndent,tabLevel)+"<DERIVATIVE>\n";
			markup += javaIsBusted(xmlIndent,tabLevel+1)+"<NAME>"+t.getName()+"</NAME>\n";
			if(!visited.contains(t.getName())){
				visited.add(t.getName());
				markup+=ppHelp(((Function)t).getSubject(),visited,tabLevel+1);
			}else{
				markup += javaIsBusted(xmlIndent,tabLevel+1)+"...body defined previously...\n";
			}
			markup += ppBundle(t.getBundle(),tabLevel+1);
			markup += javaIsBusted(xmlIndent,tabLevel)+"</DERIVATIVE>\n";
		}
		else if(t instanceof Sequence){
			markup += javaIsBusted(xmlIndent,tabLevel)+"<SEQUENCE>\n";
			markup += javaIsBusted(xmlIndent,tabLevel+1)+"<NAME>"+t.getName()+"</NAME>\n";
			if(!visited.contains(t.getName())){
				visited.add(t.getName());
				for(Entity sub:((Sequence)t).getElements()){
					markup += ppHelp(sub,visited,tabLevel+1);
				}
			}else{
				markup += javaIsBusted(xmlIndent,tabLevel+1)+"...body defined previously...\n";
			}
			markup += ppBundle(t.getBundle(),tabLevel+1);
			markup += javaIsBusted(xmlIndent,tabLevel)+"</SEQUENCE>\n";
		}
		else {//plain Thing
			markup += javaIsBusted(xmlIndent,tabLevel)+"<THING>\n";
			markup += javaIsBusted(xmlIndent,tabLevel+1)+"<NAME>"+FakeXMLProcessor.escape(t.getName())+"</NAME>\n";
			markup += ppBundle(t.getBundle(),tabLevel+1);
			markup += javaIsBusted(xmlIndent,tabLevel)+"</THING>\n";
		}
		return markup;
	}
	private static String ppBundle(Bundle b,int tabLevel){
		if(b==null){return "";}
		String markup = javaIsBusted(xmlIndent,tabLevel)+"<BUNDLE>\n";
		for(Thread t:b){
			markup += ppThread(t,tabLevel+1);
		}
		markup += javaIsBusted(xmlIndent,tabLevel)+"</BUNDLE>\n";
		return markup;
	}
	private static String ppThread(Thread t, int tabLevel){
		String markup = javaIsBusted(xmlIndent,tabLevel)+ "<THREAD> ";
		for(String s:t){
			markup += FakeXMLProcessor.escape(s) + " ";
		}
		markup += "</THREAD>\n";
		return markup;
	}
	
	//private static String javaLeavesMuchToBeDesired(String s, unsigned int multiplier){
	//but wait, no unsigned types...
	private static String javaIsBusted(String s, int multiplier){
		// if "foo"+2 is "foo2", why isn't "foo"*2 "foofoo", for crissake?
		if(multiplier==0)return "";
		else if(multiplier > 0) {
			return s+javaIsBusted(s,multiplier-1);
		}else{
			throw new IllegalArgumentException("multiplying String by a negative integer is undefined.");
		}
	}
	
	
	public static boolean cheapCompare(Entity a, Entity b){
		//a linear (w/ # of bundles in tree) comparison for equivalence
		if(!EntityUtils.getRepType(a).equals(EntityUtils.getRepType(b))){
			return false;
		}else if(Operations.distance(a.getBundle(), b.getBundle())>Operations.EPSILON){
			return false;
		} else if (a.getChildren().size() != b.getChildren().size()){
			return false;
		}
		Iterator<Entity> aIter = a.getChildren().iterator();
		Iterator<Entity> bIter = b.getChildren().iterator();
		while(aIter.hasNext()){
			if(!cheapCompare(aIter.next(),bIter.next())){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isConsistent(Entity a, Entity b){
		//somewhat weaker than equivalence, this definition of consistency is its own best documentation
		if(!EntityUtils.getRepType(a).equals(EntityUtils.getRepType(b))){
			return false;
		}
		else if(Operations.distance(a.getBundle(), b.getBundle())<Operations.EPSILON){
			return true;
		} 
		else if (a.getChildren().size() != b.getChildren().size()){
			return false;
		}
		Iterator<Entity> aIter = a.getChildren().iterator();
		Iterator<Entity> bIter = b.getChildren().iterator();
		while(aIter.hasNext()){
			if(!isConsistent(aIter.next(),bIter.next())){
				return false;
			}
		}
		return true;
	}
	
	public static boolean hasComponents(Entity a){
		return a.getDescendants()!=null && a.getDescendants().size()!=0;
	}
	
	/**
	 * replaces all components (children, bundle) of replaceIn with those of replaceWith. 
	 * @param replaceIn
	 * @param replaceWith
	 */
	public static void replaceAllComponents(Entity replaceIn, Entity replaceWith){
		replaceWith = replaceWith.deepClone(); //man, that was really sneaky.  10+ hours of debugging before I suspected that!!!
		replaceChildren(replaceIn, replaceWith);
		replaceIn.setBundle(replaceWith.getBundle());
	}
	
	/**
	 * replaces children of replaceIn with children of replaceWith.  does not replace bundle of replaceIn.
	 * @param replaceIn
	 * @param replaceWith
	 */
	public static void replaceChildren(Entity replaceIn, Entity replaceWith){
		replaceIn.setName(replaceWith.getName());
		//replaceIn.setModifiers(replaceWith.getModifiers());
		replaceIn.clearModifiers();
		for(Entity t: replaceWith.getModifiers()){
			replaceIn.addModifier(t);
		}
		if(replaceIn instanceof Relation){
			assert(replaceWith instanceof Relation);
			replaceIn.setObject(replaceWith.getObject());
		}
		if(replaceIn instanceof Function){
			assert (replaceWith instanceof Function);
			replaceIn.setSubject(replaceWith.getSubject());
		}
		if(replaceIn instanceof Sequence){
			assert (replaceWith instanceof Sequence);
			((Sequence)replaceIn).setElements(replaceWith.getElements());
		}
	}
	
	public static List<Entity> getOrderedChildren(Entity t){
		if(t instanceof Relation){
			return getOrderedRChildren((Relation)t);
		} else if(t instanceof Function){
			return getOrderedDChildren((Function)t);
		} else if(t instanceof Sequence){
			return getOrderedSChildren((Sequence)t);
		} else
			return new ArrayList<Entity>();
	}
	public static List<Entity> getOrderedDescendants(Entity t) {
        List<Entity> result = new ArrayList<Entity>();
        List<Entity> queue = new LinkedList<Entity>();
        queue.addAll(getOrderedChildren(t));
        Entity next;
        while (!queue.isEmpty()) {
            next = (Entity) queue.remove(0);
            if (!result.add(next)) {
                continue;
            }
            queue.addAll(next.getChildren());
        }
        return result;
    }
	
	private static List<Entity> getOrderedRChildren(Relation t){
		List<Entity> superCh = getOrderedDChildren(t);
		superCh.add(t.getObject());
		return superCh;
	}
	private static List<Entity> getOrderedDChildren(Function t){
		 List<Entity> result = new ArrayList<Entity>();
	     result.add(t.getSubject());
	     return result;
	}
	private static List<Entity> getOrderedSChildren(Sequence t){
		List<Entity> result = new ArrayList<Entity>();
        result.addAll(t.getElements());
        return result;
	}
	
	public static Object getRepType(Entity input){
		// System.out.println("getRepTyp working on " + input);
		//what is the outermost description of a Thing?
		Thread tag = input.getPrimedThread();
		if(tag.contains(Markers.TRAJECTORY_MARKER)){
			return RecognizedRepresentations.TRAJECTORY_THING;
		}
		else if(tag.contains(Markers.CAUSE_MARKER)){
			return RecognizedRepresentations.CAUSE_THING;
		}
		else if(tag.contains(Markers.TIME_MARKER)){
			return RecognizedRepresentations.TIME_REPRESENTATION;
		}		
		else if(tag.contains(Markers.PLACE_MARKER)){
			return RecognizedRepresentations.PLACE_REPRESENTATION;
		}
		else if(tag.contains(Markers.PATH_ELEMENT_MARKER)){
			return RecognizedRepresentations.PATH_ELEMENT_THING;
		}
		else if(tag.contains(Markers.PATH_MARKER)){
			return RecognizedRepresentations.PATH_THING;
		}
		else if(tag.contains(Markers.ROLE_MARKER)){
			return RecognizedRepresentations.ROLE_THING;
		}
		else if(tag.contains(Markers.ACTION_WORD)){
			return RecognizedRepresentations.ACTION_REPRESENTATION;
		}
		else if(tag.contains(Markers.SOCIAL_MARKER)){
			return RecognizedRepresentations.SOCIAL_REPRESENTATION;
		}
		else if(tag.contains(Markers.MENTAL_STATE_MARKER)){
			return RecognizedRepresentations.MENTAL_STATE_THING;
		}
		else if(tag.contains(Markers.TRANSFER_MARKER)){
			return RecognizedRepresentations.TRANSFER_THING;
		}
		else if(tag.contains(Markers.TRANSITION_MARKER)){
			return RecognizedRepresentations.TRANSITION_REPRESENTATION;
		}
		else if(tag.contains("geometry")){
			return RecognizedRepresentations.GEOMETRY_THING;
		}
		else if(tag.contains("force")){
			return RecognizedRepresentations.FORCE_THING;
		}
		else if(tag.contains(RecognizedRepresentations.QUESTION_THING)){
			return RecognizedRepresentations.QUESTION_THING;
		}
		else if(tag.contains("answer")){
			return RecognizedRepresentations.ANSWER_THING;
		}
		else if(tag.contains("block")){
			return RecognizedRepresentations.BLOCK_THING;
		}
		else if(tag.contains("threadMemory")){
			return RecognizedRepresentations.THREAD_THING;
		}
		else if(tag.contains(RecognizedRepresentations.PLACE_REPRESENTATION)){
			return RecognizedRepresentations.PLACE_REPRESENTATION;
		}
		
		//...
		else return "unknown representation type";
	}
	
	public static String toOpenOfficeFormula(Entity t){
		String s = " bold ";
		if(t instanceof Relation){
			s += "color red left [  font fixed stack{  \n";
			for(Entity child:getOrderedChildren(t)){
				s += "alignl "+toOpenOfficeFormula(child) + " # ";
			}
			s += "alignl size 8 \"R \" ";
		} else if (t instanceof Function){
			s += "color blue left [  font fixed stack{  \n";
			for(Entity child:getOrderedChildren(t)){
				s += " alignl "+toOpenOfficeFormula(child) + " # ";
			}
			s += "alignl size 8 \"D \" ";
		} else if (t instanceof Sequence) {
			s += "color black left [  font fixed stack{   \n";
			for(Entity child:getOrderedChildren(t)){
				s += " alignl "+toOpenOfficeFormula(child)+ " # ";
			}
			s += "alignl size 8 \"S \" ";
		} else {
			s += "color green left [  font fixed stack{   alignl size 8 \"T \"\n";
		}
		boolean first = true;
		for(Thread thread:t.getBundle()){
			if(first){
				first = false;
			} else {
				s += " # alignl ";
			}
			s += "nbold color black { ";
			Iterator<String> si = thread.iterator();
			int counter = thread.size();
			while(si.hasNext()){
				String label = si.next();
				if(counter == 5 && counter != thread.size() && si.hasNext()){
					s += "... rightarrow ";
					counter--;
					continue;
				}
				else if(counter > 5 && counter != thread.size()&& si.hasNext()){
					counter--;
					continue;
				}
				else if(label.equals("action")){
					continue;
				} else if(label.equalsIgnoreCase("thing")){
					continue;
				}
				else if(label.equalsIgnoreCase("from")){
					s += " italic \"FROM\"";
				}
				else if(label.equalsIgnoreCase("to")){
					s += " italic \"TO\"";
				}
				else if(label.equalsIgnoreCase("toward")){
					s += " italic \"TOWARD\"";
				}
				else if(label.equalsIgnoreCase("size")){
					s += " italic \"SIZE\"";
				}
				else if(label.equalsIgnoreCase("green")){
					s += " italic \"GREEN\"";
				}
				else{
					s += label.toUpperCase() + " ";
				}
				if(si.hasNext()){
					s += "rightarrow ";
				}
				counter--;
			}
			s += " }\n";
		}
		s += "} right none \n";
		return s;
	}
	
}
