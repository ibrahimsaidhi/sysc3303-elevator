package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import sysc3303_elevator.networking.BlockingChannelBuilder;
import sysc3303_elevator.networking.UdpClientQueue;
import sysc3303_elevator.networking.UdpServerQueue;

/**
 * @author Quinn Parrott
 *
 */
public class Main {
	
	/**
	 * Used by `GroupBy` to turn an item into a key.
	 * 
	 * @see GroupBy
	 * @author Quinn Parrott
	 */
	public interface GrouperFunction<K, V> { K byKey(V t1); }
	
	/**
	 * Group a collection if items into buckets
	 * 
	 * @param collection The list of items.
	 * @param grouperFun The function that will choose which bucket an item goes into.
	 * @author Quinn Parrott
	 */
	public static <K, V> HashMap<K, ArrayList<V>> GroupBy(Iterable<V> collection, GrouperFunction<K, V> grouperFun) {
		var groups = new HashMap<K, ArrayList<V>>();

		for (var item : collection) {
			var key = grouperFun.byKey(item);
			var groupItems = groups.getOrDefault(key, new ArrayList<>());
			groupItems.add(item);
			groups.putIfAbsent(key, groupItems);
		}

		return groups;
	}

	public static void Run(ArrayList<FloorEvent> events) throws UnknownHostException, SocketException {
		var floors = GroupBy(events, event -> event.floor());
		
		int port1 = 10101;
		int port2 = 10102;
		
		var client1 = new UdpClientQueue<Message, FloorEvent>(InetAddress.getLocalHost(), port1);
		var server1 = new UdpServerQueue<FloorEvent, Message>(port1);
		
		var client2 = new UdpClientQueue<Message, FloorEvent>(InetAddress.getLocalHost(), port2);
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
		var schedularFloorSender = server1.getSender();
		var schedularFloorReceiver = server1.getReceiver();
		
		var schedularElevatorSender = client2.getSender();
		var schedularElevatorReceiver = client2.getReceiver();
		
		
		//ElevatorUDP
		
		var schedularToElevatorReceiver = server2.getReceiver();
		var ElevatorToSchedularSender = server2.getSender();
		
		
		
		//var floorToSchedulerQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<FloorEvent>());
		//var schedulerToFloorQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<Message>());
		//var schedulerToElevatorQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<FloorEvent>());
		//var elevatorToSchedulerQueue = BlockingChannelBuilder.FromBlockingQueue(new LinkedBlockingQueue<Message>());
		
		var f1 = new Floor(floorToSchedulerSender, floorToSchedulerReceiver, events);
	
		var s1 = new Scheduler(schedularFloorReceiver, schedularElevatorReceiver, schedularFloorSender, schedularElevatorSender);
		var es1 = new ElevatorSubsystem(5, 1, schedularToElevatorReceiver, ElevatorToSchedularSender);

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

	public static void main(String[] args) throws UnknownHostException, SocketException {

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
