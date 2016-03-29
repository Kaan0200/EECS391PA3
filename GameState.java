package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.util.Pair;
import edu.cwru.sepia.util.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent the state of the game after applying one of the avai'able actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
	
	//--------------------STATIC VALUES---------------------//
	// map dimensions
	public int mapX, mapY;
	// requirements for completion
	public int requiredGold, requiredWood;
	// townhall position
	public Position townhallPos;
	// are we allowing building of more peasants?
	public boolean allowBuildPeasants;
	//-------------------DYNAMIC VALUES---------------------//
	// current values of resources
	public int currentGold, currentWood;
	// current food available from the townhall (default is 3)
	public int townhallFood;
	// actions took to get to this place
	public List<StripsAction> prerequisiteActions = null;
	public List<Peasant> peasants = null;
	public List<Resource> resources = null;
	//-------------------INTERNAL CLASSES-------------------//
	// class representing a peasant and what they are holding
	public class Peasant {
		public int id;
		public Position pos;
		public Pair<ResourceType, Integer> holding;
	}
	// class representing a resource and how much can be mined
	public class Resource {
		public int id;
		public Position pos;
		public ResourceType type;
		public Integer quantity;
	}
	
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
    	// find and grab peasant
    	mapX = state.getXExtent();
    	mapY = state.getYExtent();
    	this.requiredGold = requiredGold;
    	this.requiredWood = requiredWood;
    	allowBuildPeasants = buildPeasants;
    	// set all the fields to clean values, we know they will all be empty;
    	prerequisiteActions = null;
    	currentGold = 0;
    	currentWood = 0;
    	townhallFood = 3;
    	peasants = new ArrayList<>();
    	resources = new ArrayList<>();
    	// get all resource nodes & peasants
    	state.getAllResourceNodes().forEach((n) -> {
    		Resource newR = new Resource();
    		newR.id = n.getID();
    		newR.pos = new Position(n.getXPosition(), n.getYPosition());
    		newR.quantity = n.getAmountRemaining();
    		newR.type = (n.getType() == ResourceNode.Type.TREE ? ResourceType.WOOD : ResourceType.GOLD);
    		resources.add(newR);
    	});
    	state.getAllUnits().forEach((u) ->{
    		// is actually townhall
    		if (u.getTemplateView().getName().equals("TownHall")) {
    			townhallPos = new Position(u.getXPosition(), u.getYPosition());
    		} else { // creating peasant
    			Peasant newP = new Peasant();
    			newP.id = u.getID();
    			newP.pos = new Position(u.getXPosition(), u.getYPosition());
    			newP.holding = null;
    			peasants.add(newP);
    		}
    	});
    }
    
    /**
     * This is a constructor that will be the next game state with all the actions applied
     * @param prevState
     * @param actions
     */
    public GameState(GameState prevState, List<StripsAction> actions) {
    	// peel off variables
    	this.mapX = prevState.mapX;
    	this.mapY = prevState.mapY;
    	this.requiredGold = prevState.requiredGold;
    	this.requiredWood = prevState.requiredWood;
    	this.townhallPos = prevState.townhallPos;
    	this.allowBuildPeasants = prevState.allowBuildPeasants;
    	this.currentGold = prevState.currentGold;
    	this.currentWood = prevState.currentWood;
    	this.townhallFood = prevState.townhallFood;
    	this.peasants = prevState.peasants;
    	this.resources = prevState.resources;
    	
    	// save the actions that generated this change state
    	this.prerequisiteActions = actions;
    	// apply all actions
    	for(StripsAction a : actions) {
    		prevState = a.apply(prevState);
    	}
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
    	// do we have the required gold and wood?
        if ((requiredGold == currentGold) && (requiredWood == currentWood)) {
        	return true;
        }
        return false;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
    	// create return list
    	List<GameState> returnStates = new ArrayList<GameState>();
    	
    	Peasant p = peasants.get(0);
    	
        for(Direction d : Direction.values()) {
        	// check to make sure direction is in bounds
        	if (p.pos.move(d).inBounds(mapX, mapY)){
        		// check that it is not a resource
        		for(Resource r : resources){
        			if (r.pos == p.pos.move(d)){
        				// the move would put him on a resource
        				if (p.holding == null) {
        					// holding nothing so, farm from it.
        					
        					//-----------------------
        					System.out.println("attempting to harvest");
        					
        				}
        			}
        		}
        		// check if attempting to move into townhall
        		if (p.pos.move(d).equals(townhallPos)){
        			
        			if (p.holding != null) {
        				
        				System.out.println("attempting to deposit into townhall :" + d.toString());
        			}
        		} else {
        			// not trying to move into townhall or resource, so just movement
        			
        			//-----------------------
        			System.out.println("Moving in direction :" + d.toString());
        		}
        	}
        }

    	if (allowBuildPeasants) {
    		// check for the available resources
    		if ((townhallFood > 0) && (currentGold >= 400)){
    			// TODO: create new peasant
    		}
    	}
    	

        return returnStates;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Implement me!
        return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
    	// check for gamestates only
    	if (!(o instanceof GameState)) return false;
        // TODO: Implement me!
        return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
        return 0;
    }
}

enum ResourceType {
  GOLD,
  WOOD
}
