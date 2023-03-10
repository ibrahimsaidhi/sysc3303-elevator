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

	private BlockingReceiver<FloorEvent> floorToSchedulerQueue;
	private BlockingReceiver<Message> elevatorToSchedulerQueue;

	private BlockingSender<Message> schedulerToFloorQueue;
	private BlockingSender<FloorEvent> schedulerToElevatorQueue;
	
	private SchedulerState state;
	//private FloorEvent event;
	//private Message message;
	private ManyBlockingReceiver<FloorEvent> floorReceiver;
	private ManyBlockingReceiver<Message> elevatorReceiver;
	

	public Scheduler(
			List<Pair <BlockingSender<Message>, BlockingReceiver<FloorEvent>>> elevators,
			List<Pair <BlockingSender<FloorEvent>, BlockingReceiver<Message>>> floors,
			BlockingReceiver<Message> elevatorToSchedulerQueue,
			BlockingSender<Message> schedulerToFloorQueue,
			BlockingReceiver<FloorEvent> floorToSchedulerQueue,
			BlockingSender<FloorEvent> schedulerToElevatorQueue
			
			
	) {

		List<Pair <Integer, BlockingReceiver<FloorEvent>>> elevatorList = new ArrayList<>();
		List<Pair <Integer, BlockingReceiver<Message>>> floorList = new ArrayList<>();
		for (var e: elevators) {
			elevatorList.add(new Pair<>(0, e.second()));
			//TODO: Integer is not necessarily needed
		}
	
		this.floorToSchedulerQueue = floorToSchedulerQueue;
		this.elevatorToSchedulerQueue = elevatorToSchedulerQueue;
		this.schedulerToFloorQueue = schedulerToFloorQueue;
		this.schedulerToElevatorQueue = schedulerToElevatorQueue;
		this.floorReceiver = new ManyBlockingReceiver<FloorEvent>(elevatorList);
		this.elevatorReceiver = new ManyBlockingReceiver<Message>(floorList);
		this.state = new FloorListeningState(this);
		
	}
	


	public void setState(SchedulerState state) {
		this.state = state;
	}


	
	

	@Override
	public void run() {
		Thread floorThread = new Thread(floorReceiver);
		
		Thread elevatorThread = new Thread(elevatorReceiver);
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Pair <Integer, FloorEvent> event = floorReceiver.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
		});
		floorThread.start();
		elevatorThread.start();
		/*
		while (true) {
			
			state.dealWithMessage();
			

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}

			



		}
		*/

	}


}
