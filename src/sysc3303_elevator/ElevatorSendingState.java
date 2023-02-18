package sysc3303_elevator;

public class ElevatorSendingState extends SchedulerState{

	public ElevatorSendingState(Scheduler scheduler) {
		super(scheduler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dealWithMessage() {
		scheduler.setState(new FloorListeningState(scheduler));
		scheduler.sendToFloor();
	}

}
