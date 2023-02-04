package sysc3303_elevator;

public class Logger {

	public synchronized static void println(String msg) {
		System.out.println(String.format("%16s: %s",Thread.currentThread().getName(), msg));
	}
}
