/**
 * 
 */
package sysc3303_elevator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;


/**
 * @author liful
 *
 */
public class Elevator extends Thread {
	
	private Direction direction;
	public boolean moving;
	private int currentFloor;
	private ElevatorDoorState doorState;
	private String status;
	
	private List<Integer> requestedFloors;
	private HashMap<Integer, String> lamps; //<Buttonlamp, on/of >
	private BlockingQueue<FloorEvent> queue;
	
	
	/**
	 * 
	 */
	public Elevator(BlockingQueue<FloorEvent> schedulerToElevatorQueue) {
		
		this.doorState = ElevatorDoorState.closed;
		this.direction = null;
		this.moving = false;
		this.setCurrentFloor(0); //Main Floor
		this.lamps = new HashMap<>();
		this.requestedFloors = new ArrayList<>();
		this.queue = schedulerToElevatorQueue;
		
	}
	
		public void run() {
			while (true) {
		        try {
		          FloorEvent message = this.queue.take();		   
		          System.out.println("Elevator received message from scheduler: " + message);
		          
		          processMessage(message);
		          Thread.sleep(1000);

		          System.out.println("Elevator sending out message: " + message);
		          queue.put(message); //Returning same message since I'm limited by the FloorEvent format while using the same queue. TO DO; Return a suitable response 
		          
		        } catch (InterruptedException e) {
		          System.out.println("Elevator Thread interrupted");
		        }
		      }
		}

		

	private void processMessage(FloorEvent message) {
		
		//Assumption; processing one message(FloorEvent) at a time
		requestedFloors.add(message.floor());
		
		requestedFloors.add(message.carButton());
		this.lamps.put(message.carButton(), "OFF");
		
		processDestinations(requestedFloors);
		
		}
	
	
	private void processDestinations(List<Integer> destinations) {
		//TO DO process floors requests differently from car button requests
		if(!destinations.isEmpty()) {
			
			for(int destinationfloor: destinations){
				
				if(this.lamps.containsKey(destinationfloor)) {
					this.lamps.put(destinationfloor, "ON");
				}
				
				this.moving = true;
				
				if(this.getCurrentFloor() < destinationfloor) {
					System.out.println("Elevator going up to floor: " + destinationfloor);
					this.setDirection(Direction.Up);
					
				}else{
					System.out.println("Elevator going down to floor: " + destinationfloor);
					this.setDirection(Direction.Down);
					
				}
				
				this.setCurrentFloor(destinationfloor);
				this.moving = false;
				System.out.println("Elevator reached floor: " + destinationfloor + ".  Opening doors");
				this.doorState = ElevatorDoorState.opened;
				
				
				System.out.println("Closing doors");
				this.doorState = ElevatorDoorState.closed;
				
				if(this.lamps.containsKey(destinationfloor)) {
					this.lamps.put(destinationfloor, "OFF");
					this.lamps.remove(destinationfloor);
				}
				
			}
		}
	}

	/**
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}
	
	
	/**
	 * @param direction the direction to set
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	/**
	 * @return the status
	 */
	public String getElevatorStatus() {
		return status;
	}


	/**
	 * @return the currentFloor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}


	/**
	 * @param currentFloor the currentFloor to set
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}

}
