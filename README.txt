aka43
jrf118
Introduction to AI - EECS 391 PA3

This is the project submission for Alan Kaan Atesoglu (ak43) and Joe Fennimore (jrf118), for programming assignment 3 in Introduction to Artificial Intelligence.

Files included:
  GameState.java
  PEAgent.java
  PlannerAgent.java
  ResourceType.java
  actions >
    StripsAction.java
	StripsBuildPeasant.java
	StripsCollect.java
	StripsCollect_2.java
	StripsCollect_3.java
	StripsDeposit.java
	StripsDeposit_2.java
	StripsDeposit_3.java
	StripsMove.java
	StripsMove_2.java
	StripsMove_3.java
	
Notes:
 - When planning out the moves for the agent, we decided to abstract away any positions for anything.  This allowed us not
   to worry about any logic dealing with positioning and making sure that peasants are indeed directly next to the townhall
   or resources.  This means that peasants have booleans associating their positions.  They have one indicating that they
   are next to the townhall, and 2 to indicate if they are next to gold or wood.  This simplifies the logic while keeping in
   line with STRIPS planning.
   
 - There is also no precondition checking inside the PEAgent.  In the comment above MiddleStep, it indicates we should be
   checking the preconditions in this method.  However, in order for this to happen, the plan would need to be able to check
   against the GameState.  Which it does not have access to at this location.  The preconditions are checked when adding to
   the plan and not when converting the plan to sepia.  We felt this went against the design of this algorithm as the plans
   should be assumed correct when executing them, hence there should be no need to validate them again.

 - When creating multiple peasants and telling them to simultaniously execute actions, there is a constant problem with
   their movements causing them to be unable to get next to the resources.  We fixed this by checking to see if the peasants
   managed to succesfully completely their move to a resource, if they don't then we re-execute this action until it has finished
   successfully.  We also wanted to make sure that multiple peasants don't have any issues when attempting to deposit or gather
   so when there are multiple peasants they execute compound gathers and deposits AFTER their compound moves to make sure they
   are not having issues with intersecting other peasants.
   
 - 