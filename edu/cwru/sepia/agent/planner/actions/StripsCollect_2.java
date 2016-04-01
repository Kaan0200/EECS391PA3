package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.ResourceType;
import edu.cwru.sepia.util.Pair;

public class StripsCollect_2 implements StripsAction {
	
	private Peasant collector;
	private Peasant collector2;
	private Resource collection;

	
	public StripsCollect_2(Peasant collector, Peasant collector2, Resource collection){
		this.collection = collection;
		this.collector = collector;
		this.collector2 = collector2;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// must be next to the correct and not holding anything
		if(collection.type == ResourceType.GOLD) {
			return collector.nextToGold && collector.holding == null && collector2.nextToGold && collector2.holding == null && collection.quantity > 0;
		}
		else if (collection.type == ResourceType.WOOD) {
			return collector.nextToWood && collector.holding == null && collector2.nextToWood && collector2.holding == null && collection.quantity > 0; 
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
		collector2 = state.getPeasant(collector2.id);
		collection = state.getResource(collection.id);
		if(collection.quantity >= 200) {
			collector.holding = new Pair<>(collection.type, 100);
			collector2.holding = new Pair<>(collection.type, 100);
			collection.quantity = collection.quantity-200;
		} else {
			collector.holding = new Pair<>(collection.type, collection.quantity/2);
			collector2.holding = new Pair<>(collection.type, collection.quantity/2);
			collection.quantity = 0;
		}
		state.cost++;
		state.prerequisiteActions.add(this);
		return state;
	}
	
	@Override
	public String toString() {
		return "COLLECT_2 P" + collector.id + "&" + collector2.id + " " + collection.type + " " + (collector.holding.b + collector2.holding.b);
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
	
	public Peasant getCollector2() {
		return collector2;
	}
	
	public Resource getCollection() {
		return collection;
	}
}
