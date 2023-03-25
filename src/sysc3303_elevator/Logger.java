package sysc3303_elevator;

public class Logger {
	private static final boolean DEBUG = false;

	public synchronized static void debugln(String msg) {
		if (DEBUG) {
			System.out.println(String.format("%18s: -%s",Thread.currentThread().getName(), msg));
		}
	}

	public synchronized static void println(String msg) {
		System.out.println(String.format("%18s: %s",Thread.currentThread().getName(), msg));
	}
}
