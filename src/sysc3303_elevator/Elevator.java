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
	private List<Integer> destinationFloors;

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
		this.destinationFloors = new ArrayList<>();

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

	/**
	 * method to process events from elevator subsystem and return a complete
	 * message
	 * 
	 * @param event
	 * @return Message
	 * 
	 * @author Tao Lufula, 101164153
	 */
	public Message processFloorEvent(FloorEvent event) {

		int carButton = event.carButton();
		int floorButton = event.floor();

		this.destinationFloors.add(floorButton);
		this.destinationFloors.add(carButton);

		for (int destinationFloor : destinationFloors) {

			if (this.getCurrentFloor() != destinationFloor) {

				// floor request or Car Button request
				if (destinationFloor == floorButton) {
					System.out.println("Elevator is on floor " + this.getCurrentFloor()
							+ " and has been requested on floor " + floorButton);
				} else {
					this.getButtonLampStates()[carButton] = ButtonLampState.ON;
					System.out.println("Elevator is on floor " + this.getCurrentFloor() + ". Car button " + carButton
							+ " lamp is " + this.getButtonLampStates()[carButton]);
				}

				this.setDoorState(DoorState.CLOSED);
				this.setMotorOn(true);
				this.setMoving(true);

				System.out
						.println("Elevator doors are " + this.getDoorState() + ", motor is ON. Elevator is moving... ");

				if (this.getCurrentFloor() < destinationFloor) {

					this.setDirection(Direction.Up);

				} else {
					this.setDirection(Direction.Down);

				}
				// print the direction in which the elevator is moving
				System.out.println("Elevator going " + this.getDirection() + " to floor: " + destinationFloor);

				if (destinationFloor == carButton) {
					this.getButtonLampStates()[carButton] = ButtonLampState.OFF;
					System.out.println(
							"car button " + carButton + " lamp is " + this.getButtonLampStates()[destinationFloor]);
				}
				this.setCurrentFloor(destinationFloor);
				this.setMotorOn(false);
				this.setMoving(false);

				System.out.println("Elevator reached floor: " + destinationFloor + ".  Opening doors");
				this.setDoorState(DoorState.OPEN);

				System.out.println("Closing doors");
				this.setDoorState(DoorState.CLOSED);

			} else {

				System.out.println("Elevator is on floor " + this.getCurrentFloor() + ". Opening doors");
				this.setDoorState(DoorState.OPEN);

				System.out.println("Closing doors");
				this.setDoorState(DoorState.CLOSED);
			}
		}
		Message message = new Message("Processing FloorEvent : Done");
		return message;
	}

}
