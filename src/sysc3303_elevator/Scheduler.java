/**
 *
 */
package sysc3303_elevator;

import java.util.concurrent.BlockingQueue;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler implements Runnable {

	private BlockingQueue<FloorEvent> floorToSchedulerQueue;
	private BlockingQueue<Message> elevatorToSchedulerQueue;

	private BlockingQueue<Message> schedulerToFloorQueue;
	private BlockingQueue<FloorEvent> schedulerToElevatorQueue;

	public Scheduler(BlockingQueue<Message> elevatorToSchedulerQueue, BlockingQueue<Message> schedulerToFloorQueue,
			BlockingQueue<FloorEvent> floorToSchedulerQueue, BlockingQueue<FloorEvent> schedulerToElevatorQueue) {

		this.floorToSchedulerQueue = floorToSchedulerQueue;
		this.elevatorToSchedulerQueue = elevatorToSchedulerQueue;
		this.schedulerToFloorQueue = schedulerToFloorQueue;
		this.schedulerToElevatorQueue = schedulerToElevatorQueue;
	}


	/**
	 * read data sent by elevator -- could return data in form of data structure

	public Object readDataFromElevator() {
		// TODO
	}
	*/


	/**
	 * read data sent by floor and add to elevator queue
	 */
	public void readDataFromFloorToElevator() {
		try {
			FloorEvent event = floorToSchedulerQueue.take();
			Logger.println("Got from Floor. Sending to elevator...");

			schedulerToElevatorQueue.put(event);
		} catch (InterruptedException e) {
			System.err.println(e);
		}

	}

	/**
	 * sends data received from elevator to floor system
	 */
	public void sendElevatorDataToFloorSystem() {
		try {
			Message elevatorMessage = elevatorToSchedulerQueue.take();
			Logger.println("Got from Elevator. Sending to floor...");

			schedulerToFloorQueue.put(elevatorMessage);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	@Override
	public void run() {
		while (true) {
			readDataFromFloorToElevator();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}

			sendElevatorDataToFloorSystem();



		}

	}


}
