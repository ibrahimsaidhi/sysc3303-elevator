package sysc3303_elevator;


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
	private BlockingSender<ElevatorResponse> elevatorSubsystemToSchedulerQueue;
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
			BlockingSender<ElevatorResponse> elevatorSubsystemToScheduler
	) {

		this.schedulerToElevatorSubsystemQueue = schedulerToElevatorSubsystem;
		this.elevatorSubsystemToSchedulerQueue = elevatorSubsystemToScheduler;
		this.elevator = new Elevator(numberOfFloors);
		this.elevator.addObserver(this);

		this.elevatorThread = new Thread(this.elevator, "elevator " + elevatorId);
	}

	@Override
	public void run() {
		Logger.debugln("Elevator subsystem init");
		this.elevatorThread.start();
		while (true) {
			try {

				FloorEvent event = this.schedulerToElevatorSubsystemQueue.take();
				Logger.debugln(String.format("Receivece msg from elevator '%s'", event.toString()));

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
	public void onEventProcessed(ElevatorResponse message) {
		Logger.debugln("Sending out message to Scheduler");
		try {
			elevatorSubsystemToSchedulerQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
