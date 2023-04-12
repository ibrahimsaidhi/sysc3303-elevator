package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;

import sysc3303_elevator.ThreadHelper.ThreadOption;
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
	public static final String resourcePath = "input.resources";

	public static Thread RunElevator(InetAddress host, int elevatorId, ElevatorSettings settings,
			ArrayList<ElevatorErrorEvent> errorEvents)
			throws SocketException, UnknownHostException {
		var elevatorClient1 = new UdpClientQueue<FloorEvent, ElevatorResponse>(host, elevatorPort);

		var es1 = new ElevatorSubsystem(settings, elevatorId, elevatorClient1.getReceiver(),
				elevatorClient1.getSender(),
				errorEvents);

		return ThreadHelper.runThreads("elevator_prog", new Thread[] {
				new Thread(elevatorClient1, "elev_c_" + elevatorId),
				new Thread(es1, "elevator_syst__" + elevatorId) }, ThreadOption.Waiting);
	}

	public static Thread RunScheduler() throws SocketException, UnknownHostException {
		var floorServer = new UdpServerQueue<Message, FloorEvent>(floorPort);

		var elevatorServer = new UdpServerQueue<FloorEvent, ElevatorResponse>(elevatorPort);

		var s1 = new Scheduler<UdpClientIdentifier, UdpClientIdentifier>(elevatorServer, floorServer);
		var schedulerGUI = new SchedulerGUI<UdpClientIdentifier>();
		s1.addView(schedulerGUI);

		return ThreadHelper.runThreads("scheduler_prog", new Thread[] {
				new Thread(s1, "scheduler_1"),
				new Thread(floorServer, "floor_serv"),
				new Thread(elevatorServer, "elev_serv"), }, ThreadOption.Waiting);
	}

	public static Thread RunFloor(InetAddress host, ArrayList<FloorEvent> events)
			throws SocketException, UnknownHostException {
		var floorClient1 = new UdpClientQueue<Message, FloorEvent>(host, floorPort);
		var f1 = new Floor(floorClient1.getSender(), floorClient1.getReceiver(), events);

		return ThreadHelper.runThreads("floors_prog", new Thread[] {
				new Thread(f1, "floor_1"),
				new Thread(floorClient1, "floor_c_1"), }, ThreadOption.Waiting);
	}

	private static Optional<InputStream> readFile() throws FileNotFoundException {
		InputStream inputStream = Main.class.getResourceAsStream(resourcePath);
		if (inputStream == null) {
			return Optional.of(new ByteArrayInputStream( // hard coded floor events in case something goes wrong
					"14:05:15.0 2 up 4\n14:05:16.0 1 up 3\n14:05:17.0 3 down 2\n14:05:18.0 2 up 3".getBytes()));
		}
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, length);
			}
			byteArrayOutputStream.flush();
			return Optional.of(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
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

	public static void main(String[] args)
			throws SocketException, UnknownHostException, InterruptedException, FileNotFoundException {
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

		var floorReader = new FloorFormatReader(fileStream.get());
		var events = floorReader.toList();

		var floorEvents = events.first();
		var errorEvents = events.second();

		int elevator_id_counter = 1;
		var tasks = new ArrayList<Thread>();
		Optional<InetAddress> hostOptional = Optional.empty();
		int optionFloorCount = 22;
		int optionBetweenFloors = 7383;
		Optional<Integer> optionOpenClose = Optional.empty(); // 1500;
		Optional<Integer> optionLoadUnload = Optional.empty(); // 6483;

		final String TIME_OPEN_DOOR_OPTION = "open_close_door=";
		final String TIME_LOAD_UNLOAD_OPTION = "load_unload=";

		for (var argRaw : args) {
			var arg = argRaw.toLowerCase();
			switch (arg) {
				case "elevator":
				case "e": {
					var host = hostOptional.orElse(InetAddress.getLocalHost());
					Logger.println(
							String.format("Creating elevator %s (connecting to '%s')", elevator_id_counter, host));
					if (optionOpenClose.isEmpty())
						throw new RuntimeException(String.format(
								"Missing '%s' argument (must be before creating elevator arg)", TIME_OPEN_DOOR_OPTION));
					if (optionLoadUnload.isEmpty())
						throw new RuntimeException(
								String.format("Missing '%s' argument (must be before creating elevator arg)",
										TIME_LOAD_UNLOAD_OPTION));
					var settings = new ElevatorSettings(
							optionFloorCount,
							optionBetweenFloors,
							200,
							optionOpenClose.get(),
							200,
							optionLoadUnload.get());

					tasks.add(RunElevator(host, elevator_id_counter, settings, errorEvents));
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
					tasks.add(RunFloor(host, new ArrayList<>(floorEvents)));
					break;
				}
				case "debug":
				case "-v":
				case "d": {
					Logger.setDebug(true);
					break;
				}
				case "nodebug": {
					Logger.setDebug(false);
					break;
				}
				case "perf":
				case "-p":
				case "p": {
					Logger.setPerf(true);
					break;
				}
				case "noperf": {
					Logger.setPerf(false);
					break;
				}
				default: {
					final String HOST_OPTION = "host=";
					final String TIME_BETWEEN_FLOORS_OPTION = "between_floors=";
					final String FLOOR_COUNT = "floors=";
					if (arg.startsWith(HOST_OPTION)) {
						var hostString = arg.substring(HOST_OPTION.length()).strip();
						if (hostString.length() > 0) {
							hostOptional = Optional.of(InetAddress.getByName(hostString));
						} else {
							hostOptional = Optional.empty();
						}
					} else if (arg.startsWith(TIME_OPEN_DOOR_OPTION)) {
						var subString = arg.substring(TIME_OPEN_DOOR_OPTION.length()).strip();
						try {
							optionOpenClose = Optional.of(Integer.parseInt(subString));
						} catch (NumberFormatException ex) {
							throw new RuntimeException(String.format("'%s' was not passed a valid number.", subString));
						}
					} else if (arg.startsWith(TIME_LOAD_UNLOAD_OPTION)) {
						var subString = arg.substring(TIME_LOAD_UNLOAD_OPTION.length()).strip();
						try {
							optionLoadUnload = Optional.of(Integer.parseInt(subString));
						} catch (NumberFormatException ex) {
							throw new RuntimeException(String.format("'%s' was not passed a valid number.", subString));
						}
					} else if (arg.startsWith(TIME_BETWEEN_FLOORS_OPTION)) {
						var subString = arg.substring(TIME_BETWEEN_FLOORS_OPTION.length()).strip();
						try {
							optionBetweenFloors = Integer.parseInt(subString);
						} catch (NumberFormatException ex) {
							throw new RuntimeException(String.format("'%s' was not passed a valid number.", subString));
						}
					} else if (arg.startsWith(FLOOR_COUNT)) {
						var subString = arg.substring(FLOOR_COUNT.length()).strip();
						try {
							optionFloorCount = Integer.parseInt(subString);
						} catch (NumberFormatException ex) {
							throw new RuntimeException(String.format("'%s' was not passed a valid number.", subString));
						}
					} else {
						var params = new ArrayList<String>();
						params.add("elevator");
						params.add("e");
						params.add("scheduler");
						params.add("s");
						params.add("floor");
						params.add("f");
						params.add("debug");
						params.add("-v");
						params.add("d");
						params.add("nodebug");
						params.add("perf");
						params.add("-p");
						params.add("p");
						params.add("noperf");
						params.add(HOST_OPTION);
						params.add(TIME_BETWEEN_FLOORS_OPTION);
						params.add(FLOOR_COUNT);
						params.add(TIME_OPEN_DOOR_OPTION);
						params.add(TIME_LOAD_UNLOAD_OPTION);
						throw new RuntimeException(String.format(
								"Argument '%s' is not valid. Use either "
										+ join(params.toArray(new String[params.size()]), ", "),
								arg));
					}
				}
			}
		}

		ThreadHelper.runThreads("root", tasks.toArray(new Thread[tasks.size()]), ThreadOption.Start).join();

		Logger.println("All done.");
	}

}
