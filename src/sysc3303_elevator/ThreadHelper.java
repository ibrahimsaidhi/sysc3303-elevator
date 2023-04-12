package sysc3303_elevator;

public class ThreadHelper {

    static public enum ThreadOption {
        Start,
        Waiting
    }

    public static Thread runThreads(String name, Thread[] threads, ThreadOption option) {
        var t = new Thread(new Runnable() {

            @Override
            public void run() {
                for (var thread : threads) {
                    Logger.println(String.format("Starting '%s'", thread.getName()));
                    if (!thread.isAlive()) {
                        thread.start();
                    }
                }

                // Wait for all threads to exit
                for (var thread : threads) {
                    try {
                        thread.join();
                        Logger.println(String.format("Thread '%s' joined", thread.getName()));
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                for (var thread : threads) {
                    if (!thread.isInterrupted()) {
                        thread.interrupt();
                    }
                    Logger.println(String.format("Thread '%s' interrupted", thread.getName()));
                }
            }

        }, name);

        if (option.equals(ThreadOption.Start)) {
            t.start();
        }
        return t;
    }

}