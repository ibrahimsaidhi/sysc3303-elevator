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
import java.util.concurrent.LinkedBlockingQueue;

import sysc3303_elevator.networking.BlockingChannelBuilder;
import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;
import sysc3303_elevator.networking.RawMultiplexer;
import sysc3303_elevator.networking.UdpClientQueue;
import sysc3303_elevator.networking.UdpServerQueue;

/**
 * @author Quinn Parrott
 *
 */
public class Main {

	public static void Run(ArrayList<FloorEvent> events) throws SocketException, UnknownHostException {
		int port1 = 10101;
		int port2 = 10102;
		
		var client1 = new UdpClientQueue<Message, FloorEvent>(InetAddress.getLocalHost(), port1);
		var server1 = new UdpServerQueue<FloorEvent, Message>(port1);
		
		var client2 = new UdpClientQueue<FloorEvent, ElevatorResponse>(InetAddress.getLocalHost(), port2);
		var server2 = new UdpServerQueue<FloorEvent, Message>(port2);
		
		var clientThread1 = new Thread(client1);
		var serverThread1 = new Thread(server1);
		serverThread1.start();
		clientThread1.start();
		
		var clientThread2 = new Thread(client2);
		var serverThread2 = new Thread(server2);
		serverThread2.start();
		clientThread2.start();
		
		//Floor UDP
		var floorToSchedulerSender = client1.getSender();
		var floorToSchedulerReceiver = client1.getReceiver();
		
		//Schedular UDP
		//var schedularFloorSender = server1.getSender();
		//var schedularFloorReceiver = server1.getReceiver();
		
		
		//ElevatorUDP
		var ElevatorSchedulerSender = client2.getSender();
		var ElevatorSchedulerReceiver = client2.getReceiver();
		//var schedularToElevatorReceiver = server2.getReceiver();
		//var ElevatorToSchedularSender = server2.getSender();
		
		
		//var floorToSchedulerQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<FloorEvent>());
		//var schedulerToFloorQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<Message>());
		//var schedulerToElevatorQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<FloorEvent>());
		//var elevatorToSchedulerQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<ElevatorResponse>());

		var es1 = new ElevatorSubsystem(5, 1,ElevatorSchedulerReceiver ,ElevatorSchedulerSender);
		var elevators = new ArrayList<Pair<BlockingSender<ElevatorResponse>, BlockingReceiver<FloorEvent>>>();
		
		var p1 = new Pair<>(ElevatorSchedulerSender, ElevatorSchedulerReceiver);
		elevators.add(p1);
		var elevatorMux = new RawMultiplexer<>(elevators);

		var f1 = new Floor(floorToSchedulerSender, floorToSchedulerReceiver, events);
		var floors = new ArrayList<Pair<BlockingSender<FloorEvent>, BlockingReceiver<Message>>>();
		var p2 = new Pair<>(floorToSchedulerSender, floorToSchedulerReceiver);
		floors.add(p2);
		var floorMux = new RawMultiplexer<>(floors);

		var s1 = new Scheduler<Integer, Integer>(elevatorMux, floorMux);

		var threads = new Thread[] {
			new Thread(f1, "floor_1"),
			//new Thread(s1, "scheduler_1"),
			//new Thread(elevatorMux, "sch_elevator_mux"),
			//new Thread(floorMux, "sch_floor_mux"),
			//new Thread(es1, "elevatorSubsytem")
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

	public static void main(String[] args) throws SocketException, UnknownHostException {

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
