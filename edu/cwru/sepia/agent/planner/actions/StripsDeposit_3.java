package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.ResourceType;

public class StripsDeposit_3 implements StripsAction{

	private Peasant depositer;
	private Peasant depositer2;
	private Peasant depositer3;
	//Save the resourceType in this variable since depositer's holding will become null when this action is applied
	ResourceType resourceType;
	private int curWood;
	private int curGold;
	
	public StripsDeposit_3(Peasant depositer, Peasant depositer2, Peasant depositer3){
		this.depositer = depositer;
		this.depositer2 = depositer2;
		this.depositer3 = depositer3;
		if(depositer.holding != null && depositer2.holding != null && depositer3.holding != null) {
			resourceType = depositer.holding.a;
		}
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// must be holding something and next to townhall
		return ((depositer.holding != null) && depositer.nextToTownhall
				&& (depositer2.holding != null) && depositer2.nextToTownhall
				&& (depositer3.holding != null) && depositer3.nextToTownhall);
	}

	@Override
	public GameState apply(GameState state) {
		/* The peasant was assigned from 'this' instance, but the state that should be affected by this 
		 * StripsAction is a copy of that state, and thus so is the peasant.
		 */
		depositer = state.getPeasant(depositer.id);
		depositer2 = state.getPeasant(depositer2.id);
		depositer3 = state.getPeasant(depositer3.id);
		if (depositer.holding.a == ResourceType.GOLD && depositer2.holding.a == ResourceType.GOLD && depositer3.holding.a == ResourceType.GOLD){
			state.currentGold += depositer.holding.b + depositer2.holding.b + depositer3.holding.b;
			depositer.holding = null;
			depositer2.holding = null;
			depositer3.holding = null;
		}
		else if (depositer.holding.a == ResourceType.WOOD && depositer2.holding.a == ResourceType.WOOD && depositer3.holding.a == ResourceType.WOOD){
			state.currentWood += depositer.holding.b + depositer2.holding.b + depositer3.holding.b;
			depositer.holding = null;
			depositer2.holding = null;
			depositer3.holding = null;
		} else {
			System.err.println("Unhandled attempt to deposit unknown resource type");
		}
		curWood = state.currentWood;
		curGold = state.currentGold;
		
		state.cost++;
		state.prerequisiteActions.add(this);
		System.out.println("CurrentWood: " + state.currentWood + " CurrentGold: " + state.currentGold);
		return state;
	}
	
	@Override
	public String toString() {
		return "DEPOSIT_3 P" + depositer.id + "&" + depositer2.id + "&" + depositer3.id + " " + resourceType + 
				" TO TOWNHALL - CG:" + curGold + " CW:" + curWood;
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
	
	public Peasant getDepositer() {
		return depositer;
	}
	
	public Peasant getDepositer2() {
		return depositer2;
	}
	
	public Peasant getDepositer3() {
		return depositer3;
	}
}
