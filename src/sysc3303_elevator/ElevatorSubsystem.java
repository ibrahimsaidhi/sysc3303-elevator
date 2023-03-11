package sysc3303_elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;

/**
 * ElevatorSubsystem Class
 *
 * Class responsible for receiving messages from the Scheduler, send it to
 * elevator and then return a complete message.
 *
 * @author Tao Lufula, 101164153
 */
public class ElevatorSubsystem implements Runnable, ElevatorObserver {

	private BlockingReceiver<FloorEvent> schedulerToElevatorSubsystemQueue;
	private BlockingSender<Message> elevatorSubsystemToSchedulerQueue;
	private Elevator elevator;
	private Thread elevatorThread;
	int elevatorFloors;

	/**
	 * Constructor for Elevator Class
	 *
	 */
	public ElevatorSubsystem(
			int numberOfFloors,
			int elevatorId,
			BlockingReceiver<FloorEvent> schedulerToElevatorSubsystem,
			BlockingSender<Message> elevatorSubsystemToScheduler
	) {

		this.schedulerToElevatorSubsystemQueue = schedulerToElevatorSubsystem;
		this.elevatorSubsystemToSchedulerQueue = elevatorSubsystemToScheduler;
		this.elevator = new Elevator(numberOfFloors);
		this.elevator.addObserver(this);

		this.elevatorThread = new Thread(this.elevator, "elevator " + elevatorId);
	}

	public void run() {
		this.elevatorThread.start();
		while (true) {
			try {

				FloorEvent event = this.schedulerToElevatorSubsystemQueue.take();
				Logger.println("Got message from scheduler.");

				// Pass the event to the elevator and wait for a message;
				this.elevator.processFloorEvent(event);
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				Logger.println("ElevatorSubsystem Thread interrupted");
				break;
			}
		}
		this.elevatorThread.interrupt();
	}

	@Override
	public void onEventProcessed(Message message) {
		// TODO: Message -> ElevatorResponse
		Logger.println("Sending out message to Scheduler");
		try {
			elevatorSubsystemToSchedulerQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
