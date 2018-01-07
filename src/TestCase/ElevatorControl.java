package TestCase;

/*
 * This class receives requests for service and dispatches them
 * to the elevator associated with the control object
 */
public class ElevatorControl {
	
	private Elevator myElevator;
	private ElevatorInterface ei;
	
	/*
	 * Constructor
	 */
	public ElevatorControl(Elevator elevator) {
		myElevator = elevator;
		ei = ElevatorInterface.getFromList(this);
    }
	
	/*
	 * Constructor
	 */
    public ElevatorControl(int EID) {
        myElevator = Elevator.selectElevator(EID);
        ei = ElevatorInterface.getFromList(this);
    }
	
	/*
	 * Requests to stop the elevator upon arrival at a floor for which a stop is requested
	 */
    public void stopElevator() {
        myElevator.stopElevator();
    }
	
	/*
	 * Simulates opening elevator's door
	 */
    public void openDoor() {
        myElevator.openDoor();
    }
	
	/*
	 * Simulates closing elevator's door
	 */
    public void closeDoor() {
        myElevator.closeDoor();
    }

    /*
	 * Requests a stop at a specified floor
	 */
    public void requestStop(int floor) {
    	
        myElevator.addStop(floor,true);
    }

    /*
	 * @return the associated elevator
	 */
	public Elevator getElevator() {
		return myElevator;
	}
	
	/*
	 * Simulates motor moving down
	 */
	public void motorMoveDown() {
		ei.motorMoveDown();
	}
	
	/*
	 * Simulates motor moving up
	 */
	public void motorMoveUp() {
		ei.motorMoveUp();
	}
	
	/*
	 * Simulates stopping motor
	 */
	public void motorStop() {
		ei.motorStop();
	}
}
