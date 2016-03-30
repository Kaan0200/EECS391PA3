package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.*;

public class StripsMove implements StripsAction {
	
	public Peasant mover;
	public Object location;
	
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
			return mover.nextToTownhall;
		}
	}

	@Override
	public GameState apply(GameState state) {
		// moving to townhall
		if (location == null) {
			mover.nextToTownhall = true;
			mover.nextToResource = false;
		} else {
			mover.nextToResource = true;
			mover.nextToTownhall = false;
		}

		return state;
	}
}
