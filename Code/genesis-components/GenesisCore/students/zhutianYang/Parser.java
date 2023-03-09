package zhutianYang;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import frames.entities.Entity;
import translator.Translator;
import utils.Mark;
import utils.Z;

public class Parser {
	
	static int INDENT = 2;
	static Translator t = Translator.getTranslator();

	public static void main(String[] args) {
//		String english = "You need to find the yellow cup and pour the water from the cup to the bowl.";
//		parse(english);
		
		List<String> sentences = new ArrayList<>();
		// pour
		sentences.add("pour water into the cup");
		sentences.add("put more water to the cup");
		sentences.add("pass the water into the cup");
		sentences.add("transfer the water into the cup");
		sentences.add("add water to the cup");
		sentences.add("fill the cup with water");
		sentences.add("drip some lemon juice into the cup");
		sentences.add("discard the remaining water to the cup");
		sentences.add("clear the cup");
		
		// get
		sentences.add("get water from the cup");
		sentences.add("take out water from the cup");
		sentences.add("get rid of the water from the cup");
		sentences.add("clear the water from the cup");
		sentences.add("discard the water in the cup");
		sentences.add("pour the water out of the cup");

		int length = 40;
		for(String sentence:sentences) {
			Entity entity = t.translate(sentence).getElement(0);
			String classifications = entity.getPrimedThread().toString();
			classifications = classifications.replace("<thread>", "")
					.replace("</thread>", "").replace("\r", "").replace("\n", "");
			
			String space = "";
			for(int i=length;i>sentence.length();i--) space+=" ";
			Mark.say(sentence + space + classifications);
		}
		
	}
	
	public static JSONObject parse(String english) {
		JSONObject json = new JSONObject();
		Entity entity = t.translate(english);
		for(int i = 0; i < entity.getElements().size();i++) {
			Entity entity1 = entity.getElement(i);
			JSONObject json1 = new JSONObject();
			
			Map<String, String> roles = Z.getRoles(entity1);
			json1.putAll(roles);
			json.put(i,json1);
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Mark.say(gson.toJson(json));
		return json;
	}

}
