/**
 *
 */
package sysc3303_elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;
import sysc3303_elevator.networking.ManyBlockingReceiver;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler implements Runnable {

	// private BlockingReceiver<FloorEvent> floorToSchedulerQueue;
	// private BlockingReceiver<Message> elevatorToSchedulerQueue;

	// private BlockingSender<Message> schedulerToFloorQueue;
	// private BlockingSender<FloorEvent> schedulerToElevatorQueue;

	private SchedulerState state; // TODO: Might not need?
	private ManyBlockingReceiver<FloorEvent> floorReceiver;
	private ManyBlockingReceiver<Message> elevatorReceiver;

	private HashMap<Integer, Pair<Optional<ElevatorInfo>, BlockingSender<FloorEvent>>> elevators;

	private ArrayList<FloorEvent> requestQueue = new ArrayList<>();


	public Scheduler(
			List<Pair<BlockingSender<FloorEvent>, BlockingReceiver<Message>>> elevators,
			List<Pair<BlockingSender<Message>, BlockingReceiver<FloorEvent>>> floors,
			// BlockingReceiver<Message> elevatorToSchedulerQueue,
			// BlockingSender<Message> schedulerToFloorQueue,
			// BlockingReceiver<FloorEvent> floorToSchedulerQueue,
			// BlockingSender<FloorEvent> schedulerToElevatorQueue
	) {

		List<Pair <Integer, BlockingReceiver<FloorEvent>>> elevatorList = new ArrayList<>();
		int elevator_i = 0;
		for (var elevator: elevators) {
			elevatorList.add(new Pair<>(elevator_i, elevator.second()));
			this.elevators.put(elevator_i, new Pair(Optional.empty(), elevator.first));
			elevator_i += 1;
		}

		List<Pair <Integer, BlockingReceiver<Message>>> floorList = new ArrayList<>();
		for (var f: floors) {
			elevatorList.add(new Pair<>(0, f.second()));
			//TODO: Integer is not necessarily needed
		}

		// this.floorToSchedulerQueue = floorToSchedulerQueue;
		// this.elevatorToSchedulerQueue = elevatorToSchedulerQueue;
		// this.schedulerToFloorQueue = schedulerToFloorQueue;
		// this.schedulerToElevatorQueue = schedulerToElevatorQueue;
		this.floorReceiver = new ManyBlockingReceiver<FloorEvent>(elevatorList);
		this.elevatorReceiver = new ManyBlockingReceiver<Message>(floorList);
		// this.state = new FloorListeningState(this);

	}

	// public void setState(SchedulerState state) {
	// 	// TODO: Might not need?
	// 	this.state = state;
	// }


	public void trySendElevatorGoto() {
		// TODO: Narrow down the sync block
		synchronized (this.requestQueue) {
			while (this.requestQueue.size() > 0) {
				boolean foundElement = false;
				for (var entry : this.elevators.entrySet()) {
					var status = entry.getValue();
					if (status.first().isPresent()) {
						var elevatorInfo = status.first().get();
						if (elevatorInfo.status().equals(ElevatorStatus.Idle)) {
							// Found idle elevator. Send request!
							status.second().put(this.requestQueue.remove(0));
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
		Thread floorThread = new Thread(floorReceiver);

		Thread elevatorThread = new Thread(elevatorReceiver);

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Pair <Integer, FloorEvent> event = floorReceiver.take();
						FloorEvent e = event.second();
						synchronized (requestQueue) {
							requestQueue.add(e);
						}
						trySendElevatorGoto();
					} catch (InterruptedException e) {
						break;
					}
				}
			}

		});
		floorThread.start();
		elevatorThread.start();
		t.start();

		while(true) {
			try {
				Pair<Integer, Message> event = elevatorReceiver.take();
				Message e = event.second();
				trySendElevatorGoto();
			} catch (InterruptedException e) {
				break;
			}
		}

		floorThread.interrupt();
		elevatorThread.interrupt();
		t.interrupt();
	}


}
