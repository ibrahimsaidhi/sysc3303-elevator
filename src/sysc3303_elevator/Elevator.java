/**
 * 
 */
package sysc3303_elevator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author tao
 *
 */
public class Elevator extends Thread {

	private Direction direction;
	public boolean moving;
	private int currentFloor;
	private List<Integer> requestedFloors;
	private String status;
	private HashMap<Integer, String> lamps = new HashMap<>();
	

	

}
