/**
 * 
 */
package sysc3303_elevator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler implements Runnable{

	private BlockingQueue<FloorEvent> floorToSchedulerQueue;
	private BlockingQueue<Object> elevatorToSchedulerQueue;
	
	private BlockingQueue<Object> schedulerToFloorQueue;
	private BlockingQueue<Object> schedulerToElevatorQueue;
	
	public Scheduler(BlockingQueue<Object> elevatorToSchedulerQueue, BlockingQueue<Object> schedulerToFloorQueue, 
			BlockingQueue<FloorEvent> floorToSchedulerQueue, BlockingQueue<Object> schedulerToElevatorQueue) {
		
		this.floorToSchedulerQueue = floorToSchedulerQueue;
		this.elevatorToSchedulerQueue = elevatorToSchedulerQueue;
		this.schedulerToFloorQueue = schedulerToFloorQueue;
		this.schedulerToElevatorQueue = schedulerToElevatorQueue;
	}
	
	
	/**
	 * read data sent by elevator -- could return data in form of data structure
	 
	public Object readDataFromElevator() {
		// TODO 
	}
	*/

	
	/**
	 * read data sent by floor and add to elevator queue
	 */
	public void readDataFromFloorToElevator() {
		try {
			FloorEvent event = floorToSchedulerQueue.take();
			System.out.println("Event received from Floor. Sending to elevator...");
			
			schedulerToElevatorQueue.put(event);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
		
	}
	
	/**
	 * sends data received from elevator to floor system
	 */
	public void sendElevatorDataToFloorSystem() {
		
	}
	
	@Override
	public void run() {
		while (true) {
			readDataFromFloorToElevator();
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			
			sendElevatorDataToFloorSystem();
			
			
			
		}
		
	}
	

}
