package sysc3303_elevator;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ ElevatorTest.class, FloorFormatReaderTest.class, SchedulerTest.class })
public class AllTests {

}
