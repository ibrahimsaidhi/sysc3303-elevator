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
	private ArrayList<FieldListener> views = new ArrayList<>();

	public Scheduler(
			BlockingMultiplexer<I, FloorEvent, ElevatorResponse> elevatorMux,
			BlockingMultiplexer<R, Message, FloorEvent> floorMux) {
		this.elevatorStateCache = new HashMap<>();
		this.elevatorMux = elevatorMux;
		this.floorMux = floorMux;
	}
	
	public void addView(FieldListener e) {
		views.add(e);
	}
	
	public void removeView(FieldListener e) {
		views.remove(e);
	}

	public void trySendElevatorGoto() throws InterruptedException {
		//Logger.debugln("Elevator States:");
		for (FieldListener e: views) {
			e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(),"Elevator States:\n"));
		}
		for (var entry : this.elevatorStateCache.entrySet()) {
			//Logger.debugln("   " + entry.getKey() + " " + entry.getValue());
			for (FieldListener e: views) {
				e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(),"   " + entry.getKey() + " " + entry.getValue() + "\n"));
			}
		}

		// TODO: Narrow down the sync block
		synchronized (this.requestQueue) {
			var requestsByDirection = CollectionHelpers.splitBy(this.requestQueue,
					request -> request.direction().equals(Direction.Up));

			// Send additional up request to elevators that are already going up
			for (var upRequest : requestsByDirection.first()) {
				//Logger.debugln("Up request " + upRequest.toString());
				for (FieldListener e: views) {
					e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Up request " + upRequest.toString() + "\n"));
				}
				// Find the closest elevator that is going in the right direction
				Optional<Pair<I, ElevatorResponse>> closestEntry = Optional.empty();
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var elevatorInfo = entry.getValue();
					
					// Elevators that are going up should be sent requests that go down
					if (!elevatorInfo.state().equals(ElevatorStatus.ShutDown)) {
						if (elevatorInfo.state().equals(ElevatorStatus.Idle)
								|| elevatorInfo.direction().equals(Direction.Down)) {
							//Logger.debugln("Ignored");
							for (FieldListener<I> e: views) {
								e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Ignored" + "\n"));
								e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
							}
							continue;
						}

						//Logger.debugln("Step 1");
						for (FieldListener<I> e: views) {
							e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Step 1" + "\n"));
							e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
						}
						if (upRequest.srcFloor() > elevatorInfo.currentFloor()) {
							//Logger.debugln("Step 2");
							for (FieldListener<I> e: views) {
								e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Step 2" + "\n"));
								e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
							}
							if (closestEntry.isPresent()) {
								//Logger.debugln("Step 3");
								for (FieldListener<I> e: views) {
									e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Step 3" + "\n"));
									e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
								}
								var previous = closestEntry.get();
								if (previous.second().currentFloor() > elevatorInfo.currentFloor()) {
									closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
								}
							} else {
								closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
							}
						}
					}
					
				}

				if (closestEntry.isPresent()) {
					var newRequest = closestEntry.get();
					var channelId = newRequest.first();
					var elevatorInfo = newRequest.second();

					// Found idle elevator. Send request!
					//Logger.println("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString()
						//	+ " (up append)");
					for (FieldListener<I> e: views) {
						e.updateSchedulerFieldArea("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString()
							+ " (up append)" + "\n");
						e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
					}
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
				//Logger.debugln("Down request " + downRequest.toString());
				for (FieldListener e: views) {
					e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Down request " + downRequest.toString() + "\n"));
				}
				// Find the closest elevator that is going in the right direction
				Optional<Pair<I, ElevatorResponse>> closestEntry = Optional.empty();
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var elevatorInfo = entry.getValue();

					// Elevators that are going up should be sent requests that go down
					if (!elevatorInfo.state().equals(ElevatorStatus.ShutDown)) {
						if (elevatorInfo.state().equals(ElevatorStatus.Idle)
								|| elevatorInfo.direction().equals(Direction.Up)) {
							//Logger.debugln("Ignored");
							for (FieldListener<I> e: views) {
								e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Ignored" + "\n"));
								e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
							}
							continue;
						}
	
						//Logger.debugln("Step 1");
						for (FieldListener<I> e: views) {
							e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Step 1" + "\n"));
							e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
						}
						if (downRequest.srcFloor() < elevatorInfo.currentFloor()) {
							//Logger.debugln("Step 2");
							for (FieldListener<I> e: views) {
								e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Step 2" + "\n"));
								e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
							}
							if (closestEntry.isPresent()) {
								//Logger.debugln("Step 3");
								for (FieldListener<I> e: views) {
									e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Step 3" + "\n"));
									e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
								}
								var previous = closestEntry.get();
								if (previous.second().currentFloor() < elevatorInfo.currentFloor()) {
									closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
								}
							} else {
								closestEntry = Optional.of(new Pair<>(channelId, elevatorInfo));
							}
						}
					}
				}

				if (closestEntry.isPresent()) {
					var newRequest = closestEntry.get();
					var channelId = newRequest.first();
					var elevatorInfo = newRequest.second();

					// Found idle elevator. Send request!
					//Logger.println("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString()
							//+ " (down append)");
					for (FieldListener<I> e: views) {
						e.updateSchedulerFieldArea(String.format("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString()
						+ " (down append)" + "\n"));
						e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
					}
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
				//Logger.debugln("Queue size: " + this.requestQueue.size());
				for (FieldListener e: views) {
					e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(),"Queue size: " + this.requestQueue.size() + "\n"));
				}
				boolean foundElement = false;
				for (var entry : this.elevatorStateCache.entrySet()) {
					var channelId = entry.getKey();
					var elevatorInfo = entry.getValue();
					//Logger.debugln("Entry " + channelId + " " + elevatorInfo);
					for (FieldListener<I> e: views) {
						e.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(),"Entry " + channelId + " " + elevatorInfo + "\n"));
						e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
					}
					if (!elevatorInfo.state().equals(ElevatorStatus.ShutDown)) {
						if (elevatorInfo.state().equals(ElevatorStatus.Idle)) {
							// Found idle elevator. Send request!
							//Logger.println("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString());
							for (FieldListener<I> e: views) {
								e.updateSchedulerFieldArea("Goto:  Sending to " + channelId + " with state " + elevatorInfo.toString() + "\n");
								e.updateElevatorFieldArea(channelId, String.valueOf(elevatorInfo.currentFloor()), String.valueOf(elevatorInfo.direction()), String.valueOf(elevatorInfo.state()));
							}
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
						TaggedMsg<R, FloorEvent> event = floorMux.take();
						FloorEvent e = event.content();
						//Logger.debugln("Got " + event.toString());
						for (FieldListener view: views) {
							view.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Got " + event.toString() + "\n"));
						}
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
		
		//Logger.println("Scheduler initialized");
		for (FieldListener<I> e: views) {
			e.updateSchedulerFieldArea("Scheduler initialized" + "\n");
		}

		while (true) {
			try {
				TaggedMsg<I, ElevatorResponse> event = elevatorMux.take();
				ElevatorResponse response = event.content();
				//Logger.debugln("Got " + event.toString());
				for (FieldListener<I> view: views) {
					view.updateSchedulerFieldArea(String.format("%18s: -%s",Thread.currentThread().getName(), "Got " + event.toString() + "\n"));
					view.updateElevatorFieldArea(event.id(), String.valueOf(response.currentFloor()), String.valueOf(response.direction()), String.valueOf(response.state()));
				}
				this.elevatorStateCache.put(event.id(), response);

				trySendElevatorGoto();
			} catch (InterruptedException e) {
				break;
			}
		}

		//Logger.println("Scheduler interrupted");
		for (FieldListener e: views) {
			e.updateSchedulerFieldArea("Scheduler interrupted" + "\n");
		}
		t.interrupt();
	}

}
