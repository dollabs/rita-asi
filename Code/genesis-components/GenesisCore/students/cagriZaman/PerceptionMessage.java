package cagriZaman;

import frames.entities.Entity;
import frames.entities.Relation;
import generator.RoleFrames;
import translator.Translator;
import utils.Mark;

public class PerceptionMessage {
public String Type;
public String Message;
public String Subject;
public String Verb;
public String Object;
public String Adverb;
public String Feature;

public static Entity toRoleFrame(String S, String V, String A,String O){

	
	if(O==null)
		return toRoleFrame(S,V);
	else if(A.equals(""))
		return RoleFrames.makeRoleFrame(S,V,O);
	else
		return RoleFrames.makeRoleFrame(S, V, A,O);
}

public static Entity toRoleFrame(String S,String V){
	if(V==null)
		return RoleFrames.makeEntity(S);
	else
		return RoleFrames.makeRoleFrame(S, V);
	
}

public static Entity decodeMessage(String type,String message, String... args){
	
	if (type.equals("Feature"))
		return decodeFeatureMessage(message,args[0]);
	else if (type.equals("RoleFrame"))
		return decodeRoleFrameMessage(message);
	else if (type.equals("Action"))
		return decodeActionMessage(message);
	else
		return null;
	
}

private static Entity decodeFeatureMessage(String message, String subject){
	Entity e = Entity.getClassifiedThing(subject);
	e.addFeature(message);
	return e;
}

private static Entity decodeRoleFrameMessage(String message){
	Translator t=Translator.getTranslator();
	Entity e = t.internalize(message);
	return e;
	
}

private static Entity decodeActionMessage(String message){
	Translator t=Translator.getTranslator();
	Entity e = t.translate(message);
	return e;
	
}


//With Adverb
public static Relation toRelation( String Subject,String Object, String relation){
	
	return new Relation(relation,Entity.getClassifiedThing(Subject),Entity.getClassifiedThing(Object));
}

}

