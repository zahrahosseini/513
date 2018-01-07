package TestCase;

/*
 * This class is the boundary class that allows the interaction with 
 * the different elevators and their button panels
 */
import java.util.Vector;
import java.util.Enumeration;

public class ElevatorInterface {
	
	//	List of all ElevatorButtonInterface objects
    private static Vector list = new Vector(); 

    //  ID of the elevator this is matched to.
    private int elevatorID; 

    /*
     * Constructor
     */
	public ElevatorInterface(Elevator e){
		list.add(this);
	    elevatorID = e.getElevatorID();
    }

	/*
	 * @return an elevator interface from a list of all interfaces
	 * 
	 * @param the elevator control object associated with the interface
	 */
	public static ElevatorInterface getFromList(ElevatorControl ec){
		int EIDNeeded = ec.getElevator().getElevatorID();
        for (Enumeration e = list.elements(); e.hasMoreElements();) {  //Loop through all elevators
	          ElevatorInterface testIF = (ElevatorInterface) e.nextElement();
	          if (testIF.elevatorID == EIDNeeded) return testIF;
		}
        return null;
    }

    /*
     * This method is called whenever a stop is requested from the 
     * elevator associated with the interface.
     * 
     * @param the floor to stop at
     */
	public void requestStop(int floor) {
		if ((floor >= 0) && (floor < ElevatorGroup.numFloors)) {
	        ElevatorControl ec = new ElevatorControl(elevatorID);
	        ec.requestStop(floor);
			ElevatorGroup.elevatorDisplay(elevatorID + 1, "Stop requested at floor " + (floor));
		} else System.out.println("No such floor: "+ floor + " .");
    }
	/*
	 * @return the elevator unique ID
	 */
	public int getElevatorID() {
		return elevatorID;
	}
	/*
	 * To simulate elevator's motor movement down
	 */
	public void motorMoveDown() {
		ElevatorGroup group = ElevatorGroup.getGroup(ElevatorGroup.numElevators, ElevatorGroup.numFloors);
		group.motorMoving(elevatorID,-1,Elevator.selectElevator(elevatorID).getFloor().getFloorID());
	}

	/*
	 * To simulate elevator's motor movement up
	 */
	public void motorMoveUp() {
		ElevatorGroup group = ElevatorGroup.getGroup(ElevatorGroup.numElevators, ElevatorGroup.numFloors);
		group.motorMoving(elevatorID,1,Elevator.selectElevator(elevatorID).getFloor().getFloorID());
	}

	/*
	 * To simulate elevator's motor stop when arriving to a specific floor
	 */
	public void motorStop() {
		ElevatorGroup.elevatorDisplay(elevatorID + 1, "Motor stopped.");
	}
}
