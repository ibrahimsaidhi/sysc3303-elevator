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

	private ArrayBlockingQueue<Object> floorToSchedulerQueue;
	private ArrayBlockingQueue<Object> elevatorToSchedulerQueue;
	
	private ArrayBlockingQueue<Object> schedulerToFloorQueue;
	private ArrayBlockingQueue<Object> schedulerToElevatorQueue;
	
	public Scheduler(ArrayBlockingQueue<Object> elevatorToSchedulerQueue,ArrayBlockingQueue<Object> schedulerToFloorQueue, 
			ArrayBlockingQueue<Object> floorToSchedulerQueue, ArrayBlockingQueue<Object> schedulerToElevatorQueue) {
		
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
		List<Object> fillerList = new ArrayList<Object>();
		for (int i = 0; i < floorToSchedulerQueue.size(); i++) {
			try {
				fillerList.add(floorToSchedulerQueue.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (Object item: fillerList) {
			try {
				schedulerToElevatorQueue.put(item);
			} catch (InterruptedException e) {
				System.err.println(e);
			}
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
