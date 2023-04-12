package sysc3303_elevator;

public class Logger {
	private static boolean shouldDebugLog = false;
	private static boolean shouldPerformanceLog = false;

	public synchronized static void debugln(String msg) {
		if (shouldPerformanceLog) {
			return;
		}
		if (shouldDebugLog) {
			System.out.println(String.format("%18s: -%s", Thread.currentThread().getName(), msg));
		}
	}

	public synchronized static void println(String msg) {
		if (shouldPerformanceLog) {
			return;
		}
		System.out.println(String.format("%18s: %s", Thread.currentThread().getName(), msg));
	}

	public synchronized static void outputPerf(String msg) {
		if (shouldPerformanceLog) {
			System.out.println(msg);
		}
	}

	public synchronized static void setDebug(boolean debug) {
		shouldDebugLog = debug;
	}

	public synchronized static void setPerf(boolean performance) {
		shouldPerformanceLog = performance;
	}
}
