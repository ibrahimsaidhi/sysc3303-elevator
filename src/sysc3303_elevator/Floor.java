package sysc3303_elevator;

import java.util.ArrayList;

import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;

/**
 *
 * Floor class is used to send valid floor events and send them to the schedular
 * @author Hamza Zafar 101119026
 * @version 1.0
 *
 */

public class Floor implements Runnable {

	private BlockingSender<FloorEvent> floorToScheduler;
	private BlockingReceiver<Message> schedulerToFloor;
	private ArrayList<FloorEvent> eventList;

	/**
	 * Constructer class for Floor
	 * @param currentFloorNum Permanent floor number
	 * @param floorToSchedular queue of floorEvents being sent from the Floor to the schedular
	 * @param schedulerToFloor queue of messages being sent from the schedular to the Floor
	 * @param eventList List of events that that have to be validated and passed along
	 */

	public Floor(BlockingSender<FloorEvent> floorToSchedular, BlockingReceiver<Message> schedulerToFloor, ArrayList<FloorEvent> eventList) {
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

	public boolean validateRequest(Direction direction, int newFloorNum, int currentFloorNum) {
		return ((direction == Direction.Up) && (currentFloorNum < newFloorNum)) || ((direction == Direction.Down) && (currentFloorNum > newFloorNum));
	}
	/**
	 * floorToScheduler
	 * this method is used to pass floorEvents too queue
	 * @param floorevent
	 */
	public void floorToScheduler(FloorEvent floorevent) {
		if(validateRequest(floorevent.direction(), floorevent.carButton(), floorevent.floor())) {
			try {
				Logger.println(String.format("Requesting elevator '%s'", floorevent.toString()));
				floorToScheduler.put(floorevent);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Logger.println(String.format("Invalid floor event '%s'", floorevent));
		}

	}
	/**
	 * recieveMessage
	 * return a message from the schedular
	 */
	public void recieveMessage() {
		try {
			var msg = schedulerToFloor.take();
			Logger.debugln(String.format("Got '%s'", msg));
		} catch (InterruptedException e) {
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
