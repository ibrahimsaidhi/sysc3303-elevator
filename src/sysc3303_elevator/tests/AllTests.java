package sysc3303_elevator.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	ElevatorTest.class,
	FloorFormatReaderTest.class,
	FloorTest.class,
	ManyBlockingReceiverTest.class,
	SchedulerTest.class,
	UdpQueueTest.class,
})
public class AllTests {

}
