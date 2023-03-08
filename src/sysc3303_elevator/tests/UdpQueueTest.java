package sysc3303_elevator.tests;


import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import sysc3303_elevator.networking.UdpClientQueue;
import sysc3303_elevator.networking.UdpServerQueue;


public class UdpQueueTest {
	@Test
	void testBasic() throws Throwable {
		int port = 10101;
		var client = new UdpClientQueue<String>(InetAddress.getLocalHost(), port);
		var server = new UdpServerQueue<String>(port);
		
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

}
