package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;

    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
    	// if a new peasant is added we don't have it in the peasant Map
    	// remove one from unit because of townhall
    	if (peasantIdMap.size() < stateView.getAllUnits().size()-1) {
    		for(UnitView u : stateView.getAllUnits()){
    			if (u.getID() == 0) {
    				// we don't care about the townhall
    			} else {
    				if (!peasantIdMap.values().contains(u.getID())){
    					peasantIdMap.put(peasantIdMap.size()+1, u.getID());
    				}
    			}
    		}
    	}
    	
    	Map<Integer, Action> sepiaActions = new HashMap<Integer, Action>();
    	boolean stillExecuting = false;
    	
    	// loop through all peasants to find if any are still executing
    	for(UnitView u : stateView.getAllUnits()){
			// verify they are peasants
			if (u.getTemplateView().getName().equals("Peasant")) {
				// check if it is executing still
				Map<Integer, ActionResult> results = historyView
						.getCommandFeedback(0, stateView.getTurnNumber() - 1);
				for (ActionResult result : results.values()) {
					if (result.getFeedback().toString().equals("INCOMPLETE")) {
						stillExecuting = true;
					}
				}
			}
		}
    	if (plan.isEmpty()){
    		System.out.println("===========VICTORY============\n===Plan finished executing!===");
    		System.exit(0);
    		
    	}
    	// do next action if we are done with everything
    	if (stillExecuting == false){
    		List<Action> next = createSepiaAction(plan.pop(), stateView);
    		
    		for (int i = 0; i < next.size(); i++) {
    			System.out.println(next.get(i).toString());
    			// if this is a produce then apply the move to the townhall with ID = 0
    			sepiaActions.put(next.get(i).getType() == ActionType.PRIMITIVEPRODUCE ? i : peasantIdMap.get(i+1) , next.get(i));
    		}
    	}
    	
        return sepiaActions;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private List<Action> createSepiaAction(StripsAction action, State.StateView state) {
    	List<Action> returnAction = new ArrayList<Action>();
    	
        if(action instanceof StripsMove){
        	StripsMove move = (StripsMove) action;
        	// moving back to townhall
        	if (move.location == null) {
        		returnAction.add(
        				Action.createCompoundMove(peasantIdMap.get(move.mover.id),
        						state.getUnit(townhallId).getXPosition(),
        						state.getUnit(townhallId).getYPosition()));
        						
        	} else {
        		returnAction.add(
        				Action.createCompoundMove(peasantIdMap.get(move.mover.id),
        									  	move.location.pos.x,
        									  	move.location.pos.y));
        	}
        	
        } else if (action instanceof StripsCollect){
        	StripsCollect collect = (StripsCollect) action;
        	returnAction.add(
        			Action.createPrimitiveGather(peasantIdMap.get(collect.getCollector().id),
        					new Position(state.getUnit(peasantIdMap.get(collect.getCollector().id)).getXPosition(),
        							     state.getUnit(peasantIdMap.get(collect.getCollector().id)).getYPosition()).getDirection(
        							    		 collect.getCollection().pos)));
        	// needs to figure out direction
        	
        } else if (action instanceof StripsDeposit){
        	StripsDeposit deposit = (StripsDeposit) action;
        	returnAction.add(
        			Action.createPrimitiveDeposit(peasantIdMap.get(deposit.getDepositer().id),
        					new Position(state.getUnit(peasantIdMap.get(deposit.getDepositer().id)).getXPosition(),
   							             state.getUnit(peasantIdMap.get(deposit.getDepositer().id)).getYPosition()).getDirection(
   							            		 		new Position(state.getUnit(townhallId).getXPosition(),
   							            				state.getUnit(townhallId).getYPosition()))));
        	
        } else if (action instanceof StripsBuildPeasant){
        	//StripsBuildPeasant build = (StripsBuildPeasant) action;
        	returnAction.add(Action.createPrimitiveProduction(townhallId, peasantTemplateId));
        	
        } else if (action instanceof StripsMove_2) {
        	StripsMove_2 move = (StripsMove_2) action;
        	if (move.getLocation() == null){
        		returnAction.add (
        				Action.createCompoundMove(peasantIdMap.get(move.getMover().id),
        						state.getUnit(townhallId).getXPosition(),
        						state.getUnit(townhallId).getYPosition()));
        		returnAction.add (Action.createCompoundMove(peasantIdMap.get(move.getMover2().id),
						state.getUnit(townhallId).getXPosition(),
						state.getUnit(townhallId).getYPosition()));
        	} else {
        		returnAction.add(Action.createCompoundMove(peasantIdMap.get(move.getMover().id), move.getLocation().pos.x, move.getLocation().pos.y));
        		returnAction.add(Action.createCompoundMove(peasantIdMap.get(move.getMover2().id),move.getLocation().pos.x, move.getLocation().pos.y));
        	}
        } else if (action instanceof StripsCollect_2) {
        	StripsCollect_2 collect = (StripsCollect_2) action;
        	returnAction.add(Action.createCompoundGather(peasantIdMap.get(collect.getCollector().id), collect.getCollection().id));
        	// Using to make sure that both peasants are BOTH next to resource before gathering
        	// removes any issues from compound move when both attempt to move to same location
        	returnAction.add(Action.createCompoundGather(peasantIdMap.get(collect.getCollector2().id), collect.getCollection().id));
        	
        } else if (action instanceof StripsDeposit_2) {
        	StripsDeposit_2 deposit = (StripsDeposit_2) action;
        	returnAction.add(Action.createCompoundDeposit(peasantIdMap.get(deposit.getDepositer().id),
        			state.getUnit(townhallId).getID()));

        	returnAction.add(Action.createCompoundDeposit(peasantIdMap.get(deposit.getDepositer2().id),
        			state.getUnit(townhallId).getID()));
        	
        } else if (action instanceof StripsMove_3) {
        	StripsMove_3 move = (StripsMove_3) action;
        	if (move.getLocation() == null){
        		returnAction.add (Action.createCompoundMove(peasantIdMap.get(move.getMover().id),
        						state.getUnit(townhallId).getXPosition(),
        						state.getUnit(townhallId).getYPosition()));
        		returnAction.add (Action.createCompoundMove(peasantIdMap.get(move.getMover2().id),
						state.getUnit(townhallId).getXPosition(),
						state.getUnit(townhallId).getYPosition()));
        		returnAction.add (Action.createCompoundMove(peasantIdMap.get(move.getMover3().id),
						state.getUnit(townhallId).getXPosition(),
						state.getUnit(townhallId).getYPosition()));
        	} else {
        		returnAction.add(Action.createCompoundMove(peasantIdMap.get(move.getMover().id),
        									  	move.getLocation().pos.x,
        									  	move.getLocation().pos.y));
        		returnAction.add(Action.createCompoundMove(peasantIdMap.get(move.getMover2().id),
        									  	move.getLocation().pos.x,
        									  	move.getLocation().pos.y));
        		returnAction.add(Action.createCompoundMove(peasantIdMap.get(move.getMover3().id),
        									  	move.getLocation().pos.x,
        									  	move.getLocation().pos.y));
        	}
        }else if (action instanceof StripsCollect_3) {
        	
        }else if (action instanceof StripsDeposit_3) {
        	
        }
        else {
        	System.err.println("Unhandled attempt to convert STRIPS action to SEPIA action");
        }
        return returnAction;

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
}
