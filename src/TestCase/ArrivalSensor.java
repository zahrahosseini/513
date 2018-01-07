
package TestCase;

/**
 * This is the ArrivalSensor Class. It consists of a method that communicates * with the Elevator class to determine if an elevator needs to stop.
 */

public class ArrivalSensor {

	/*
	 * The elevator reaching a new floor
	 */
	private Elevator elevator;

	/*
	 * The floor to which the sensor is associated
	 */
	private Floor floor;

	/*
	 * Constructor that initializes the reference to Floor entity
	 */
    public ArrivalSensor(Floor floor){
    	this.floor = floor;
    }

  /*
   * When this method is called, the sensor signals an approaching
   * elevator to stop, if a stop has been requested on that floor.
   *
   */
   public boolean stopAtThisFloor(int elevatorID) {
	   boolean stopped = false;
	   elevator = Elevator.selectElevator(elevatorID);
    
	   stopped = elevator.notifyNewFloor(floor);
	   return stopped;
   }

  /*
   * @return the reference to the floor associated with the sensor
   */
   public Floor getTheFloor(){
	   return floor;
   }

}
