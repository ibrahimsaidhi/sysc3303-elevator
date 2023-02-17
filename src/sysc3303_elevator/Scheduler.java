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
	
	private SchedulerState state;
	private FloorEvent event;
	private Message message;

	public Scheduler(BlockingQueue<Message> elevatorToSchedulerQueue, BlockingQueue<Message> schedulerToFloorQueue,
			BlockingQueue<FloorEvent> floorToSchedulerQueue, BlockingQueue<FloorEvent> schedulerToElevatorQueue) {

		this.floorToSchedulerQueue = floorToSchedulerQueue;
		this.elevatorToSchedulerQueue = elevatorToSchedulerQueue;
		this.schedulerToFloorQueue = schedulerToFloorQueue;
		this.schedulerToElevatorQueue = schedulerToElevatorQueue;
		this.state = new FloorListeningState(this);
		
	}


	public void setState(SchedulerState state) {
		this.state = state;
	}


	/**
	 * read data sent by floor and add to elevator queue
	 */
	public void sendToElevator() {
		try {
			schedulerToElevatorQueue.put(event);
		} catch (InterruptedException e) {
			System.err.println(e);
		}

	}
	
	/**
	 * Listens to the floor-to-elevator queue for new floor events
	 */
	public void listenToFloor() {
		try {
			event = floorToSchedulerQueue.take();
			Logger.println("Got message from Floor. Sending to elevator...");
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	/**
	 * listens to the data response from elevator
	 */
	public void listenToElevator() {
		try {
			message = elevatorToSchedulerQueue.take();
			Logger.println("Got message from Elevator. Sending to floor...");

			
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * sends data received from elevator to floor system
	 */
	public void sendToFloor() {
		try {
			schedulerToFloorQueue.put(message);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
		
	}

	@Override
	public void run() {
		while (true) {
			state.dealWithMessage();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}

			



		}

	}


}
