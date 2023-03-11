package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import sysc3303_elevator.networking.BlockingChannelBuilder;
import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;
import sysc3303_elevator.networking.RawMultiplexer;

/**
 * @author Quinn Parrott
 *
 */
public class Main {

	public static void Run(ArrayList<FloorEvent> events) {

		var floorToSchedulerQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<FloorEvent>());
		var schedulerToFloorQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<Message>());
		var schedulerToElevatorQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<FloorEvent>());
		var elevatorToSchedulerQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<ElevatorResponse>());

		var es1 = new ElevatorSubsystem(5, 1, schedulerToElevatorQueue.second(), elevatorToSchedulerQueue.first());
		var elevators = new ArrayList<Pair<BlockingSender<FloorEvent>, BlockingReceiver<ElevatorResponse>>>();
		elevators.add(new Pair<>(schedulerToElevatorQueue.first(), elevatorToSchedulerQueue.second()));
		var elevatorMux = new RawMultiplexer<>(elevators);

		var f1 = new Floor(floorToSchedulerQueue.first(), schedulerToFloorQueue.second(), events);
		var floors = new ArrayList<Pair<BlockingSender<Message>, BlockingReceiver<FloorEvent>>>();
		floors.add(new Pair<>(schedulerToFloorQueue.first(), floorToSchedulerQueue.second()));
		var floorMux = new RawMultiplexer<>(floors);

		var s1 = new Scheduler<>(elevatorMux, floorMux);

		var threads = new Thread[] {
			new Thread(f1, "floor_1"),
			new Thread(s1, "scheduler_1"),
			new Thread(elevatorMux, "sch_elevator_mux"),
			new Thread(floorMux, "sch_floor_mux"),
			new Thread(es1, "elevatorSubsytem")
		};

		for (var thread : threads) {
			Logger.println(String.format("Starting '%s'", thread.getName()));
			thread.start();
		}


		// Wait for all threads to exit
		for (var thread : threads) {
			try {
				thread.join();
				Logger.println(String.format("Thread '%s' joined", thread.getName()));
			} catch (InterruptedException e) { }
		}
		Logger.println("All done.");
	}

	public static void main(String[] args) {

		Optional<InputStream> fileStream = Optional.empty();
		fileStream = Optional.of(new ByteArrayInputStream("14:05:15.0 2 up 4\n14:05:16.0 1 up 3\n14:05:17.0 3 down 2\n14:05:18.0 2 up 3".getBytes()));
		try {
			if (fileStream.isEmpty()) {
				fileStream = Optional.of(new FileInputStream(args[0]));
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return;
		}

		var floorReader = new FloorFormatReader(fileStream.get());
		Run(floorReader.toList());
	}

}
