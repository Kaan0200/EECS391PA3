package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.*;
import edu.cwru.sepia.agent.planner.ResourceType;

public class StripsMove_2 implements StripsAction {
	
	private Peasant mover;
	private Peasant mover2;
	private Resource location;
	
	/**
	 * constructor for moving to a specific resource
	 * @param mover
	 * @param resource
	 */
	public StripsMove_2(Peasant mover, Peasant mover2, Resource resource){
		this.mover = mover;
		this.mover2 = mover2;
		this.location = resource;
	}
	
	/**
	 * Constructor for moving towards the townhall
	 * @param mover
	 */
	public StripsMove_2(Peasant mover, Peasant mover2) {
		this.mover = mover;
		this.mover2 = mover2;
		this.location = null;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if(location == null){
			// return false if we are already next to the townhall
			return !mover.nextToTownhall && !mover2.nextToTownhall;
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
		mover2 = state.getPeasant(mover2.id);
		// moving to townhall
		if (location == null) {
			//We're assuming that the peasant is ALWAYS positioned at either the townhall or the nearest resource of gold/wood, so the cost
			//to reach the nearest gold/wood is assumed to be the distance between the townhall and that resource.
			double distTownToNearestGold = state.townhallPos.euclideanDistance(state.findNearestGold(state.townhallPos).pos);
    		double distTownToNearestWood = state.townhallPos.euclideanDistance(state.findNearestWood(state.townhallPos).pos);
			if(mover.nextToGold && mover2.nextToGold) {
				state.cost += distTownToNearestGold;
			}
			else if(mover.nextToWood && mover2.nextToWood) {
				state.cost += distTownToNearestWood;
			}
    		mover.nextToTownhall = true;
			mover.nextToGold = false;
			mover.nextToWood = false;
			mover2.nextToTownhall = true;
			mover2.nextToGold = false;
			mover2.nextToWood = false;
		} else {
			if(location.type == ResourceType.GOLD) {
				mover.nextToGold = true;
				mover.nextToWood = false;
				mover2.nextToGold = true;
				mover2.nextToWood = false;
			}
			else if (location.type == ResourceType.WOOD) {
				mover.nextToWood = true;
				mover.nextToGold = false;
				mover2.nextToWood = true;
				mover2.nextToGold = false;
			}
			mover.nextToTownhall = false;
			mover2.nextToTownhall = false;
			state.cost += mover.pos.euclideanDistance(location.pos);
		}
		state.prerequisiteActions.add(this);
		
		return state;
	}
	
	@Override
	public String toString() {
		if(location == null) {
			return "MOVE_2 P" + mover.id + "&" + mover2.id + " TO TOWNHALL";
		}
		else {
			return "MOVE_2 P" + mover.id + "&" + mover2.id + " TO " + location.type + location.id + 
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
	
	public Peasant getMover() {
		return mover;
	}
	
	public Peasant getMover2() {
		return mover2;
	}
	
	public Resource getLocation() {
		return location;
	}
}

