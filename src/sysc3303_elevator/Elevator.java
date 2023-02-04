package sysc3303_elevator;

import java.util.concurrent.BlockingQueue;

/**
 * Elevator Class
 *
 * Class responsible for receiving messages from the Scheduler, process it and
 * then return a complete message.
 *
 * @author Tao Lufula, 101164153
 */
public class Elevator implements Runnable {

	private BlockingQueue<FloorEvent> schedulerToElevatorQueue;
	private BlockingQueue<Message> elevatorToScheduler;

	/**
	 * Constructor for Elevator Class
	 *
	 * @author Tao Lufula, 101164153
	 */
	public Elevator(BlockingQueue<FloorEvent> schedulerToElevatorQueue, BlockingQueue<Message> elevatorToScheduler) {

		this.schedulerToElevatorQueue = schedulerToElevatorQueue;
		this.elevatorToScheduler = elevatorToScheduler;
	}


	public void run() {
		while (true) {
			try {

				FloorEvent event = this.schedulerToElevatorQueue.take();
				Logger.println("Got message from scheduler.");

				Thread.sleep(1000);

				Message message = new Message("Processing FloorEvent : Done");

				Logger.println("Sending out message to Scheduler");
				elevatorToScheduler.put(message);

			} catch (InterruptedException e) {
				Logger.println("Elevator Thread interrupted");
			}
		}
	}

}
