package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.ResourceType;

public class StripsDeposit implements StripsAction{

	Peasant depositer;
	//Save the resourceType in this variable since depositer's holding will become null when this action is applied
	ResourceType resourceType;
	
	public StripsDeposit(Peasant depositer){
		this.depositer = depositer;
		if(depositer.holding != null) {
			resourceType = depositer.holding.a;
		}
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// must be holding something and next to townhall
		return ((depositer.holding != null) &&
				depositer.nextToTownhall);
	}

	@Override
	public GameState apply(GameState state) {
		/* The peasant was assigned from 'this' instance, but the state that should be affected by this 
		 * StripsAction is a copy of that state, and thus so is the peasant.
		 */
		depositer = state.getPeasant(depositer.id);
		if (depositer.holding.a == ResourceType.GOLD){
			state.currentGold += depositer.holding.b;
			depositer.holding = null;
		}
		else if (depositer.holding.a == ResourceType.WOOD){
			state.currentWood += depositer.holding.b;
			depositer.holding = null;
		} else {
			System.err.println("Unhandled attempt to deposit unknown resource type");
		}
		
		state.cost++;
		state.prerequisiteActions.add(this);
		System.out.println("CurrentWood: " + state.currentWood + " CurrentGold: " + state.currentGold);
		return state;
	}
	
	@Override
	public String toString() {
		return "DEPOSIT P" + depositer.id + " " + resourceType + " TO TOWNHALL";
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
}
