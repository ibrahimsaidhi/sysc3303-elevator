package sysc3303_elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * ElevatorSubsystem Class
 *
 * Class responsible for receiving messages from the Scheduler, send it to
 * elevator and then return a complete message.
 *
 * @author Tao Lufula, 101164153
 */
public class ElevatorSubsystem implements Runnable, ElevatorObserver {

	private BlockingQueue<FloorEvent> schedulerToElevatorSubsystemQueue;
	private BlockingQueue<Message> elevatorSubsystemToSchedulerQueue;
	private List<Elevator> elevators;
	int elevatorFloors;

	/**
	 * Constructor for Elevator Class
	 *
	 */
	public ElevatorSubsystem(int numberOfFloors, int numberOfElevators,
			BlockingQueue<FloorEvent> schedulerToElevatorSubsystem,
			BlockingQueue<Message> elevatorSubsystemToScheduler) {

		this.schedulerToElevatorSubsystemQueue = schedulerToElevatorSubsystem;
		this.elevatorSubsystemToSchedulerQueue = elevatorSubsystemToScheduler;
		this.elevatorFloors = numberOfFloors;
		this.elevators = new ArrayList<>();

		// creating elevators given the number of floors. Note: only one elevator will
		// be used for now
		for (int i = 0; i < numberOfElevators; i++) {
			Elevator e1 = new Elevator(elevatorFloors);
			this.elevators.add(e1);
			e1.addObserver(this);
			new Thread(e1, "Elevator 1").start();
			
		}
	}

	/**
	 * Getter method to get elevator given index
	 * 
	 * @param index
	 * @return Elevator
	 */
	public Elevator getElevator(int index) {
		return elevators.get(index);
	}

	public void run() {
		while (true) {
			try {

				FloorEvent event = this.schedulerToElevatorSubsystemQueue.take();
				Logger.println("Got message from scheduler.");

				// Pass the event to the elevator and wait for a message;
				this.getElevator(0).processFloorEvent(event);				
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				Logger.println("ElevatorSubsystem Thread interrupted");
			}
		}
	}

	@Override
	public void onEventProcessed(Message message) {
		Logger.println("Sending out message to Scheduler");
		try {
			elevatorSubsystemToSchedulerQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
