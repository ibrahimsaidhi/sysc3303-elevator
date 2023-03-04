/**
 *
 */
package sysc3303_elevator;

import java.util.concurrent.BlockingQueue;

import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler implements Runnable {

	private BlockingReceiver<FloorEvent> floorToSchedulerQueue;
	private BlockingReceiver<Message> elevatorToSchedulerQueue;

	private BlockingSender<Message> schedulerToFloorQueue;
	private BlockingSender<FloorEvent> schedulerToElevatorQueue;
	
	private SchedulerState state;
	private FloorEvent event;
	private Message message;

	public Scheduler(
			BlockingReceiver<Message> elevatorToSchedulerQueue,
			BlockingSender<Message> schedulerToFloorQueue,
			BlockingReceiver<FloorEvent> floorToSchedulerQueue,
			BlockingSender<FloorEvent> schedulerToElevatorQueue
	) {

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
