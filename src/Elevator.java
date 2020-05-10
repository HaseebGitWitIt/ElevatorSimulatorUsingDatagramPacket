import java.util.Random;

/*
 * SYSC 3303 Elevator Group Project
 * Elevator.java
 * @ author Samy Ibrahim 
 * @ student# 101037927
 * @ version 1
 * 
 * The elevator class consists of the buttons and lamps inside of the elevator used to select floors and indicate the
 * floors selected, and to indicate the location of the elevator itself. The elevator subsystem is also used to operate the
 * motor and to open and close the doors. Each elevator has its own elevator subsystem. 
 * 
 * For the purpose of this project, the elevator subsystem listens for packets from the scheduler to control the motor
 * and to open the doors. The elevator subsystem also has to monitor the floor subsystem for destination requests
 * (button presses inside of the elevator car, rather than button presses at the floors) from the input file. Button presses
 * are to be rerouted to the scheduler system. Lamp (floor indications) from button pushes do not have to originate
 * from the scheduler. Rather, when the elevator subsystem detects a button request, it can then light the
 * corresponding lamp. When the elevator reaches a floor, the scheduling subsystem signals the elevator subsystem to
 * turn the lamp of
 * 
 * Last Edited March 7, 2019.
 */
public class Elevator implements Runnable {
	// The elevator car number
	private int elevatorNumber;
	// The current floor the elevator is on
	private int currentFloor = 0;
	
	private UtilityInformation.DoorState door = UtilityInformation.DoorState.CLOSE;
	
	// The lamps indicate the floor(s) which will be visited by the elevator
	private UtilityInformation.LampState[] allButtons;
	
	private Elevator_Subsystem controller;
	
	/*
	 * General Constructor for Elevator Class
	 */	
	// USED ENUMS:
	// State machine states
	public enum Action {
		MOVE_UP,
		MOVE_DOWN,
		OPEN_DOOR,
		STOP,
		CLOSE_DOOR,
		BROKEN,
		DAMAGED,
		WAITING,
		FIXED
	}
	
	private Action currAction;
	
	public Elevator(Elevator_Subsystem controller, int number, int numFloors) {
		elevatorNumber = number;
		this.controller = controller;
		currAction = Action.WAITING;
		
        allButtons = new UtilityInformation.LampState[numFloors];
        for (int k = 0; k < numFloors; k++) {
            allButtons[k] = UtilityInformation.LampState.OFF; // currently making everything OFF
        }
	}
	
	// getters for private variables
	public int getElevatorNumber() {return this.elevatorNumber;}
	public int getCurrentFloor() {return this.currentFloor;}
	public UtilityInformation.DoorState getDoorState() {return this.door;}
	
	/*
	 * Method to print out each elevators data.
	 */
	public void display() {
		// Simply display 
		System.out.println("Elevator Number: " + this.getElevatorNumber());
		System.out.println("Floor Number: " + this.getCurrentFloor() + "\n");
		for(int i=0; i<allButtons.length; i++) {
			System.out.println("Floor Number " + i + ": " +allButtons[i]);
		}
		System.out.println("\n");
	}
	
	/*
	 * Method to make the elevator move up one floor.
	 */
	public void move(UtilityInformation.ElevatorDirection dir) {		
    	System.out.println(String.format("Elevator Moving %s One Floor", dir.toString()));
    	
    	if (dir.equals(UtilityInformation.ElevatorDirection.UP)) {
    	    if ((currentFloor == controller.getNumFloors() - 1)) {
    	        System.out.println("Error: Invalid Movement Instruction");
                System.exit(1);
    	    }
    	    
    		try  { 
                Thread.sleep(UtilityInformation.TIME_UP_ONE_FLOOR);
            } catch (InterruptedException ie)  {

            }              
            
            currentFloor++;
    	} else if (dir.equals(UtilityInformation.ElevatorDirection.DOWN)) {
    	    if (currentFloor == 0) {
    	        System.out.println("Error: Invalid Movement Instruction");
                System.exit(1);
    	    }
    	    
    		try  { 
                Thread.sleep(UtilityInformation.TIME_DOWN_ONE_FLOOR);
            } catch (InterruptedException ie)  {

            }              
            
            currentFloor--;
    	} else if (dir.equals(UtilityInformation.ElevatorDirection.STATIONARY)) {
    	    try  { 
                Thread.sleep(UtilityInformation.TIME_STOP_AT_FLOOR);
            } catch (InterruptedException ie)  {

            }  
    	}
        
        controller.sendFloorSensorMessage(elevatorNumber);
        
        System.out.println("Elevator arrives on floor");
	}
	
	/*
	 * Method to make the elevator stop moving.
	 */
	public void Stop() {
        System.out.println("The elevator has stopped moving");
	}

	/*
	 * Method to open the elevator door.
	 */
	public void changeDoorState(UtilityInformation.DoorState newState) {
		if (newState.equals(UtilityInformation.DoorState.OPEN)) {
			try  { 
	            Thread.sleep(UtilityInformation.OPEN_DOOR_TIME);
	        } catch (InterruptedException ie)  {

	        }
		} else if (newState.equals(UtilityInformation.DoorState.CLOSE)) {
			try  { 
	            Thread.sleep(UtilityInformation.CLOSE_DOOR_TIME);
	        } catch (InterruptedException ie)  {

	        }
		}        
        
        System.out.println(String.format("Elevator Door %s", newState.toString()));
        
		door = newState;
		System.out.println("Door: " + door + " on floor: " + currentFloor);
	}
	
	/**
	 * brokenElevator
	 * 
	 * Set this elevator to the broken state.
	 * 
	 * @param  None
	 * 
	 * @return None
	 */
	public void brokenElevator() {
		System.out.println("Elevator is Broken");
	}
	
	/**
	 * elevatorFixed
	 * 
	 * Set this elevator to the fixed state
	 * 
	 * @param  None
	 * 
	 * @return None
	 */
	public void elevatorFixed() {
		System.out.println("Elevator is Fixed");
	}
	
	/**
	 * fixDoorStuckError
	 * 
	 * Fix this elevator door. Runs a wait loop until the
	 * door is fixed. Current probability of the door being fixed
	 * is 40 percent.
	 * 
	 * @param doorState    The door state that the elevator is broken in
	 * 
	 * @return None
	 */
	public void fixDoorStuckError() {
	    // Run the loop until the door is fixed.
        Random r = new Random();
        boolean broken = true;
        float chance;
        
        // The percent change that the elevator will be fixed
        float percentChanceFixDoor = 0.4f;
        
        int sleepTimeBetweenAttempts = 1000;
        
        while(broken) {
            try {
                Thread.sleep(sleepTimeBetweenAttempts);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("Attempting to fix door...");
            chance = r.nextFloat();
            if(chance <= percentChanceFixDoor){
                broken = false;
            }
        }
    
	    // Set the door to the fixed state
	    if (door == UtilityInformation.DoorState.OPEN) {
	        changeDoorState(UtilityInformation.DoorState.CLOSE);
	    } else if (door == UtilityInformation.DoorState.CLOSE) {
	    	changeDoorState(UtilityInformation.DoorState.OPEN);
	    } else {
	        System.out.println("Error: Unknown error type in Elevator fixDoorStuckError.");
	        System.exit(1);
	    }
	    
	    // Tell the controller that the door is fixed
	    controller.sendElevatorDoorFixedMessage(elevatorNumber);
	    
	    controller.sendFloorSensorMessage(elevatorNumber);
	}

	/**
	 * getCurrAction
	 * 
	 * Returns the current action being executed
	 * 
	 * @param  None
	 * 
	 * @return Action  The current action being executed
	 */
	public Action getCurrAction() {
		return(currAction);
	}

	/**
	 * isInErrorState
	 * 
	 * Returns whether or not the elevator is currently in an error state
	 * 
	 * @param  None
	 * 
	 * @return boolean True if elevator is currently in an error state
	 *                 False otherwise
	 */
	public boolean isInErrorState() {
		return(currAction.equals(Action.BROKEN));
	}
	
	/**
	 * changeAction
	 * 
	 * Changes the currently executing action to the given action type.
	 *
	 * @param newAction    The new action for the elevator to execute
	 * 
	 * @return void
	 */
	public void changeAction(Action newAction) {
		currAction = newAction;
		
		switch (currAction) {
		case MOVE_UP:
			move(UtilityInformation.ElevatorDirection.UP);
			break;
		case MOVE_DOWN:
			move(UtilityInformation.ElevatorDirection.DOWN);
			break;
		case STOP:
		    move(UtilityInformation.ElevatorDirection.STATIONARY);
		    break;
		case OPEN_DOOR:
			changeDoorState(UtilityInformation.DoorState.OPEN);
			break;
		case CLOSE_DOOR:
			changeDoorState(UtilityInformation.DoorState.CLOSE);
			break;
		case BROKEN:
			brokenElevator();
			break;
		case DAMAGED:
			fixDoorStuckError();
			break;
		case WAITING:
			break;
		default:
			System.out.println("Error: Unknown Action.");
			System.exit(1);
		}
		
		currAction = Action.WAITING;
	}

	/**
	 * run
	 * 
	 * override
	 * 
	 * Infinitely repeats the following:
	 *     Get the next action for the elevator from the controller
	 *     Execute the retrieved action
	 *     
	 * @param  None
	 * 
	 * @return void
	 */
	@Override
	public void run() {		
		while (true) {
			Action nextAction = controller.getNextActionForElevator(elevatorNumber);
			changeAction(nextAction);
		}
		
	}

	/**
	 * turnOffDestButton
	 * 
	 * Turns off the destination button corresponding to the given destination
	 * 
	 * @param destinationFloor Number of button to turn off
	 * 
	 * @return void
	 */
    public void turnOffDestButton(int destinationFloor) {
        allButtons[destinationFloor] = UtilityInformation.LampState.OFF; 
    }
    
    /**
     * turnOnDestButton
     * 
     * Turns on the destination button corresponding to the given destination
     * 
     * @param destinationFloor Number of button to turn on
     * 
     * @return void
     */
    public void turnOnDestButton(int destinationFloor) {
        allButtons[destinationFloor] = UtilityInformation.LampState.ON; 
    }
	
}