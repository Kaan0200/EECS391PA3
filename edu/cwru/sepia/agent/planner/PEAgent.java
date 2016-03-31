package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionResult;
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
    		Action next = createSepiaAction(plan.pop(), stateView);
    		System.out.println(next.toString());
    		sepiaActions.put(1, next);
    	}
    	
        return sepiaActions;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(StripsAction action, State.StateView state) {
    	Action returnAction = null;
    	
        if(action instanceof StripsMove){
        	StripsMove move = (StripsMove) action;
        	// moving back to townhall
        	if (move.location == null) {
        		returnAction =
        				Action.createCompoundMove(peasantIdMap.get(move.mover.id),
        						state.getUnit(townhallId).getXPosition(),
        						state.getUnit(townhallId).getYPosition());
        						
        	} else {
        		returnAction = 
        				Action.createCompoundMove(peasantIdMap.get(move.mover.id),
        									  	move.location.pos.x,
        									  	move.location.pos.y);
        	}
        	
        } else if (action instanceof StripsCollect){
        	StripsCollect collect = (StripsCollect) action;
        	returnAction = 
        			Action.createPrimitiveGather(peasantIdMap.get(collect.collector.id),
        					new Position(state.getUnit(peasantIdMap.get(collect.collector.id)).getXPosition(),
        							     state.getUnit(peasantIdMap.get(collect.collector.id)).getYPosition()).getDirection(
        							    		 collect.collection.pos));
        	// needs to figure out direction
        	
        } else if (action instanceof StripsDeposit){
        	StripsDeposit deposit = (StripsDeposit) action;
        	returnAction = 
        			Action.createCompoundDeposit(peasantIdMap.get(deposit.depositer.id), peasantTemplateId);
        	
        } else if (action instanceof StripsBuildPeasant){
        	StripsBuildPeasant build = (StripsBuildPeasant) action;
        	returnAction = 
        			Action.createPrimitiveProduction(townhallId, peasantTemplateId);
        	
        } else {
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
