package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

    	GameState startState = new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants);
        Stack<StripsAction> plan = AstarSearch(startState);

        if(plan == null) {
            System.err.println("No plan was found");
            //System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
    	
    	HashSet<GameState> closedList = new HashSet<GameState>();
    	Stack<GameState> astarPath = AstarInternal(startState, closedList);
    	
    	
    	
    	
        // TODO: Implement me!
        return null;
    }
    
    private Stack<GameState> AstarInternal(GameState initialState, HashSet<GameState> closedList) {
    	//Get the possible choices
    	ArrayList<GameState> stateList = (ArrayList<GameState>) initialState.generateChildren();
    	
    	//Remove states that are in the closedList
    	stateList = removeClosedList(stateList, closedList);
    	
    	//Populate mapping of GameStates and A* cost
    	HashMap<GameState, Double> heuristicCosts = new HashMap<>();
    	for(GameState g : stateList) {
    		heuristicCosts.put(g, g.heuristic() + g.getCost());
    	}
    	
    	if(heuristicCosts.isEmpty()) {
    		//System.out.println("No more good moves from this state")
    		return null;
    	}
    	
    	//Sort children by cost + heuristic
    	ArrayList<Map.Entry<GameState, Double>> sortedOptions = sortHashMap(heuristicCosts);
    	
    	//Recurse
    	closedList.add(initialState);
    	
    	//For each of the viable options from this node, try to find a path to the goal starting from the most promising cell and work down.
        for(Map.Entry<GameState, Double> m : sortedOptions) {
        	//Is m the goal? If so, return a path that ends in this cell, as the cell NEXT to the goal is the real final destination for the stack
        	if(m.getKey().isGoal()) {
            	Stack<GameState> path = new Stack<>();
            	path.push(initialState);
            	return path;
            }
            
        	//System.out.println("Expanding: (" + m.getKey().x + "," + m.getKey().y + ",  " + m.getValue() + ")");
            HashSet<GameState> mClosedList = (HashSet<GameState>) closedList.clone();
         
            //Add all currently reachable cells to the closed list so that only paths which strictly could not be reached any quicker are considered.
            for(Map.Entry<GameState, Double> m2 : sortedOptions) {
            	mClosedList.add(m2.getKey());
            }
        	Stack<GameState> pathFromM = AstarInternal(m.getKey(), mClosedList);
        	if(pathFromM == null) {
        		continue; //Every possible path from m resulted in a dead end
        	}
        	else {
        		//This was a good path- add yourself to the path and return. *Means that we'll have to pop the stack once to get rid of the start for the finished path!*
        		pathFromM.push(initialState);
        		return pathFromM;
        	}
        }
    	
    	return null;
    }
    
    private ArrayList<GameState> removeClosedList(ArrayList<GameState> statelist, HashSet<GameState> closedList) {
    	ArrayList<GameState> newStateList = new ArrayList<>();
    	for(GameState closed: closedList) {
    		for(GameState g: statelist) {
    			if(!g.equals(closed)) {
    				newStateList.add(g);
    			}
    		}
    	}
    	return newStateList;
    }
    
    private ArrayList<Map.Entry<GameState, Double>> sortHashMap (HashMap<GameState, Double> heuristicCosts) {
    	ArrayList<Map.Entry<GameState, Double>> sortedOptions = new ArrayList<Map.Entry<GameState, Double>>((Collection) heuristicCosts.entrySet());
        Collections.sort( sortedOptions, new Comparator<Map.Entry<GameState, Double>>()
        {
            @Override
            public int compare( Map.Entry<GameState, Double> opt1, Map.Entry<GameState, Double> opt2 )
            {
                return ((opt1.getValue()).compareTo( opt2.getValue() )); //Remember, smaller values are supposed to be more optimal!
            }
        } );
        return sortedOptions;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}
