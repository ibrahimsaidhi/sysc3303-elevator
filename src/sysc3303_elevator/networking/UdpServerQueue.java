package sysc3303_elevator.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpServerQueue<T> implements Runnable {
	
	public static record UdpDatagramMessage<T>(int port, InetAddress address, T content) implements Serializable {
		public <E> UdpDatagramMessage<E> fromContent(E content) {
			return new UdpDatagramMessage<>(this.port, this.address, content);
		}
	}
	
	private DatagramSocket socket;
	private BlockingQueue<UdpDatagramMessage<T>> queue;

	public UdpServerQueue(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public BlockingSender<UdpDatagramMessage<T>> getSender() {
		return new BlockingSender<UdpDatagramMessage<T>>() {
			@Override
			public void put(UdpDatagramMessage<T> e) throws InterruptedException {
				var stream = new ByteArrayOutputStream();
				try {
					(new ObjectOutputStream(stream)).writeObject(e.content());
					var data = stream.toByteArray();
					socket.send(new DatagramPacket(data, data.length, e.address(), e.port()));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}};
	}

	public BlockingReceiver<UdpDatagramMessage<T>> getReceiver() {
		return new BlockingChannelBuilder.ReceiverWrapper<>(this.queue);
	}
	
	public void run() {
		while (true) {
			byte data[] = new byte[10000];
			var receivePacket = new DatagramPacket(data, data.length);
			try {
				socket.receive(receivePacket);
				var objectInput = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
				Object obj = objectInput.readObject();
				this.queue.put(new UdpDatagramMessage<>(receivePacket.getPort(), receivePacket.getAddress(), (T) obj));
			} catch (IOException | ClassNotFoundException | InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
