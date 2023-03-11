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

	private BlockingReceiver<FloorEvent> floorToScheduler;
	private BlockingReceiver<Message> elevatorToScheduler;

	private BlockingSender<Message> schedulerToFloor;
	private BlockingSender<FloorEvent> schedulerToElevator;
	
	private SchedulerState state;
	private FloorEvent event;
	private Message message;

	public Scheduler(
			BlockingReceiver<FloorEvent> floorToScheduler,
			BlockingReceiver<Message> elevatorToScheduler,
			BlockingSender<Message> schedulerToFloor,
			BlockingSender<FloorEvent> schedulerToElevator
	) {
		this.floorToScheduler = floorToScheduler;
		this.elevatorToScheduler = elevatorToScheduler;
		this.schedulerToFloor = schedulerToFloor;
		this.schedulerToElevator = schedulerToElevator;
		
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
			schedulerToElevator.put(event);
		} catch (InterruptedException e) {
			System.err.println(e);
		}

	}
	
	/**
	 * Listens to the floor-to-elevator queue for new floor events
	 */
	public void listenToFloor() {
		try {
			event = floorToScheduler.take();
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
			message = elevatorToScheduler.take();
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
			schedulerToFloor.put(message);
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
