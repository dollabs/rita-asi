package matchers.representations;

import java.util.ArrayList;
import java.util.List;

import matchers.BindingValidator;
import utils.PairOfEntities;
import utils.minilisp.LList;

public class EntityMatchResult {
	public List<BindingPair> bindings = new ArrayList<>();
	public double score = -1;
	public boolean structureMatch = false;
	public boolean semanticMatch = false;
	public boolean inversion = false;
	
	public boolean isMatch() {
		return structureMatch && semanticMatch && !inversion;
	}
	
	public boolean isInversion() {
		return structureMatch && semanticMatch && inversion;
	}
	
	public EntityMatchResult() {
		
	}
	
	public EntityMatchResult(double score, boolean inversion, boolean structureMatch, List<BindingPair> bindings) {
		this.bindings.addAll(bindings);
		this.score = score;
		this.inversion = inversion;
		this.structureMatch = structureMatch;
		
		if(score < 0)
			semanticMatch = false;
		else
			semanticMatch = true;
		
		if(!isMatch() && !isInversion())
			this.bindings.clear();
	}
	
	public EntityMatchResult(boolean match, double score, boolean inversion, boolean structureMatch, List<BindingPair> bindings) {
		this.bindings.addAll(bindings);
		this.score = score;
		this.inversion = inversion;
		this.structureMatch = structureMatch;
		this.semanticMatch = match;
		
		if(score < 0)
			this.semanticMatch = false;
		
		if(!isMatch() && !isInversion())
			this.bindings.clear();
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "(match = "+semanticMatch+")\n";
		str += "(inversion = "+inversion+")\n";
		str += "(score: "+score+")\n";
		str += "(structureMatch: "+structureMatch+")\n";
		str += "(bindings:\n";
		if(bindings != null) {
			for(BindingPair pair : bindings)
				str += pair+"\n";
		} else {
			str += "null\n";
		}
		str = str+")\n"; 
		str += "(match = "+semanticMatch+")";
		return str;
	}
	
	//Shortcut methods, provide only aesthetic value//
	public LList<PairOfEntities> toLList() {
		if(this.isMatch()) {
			return BindingValidator.convertToLList(bindings);
		} else {
			return null;
		}
	}
	
	public LList<PairOfEntities> toNegationLList() {
		if(this.isInversion()) {
			return BindingValidator.convertToLList(bindings);
		} else {
			return null;
		}
	}
	
	public void validateBindings() {
		BindingValidator bv = new BindingValidator();
		this.bindings = bv.validateBindings(this.bindings);
	}
	
	public void validateBindings(LList<PairOfEntities> constraints) {
		BindingValidator bv = new BindingValidator();
		this.bindings = bv.validateBindings(this.bindings, constraints);
	}
}