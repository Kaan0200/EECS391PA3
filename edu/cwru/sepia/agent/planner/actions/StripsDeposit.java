package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.ResourceType;

public class StripsDeposit implements StripsAction{

	Peasant depositer;
	
	public StripsDeposit(Peasant depositer){
		this.depositer = depositer;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// must be holding something and next to townhall
		return ((depositer.holding != null) &&
				depositer.nextToTownhall);
	}

	@Override
	public GameState apply(GameState state) {
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
		return state;
	}
}
