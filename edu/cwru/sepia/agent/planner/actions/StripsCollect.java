package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.ResourceType;
import edu.cwru.sepia.util.Pair;

public class StripsCollect implements StripsAction {
	
	private Peasant collector;
	private Resource collection;

	
	public StripsCollect(Peasant collector, Resource collection){
		this.collection = collection;
		this.collector = collector;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// must be next to the correct and not holding anything
		if(collection.type == ResourceType.GOLD) {
			return collector.nextToGold && collector.holding == null && collection.quantity > 0;
		}
		else if (collection.type == ResourceType.WOOD) {
			return collector.nextToWood && collector.holding == null && collection.quantity > 0; 
		}
		else {
			return false;
		}
	}

	@Override
	public GameState apply(GameState state) {
		/* The peasant was assigned from 'this' instance, but the state that should be affected by this 
		 * StripsAction is a copy of that state, and thus so is the peasant.
		 */
		collector = state.getPeasant(collector.id);
		collection = state.getResource(collection.id);
		collector.holding = new Pair<>(collection.type, 100);
		collection.quantity = collection.quantity-100;
		state.cost++;
		state.prerequisiteActions.add(this);
		return state;
	}
	
	@Override
	public String toString() {
		return "COLLECT P" + collector.id + " " + collection.type + " " + collector.holding.b;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof StripsAction)) {
			return false;
		}
		else {
			StripsAction s = (StripsAction) o;
			return this.toString().equals(s.toString());
		}
	}
	
	public Peasant getCollector() {
		return collector;
	}
	
	public Resource getCollection() {
		return collection;
	}
}
