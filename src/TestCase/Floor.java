package TestCase;

/**
 * This is the Floor Class. It consists of a list of the floors in the system
 * and each Floor object has its unique ID and an arrival sensor. Methods from 
 * this class interact with the Elevator class in order to service requests from 
 * users at a floor.
 */

public class Floor {

	private ArrivalSensor arrivalsensor;
	private Elevator elevator;

	/*
	 * List of all the floors in the system
	 */
	private static Floor[] allFloors = null;

    /*
     * Unique floor number, sequential integers 0 to (numFloors - 1)
     */
    private int floorID;
   /*
    * button set to true when a request for elevator going up has been
    * made
    */
    private boolean upButtonPressed = false;

   /*
    * button set to true when a request for elevator going down has been
    * made
    */
    private boolean downButtonPressed = false;

   /*
    * Constructor that initializes reference to class ArrivalSensor
    * and adds this floor object to the list of floors
    */
    public Floor(int floorID){
    	if (allFloors == null) {
    		allFloors = new Floor[ElevatorGroup.numFloors];
    	}
	    arrivalsensor = new ArrivalSensor(this);
	    this.floorID = floorID;
	    Floor.allFloors[floorID] = this;
    }

   /*
    * @ return the reference to the desired
    * floor object from a list of floors in the system
    */
    public static Floor selectFloor(int floorID) {
    	if ((floorID < ElevatorGroup.numFloors) && (floorID > -1)) {
    		return allFloors[floorID];  
    	} else 
    		return null; 
    }

   /*
    * @return number of floors in the system
    */
    public static int getNoFloors() {
    	return ElevatorGroup.numFloors;
    }

  /*
   * Makes a request for an elevator to stop at this floor, heading up.
   * This method is called by FloorControl, to get an elevator to service
   * the request and add the stop to the elevator's list of stops
   * 
   * @return the best elevator that can provide the service
   */
    public Elevator requestUp() {
	    upButtonPressed = true;
	    elevator = Elevator.getBestElevator(floorID);
	    if (floorID == ElevatorGroup.numFloors-1) {
	    	System.out.println("No up requests are permitted at this floor.");
	    } else if (elevator.getFloor().getFloorID() != this.getFloorID()){
	    	System.out.println("Request for elevator going UP made at floor " + floorID);
	    	System.out.println("Best Elevator is: "+ (elevator.getElevatorID()+1));
	    	elevator.addStop(floorID,true);
	    	System.out.println("Stop added at " +floorID);
	    } else {
	    	elevator.addStop(floorID,true);
	    }
	    return elevator;
    }

  /*
   * Makes a request for an elevator to stop at this floor, heading down.
   * This method is called by FloorControl, to get an elevator to service 
   * the request and add the stop to the elevator's list of stops
   * 
   * @return the best elevator that can provide the service
   */
    public Elevator requestDown() {
	    downButtonPressed = true;
	    elevator = Elevator.getBestElevator(floorID);
	    if (floorID == 0) {
	    	System.out.println("No down requests are permitted at this floor.");
	    } else if (elevator.getFloor().getFloorID() != this.getFloorID()){
	    	System.out.println("Request for elevator going DOWN made at floor " + floorID);
	    	System.out.println("Best Elevator is: "+ (elevator.getElevatorID()+1));
	    	elevator.addStop(floorID,true);
	    	System.out.println("Stop added at " + floorID);
	    } else {
	    	elevator.addStop(floorID,true);
	    }               
	    return elevator;
	}

   /*
    * @return true when a request for elevator going up has already been
    * made
    */
    public boolean requestUpMade() {
	    if(upButtonPressed == true) {
	       return true;
        } else { 
        	return false; 
        }
    }

   /*
    * @return true when a request for elevator going down has already been
    * made
    */
    public boolean requestDownMade() {
	    if (downButtonPressed == true) {
	       return true;
        } else { 
        	return false; 
        }
    }

   /*
    * Method called when a request for elevator going up has been serviced.
    * Resets the button for that floor.
    */
    public void requestUpServiced() {
	    System.out.println("Elevator going UP has arrived at floor " + floorID+ ".");
	    upButtonPressed = false;
    }

   /*
    * Method called when a request for elevator going down has been serviced.
    * Resets the button for that floor.
    */
    public void requestDownServiced() {
	    System.out.println("Elevator going DOWN has arrived at floor " + floorID+ ".");
	    downButtonPressed = false;
    }

	/*
	 * @return the ID of the floor object
	 */
	public int getFloorID() {
		return floorID;
	}

   /*
    * @return reference to the arrival sensor for the floor object
    */
    public ArrivalSensor getSensor(){
    	return arrivalsensor;
    }
    
    public static void removeFloors() {
    	allFloors = null;
    }
}
