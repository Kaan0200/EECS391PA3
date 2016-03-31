package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.*;
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
	// Cost to reach this state
	public int cost;
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
		public boolean nextToGold;
		public boolean nextToWood;
		public boolean nextToTownhall;
		public Pair<ResourceType, Integer> holding;
		
		public Peasant(int id, Position pos, boolean nextToGold, boolean nextToWood, boolean nextToTownhall, Pair<ResourceType, Integer> holding) {
			this.id = id;
			this.pos = pos;
			this.nextToGold = nextToGold;
			this.nextToWood = nextToWood;
			this.nextToTownhall = nextToTownhall;
			this.holding = holding;
		}
		
		public Peasant() {
			//this(null, null, null, null, null);
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Peasant)) return false;
			else {
				Peasant other = (Peasant) o;
				boolean same = this.id == other.id &&
						this.pos.equals(other.pos) &&
						this.nextToGold == other.nextToGold &&
						this.nextToWood == other.nextToWood &&
						this.nextToTownhall == other.nextToTownhall;
				return same;
			}
		}
	}
	// class representing a resource and how much can be mined
	public class Resource {
		public int id;
		public Position pos;
		public ResourceType type;
		public Integer quantity;
		
		public Resource(int id, Position pos, ResourceType type, Integer quantity) {
			this.id = id;
			this.pos = pos;
			this.type = type;
			this.quantity = quantity;
		}
		
		public Resource() {
			
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Resource)) return false;
			else {
				Resource other = (Resource) o;
				boolean same = this.id == other.id &&
						this.pos.equals(other.pos) &&
						this.type == other.type &&
						this.quantity == other.quantity;
				return same;
			}
		}
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
    	prerequisiteActions = new ArrayList<StripsAction>();
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
    	state.getAllUnits().forEach((u) -> {
    		// is actually townhall
    		if (u.getTemplateView().getName().equals("TownHall")) {
    			townhallPos = new Position(u.getXPosition(), u.getYPosition());
    		} else { // creating peasant
    			Peasant newP = new Peasant();
    			newP.id = u.getID();
    			newP.pos = new Position(u.getXPosition(), u.getYPosition());
    			newP.nextToTownhall = true;
    			newP.holding = null;
    			peasants.add(newP);
    		}
    	});
    }
    
    /**
     * Need a constructor that will create an exact copy of the given state
     * @param prevState
     */
    public GameState(GameState prevState) {
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
    	peasants = new ArrayList<>();
    	resources = new ArrayList<>();
    	for(Peasant p : prevState.peasants) {
    		this.peasants.add(new Peasant(p.id, p.pos, p.nextToGold, p.nextToWood, p.nextToTownhall, p.holding));
    	}
    	for(Resource r : prevState.resources) {
    		this.resources.add(new Resource(r.id, r.pos, r.type, r.quantity));
    	}
    	prerequisiteActions = new ArrayList<StripsAction>();
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
        if ((requiredGold <= currentGold) && (requiredWood <= currentWood)) {
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
		ArrayList<ArrayList<StripsAction>> peasantActions = new ArrayList<ArrayList<StripsAction>>();
		
		int index = 0; //Index of the peasantActions
	  	for (Peasant p : peasants) {
	  		peasantActions.add(new ArrayList<StripsAction>());
		      // check if we even need to be looking for more resources
		      if (p.holding != null) {
		        // next to townhall?
		        if (p.nextToTownhall) {
		          //Create StripsDeposit object
		        	StripsDeposit stripD = new StripsDeposit(p);
		        	if(stripD.preconditionsMet(this)) {
		        		peasantActions.get(index).add(stripD);
		        	}
		        } else {
		          // move back to be townhall
		          //Create StripsMove to townhall
		        	StripsMove stripM = new StripsMove(p);
		        	if(stripM.preconditionsMet(this)) {
		        		peasantActions.get(index).add(stripM);
		        	}
		        }
		      }
	          //find nearest gold
	      	  Resource nearestGold = findNearestGold(p.pos);
		      if(p.holding == null && p.nextToGold) {
	      		  //Create StripsCollect object
		    	  StripsCollect stripCollectGold = new StripsCollect(p, nearestGold);
		    	  if(stripCollectGold.preconditionsMet(this)) {
		        		peasantActions.get(index).add(stripCollectGold);
		    	  }
	      	  }
		      else {
		    	  //Create StripsMove object
		    	  StripsMove stripMoveGold = new StripsMove(p,nearestGold);
		    	  if(stripMoveGold.preconditionsMet(this)) {
		    		  peasantActions.get(index).add(stripMoveGold);
		    	  }
		      }
	          //find nearest forest
	      	  Resource nearestWood = findNearestWood(p.pos);
		      if(p.holding == null && p.nextToWood) {
	      		  //Create StripsCollect object
		    	  StripsCollect stripCollectWood = new StripsCollect(p, nearestWood);
		    	  if(stripCollectWood.preconditionsMet(this)) {
		        		peasantActions.get(index).add(stripCollectWood);
		    	  }
	      	  }
		      else {
		    	  //Create StripsMove object
		    	  StripsMove stripMoveWood = new StripsMove(p,nearestWood);
		    	  if(stripMoveWood.preconditionsMet(this)) {
		    		  peasantActions.get(index).add(stripMoveWood);
		    	  }
		      }
		      index++;
	        
	    }
	  	/* If resources and capacity allow for it, the action to create a new peasant will be treated as
	  	 * the action of a separate peasant in peasantActions.  This will ensure that the creation of the new
	  	 * peasant will be included in all possible child GameStates through combineStrips. */
	    if (allowBuildPeasants) {
    		// check for the available resources
    		if ((townhallFood > 0) && (currentGold >= 400)){
    			// TODO: create new peasant
    			StripsBuildPeasant stripBuild = new StripsBuildPeasant();
    			if(stripBuild.preconditionsMet(this)) {
	        		peasantActions.get(index).add(stripBuild);
    			}
    		}
    	}
	    //create a combinatorial list of StripsActions
	    returnStates = combineStrips(peasantActions);
	    return returnStates;
    }

    /**
     * Returns a combinatorial list of GameStates based on all of the possible actions that the peasants can make.
     * @param peasantActions
     * @return
     */
    private List<GameState> combineStrips(ArrayList<ArrayList<StripsAction>> peasantActions) {
    	ArrayList<GameState> stateList = new ArrayList<>();
    	//TODO This object right now, a list of actionsForOneState will need to be consolidated into StripsMove_k and the like
    	ArrayList<StripsAction> actionsForOneState = new ArrayList<>();
    	int solutions = 1;
        for(int i = 0; i < peasantActions.size(); solutions *= peasantActions.get(i).size(), i++);
        for(int i = 0; i < solutions; i++) {
            actionsForOneState.clear();
        	int j = 1;
            for(ArrayList<StripsAction> onePeasantActions : peasantActions) {
                actionsForOneState.add(onePeasantActions.get((i/j)%onePeasantActions.size()));
            	//System.out.print(onePeasantActions.get((i/j)%onePeasantActions.size()) + " ");
                j *= onePeasantActions.size();
            }
            //Create a clone of the current state
            GameState newState = new GameState(this);
            //Apply all of the new actions
            for(StripsAction action : actionsForOneState) {
            	newState = action.apply(newState);
            }
            stateList.add(newState);
        }
		return stateList;
	}

    /**
     * Find the nearest forest resource to the given peasant
     * @param p
     * @return
     */
	private Resource findNearestWood(Position p) {
		ArrayList<Pair<Resource,Double>> resourceDistances = new ArrayList<>();
		for(Resource r : resources) {
			if(r.type == ResourceType.WOOD && r.quantity > 0) {
				resourceDistances.add(new Pair<Resource,Double>(r,r.pos.euclideanDistance(p)));
			}
		}
		//Find min distance
		Resource nearestWood = null;
		double minDist = Double.MAX_VALUE;
		for(Pair<Resource,Double> pair : resourceDistances) {
			if(pair.b < minDist) {
				nearestWood = pair.a;
				minDist = pair.b;
			}
		}
		if(nearestWood == null) {
			System.err.println("There don't appear to be any non-empty forests left!");
		}
		return nearestWood;
	}

	/**
	 * Find the nearest gold resource to the given peasant
	 * @param p
	 * @return
	 */
	private Resource findNearestGold(Position p) {
		ArrayList<Pair<Resource,Double>> resourceDistances = new ArrayList<>();
		for(Resource r : resources) {
			if(r.type == ResourceType.GOLD && r.quantity > 0) {
				resourceDistances.add(new Pair<Resource,Double>(r,r.pos.euclideanDistance(p)));
			}
		}
		//Find min distance
		Resource nearestGold = null;
		double minDist = Double.MAX_VALUE;
		for(Pair<Resource,Double> pair : resourceDistances) {
			if(pair.b < minDist) {
				nearestGold = pair.a;
				minDist = pair.b;
			}
		}
		if(nearestGold == null) {
			System.err.println("There don't appear to be any non-empty gold mines left!");
		}
		return nearestGold;
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
    	//Estimate the number of trips it will take to transport the required amount of resource from the nearest
    	//source to the townhall.  Multiply by 2 to account for round trips.
    	double goldTrips = (Math.max((requiredGold-currentGold),0)/(100 * peasants.size())) * 2;
    	double goldCost = goldTrips * townhallPos.euclideanDistance(findNearestGold(townhallPos).pos);
    	
    	double woodTrips = (Math.max((requiredWood-currentWood),0)/(100 * peasants.size())) * 2;
    	double woodCost = woodTrips * townhallPos.euclideanDistance(findNearestWood(townhallPos).pos);
    	
    	//If any peasant is nearer to a resource than the distance between the townhall and that resource, let it count in the agent's favor
    	double nearToResource = 0;
    	
    	for(Peasant p : peasants) {
    		//If the peasant is holding a resource and is nextToTownhall, ready to deposit, then subtract a roundtrip from the cost to that resource
    		//Actually, subtract ALMOST a full roundtrip.  Depositing will actually reduce by a full roundtrip.
    		if(p.holding != null && p.nextToTownhall) {
    			//If a peasant is holding a resource, subtract one trips' worth of cost from that resource
    			if(p.holding.a == ResourceType.GOLD) {
    				goldCost -= 1.5 * townhallPos.euclideanDistance(findNearestGold(townhallPos).pos);
    			}
    			else if(p.holding.a == ResourceType.WOOD) {
    				woodCost -= 1.5 * townhallPos.euclideanDistance(findNearestWood(townhallPos).pos);
    			}
    		}
    		//Assume that the peasant will move toward whichever resource wood/gold is nearer, and use that distance to get a higher nearest resource value
    		double pNearestResource = Math.max(townhallPos.euclideanDistance(findNearestWood(townhallPos).pos) - p.pos.euclideanDistance(findNearestWood(townhallPos).pos),
    				townhallPos.euclideanDistance(findNearestGold(townhallPos).pos) - p.pos.euclideanDistance(findNearestGold(townhallPos).pos));
    		nearToResource += pNearestResource;
    	}
        return Math.round((goldCost + woodCost - nearToResource) * 1000)/1000.0;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return cost;
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
        
    	else {
    		GameState other = (GameState) o;
    		boolean same = this.mapX == other.mapX &&
	        	this.mapY == other.mapY &&
	        	this.requiredGold == other.requiredGold &&
	        	this.requiredWood == other.requiredWood &&
	        	this.townhallPos == other.townhallPos &&
	        	this.allowBuildPeasants == other.allowBuildPeasants &&
	        	this.currentGold == other.currentGold &&
	        	this.currentWood == other.currentWood &&
	        	this.townhallFood == other.townhallFood;
    		if(same) {
    			for(StripsAction action : this.prerequisiteActions) {
    				boolean actionFound = false;
    				for(StripsAction otherA : other.prerequisiteActions) {
    					if(action.equals(otherA)) {
    						actionFound = true;
    						break;
    					}
    				}
    				if(!actionFound) {
    					same = false;
    					break;
    				}
    			}
    		}
    		if(same) {
	        	for(Peasant p : this.peasants) {
	        		boolean peasantFound = false;
	        		for(Peasant otherP : other.peasants) {
	        			if(p.equals(otherP)) {
	        				peasantFound = true;
	        				break;
	        			}
	        		}
	        		if(!peasantFound) {
	        			same = false;
	        			break;
	        		}
	        	}
    		}
    		if(same) {
	        	for(Resource r : this.resources) {
	        		boolean resourceFound = false;
	        		for(Resource otherR : other.resources) {
	        			if(r.equals(otherR)) {
	        				resourceFound = true;
	        				break;
	        			}
	        		}
	        		if(!resourceFound) {
	        			same = false;
	        			break;
	        		}
	        	}
    		}
    		return same;
    	}
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
    
    public Peasant getPeasant(int id) {
    	for(Peasant p: peasants) {
    		if(p.id == id) {
    			return p;
    		}
    	}
    	System.err.println("Error! This GameState instance does not have a peasant with the given id: " + id);
    	return null;
    }
}


