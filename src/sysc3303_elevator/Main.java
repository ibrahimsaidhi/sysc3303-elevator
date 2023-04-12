package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;

import sysc3303_elevator.networking.UdpClientQueue;
import sysc3303_elevator.networking.UdpServerQueue;
import sysc3303_elevator.networking.UdpServerQueue.UdpClientIdentifier;

/**
 * @author Quinn Parrott
 *
 */
public class Main {

	private static final Integer floorPort = 10101;
	private static final Integer elevatorPort = 10102;
	private static final String FILE_PATH = "input.txt";
	private static final String HOST_PREFIX = "host:";
	private static ElevatorSubsystem es;
	private static Floor floor;

	public static Thread RunElevator(InetAddress host, int elevatorId) throws SocketException, UnknownHostException {
		var elevatorClient1 = new UdpClientQueue<FloorEvent, ElevatorResponse>(host, elevatorPort);

		var es1 = new ElevatorSubsystem(5, elevatorId, elevatorClient1.getReceiver(), elevatorClient1.getSender());
		es = es1;
		
		return ThreadHelper.runThreads("elevator_prog", new Thread[] {
				new Thread(elevatorClient1, "elev_c_" + elevatorId),
				new Thread(es1, "elevator_syst__" + elevatorId)
		});
	}

	public static Thread RunScheduler() throws SocketException, UnknownHostException {
		var floorServer = new UdpServerQueue<Message, FloorEvent>(floorPort);

		var elevatorServer = new UdpServerQueue<FloorEvent, ElevatorResponse>(elevatorPort);

		var s1 = new Scheduler<UdpClientIdentifier, UdpClientIdentifier>(elevatorServer, floorServer);
		SchedulerGUI schedulerGUI = new SchedulerGUI(s1);
		return ThreadHelper.runThreads("scheduler_prog", new Thread[] {
				new Thread(s1, "scheduler_1"),
				new Thread(floorServer, "floor_serv"),
				new Thread(elevatorServer, "elev_serv"),
		});
	}

	public static Thread RunFloor(InetAddress host, ArrayList<FloorEvent> events)
		throws SocketException, UnknownHostException {
		var floorClient1 = new UdpClientQueue<Message, FloorEvent>(host, floorPort);
		var f1 = new Floor(floorClient1.getSender(), floorClient1.getReceiver(), events);
		floor = f1;

		return ThreadHelper.runThreads("floors_prog", new Thread[] {
				new Thread(f1, "floor_1"),
				new Thread(floorClient1, "floor_c_1"),
		});
	}

	private static Optional<InputStream> readFile() {
		try {
			return Optional.of(new FileInputStream(FILE_PATH));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();

			return Optional.of(new ByteArrayInputStream( // hard coded floor events incase something goes wrong
					"14:05:15.0 2 up 4\n14:05:16.0 1 up 3\n14:05:17.0 3 down 2\n14:05:18.0 2 up 3".getBytes()));
		}
	}

	private static StringBuilder join(String[] strings, String delimiter) {
		var builder = new StringBuilder();

		int i = 0;
		for (var elem : strings) {
			if (i > 0) {
				builder.append(delimiter);
			}
			builder.append(elem);
			i += 1;
		}

		return builder;
	}

	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
		
		if (args.length == 0) {
			args = new String[] { "elevator", "scheduler", "floor" };
			Logger.println(String.format("Using default arguments: {%s}", join(args, ", ")));
		} else {
			Logger.println(String.format("Using cli arguments: {%s}", join(args, ", ")));
		}

		Optional<InputStream> fileStream = readFile();
		if (fileStream.isEmpty()) {
			return; // If the InputStream is empty, stop the program execution
		}
		//Measurements measurements = Measurements.getInstance();
		//measurements.startTimer();
		var floorReader = new FloorFormatReader(fileStream.get());
		var events = floorReader.toList();

		int elevator_id_counter = 1;
		var tasks = new ArrayList<Thread>();
		Optional<InetAddress> hostOptional = Optional.empty();
		for (var argRaw : args) {
			var arg = argRaw.toLowerCase();
			switch (arg) {
				case "elevator":
				case "e": {
					var host = hostOptional.orElse(InetAddress.getLocalHost());
					Logger.println(
							String.format("Creating elevator %s (connecting to '%s')", elevator_id_counter, host));
					tasks.add(RunElevator(host, elevator_id_counter));
					elevator_id_counter += 1;
					break;
				}
				case "scheduler":
				case "s": {
					Logger.println("Creating scheduler");
					tasks.add(RunScheduler());
					break;
				}
				case "floor":
				case "f": {
					var host = hostOptional.orElse(InetAddress.getLocalHost());
					Logger.println(String.format("Creating floor (connecting to '%s')", host));
					tasks.add(RunFloor(host, new ArrayList<>(events)));
					break;
				}
				default: {
					if (arg.startsWith(HOST_PREFIX)) {
						var hostString = arg.substring(HOST_PREFIX.length()).strip();
						if (hostString.length() > 0) {
							hostOptional = Optional.of(InetAddress.getByName(hostString));
						} else {
							hostOptional = Optional.empty();
						}
					} else {
						throw new RuntimeException(String.format(
								"Argument '%s' is not valid. Use either 'elevator', 'e', 'scheduler', 's', 'floor', 'f', 'host:*'",
								arg));
					}
				}
			}
		}
		Measurements timer = new Measurements(es, floor);
		Thread timerThread = new Thread(timer);
		timerThread.start();
		System.out.println("start timer");
		ThreadHelper.runThreads("root", tasks.toArray(new Thread[tasks.size()])).join();
		Logger.println("All done.");
	}

}
