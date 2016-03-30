package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.util.Pair;

public class StripsCollect implements StripsAction {
	
	Peasant collector;
	Resource collection;
	
	public StripsCollect(Peasant collector, Resource collection){
		this.collection = collection;
		this.collector = collector;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// must be next to a resource and not holding anything
		return ((collector.nextToGold || collector.nextToWood) &&
				(collector.holding == null) &&
				(collection.quantity > 0));
	}

	@Override
	public GameState apply(GameState state) {
		/* The peasant was assigned from 'this' instance, but the state that should be affected by this 
		 * StripsAction is a copy of that state, and thus so is the peasant.
		 */
		collector = state.getPeasant(collector.id);
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
}
