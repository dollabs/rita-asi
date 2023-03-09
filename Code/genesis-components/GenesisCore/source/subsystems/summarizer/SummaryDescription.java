package subsystems.summarizer;

import java.util.*;

import frames.entities.Entity;
import storyProcessor.ConceptDescription;
import utils.Mark;

/*
 * Created on Dec 26, 2013
 * @author phw
 */

public class SummaryDescription {

	Set<Entity> randomSummary = new HashSet<Entity>();

	Set<Entity> completeStory = new HashSet<Entity>();

	Set<Entity> essential = new HashSet<Entity>();

	Set<Entity> connected = new HashSet<Entity>();

	Set<Entity> concept = new HashSet<Entity>();

	Set<Entity> dominant = new HashSet<Entity>();

	Set<Entity> special = new HashSet<Entity>();

	List<Entity> questions = new ArrayList<Entity>();

	List<ConceptDescription> conceptDescriptions;

	public Set<Entity> getRandomSummary() {
		return randomSummary;
	}

	public void setRandom(Set<Entity> random) {
		this.randomSummary = random;
	}

	public Set<Entity> getCompleteStory() {
		return completeStory;
	}

	public void setCompleteStory(Set<Entity> x) {
		this.completeStory = x;
	}

	public Set<Entity> getEssential() {
		return essential;
	}

	public void setEssential(Set<Entity> x) {
		this.essential = x;
	}

	public Set<Entity> getConnected() {
		return connected;
	}

	public void setConnected(Set<Entity> x) {
		this.connected = x;
	}

	public Set<Entity> getConcept() {
		return concept;
	}

	public void setConcept(Set<Entity> x) {
		this.concept = x;
	}

	public Set<Entity> getDominant() {
		return dominant;
	}

	public void setDominant(Set<Entity> dominant) {
		this.dominant = dominant;
	}

	public Set<Entity> getSpecial() {
		return special;
	}

	public void setSpecial(Set<Entity> x) {
		this.special = x;
	}

	public HashSet<Entity> getQuestions() {
		HashSet<Entity> result = new HashSet<Entity>();
		result.addAll(questions);
		return result;
	}

	public void setQuestions(List<Entity> questions) {
		this.questions = questions;
	}

	public void clear() {
		getRandomSummary().clear();

		getEssential().clear();

		getConnected().clear();

		getConcept().clear();

		getDominant().clear();
		getQuestions().clear();

	}

	public void setConceptDescriptions(List<ConceptDescription> conceptDescriptions) {
		this.conceptDescriptions = conceptDescriptions;
	}

	public List<ConceptDescription> getConceptDescriptions() {
		return conceptDescriptions;
	}

}
