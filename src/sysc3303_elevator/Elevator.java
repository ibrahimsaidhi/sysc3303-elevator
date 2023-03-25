package sysc3303_elevator;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	private int currentFloor;
	private boolean isMoving;
	private boolean isMotorOn;
	private DoorState doorState;
	private ButtonLampState[] buttonLampStates;
	private List<Integer> destinationFloors;
	private ElevatorState state;
	private List<ElevatorObserver> observers;
	private boolean hasFailed;

	/**
	 * Constructor for Elevator Class
	 *
	 */
	public Elevator(int numberOfFloors) {

		this.currentFloor = 1; // Main Floor
		this.isMoving = false;
		this.isMotorOn = false;
		this.doorState = DoorState.CLOSED;
		this.destinationFloors = new ArrayList<>();

		this.buttonLampStates = new ButtonLampState[numberOfFloors];
		Arrays.fill(buttonLampStates, ButtonLampState.OFF);

		this.state = new ElevatorInitState();
		this.observers = new ArrayList<>();
		this.hasFailed = false;
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
		Logger.println("Door:  " + doorState.toString());
		this.doorState = doorState;
	}

	public Direction getDirection() {
		var destFloor = this.getDestinationFloors().isEmpty() ? 0 : this.getDestinationFloors().get(0);
		return (currentFloor < destFloor) ? Direction.Up : Direction.Down;
	}

	public ButtonLampState[] getButtonLampStates() {
		return buttonLampStates;
	}

	public void setButtonLampStates(ButtonLampState[] buttonLampStates) {
		this.buttonLampStates = buttonLampStates;
	}

	public List<Integer> getDestinationFloors() {
		return destinationFloors;
	}

	public void setState(ElevatorState state) {
		Logger.println("State: " + state.getClass().getSimpleName());
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
		
		synchronized (this.destinationFloors) {

			int carButton = event.carButton();
			int floorButton = event.floor();
			
			int processingTime = 3000;
			
			if (carButton != 0 && floorButton != carButton) {
				this.getDestinationFloors().add(floorButton);
				this.getDestinationFloors().add(carButton);
				this.getButtonLampStates()[carButton] = ButtonLampState.ON;

				if (this.getCurrentFloor() != this.getDestinationFloors().get(0)) {
					this.setState(new MovingState());
					
					
					Thread timer = new Thread(() -> {
						Logger.println("STARTING MY TIMER");
						try {
							Thread.sleep(processingTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						if(this.getCurrentFloor() != carButton) {
							Logger.println("ALERT!!!!!!! ELEVATOR LATE");
//							this.setState(new ElevatorFailState());
							this.hasFailed = true;
						}
						
					}); timer.start();
					
					
					
				} else {
					this.getDestinationFloors().remove(0);
					Logger.debugln("Opening doors");
					this.setDoorState(DoorState.OPEN);
					this.setState(new DoorOpenState());
					
					
					Thread timer = new Thread(() -> {
						Logger.println("STARTING MY TIMER");
						try {
							Thread.sleep(processingTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						if(this.getCurrentFloor() != carButton) {
							Logger.println("ALERT!!!!!!! ELEVATOR LATE");
//							this.setState(new ElevatorFailState());
							this.hasFailed = true;
						}
						
					}); timer.start();
					
					
				}

			} else {
				Logger.println("Elevator Shut DOWN");
			}
		}
	}

	public void run() {
		while (true) {
			try {
				synchronized (this.destinationFloors) {
					state.advance(this);
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void clearDestinationFloors() {
		this.destinationFloors.clear();
	}

	public boolean getfialed() {
		return this.hasFailed;
	}

}
