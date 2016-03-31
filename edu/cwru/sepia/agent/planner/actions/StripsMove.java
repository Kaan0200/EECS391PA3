package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.ResourceType;

public class StripsMove implements StripsAction {
	
	public Peasant mover;
	public Resource location;
	
	/**
	 * constructor for moving to a specific resource
	 * @param mover
	 * @param resource
	 */
	public StripsMove(Peasant mover, Resource resource){
		this.mover = mover;
		this.location = resource;
	}
	
	/**
	 * Constructor for moving towards the townhall
	 * @param mover
	 */
	public StripsMove(Peasant mover) {
		this.mover = mover;
		this.location = null;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if(location == null){
			// return false if we are already next to the townhall
			return !mover.nextToTownhall;
		} else {
			return true;
		}
	}

	@Override
	public GameState apply(GameState state) {
		/* The peasant was assigned from 'this' instance, but the state that should be affected by this 
		 * StripsAction is a copy of that state, and thus so is the peasant.
		 */
		mover = state.getPeasant(mover.id);
		// moving to townhall
		if (location == null) {
			mover.nextToTownhall = true;
			mover.nextToGold = false;
			mover.nextToWood = false;
			state.cost += mover.pos.euclideanDistance(state.townhallPos);
		} else {
			if(location.type == ResourceType.GOLD) {
				mover.nextToGold = true;
			}
			else if (location.type == ResourceType.WOOD) {
				mover.nextToWood = true;
			}
			mover.nextToTownhall = false;
			state.cost += mover.pos.euclideanDistance(location.pos);
		}
		state.prerequisiteActions.add(this);
		
		return state;
	}
	
	@Override
	public String toString() {
		if(location == null) {
			return "MOVE P" + mover.id + " TO TOWNHALL";
		}
		else {
			return "MOVE P" + mover.id + " TO " + location.type + location.id + 
					" X: " + location.pos.x + " Y: " + location.pos.y;
		}
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
