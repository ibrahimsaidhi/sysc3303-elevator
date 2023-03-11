package sysc3303_elevator.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import sysc3303_elevator.ElevatorResponse;
import sysc3303_elevator.ElevatorStatus;
import sysc3303_elevator.FloorEvent;
import sysc3303_elevator.Message;
import sysc3303_elevator.Pair;
import sysc3303_elevator.Scheduler;
import sysc3303_elevator.networking.BlockingReceiver;
import sysc3303_elevator.networking.BlockingSender;

class SchedulerTest {

	@Test
	void test() throws Throwable {
		var event1 = new FloorEvent(null, 0, null, 0);
		var event2 = new FloorEvent(null, 1, null, 0);
		var msg1 = new ElevatorResponse(1, ElevatorStatus.Idle);
		var msg2 = new ElevatorResponse(4, ElevatorStatus.Idle);

		var inbound = new BlockingReceiver<FloorEvent>() {
			public int takeCount = 0;

			public FloorEvent take() throws InterruptedException {
				takeCount++;
				switch (takeCount - 1) {
					case 0: {
						return event1;
					}
					case 1: {
						return event2;
					}
					default:
						while (true) {
							Thread.sleep(1000);
						}
				}

			};
		};

		var outbound = new BlockingSender<FloorEvent>() {
			public int count = 0;

			@Override
			public void put(FloorEvent e) throws InterruptedException {
				count++;
			}
		};

		var inboundResponse = new BlockingReceiver<ElevatorResponse>() {
			public int takeCount = 0;

			public ElevatorResponse take() throws InterruptedException {
				takeCount++;
				switch (takeCount - 1) {
					case 0: {
						return msg1;
					}
					case 1: {
						return msg2;
					}
					default:
						while (true) {
							Thread.sleep(1000);
						}
				}

			};
		};

		var outboundResponse = new BlockingSender<Message>() {
			public int count = 0;

			@Override
			public void put(Message e) throws InterruptedException {
				count++;
			}
		};

		var elevators = new ArrayList<Pair<BlockingSender<FloorEvent>, BlockingReceiver<ElevatorResponse>>>();
		elevators.add(new Pair<>(outbound, inboundResponse));
		var floors = new ArrayList<Pair<BlockingSender<Message>, BlockingReceiver<FloorEvent>>>();
		floors.add(new Pair<>(outboundResponse, inbound));

		var e1 = new Scheduler(
				elevators,
				floors
		);

		var t1 = new Thread(e1);


		t1.start();
		while (inbound.takeCount < 3) {Thread.sleep(100);}
		t1.interrupt();
		t1.join();

		assertEquals(3, inbound.takeCount);
		assertEquals(1, outbound.count);
		assertEquals(3, inboundResponse.takeCount);
		assertEquals(2, outboundResponse.count);
	}

}
