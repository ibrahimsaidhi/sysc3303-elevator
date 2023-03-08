package sysc3303_elevator.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ ElevatorTest.class, FloorFormatReaderTest.class, SchedulerTest.class, MainTest.class, FloorTest.class })
public class AllTests {

}
