package sysc3303_elevator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;

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
	private boolean stuckBtwFloors;
	private boolean doorStuck;
	private final int TIME_BTW_FLOORS = 1000; // milliseconds
	private final int DOOR_CLOSING_TIME = 1000; // milliseconds
	private final int TIME_BTW_FLOORS_THRESHOLD = 1200; // maximum time for moving between floors or closing door in
	private final int DOOR_CLOSING_TIME_THRESHOLD = 1200;
	private Optional<Thread> timer = Optional.empty();	

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
		this.stuckBtwFloors = false;
		this.doorStuck = false;
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

	public synchronized boolean isstuckBtwFloors() {
		return stuckBtwFloors;
	}

	public synchronized void setstuckBtwFloors(boolean stuckBtwFloors) {
		this.stuckBtwFloors = stuckBtwFloors;
	}

	public synchronized boolean isdoorStuck() {
		return doorStuck;
	}

	public synchronized void setdoorStuck(boolean doorStuck) {
		this.doorStuck = doorStuck;
	}

	/**
	 * @return the tIME_BTW_FLOORS
	 */
	public int getTIME_BTW_FLOORS() {
		return TIME_BTW_FLOORS;
	}

	/**
	 * @return the DOOR_CLOSING_TIME
	 */
	public int getDOOR_CLOSING_TIME() {
		return DOOR_CLOSING_TIME;
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
		if (!isstuckBtwFloors()) {
			var queue = this.getDestinationFloors();
			int destFloor = event.destFloor();
			int srcFloor = event.srcFloor();

			if (destFloor != 0 && srcFloor != destFloor) {
				queue.add(srcFloor);
				queue.add(destFloor);
				this.getButtonLampStates()[destFloor] = ButtonLampState.ON;

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
		} else {
			Logger.debugln("Cannot process floor Events, Elevator is shutDown");
		}
	}

	public Boolean checkAndDealWithFaults() {
		if (isdoorStuck() || isstuckBtwFloors()) {
			setState(new StuckState(this));
			return true;
		}
		return false;
	}

	public void startTimer(ElevatorStatus status) throws InterruptedException {
		stopTimer();
		timer = Optional.of(new Thread(() -> {
			try {
				int previousFloor = destionationQueue.getCurrentFloor();
	
				if (status.equals(ElevatorStatus.DoorOpen)) {
					Thread.sleep(DOOR_CLOSING_TIME_THRESHOLD);
					if (state.getClass().equals(DoorClosedState.class)) {
						return;
					}
					setdoorStuck(true);
	
				} else if (status.equals(ElevatorStatus.Moving)) {
					Thread.sleep(TIME_BTW_FLOORS_THRESHOLD);
					if (previousFloor == destionationQueue.getCurrentFloor()) {
						setstuckBtwFloors(true);
					}
				} else {
	
					return; // add more actions
				}
			}catch (InterruptedException e) {
				return;
			}

		}));
		timer.get().start();
	}
	
	public void stopTimer() throws InterruptedException {
		if (timer.isPresent()){
			timer.get().interrupt();
			timer.get().join();
		}
		timer = Optional.empty();
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
