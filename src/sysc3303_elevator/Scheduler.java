/**
 * 
 */
package sysc3303_elevator;

import java.util.concurrent.BlockingQueue;

/**
 * @author Ibrahim Said
 *
 */
public class Scheduler implements Runnable{

	private FloorEvent floorSystem;
	private Elevator elevator;
	private BlockingQueue<Object> queue;
	
	public Object floorNumber = null;
	public Object elevatorNumber = null;
	public Object time = null;
	public Object button = null;
	
	
	public Scheduler(FloorEvent floorEvent, Elevator elevator, BlockingQueue queue) {
		this.elevator = elevator;
		this.floorEvent = floorEvent;
		this.queue = queue;
	}
	
	/**
	 * scheduler takes inputs from floor system
	 * @param item1
	 * @param item2
	 * @param item3
	 * @param item4
	 
	
	/**
	 * read data sent by elevator -- could return data in form of data structure
	 */
	public Object readDataFromElevator() {
		// TODO 
	}
	
	*/
	/**
	 * read data sent by elevator -- could return data in form of data structure
	 */
	public Object readDataFromFloor() {
		// TODO 
	}
	
	/**
	 * sends data received from elevator to floor system
	 */
	public void sendElevatorDataToFloorSystem(Object item) {
		queue.put(item);
	}
	
	@Override
	public void run() {
		
		Object data = readDataFromElevator();
		
		
		sendElevatorDataToFloorSystem(data);
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {}
		
	}
	

}
