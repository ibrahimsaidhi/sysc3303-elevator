package sysc3303_elevator.tests;


import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import sysc3303_elevator.Direction;
import sysc3303_elevator.FloorEvent;
import sysc3303_elevator.Message;
import sysc3303_elevator.networking.UdpClientQueue;
import sysc3303_elevator.networking.UdpServerQueue;


public class UdpQueueTest {
	@Test
	void testBasic() throws Throwable {
		int port = 10101;
		var client = new UdpClientQueue<String, String>(InetAddress.getLocalHost(), port);
		var server = new UdpServerQueue<String, String>(port);
		
		var clientThread = new Thread(client);
		var serverThread = new Thread(server);
		serverThread.start();
		clientThread.start();
		
		var clientReceiver = client.getReceiver();
		var serverReceiver = server.getReceiver();
		var clientSender = client.getSender();
		var serverSender = server.getSender();
		

		clientSender.put("wow");
		var result1 = serverReceiver.take();
		assertEquals(result1.content(), "wow");
		
		serverSender.put(result1.fromContent("reply"));
		var result2 = clientReceiver.take();
		assertEquals(result2, "reply");

		clientSender.put("more");
		var result3 = serverReceiver.take();
		assertEquals(result3.content(), "more");
	}
	
	@Test 
	void testFloorEvent() throws SocketException, UnknownHostException, InterruptedException {
		int port = 10102;
		var client = new UdpClientQueue<Message, FloorEvent>(InetAddress.getLocalHost(), port);
		var server = new UdpServerQueue<Message, FloorEvent>(port);
		
		var clientThread = new Thread(client);
		var serverThread = new Thread(server);
		serverThread.start();
		clientThread.start();
		
		FloorEvent floorevent = new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4);
		
		var clientSender = client.getSender();
		var serverReceiver = server.getReceiver();

		clientSender.put(floorevent);
		var result1 = serverReceiver.take();
		assertEquals(result1.content(), floorevent);	
	}
	
	@Test 
	void FloorToScheduler() throws SocketException, UnknownHostException, InterruptedException {
		int port = 10103;
		var client = new UdpClientQueue<Message, FloorEvent>(InetAddress.getLocalHost(), port);
		var server = new UdpServerQueue<Message, FloorEvent>(port);
		
		var clientThread = new Thread(client);
		var serverThread = new Thread(server);
		serverThread.start();
		clientThread.start();
		
		FloorEvent floorevent = new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4);
		
		var clientSender = client.getSender();
		var serverReceiver = server.getReceiver();

		clientSender.put(floorevent);
		var result1 = serverReceiver.take();
		assertEquals(result1.content(), floorevent);	
	}
	
	@Test 
	void serverTest() throws SocketException, UnknownHostException, InterruptedException {
		int port = 10103;
		var client = new UdpClientQueue<String, String>(InetAddress.getLocalHost(), port);
		var server = new UdpServerQueue<String, String>(port);
		FloorEvent floorevent;
		
		var clientThread = new Thread(client);
		var serverThread = new Thread(server);
		serverThread.start();
		clientThread.start();
		
		
		var clientSender = client.getSender();
		var serverReceiver = server.getReceiver();

		clientSender.put("test");
		var result1 = serverReceiver.take();
		assertEquals(result1.content(), "test");	
	}

}
