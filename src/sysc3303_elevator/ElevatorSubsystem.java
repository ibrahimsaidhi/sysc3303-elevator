package sysc3303_elevator;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * ElevatorSubsystem Class
 *
 * Class responsible for receiving messages from the Scheduler, send it to elevator and
 * then return a complete message.
 *
 * @author Tao Lufula, 101164153
 */
public class ElevatorSubsystem implements Runnable {

	private BlockingQueue<FloorEvent> schedulerToElevatorSubsystemQueue;
	private BlockingQueue<Message> elevatorSubsystemToSchedulerQueue;
	private List<Elevator> elevators;
	int elevatorFloors;
	
	

	/**
	 * Constructor for Elevator Class
	 *
	 * @author Tao Lufula, 101164153
	 */
	public ElevatorSubsystem(int numberOfFloors, int numberOfElevators, BlockingQueue<FloorEvent> schedulerToElevatorSubsystem, BlockingQueue<Message> elevatorSubsystemToScheduler) {

		this.schedulerToElevatorSubsystemQueue = schedulerToElevatorSubsystem;
		this.elevatorSubsystemToSchedulerQueue = elevatorSubsystemToScheduler;
		
		//creating elevators given the number of floors. Note: only one elevator will be used
		for (int i = 0; i < numberOfElevators; i++) {
		      elevators.add(new Elevator(numberOfFloors));
		    }
	}


	public void run() {
		while (true) {
			try {

				FloorEvent event = this.schedulerToElevatorSubsystemQueue.take();
				Logger.println("Got message from scheduler.");

				Thread.sleep(1000);

				Message message = new Message("Processing FloorEvent : Done");

				Logger.println("Sending out message to Scheduler");
				elevatorSubsystemToSchedulerQueue.put(message);

			} catch (InterruptedException e) {
				Logger.println("Elevator Thread interrupted");
			}
		}
	}

}
