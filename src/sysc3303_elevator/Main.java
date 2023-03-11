package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import sysc3303_elevator.networking.BlockingChannelBuilder;

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

		var f1 = new Floor(floorToSchedulerQueue.first(), schedulerToFloorQueue.second(), events);
		var s1 = new Scheduler(elevatorToSchedulerQueue.second(), schedulerToFloorQueue.first(), floorToSchedulerQueue.second(), schedulerToElevatorQueue.first());
		var es1 = new ElevatorSubsystem(5, 1, schedulerToElevatorQueue.second(), elevatorToSchedulerQueue.first());

		var threads = new Thread[] {
			new Thread(f1, "floor_1"),
			new Thread(s1, "scheduler_1"),
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
