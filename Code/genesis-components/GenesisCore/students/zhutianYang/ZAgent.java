/**
 * 
 */
package zhutianYang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frames.entities.Entity;
import utils.Mark;
import utils.Z;

public class ZAgent {

	String name = "";
	String kind = "";
	String gender = "";
	int age = -1;
	List<String> events = new ArrayList<>();
	Map <String, String> outlooks = new HashMap<String, String>();
	Map <String, String> mentalities = new HashMap<String, String>();
	Map <String, String> reactions = new HashMap<String, String>();
	Map <String, String> roles = new HashMap<String, String>();
	Map <String, String> relationships = new HashMap<String, String>();

	public final static String KIND_HUMAN = "individual human";
	public final static String KIND_HUMAN_GROUP = "group of humans";
	
	public static void main(String[] args) {
		ZAgent zhutian = new ZAgent();
		zhutian.setName("Zhutian");
		zhutian.setAge(21);
		zhutian.addMentality("want", "love");
		Mark.say(zhutian.toString());
	}
	
	public ZAgent() {
		
	}
	
	public ZAgent(String name, String gender, String kind, int age, 
			List<String> events, Map <String, String> outlooks, 
			Map <String, String> mentalities, Map <String, String> reactions,
			Map <String, String> roles, Map <String, String> relationships) {
		
		this.name = name; // Zhutian, I
		this.kind = kind; // individual, group
		this.gender = gender; // she, he, it, they
		this.age = age; // 30
		this.events = events; // 30
		this.outlooks = outlooks; // height = "tall"
		this.mentalities = mentalities; // want = "kill Yuan", like = "beautiful women"
		this.reactions = reactions; // kill xx = "xx is disloyal";
		this.roles = roles; // king = "Ming Dynasty"
		this.relationships = relationships; // brother = "Bill", lover = "Steve"
		
	}
	
	public String toString() {
		String string = "";
		
		if(this.name.toLowerCase().equals("")) {
			string = "someone is ";
		} else if (name.toLowerCase().equals("i")) {
			string = "i am ";
		} else {
			if(this.kind==KIND_HUMAN_GROUP) {
				string = this.name + " are ";
			} else {
				string = this.name + " is ";
			}
		}
		
		if(this.age!=-1) {
			string += this.age + " years old, ";
		}
		
		if(!this.gender.equals("")) {
			string += this.gender + ", ";
		}

		if(!mentalities.isEmpty()) {
			for(String key : mentalities.keySet() ) {
				string += key + " " + mentalities.get(key) + ", ";
			}
		}
		
		if(!reactions.isEmpty()) {
			for(String key : reactions.keySet() ) {
				string += "will " + key + " if " + roles.get(key) + ", ";
			}
		}
		
		if(!roles.isEmpty()) {
			for(String key : roles.keySet() ) {
				string += key + " of " + roles.get(key) + ", ";
			}
		}
		
		if(!relationships.isEmpty()) {
			for(String key : relationships.keySet() ) {
				string += key + " of " + relationships.get(key) + ", ";
			}
		}
		
		if(string.endsWith(", ")) {
			string = Z.stringReplaceLast(string, ", ", ".");
		} else if (string.endsWith(" ")) {
			string = string.replace(" is ", "").replace(" am ", "").replace(" are ", "");
		}

		string = string.replace("is goal", "has goal");
		return string;
	}
	
	// --------------------------------------------------
	// class functions
	// -------------------------------------------------------
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.substring(0,1).toUpperCase() + name.substring(1);
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<String> getEvents() {
		return events;
	}
	
	public List<String> resetEvents() {
		return this.events = new ArrayList<>();
	}

	public void addEvent(String event) {
		this.events.add(event);
	}

	// --------------------------------------------------
	// class functions
	// -------------------------------------------------------
	public String getOutlooks(String key) {
		return outlooks.get(key);
	}
	
	public void addOutlook(String aspect, String attibute) {
		this.outlooks.put(aspect, attibute);
	}

	public String getMentality(String key) {
		return mentalities.get(key);
	}

	public void addMentality(String mentality, String value) {
		this.mentalities.put(mentality, value);
	}

	public String getReactions(String key) {
		return reactions.get(key);
	}

	public void addReaction(String action, String condition) {
		this.reactions.put(action, condition);
	}
	
	public String getRoles(String key) {
		return roles.get(key);
	}

	public void addRole(String role, String entity) {
		this.roles.put(role, entity);
	}

	public String getRelationships(String key) {
		return relationships.get(key);
	}

	public void addRelationship(String relationship, String entity) {
		this.relationships.put(relationship, entity);
	}

}
