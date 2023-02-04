package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Quinn Parrott
 *
 */
public class Main {

	public static void Run(ArrayList<FloorEvent> events) {
		var floors = new HashMap<Integer, ArrayList<FloorEvent>>();
		Logger.println(String.format("'%s' floor events", events.size()));
		for (var floorEvent : events) {
			var floorList = floors.getOrDefault(floorEvent.floor(), new ArrayList<>());
			floorList.add(floorEvent);
			Logger.println(String.format("Adding %s", floorEvent.toString()));
			floors.putIfAbsent(floorEvent.floor(), floorList);
		}

		var floorToSchedulerQueue = new LinkedBlockingQueue<FloorEvent>();
		var schedulerToFloorQueue = new LinkedBlockingQueue<Message>();
		var schedulerToElevatorQueue = new LinkedBlockingQueue<FloorEvent>();
		var elevatorToSchedulerQueue = new LinkedBlockingQueue<Message>();

		var f1 = new Floor(1, floorToSchedulerQueue, schedulerToFloorQueue, floors.getOrDefault(1, new ArrayList<>()));
		var f2 = new Floor(2, floorToSchedulerQueue, schedulerToFloorQueue, floors.getOrDefault(2, new ArrayList<>()));
		var f3 = new Floor(3, floorToSchedulerQueue, schedulerToFloorQueue, floors.getOrDefault(3, new ArrayList<>()));
		var s1 = new Scheduler(elevatorToSchedulerQueue, schedulerToFloorQueue, floorToSchedulerQueue, schedulerToElevatorQueue);
		var e1 = new Elevator(schedulerToElevatorQueue, elevatorToSchedulerQueue);

		var threads = new Thread[] {
			new Thread(f1, "floor_1"),
			new Thread(f2, "floor_2"),
			new Thread(f3, "floor_3"),
			new Thread(s1, "scheduler_1"),
			new Thread(e1, "elevator_1")
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
