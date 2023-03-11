/**
 *
 */
package sysc3303_elevator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import sysc3303_elevator.networking.TaggedMsg;
import sysc3303_elevator.networking.BlockingMultiplexer;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler<I> implements Runnable {

	private BlockingMultiplexer<I, FloorEvent, ElevatorResponse> elevatorMux;
	private BlockingMultiplexer<I, Message, FloorEvent> floorMux;

	private HashMap<I, Optional<ElevatorResponse>> elevatorStateCache;

	private ArrayList<FloorEvent> requestQueue = new ArrayList<>();

	public Scheduler(
			BlockingMultiplexer<I, FloorEvent, ElevatorResponse> elevatorMux,
			BlockingMultiplexer<I, Message, FloorEvent> floorMux
	) {
		this.elevatorStateCache = new HashMap<>();
		this.elevatorMux = elevatorMux;
		this.floorMux = floorMux;
	}

	public void trySendElevatorGoto() throws InterruptedException {
		// TODO: Narrow down the sync block
		synchronized (this.requestQueue) {
			while (this.requestQueue.size() > 0) {
				boolean foundElement = false;
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var status = entry.getValue();
					if (status.isPresent()) {
						var elevatorInfo = status.get();
						if (elevatorInfo.state().equals(ElevatorStatus.Idle)) {
							// Found idle elevator. Send request!
							Logger.println("Sending elevator goto" + elevatorInfo.toString());

							this.elevatorMux
									.put(new TaggedMsg<I, FloorEvent>(channelId, this.requestQueue.remove(0)));

							// No longer know the status of the elevator
							this.elevatorStateCache.remove(channelId);

							foundElement = true;
							break;
						}
					}
				}

				if (foundElement) {
					continue;
				} else {
					break;
				}
			}
		}
	}

	@Override
	public void run() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						TaggedMsg<I, FloorEvent> event = floorMux.take();
						FloorEvent e = event.content();
						Logger.debugln("Got " + event.toString());
						floorMux.put(event.replaceContent(new Message("ack"))); // TODO: Make this an actual message
						synchronized (requestQueue) {
							requestQueue.add(e);
						}
						trySendElevatorGoto();
					} catch (InterruptedException e) {
						break;
					}
				}
			}

		}, "sche-frecv");
		t.start();
		Logger.println("Scheduler initialized");

		while (true) {
			try {
				TaggedMsg<I, ElevatorResponse> event = elevatorMux.take();
				ElevatorResponse response = event.content();
				Logger.debugln("Got " + event.toString());
				this.elevatorStateCache.put(event.id(), Optional.of(response));

				trySendElevatorGoto();
			} catch (InterruptedException e) {
				break;
			}
		}

		Logger.println("Scheduler interrupted");

		t.interrupt();
	}

}
