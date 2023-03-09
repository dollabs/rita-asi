package matthewFay.StoryAlignment;

import utils.PairOfEntities;
import utils.minilisp.LList;
import frames.entities.Entity;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.Pair;

@SuppressWarnings("serial")
public class SequenceAlignment extends Alignment<Entity, Entity> {
	
	public LList<PairOfEntities> bindings = null;
	
	public SequenceAlignment(Alignment<Entity, Entity> alignment) {
		this.score = alignment.score;
		this.addAll(alignment);
	}
	
	public void fillGaps() {
		for(int i=0;i<this.size();i++) {
			Pair<Entity, Entity> pair = this.get(i);
			if(pair.a == null && pair.b != null) {
				//Gap Fill b into a//
				Entity eltToAdd = (Entity)pair.b.deepClone(false);
				eltToAdd = (Entity)EntityHelper.findAndReplace(eltToAdd, this.bindings, true, true);
				pair.a = eltToAdd;
				pair.a.addFeature("GapFilled");
			}
			if(pair.a != null && pair.b == null) {
				//Gap Fill a into b//
				//Mark Where Gap Filling Occurs
				Entity eltToAdd = (Entity)pair.a.deepClone(false);
				eltToAdd = (Entity)EntityHelper.findAndReplace(eltToAdd, this.bindings, true);
				pair.b = eltToAdd;
				pair.b.addFeature("GapFilled");
			}
		}
	}
	
	public void hackAlignment(Entity left_entity, Entity right_entity) {
		for(int i=0;i<this.size();i++) {
			Pair<Entity, Entity> pair = this.get(i);
			if(pair.a != null && pair.b == null) {
				String type = pair.a.getType();
				if(type.contains("eat") || type.contains("kill") || type.contains("murder")) {
					if(EntityHelper.contains(left_entity, pair.a)) {
						//Do crazy pushing
						int j = i+1;
						while(j<this.size()) {
							Pair<Entity, Entity> next_pair = this.get(j);
							if(next_pair.a == null && next_pair.b != null && !EntityHelper.contains(right_entity, next_pair.b)) {
								this.remove(i);
								this.add(j, pair);
								i = j;
								j++;
							} else {
								break;
							}
						}
					}
				}
				if(type.contains("die")) {
					//Do crazy pushing
					int j = i+1;
					while(j<this.size()) {
						Pair<Entity, Entity> next_pair = this.get(j);
						if(next_pair.a == null) {
							this.remove(i);
							this.add(j, pair);
							i = j;
							j++;
						} else {
							break;
						}
					}
				}
			}
			if(pair.a == null && pair.b != null) {
				String type = pair.b.getType();
				if(type.contains("eat") || type.contains("kill") || type.contains("murder")) {
					if(EntityHelper.contains(right_entity, pair.b)) {
						//Do crazy pushing
						int j = i+1;
						while(j<this.size()) {
							Pair<Entity, Entity> next_pair = this.get(j);
							if(next_pair.a != null && next_pair.b == null && !EntityHelper.contains(left_entity, next_pair.a)) {
								this.remove(i);
								this.add(j, pair);
								i = j;
								j++;
							} else {
								break;
							}
						}
					}
				}
				if(type.contains("die")) {
					//Do crazy pushing
					int j = i+1;
					while(j<this.size()) {
						Pair<Entity, Entity> next_pair = this.get(j);
						if(next_pair.b == null) {
							this.remove(i);
							this.add(j, pair);
							i = j;
							j++;
						} else {
							break;
						}
					}
				}
			}
		}
	}
	
	public boolean selectiveFillGaps(Entity left_entity, Entity right_entity) {
		boolean dirty = false;
		for(int i=0;i<this.size();i++) {
			Pair<Entity, Entity> pair = this.get(i);
			if(pair.a != null && pair.b == null) {
				if(EntityHelper.contains(left_entity, pair.a)) {
					//Gap Fill a into b//
					//Mark Where Gap Filling Occurs
					Entity eltToAdd = (Entity)pair.a.deepClone(false);
					//Unecessary because generics are removed earlier
					//eltToAdd = (Entity)EntityHelper.findAndReplace(eltToAdd, this.bindings, true);
					pair.b = eltToAdd;
					dirty = true;
					//pair.b.addFeature("GapFilled");
				}
			}
			if(pair.a == null && pair.b != null) {
				if(EntityHelper.contains(right_entity, pair.b)) {
					//Gap Fill b into a//
					Entity eltToAdd = (Entity)pair.b.deepClone(false);
					//Unecessary because generics are removed earlier
					//eltToAdd = (Entity)EntityHelper.findAndReplace(eltToAdd, this.bindings, true);
					pair.a = eltToAdd;
					dirty = true;
					//pair.a.addFeature("GapFilled");
				}
			}
		}
		return dirty;
	}
	
	public boolean selectiveFillGaps(Entity required_entity) {
		boolean dirty = false;
		for(int i=0;i<this.size();i++) {
			Pair<Entity, Entity> pair = this.get(i);
			if(pair.a == null && pair.b != null) {
				if(EntityHelper.contains(required_entity, pair.b)) {
					//Gap Fill b into a//
					Entity eltToAdd = (Entity)pair.b.deepClone(false);
					eltToAdd = (Entity)EntityHelper.findAndReplace(eltToAdd, this.bindings, true);
					pair.a = eltToAdd;
					dirty = true;
					//pair.a.addFeature("GapFilled");
				}
			}
			if(pair.a != null && pair.b == null) {
				if(EntityHelper.contains(required_entity, pair.a)) {
					//Gap Fill a into b//
					//Mark Where Gap Filling Occurs
					Entity eltToAdd = (Entity)pair.a.deepClone(false);
					eltToAdd = (Entity)EntityHelper.findAndReplace(eltToAdd, this.bindings, true);
					pair.b = eltToAdd;
					dirty = true;
					//pair.b.addFeature("GapFilled");
				}
			}
		}
		return dirty;
	}
}
