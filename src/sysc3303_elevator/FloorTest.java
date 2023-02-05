package sysc3303_elevator;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.Test;

/**
 * FloorTest
 * test if the floor class is correctly passingfloorevents to the scheduler 
 * @author Hamza
 * @version 1.0
 *
 */

public class FloorTest {
	/**
	 * testValidation1
	 * pass a valid floorEvent method validateRequest 
	 * @throws IOException
	 */
	@Test
	void testValidation1() throws IOException {
		BlockingQueue<FloorEvent> floorToScheduler = new ArrayBlockingQueue<>(5);
		BlockingQueue<Message> schedulerToFloor = new ArrayBlockingQueue<>(5);
		ArrayList<FloorEvent> eventList = new ArrayList<>();
		FloorEvent floorevent = new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4);
		eventList.add(floorevent);
		Floor floor = new Floor(2, floorToScheduler ,schedulerToFloor , eventList);
		Direction direction = eventList.get(0).direction();
		int newFloor = eventList.get(0).carButton();
		assertEquals(floor.validateRequest(direction, newFloor), true);
		
	}
	/**
	 * testValidation2
	 * pass a valid floorEvent method validateRequest 
	 * @throws IOException
	 */
	@Test
	void testValidation2() throws IOException {
		BlockingQueue<FloorEvent> floorToScheduler = new ArrayBlockingQueue<>(5);
		BlockingQueue<Message> schedulerToFloor = new ArrayBlockingQueue<>(5);
		ArrayList<FloorEvent> eventList = new ArrayList<>();
		FloorEvent floorevent = new FloorEvent(LocalTime.of(14, 5, 15, 0), 6, Direction.Up, 4);
		eventList.add(floorevent);
		Floor floor = new Floor(6, floorToScheduler ,schedulerToFloor , eventList);
		Direction direction = eventList.get(0).direction();
		int newFloor = eventList.get(0).carButton();
		assertEquals(floor.validateRequest(direction, newFloor), false);
		
	}
	@Test
	/**
	 * testQueuePassing1
	 * Test if floorToScheduler is sending the floorEvents to the Queue
	 * @throws IOException
	 */
	void testQueuePassing1() throws IOException {
		BlockingQueue<FloorEvent> floorToScheduler = new ArrayBlockingQueue<>(5);
		BlockingQueue<Message> schedulerToFloor = new ArrayBlockingQueue<>(5);
		ArrayList<FloorEvent> eventList = new ArrayList<>();
		FloorEvent floorevent = new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4);
		Floor floor = new Floor(2, floorToScheduler ,schedulerToFloor , eventList);
		floor.floorToScheduler(floorevent);
		assertEquals(floorToScheduler.isEmpty(), false);
		}
	@Test
	/**
	 * testQueuePassing2
	 * Test if floorToScheduler is sending the expected floorEvent to the queue
	 * @throws IOException
	 */
	void testQueuePassing2() throws IOException {
		BlockingQueue<FloorEvent> floorToScheduler = new ArrayBlockingQueue<>(5);
		BlockingQueue<Message> schedulerToFloor = new ArrayBlockingQueue<>(5);
		ArrayList<FloorEvent> eventList = new ArrayList<>();
		FloorEvent floorevent = new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4);
		FloorEvent floorevent2 = new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Down, 1);
		Floor floor = new Floor(2, floorToScheduler ,schedulerToFloor , eventList);
		floor.floorToScheduler(floorevent);
		floor.floorToScheduler(floorevent2);
		assertTrue(floorToScheduler.contains(floorevent));
		}
}
