package TestCase;

/* * This class represents one elevator in the system. An elevator is a thread 
 * that receives requests from its button panel and from floors.
 */
import java.util.Vector;
import java.util.Enumeration;

public class Elevator implements Runnable {

    private static Vector allElevators = new Vector();
    private int elevatorID;
    private boolean running;
    
    // direction in which the elevator is heading (1 for Up and -1 for Down; 0 when elevator is idle)
    public int direction;

    // An elevator can be in one of 4 states (IDLE, PREPARE, MOVING and FINDNEXT)
    private int state;

	// The floor on which the elevator is currently
	private Floor currentFloor;

	// The stops that an elevator has to serve
    private boolean[] stops = new boolean[Floor.getNoFloors()];
    private int nStops;
	private boolean motorMoving;
	private boolean doorOpen;

    public final static int IDLE=0;
	public final static int PREPARE=1;
	public final static int MOVING=2;
	public final static int FINDNEXT=3;


	//  Constructor
    public Elevator() {
		int newIndex = allElevators.size();
	    this.elevatorID = newIndex;
		//System.out.println("\nNew Elevator, ID #" + elevatorID);
		allElevators.add(this);
	    this.direction = 0; /* 0 when idle, 1 for up direction and -1 for down direction */
	    currentFloor = Floor.selectFloor(0); //New Elevators start at the bottom.
	    state=Elevator.IDLE;
	    nStops = 0;
	    doorOpen = true;
	    motorMoving = false;
	    running = true;
	    
	    //Define the stops, based on the Floors.
	    for(int i = 0; i < Floor.getNoFloors(); i++) {
			Floor floor = Floor.selectFloor(i);
			stops[floor.getFloorID()] = false;
       }
	}

    /*
     * @return an elevator corresponding to a unique ID
     */
    public static Elevator selectElevator(int elevatorID) {
        return (Elevator) allElevators.elementAt(elevatorID);
    }

    
    /*
     * Called by a Floor wanting an elevator to service a request, 
     * this method returns the best elevator for the job - an idle 
     * Elevator is preferred, then a moving elevator headed towards 
     * this floor and a moving elevator headed away if nothing else 
     * is available. 
     */
    public static Elevator getBestElevator(int floorID) {

        //A simple bubble sort
       Elevator bestElevator = null;
       for (Enumeration e = allElevators.elements(); e.hasMoreElements();) {  //Loop through all elevators
    	   Elevator testElevator= (Elevator) e.nextElement();

    	   if (bestElevator == null) 
    		   bestElevator = testElevator;
    	   else if (bestElevator.getState() != Elevator.IDLE && testElevator.getState() == Elevator.IDLE) 
    		   		bestElevator=testElevator; //Idle is better than anything else
			 	else if (bestElevator.getState() == Elevator.IDLE && testElevator.getState() == Elevator.IDLE) {//both Idle;
			 		int bestDistance= java.lang.Math.abs(bestElevator.getFloor().getFloorID() - floorID);
			 		int testDistance= java.lang.Math.abs(testElevator.getFloor().getFloorID() - floorID);
			 		if (testDistance<bestDistance)  
			 			bestElevator=testElevator;  //If test is closer, test is now best.
			 	} else if (bestElevator.getState()!=Elevator.IDLE && testElevator.getState()!=Elevator.IDLE) { //if both are active
			 		if ((testElevator.getFloor().getFloorID() - floorID) * testElevator.direction <=0) { //if test is heading in the right direction or is here
			 			if ((bestElevator.getFloor().getFloorID() - floorID) * bestElevator.direction > 0) {//Best is heading away
			 				bestElevator=testElevator;
			 			} else {// Both heading towards this floor
			 				int bestDistance= java.lang.Math.abs(bestElevator.getFloor().getFloorID() - floorID);
			 				int testDistance= java.lang.Math.abs(testElevator.getFloor().getFloorID() - floorID);
			 				if (testDistance<bestDistance)  
			 					bestElevator=testElevator;  //If test is closer, test is now best.
			 			}
			 		} // End if test is heading this way
			 	}// end if both are active
       }  //End enumeration loop.

       return bestElevator;
    }

   /*
    * Called by ArrivalSensor to tell the elevator that it's reached a new floor.
    * 
    * @return true if the elevator has to stop, and false otherwise
    */
   public boolean notifyNewFloor(Floor newFloor) {
		currentFloor = newFloor;
		ElevatorGroup.elevatorDisplay(elevatorID+1 ,"Reached floor "+(newFloor.getFloorID())); //removed +1 from floor

		if (stops[newFloor.getFloorID()] == true) {
            stopElevator();
		    return true;
        } else if((newFloor.requestUpMade()) && (direction == 1)) {
        	stopElevator();
        	return true;
        } else if((newFloor.requestDownMade()) && (direction == -1)) {
        	stopElevator();
        	return true;
        } else {
        	if (direction==1) {
        		motorMoveUp();
        	} else {
        		motorMoveDown();
        	}
        	return false;
        }//end else
   }

   /*
    * Simulates elevator's motor movement down
    */
   private void motorMoveDown() {
		ElevatorControl ec = new ElevatorControl(this);
		motorMoving = true;
		ec.motorMoveDown();
		ec = null;
    }

   /*
    * Simulates elevator's motor movement up
    */
   private void motorMoveUp() {
		ElevatorControl ec = new ElevatorControl(this);
		motorMoving = true;
		ec.motorMoveUp();
		ec = null;
	}

	/*
     * If there is a floor with a request in the current direction, move one floor in the current direction.
     * Else reverse direction and check again. 
     */
    public void moveElevator() {

		/* Three-phase movement cycle.  
		 * Prepare/move/check.  
		 * Does not loop here, since it returns control to run()
         * which calls this again if it hasn't set itself to Idle.
         */

        //  PREPARE:
        if (this.state == PREPARE) {
		   ElevatorGroup.elevatorDisplay(elevatorID + 1 ,"Door closed.");
           closeDoor();
           this.state = MOVING;
           if (direction==1) {
        	   motorMoveUp();
           } else {
        	   motorMoveDown();
           }
        }
        
        // MOVING:
        while (this.state==MOVING) {
            //Do nothing - we're waiting to be interrupted by ArrivalSensor telling us we've reached a new floor.
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
        }
        getNextDestination();
    }   
    /*
     * @return floor id to service or -1 if no more requests
     */
    public int getNextDestination() {
		while (this.state==FINDNEXT) {
            if (nStops == 0) {
            	this.state = Elevator.IDLE;
        		direction = 0;
				ElevatorGroup.elevatorDisplay(elevatorID + 1 ,"All stops handled.  Idling.");
				return -1;
            } else {
				int stopToCheck = currentFloor.getFloorID() + direction;
	            while (Floor.selectFloor(stopToCheck)!=null && state==FINDNEXT) {
	            	if (stops[stopToCheck]==true) {
						this.state=PREPARE;
						ElevatorGroup.elevatorDisplay(elevatorID + 1,"Next Stop = floor "+(stopToCheck));
	 					return stopToCheck;
					} else {
						stopToCheck += direction;
					}
	            }				
	            if (Floor.selectFloor(stopToCheck)==null && state == FINDNEXT) { //If we ran out of floors before finding a stop
	            	direction = -direction; //reverse direction
	            } 
            }
        } return -1;       		
    }
    /*
     * When an elevator reaches a stop, it stops its motor, open the door and removes the served stop from the list of stops
     */
    public void stopElevator() {
    	state=FINDNEXT;
    	addStop(currentFloor.getFloorID(),false); //to remove stop from list of stops
    	motorStop();
		ElevatorGroup.elevatorDisplay(elevatorID + 1 ,"Stopped.  ");
		ElevatorGroup.elevatorDisplay(elevatorID + 1 ,"Door open.");
        openDoor();	
    }
    /*
     * Simulates motor stopping
     */
    private void motorStop() {
		ElevatorControl ec = new ElevatorControl(this);
		motorMoving = false;
		ec.motorStop();
		ec = null;
	}

    /*
     * Simulates elevator's door opening
     */
	public void openDoor() {
        ElevatorGroup.elevatorDisplay(elevatorID+1, "Door is open on floor " + getFloor().getFloorID() + ".");
        try {
        	doorOpen = true;
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
    }

	/*
	 * Simulates elevator's door closing
	 */
    public void closeDoor() {
        ElevatorGroup.elevatorDisplay(elevatorID+1, "Door is closed on floor " + getFloor().getFloorID() + ".");
        try {
        	doorOpen = false;
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
    }

    /*
     * A stop is added to the list of elevator stops whenever a stop is requested
     * from the elevator or a request from floor has been directed to the elevator
     * after selecting it as the best elevator to do the job
     * 
     * @param floorID: the floor where to stop or to remove a stop after reaching it
     *        stopState: true if a stop is added, false if a stop at a floor is to be 
     *                   removed after reaching the floor
     */
    public void addStop(int floorID, boolean stopState) {
		if (stopState) {
			if ((floorID != currentFloor.getFloorID()) || (this.getState() == Elevator.MOVING)) {
				nStops++;
				stops[floorID] = stopState;
	            if (this.state == Elevator.IDLE) {
			       if (this.currentFloor.getFloorID()<floorID) { //If the requested floor is above the current one
			    	   direction = 1;
			    	   this.state = PREPARE;   //Start the motion cycle
	                } else {
	                	direction = -1;
					    this.state=PREPARE; 
	                }
				}
			} else {
				ElevatorGroup.elevatorDisplay(elevatorID+1, "Elevator is at requested floor.");
			}
            
        } else {
             stops[floorID] = stopState;
             nStops--;
		}
    }

    public void run() {
    	//ElevatorGroup.elevatorDisplay(elevatorID + 1, "running.");
    	while (running) {
	        try {
	        	if (this.state==Elevator.IDLE) {
	        		Thread.sleep(100);  
	        		//If you're idle, sleep for a short time.
				} else {

					moveElevator();
				}
			} catch (Exception e) { //If you're interrupted, wake up and do nothing.
	        }
	    }
    }

    /*
     * @return the direction in which the elevator is heading
     */
	public int getDirection() {
		return direction;
	}

	/*
     * @return the state of the elevator
     */
	public int getState() {
		return state;
	}

	/*
     * @return the current floor on which the elevator is
     */
	public Floor getFloor(){
		return currentFloor;
	}

	/*
     * @return the elevator's unique ID
     */
	public int getElevatorID() {
		return elevatorID;
	}

	/*
     * @return true if elevator's door is open and false otherwise
     */
	public boolean getDoorOpen() {
		return doorOpen;
	}

	/*
     * @return true if elevator's motor is moving and false otherwise
     */
	public boolean getMotorMoving() {
		return motorMoving;
	}
	
	/*
	 * @param i floor number
	 * @return true if there is a stop requested for floor i
	 */
	public boolean getStop (int i) {
		return stops[i];
	}
	
	public int getNumberOfStops() {
		return nStops;
	}
	
	public static void removeAllElevators() {
		allElevators.removeAllElements();
	}
	
	public void turnOff() {
		running = false;
	}
}
