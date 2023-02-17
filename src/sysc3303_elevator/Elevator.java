package sysc3303_elevator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Elevator Class
 *
 * Class responsible for receiving messages from the Elevator elevatorSubsystem,
 * process it and then notify the elevatorSubsystem.
 *
 * @author Tao Lufula, 101164153
 */
public class Elevator {
	private int currentFloor;
	private boolean isMoving;
	private boolean isMotorOn;
	private DoorState doorState;
	private Direction direction;
	private ButtonLampState[] buttonLampStates;
	private List<Integer> requestedFloors;

	/**
	 * Constructor for Elevator Class
	 *
	 */
	public Elevator(int numberOfFloors) {

		this.currentFloor = 1; // Main Floor
		this.isMoving = false;
		this.isMotorOn = false;
		this.doorState = DoorState.CLOSED;
		this.direction = null;
		this.requestedFloors = new ArrayList<>();

		this.buttonLampStates = new ButtonLampState[numberOfFloors];
		Arrays.fill(buttonLampStates, ButtonLampState.OFF);
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}

	public boolean isMotorOn() {
		return isMotorOn;
	}

	public void setMotorOn(boolean motorOn) {
		this.isMotorOn = motorOn;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void setMoving(boolean moving) {
		isMoving = moving;
	}

	public DoorState getDoorState() {
		return doorState;
	}

	public void setDoorState(DoorState doorState) {
		this.doorState = doorState;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public ButtonLampState[] getButtonLampStates() {
		return buttonLampStates;
	}

	public void setButtonLampStates(ButtonLampState[] buttonLampStates) {
		this.buttonLampStates = buttonLampStates;
	}


	public Message processFloorEvent(FloorEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}