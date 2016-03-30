package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.Peasant;

public class StripsBuildPeasant implements StripsAction {

	
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// state must have more then 0 food and enough gold
		return ((state.townhallFood > 0) &&
				(state.currentGold > 400));
	}

	@Override
	public GameState apply(GameState state) {
		GameState.Peasant p = state.new Peasant();
		
		p.holding = null;
		p.nextToTownhall = true;
		p.nextToResource = false;
		
		state.peasants.add(state.new Peasant());
		//Assume that this has no cost for now
		state.prerequisiteActions.add(this);
		return state;
		
	}
	
	@Override
	public String toString() {
		return "BUILD NEW PEASANT"; 
	}

}
