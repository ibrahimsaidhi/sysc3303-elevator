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

	public static Thread RunElevator(int elevatorId) throws SocketException, UnknownHostException {
		var elevatorClient1 = new UdpClientQueue<FloorEvent, ElevatorResponse>(InetAddress.getLocalHost(),
				elevatorPort);

		var es1 = new ElevatorSubsystem(5, 1, elevatorClient1.getReceiver(), elevatorClient1.getSender());

		return ThreadHelper.runThreads("elevator_prog", new Thread[] {
				new Thread(elevatorClient1, "elev_c_" + elevatorId),
				new Thread(es1, "elevatorSubsytem" + elevatorId)
		});
	}

	public static Thread RunScheduler() throws SocketException, UnknownHostException {
		var floorServer = new UdpServerQueue<Message, FloorEvent>(floorPort);

		var elevatorServer = new UdpServerQueue<FloorEvent, ElevatorResponse>(elevatorPort);

		var s1 = new Scheduler<UdpClientIdentifier, UdpClientIdentifier>(elevatorServer, floorServer);

		return ThreadHelper.runThreads("scheduler_prog", new Thread[] {
				new Thread(s1, "scheduler_1"),
				new Thread(floorServer, "floor_serv"),
				new Thread(elevatorServer, "elev_serv"),
		});
	}

	public static Thread RunFloor(ArrayList<FloorEvent> events) throws SocketException, UnknownHostException {
		var floorClient1 = new UdpClientQueue<Message, FloorEvent>(InetAddress.getLocalHost(), floorPort);
		var f1 = new Floor(floorClient1.getSender(), floorClient1.getReceiver(), events);

		return ThreadHelper.runThreads("floors_prog", new Thread[] {
				new Thread(f1, "floor_1"),
				new Thread(floorClient1, "floor_c_1"),
		});
	}

	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {

		Optional<InputStream> fileStream = Optional.empty();
		fileStream = Optional.of(new ByteArrayInputStream(
				"14:05:15.0 2 up 4\n14:05:16.0 1 up 3\n14:05:17.0 3 down 2\n14:05:18.0 2 up 3".getBytes()));
		try {
			if (fileStream.isEmpty()) {
				fileStream = Optional.of(new FileInputStream(args[0]));
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return;
		}

		var floorReader = new FloorFormatReader(fileStream.get());
		var events = floorReader.toList();


		ThreadHelper.runThreads("root", new Thread[] {
			RunElevator(1),
			RunElevator(2),
			RunFloor(events),
			RunScheduler(),
		}).join();


		Logger.println("All done.");
	}

}
