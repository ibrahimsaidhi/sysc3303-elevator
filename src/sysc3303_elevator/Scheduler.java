/**
 *
 */
package sysc3303_elevator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;
import sysc3303_elevator.networking.ManyBlockingReceiver;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler implements Runnable {

	private ManyBlockingReceiver<FloorEvent> floorReceiver;
	private ManyBlockingReceiver<ElevatorResponse> elevatorReceiver;

	private HashMap<Integer, Pair<Optional<ElevatorResponse>, BlockingSender<FloorEvent>>> elevators;
	private HashMap<Integer, BlockingSender<Message>> floors;

	private ArrayList<FloorEvent> requestQueue = new ArrayList<>();

	public Scheduler(
			List<Pair<BlockingSender<FloorEvent>, BlockingReceiver<ElevatorResponse>>> elevators,
			List<Pair<BlockingSender<Message>, BlockingReceiver<FloorEvent>>> floors
	) {

		List<Pair <Integer, BlockingReceiver<FloorEvent>>> floorList = new ArrayList<>();
		this.floors = new HashMap<>();
		int floor_i = 0;
		for (var f: floors) {
			floorList.add(new Pair<>(0, f.second()));
			this.floors.put(floor_i, f.first());
			floor_i += 1;
		}
		this.floorReceiver = new ManyBlockingReceiver<FloorEvent>(floorList);

		List<Pair<Integer, BlockingReceiver<ElevatorResponse>>> elevatorList = new ArrayList<>();
		this.elevators = new HashMap<>();
		int elevator_i = 0;
		for (var elevator: elevators) {
			elevatorList.add(new Pair<>(elevator_i, elevator.second()));
			this.elevators.put(elevator_i, new Pair<>(Optional.empty(), elevator.first()));
			elevator_i += 1;
		}
		this.elevatorReceiver = new ManyBlockingReceiver<ElevatorResponse>(elevatorList);

	}


	public void trySendElevatorGoto() throws InterruptedException {
		// TODO: Narrow down the sync block
		synchronized (this.requestQueue) {
			while (this.requestQueue.size() > 0) {
				boolean foundElement = false;
				for (var entry : this.elevators.entrySet()) {
					var status = entry.getValue();
					if (status.first().isPresent()) {
						var elevatorInfo = status.first().get();
						if (elevatorInfo.state().equals(ElevatorStatus.Idle)) {
							// Found idle elevator. Send request!
							Logger.println("Sending elevator goto" + elevatorInfo.toString());
							status.second().put(this.requestQueue.remove(0));

							// No longer know the status of the elevator
							this.elevators.put(entry.getKey(), new Pair<>(Optional.empty(), status.second()));

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
		Thread floorThread = new Thread(floorReceiver, "sche-floor");

		Thread elevatorThread = new Thread(elevatorReceiver, "sche-elev");

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Pair<Integer, FloorEvent> event = floorReceiver.take();
						FloorEvent e = event.second();
						Logger.debugln("Got " + event.toString());
						floors.get(event.first()).put(new Message("ack")); // TODO: Make this an actual message
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
		floorThread.start();
		elevatorThread.start();
		t.start();
		Logger.println("Scheduler initialized");

		while(true) {
			try {
				Pair<Integer, ElevatorResponse> event = elevatorReceiver.take();
				ElevatorResponse response = event.second();
				Logger.debugln("Got " + event.toString());
				var entry = this.elevators.get(event.first());
				var entryUpdated = new Pair<>(Optional.of(response), entry.second());
				this.elevators.put(event.first(), entryUpdated);

				trySendElevatorGoto();
			} catch (InterruptedException e) {
				break;
			}
		}

		Logger.println("Scheduler interrupted");

		floorThread.interrupt();
		elevatorThread.interrupt();
		t.interrupt();
	}


}
