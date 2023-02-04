package sysc3303_elevator;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * Floor class is used to send valid floor events and send them to the schedular
 * @author Hamza Zafar 101119026
 * @version 1.0
 *
 */

public class Floor extends Thread {
	
	final int currentFloorNum;
	private BlockingQueue<FloorEvent> floorToScheduler;
	private BlockingQueue<Message> schedulerToFloor;
	private ArrayList<FloorEvent> eventList;
	
	/**
	 * Constructer class for Floor
	 * @param currentFloorNum Permanent floor number
	 * @param floorToSchedular queue of floorEvents being sent from the Floor to the schedular
	 * @param schedulerToFloor queue of messages being sent from the schedular to the Floor
	 * @param eventList List of events that that have to be validated and passed along 
	 */
	
	public Floor(int currentFloorNum,BlockingQueue<FloorEvent> floorToSchedular, BlockingQueue<Message> schedulerToFloor, ArrayList<FloorEvent> eventList) {
		this.currentFloorNum = currentFloorNum;
		this.floorToScheduler = floorToSchedular; 
		this.schedulerToFloor = schedulerToFloor;
		this.eventList = eventList; 
		
	}
	
	/**
	 * the method validateRequest is used to ensure only valid requests are sent to the queue
	 * @param direction direction the elevator is to go 
	 * @param newFloorNum floor number the passenger would like to go too
	 * @return boolean if valid or not
	 */
	
	public boolean validateRequest(Direction direction, int newFloorNum) {
		return((direction == Direction.Up) && (currentFloorNum > newFloorNum) || (direction == Direction.Down) && (currentFloorNum < newFloorNum));
	}
	/**
	 * floorToScheduler
	 * this method is used to pass floorEvents too queue
	 * @param floorevent
	 */
	public void floorToScheduler(FloorEvent floorevent) {
		if(validateRequest(floorevent.direction(), floorevent.floor())) {
			try {
				floorToScheduler.put(floorevent);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * recieveMessage
	 * return a message from the schedular
	 */
	public void recieveMessage() {
		try {
			schedulerToFloor.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true) {
			while(!eventList.isEmpty()) {
			floorToScheduler(eventList.remove(0));
			}
			recieveMessage();
		}
	}

}
