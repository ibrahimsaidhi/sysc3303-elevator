package sysc3303_elevator;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

/**
 * cpuMeasurement class provides a method to get the CPU load of the current Java process.
 */
public class cpuMeasurement {

    /**
     * Retrieves the CPU load of the current Java process as a percentage value between 0 and 1.
     * A value of -1 indicates that the CPU load is not available.
     *
     * @return A double representing the CPU load of the current Java process.
     */
    public double getProcessCpuLoad() {
        // Obtain the OperatingSystemMXBean instance for the current Java process
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Get the process CPU load as a percentage value between 0 and 1
        double cpuLoad = operatingSystemMXBean.getProcessCpuLoad();

        // Return the CPU load value
        return cpuLoad;
    }
}