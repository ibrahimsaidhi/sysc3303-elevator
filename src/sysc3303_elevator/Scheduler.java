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
public class Scheduler<I, R> implements Runnable {

	private BlockingMultiplexer<I, FloorEvent, ElevatorResponse> elevatorMux;
	private BlockingMultiplexer<R, Message, FloorEvent> floorMux;

	private HashMap<I, ElevatorResponse> elevatorStateCache;

	private ArrayList<FloorEvent> requestQueue = new ArrayList<>();

	public Scheduler(
			BlockingMultiplexer<I, FloorEvent, ElevatorResponse> elevatorMux,
			BlockingMultiplexer<R, Message, FloorEvent> floorMux) {
		this.elevatorStateCache = new HashMap<>();
		this.elevatorMux = elevatorMux;
		this.floorMux = floorMux;
	}

	public void trySendElevatorGoto() throws InterruptedException {
		Logger.debugln("Elevator States:");
		for (var entry : this.elevatorStateCache.entrySet()) {
			Logger.debugln("   " + entry.getKey() + " " + entry.getValue());
		}

		// TODO: Narrow down the sync block
		synchronized (this.requestQueue) {
			var requestsByDirection = CollectionHelpers.splitBy(this.requestQueue,
					request -> request.direction().equals(Direction.Up));

			// Send additional up request to elevators that are already going up
			for (var upRequest : requestsByDirection.first()) {
				Logger.debugln("Up request " + upRequest.toString());
				// Find the closest elevator that is going in the right direction
				Optional<Pair<I, ElevatorResponse>> closestEntry = Optional.empty();
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var elevatorInfo = entry.getValue();

					// Elevators that are going up should be sent requests that go down
					if (elevatorInfo.state().equals(ElevatorStatus.Idle)
							|| elevatorInfo.direction().equals(Direction.Down)) {
						Logger.debugln("Ignored");
						continue;
					}

					Logger.debugln("Step 1");
					if (upRequest.srcFloor() > elevatorInfo.currentFloor()) {
						Logger.debugln("Step 2");
						if (closestEntry.isPresent()) {
							Logger.debugln("Step 3");
							var previous = closestEntry.get();
							if (previous.second().currentFloor() > elevatorInfo.currentFloor()) {
								closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
							}
						} else {
							closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
						}
					}
				}

				if (closestEntry.isPresent()) {
					var newRequest = closestEntry.get();
					var channelId = newRequest.first();
					var elevatorInfo = newRequest.second();

					// Found idle elevator. Send request!
					Logger.println("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString()
							+ " (up append)");
					if (this.requestQueue.remove(upRequest)) {
						this.elevatorMux
								.put(new TaggedMsg<I, FloorEvent>(channelId, upRequest));
					} else {
						throw new RuntimeException(); // This should never happen
					}

					// No longer know the status of the elevator
					this.elevatorStateCache.remove(channelId);

					this.requestQueue.remove(upRequest); // Request has been dispatched
				}
			}

			// Send additional down request to elevators that are already going down
			for (var downRequest : requestsByDirection.second()) {
				Logger.debugln("Down request " + downRequest.toString());
				// Find the closest elevator that is going in the right direction
				Optional<Pair<I, ElevatorResponse>> closestEntry = Optional.empty();
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var elevatorInfo = entry.getValue();

					// Elevators that are going up should be sent requests that go down
					if (elevatorInfo.state().equals(ElevatorStatus.Idle)
							|| elevatorInfo.direction().equals(Direction.Up)) {
						Logger.debugln("Ignored");
						continue;
					}

					Logger.debugln("Step 1");
					if (downRequest.srcFloor() < elevatorInfo.currentFloor()) {
						Logger.debugln("Step 2");
						if (closestEntry.isPresent()) {
							Logger.debugln("Step 3");
							var previous = closestEntry.get();
							if (previous.second().currentFloor() < elevatorInfo.currentFloor()) {
								closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
							}
						} else {
							closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
						}
					}
				}

				if (closestEntry.isPresent()) {
					var newRequest = closestEntry.get();
					var channelId = newRequest.first();
					var elevatorInfo = newRequest.second();

					// Found idle elevator. Send request!
					Logger.println("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString()
							+ " (down append)");
					if (this.requestQueue.remove(downRequest)) {
						this.elevatorMux
								.put(new TaggedMsg<I, FloorEvent>(channelId, downRequest));
					} else {
						throw new RuntimeException(); // This should never happen
					}

					// No longer know the status of the elevator
					this.elevatorStateCache.remove(channelId);

					this.requestQueue.remove(downRequest); // Request has been dispatched
				}
			}

			while (this.requestQueue.size() > 0) {
				Logger.debugln("Queue size: " + this.requestQueue.size());
				boolean foundElement = false;
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var elevatorInfo = entry.getValue();
					Logger.debugln("Entry " + channelId + " " + elevatorInfo);
					if (elevatorInfo.state().equals(ElevatorStatus.Idle)) {
						// Found idle elevator. Send request!
						Logger.println("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString());

						this.elevatorMux
								.put(new TaggedMsg<I, FloorEvent>(channelId, this.requestQueue.remove(0)));

						// No longer know the status of the elevator
						this.elevatorStateCache.remove(channelId);

						foundElement = true;
						break;
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
						TaggedMsg<R, FloorEvent> event = floorMux.take();
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

		}, "sche_floor_receive");
		t.start();
		Logger.println("Scheduler initialized");

		while (true) {
			try {
				TaggedMsg<I, ElevatorResponse> event = elevatorMux.take();
				ElevatorResponse response = event.content();
				Logger.debugln("Got " + event.toString());
				this.elevatorStateCache.put(event.id(), response);

				trySendElevatorGoto();
			} catch (InterruptedException e) {
				break;
			}
		}

		Logger.println("Scheduler interrupted");

		t.interrupt();
	}

}
