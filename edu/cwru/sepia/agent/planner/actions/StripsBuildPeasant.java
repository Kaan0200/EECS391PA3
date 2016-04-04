package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.Peasant;

public class StripsBuildPeasant implements StripsAction {

	
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// state must have more then 0 food and enough gold
		return ((state.townhallFood > 0) &&
				(state.currentGold >= 400));
	}

	@Override
	public GameState apply(GameState state) {
		GameState.Peasant p = state.new Peasant();
		
		p.id = state.peasants.size() + 1;
		p.pos = state.townhallPos;
		p.holding = null;
		p.nextToTownhall = true;
		p.nextToGold = false;
		p.nextToWood = false;
		
		state.peasants.add(p);
		
		state.currentGold -= 400;
		state.townhallFood --;

		//Assume that this has no cost for now
		state.prerequisiteActions.add(this);
		return state;
		
	}
	
	@Override
	public String toString() {
		return "BUILD NEW PEASANT"; 
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
