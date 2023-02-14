package sysc3303_elevator;

public class FloorListeningState extends SchedulerState {

	public FloorListeningState(Scheduler scheduler) {
		super(scheduler);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void dealWithMessage() {
		scheduler.setState(new ElevatorSendingState(scheduler));
	}

}
