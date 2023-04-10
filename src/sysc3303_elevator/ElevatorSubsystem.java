package sysc3303_elevator;
import java.util.Optional;
import java.util.Scanner;
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
	int elevatorId;
	public static final String resourcePath = "input.resources";
	public static final String regex  = "\\d+,[a-zA-Z]+,\\d+";

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

		this.elevatorThread = new Thread(this.elevator, "elevator_state_" + elevatorId);
		this.elevatorId = elevatorId;
	}
	

	@Override
	public void run() {
		Logger.debugln("Elevator subsystem init");
		assignErrorsToElevator(resourcePath);
		this.elevatorThread.start();
		while (true) {
			try {

				FloorEvent event = this.schedulerToElevatorSubsystemQueue.take();
				Logger.debugln(String.format("Received msg from scheduler '%s'", event.toString()));

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
	
	
	private synchronized void assignErrorsToElevator(String resourcePath) {
		var stream = Optional.ofNullable(this.getClass().getResourceAsStream(resourcePath));

		Scanner scanner = new Scanner(stream.get());

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();
			if (line.matches(regex)) {
				String[] parts = line.split(",");
				int id = Integer.parseInt(parts[0]);
	
				if (id == this.elevatorId) {
	
					this.elevator.addError(
							new ElevatorErrorEvent(parts[1].toLowerCase().contains("door") ? ElevatorError.DoorStuck
									: ElevatorError.StuckBtwFloors, Integer.parseInt(parts[2])));
				}
			} else {
				
				continue;
			}

		}
		scanner.close();
	}

}
