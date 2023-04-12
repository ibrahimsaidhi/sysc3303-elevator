package sysc3303_elevator;



public class Measurements implements Runnable {
    private long startTime;
    private long endTime;
    private boolean running;
    private ElevatorSubsystem es;
    private Floor floor;

    Measurements(ElevatorSubsystem es, Floor floor) {
        running = false;
        this.floor = floor;
        this.es = es;
    }

    // Start timer and set the running flag to true
    public synchronized void startTimer() {
        startTime = System.currentTimeMillis();
        running = true;
    }

    // Stop timer and set the running flag to false
    public synchronized void stopTimer() {
        endTime = System.currentTimeMillis();
        running = false;
    }

    // Calculate and return elapsed time in milliseconds 
    public synchronized long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        } else {
            return endTime - startTime;
        }
    }

    // Implement the run() method
    public void run() {
    	startTimer();
        while(true) {
        	if((floor.getEventList().size() == 0) && (es.getElevator().getDestinationFloors().getSize() == 0)){
        		System.out.println(getElapsedTime());}
        	break;
        }
    }
}


