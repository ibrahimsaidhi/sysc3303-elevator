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
public class Elevator implements Runnable {
	private boolean isMoving;
	private boolean isMotorOn;
	private DoorState doorState;
	private ButtonLampState[] buttonLampStates;
	private ElevatorQueue destionationQueue;
	private ElevatorState state;
	private ElevatorStatus status;
	private List<ElevatorObserver> observers;

	/**
	 * Constructor for Elevator Class
	 *
	 */
	public Elevator(int numberOfFloors) {

		this.isMoving = false;
		this.isMotorOn = false;
		this.doorState = DoorState.CLOSED;
		this.destionationQueue = new ElevatorQueue(1);

		this.buttonLampStates = new ButtonLampState[numberOfFloors];
		Arrays.fill(buttonLampStates, ButtonLampState.OFF);

		this.state = new ElevatorInitState(this);
		this.observers = new ArrayList<>();
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
		Logger.println("Door:  " + doorState.toString());
		this.doorState = doorState;
	}

	public Direction getDirection() {
		return this.destionationQueue.getDirection();
	}

	public ButtonLampState[] getButtonLampStates() {
		return buttonLampStates;
	}

	public void setButtonLampStates(ButtonLampState[] buttonLampStates) {
		this.buttonLampStates = buttonLampStates;
	}

	public ElevatorQueue getDestinationFloors() {
		return this.destionationQueue;
	}

	public void setState(ElevatorState state) {
		Logger.debugln("State: " + state.getClass().getSimpleName());
		this.state = state;
	}

	public ElevatorState getState() {
		return this.state;
	}

	public void addObserver(ElevatorObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(ElevatorObserver observer) {
		observers.remove(observer);
	}

	public void notifyObservers(ElevatorResponse message) {
		for (ElevatorObserver observer : observers) {
			observer.onEventProcessed(message);
		}
	}

	public void setStatus(ElevatorStatus status) {
		Logger.println("Status: " + status);
		this.status = status;
	}

	public ElevatorStatus getStatus() {
		return status;
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
	public void processFloorEvent(FloorEvent event) {
		var queue = this.getDestinationFloors();
		int carButton = event.carButton();
		int floorButton = event.floor();

		if (carButton != 0 && floorButton != carButton) {
			queue.add(floorButton);
			queue.add(carButton);
			this.getButtonLampStates()[carButton] = ButtonLampState.ON;

			if (queue.getCurrentFloor() != queue.peek().get()) {
				this.setState(new MovingState(this));
			} else {
				queue.next();
				Logger.debugln("Opening doors");
				this.setDoorState(DoorState.OPEN);
				this.setState(new DoorOpenState(this));
			}

		} else {
			Logger.println("Invalid floor event");
		}
	}

	public void run() {
		while (true) {
			try {
				state.advance(this);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
