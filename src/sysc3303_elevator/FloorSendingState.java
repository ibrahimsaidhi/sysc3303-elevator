package sysc3303_elevator;

public class FloorSendingState extends SchedulerState{

	public FloorSendingState(Scheduler scheduler) {
		super(scheduler);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void dealWithMessage() {
		scheduler.setState(new FloorListeningState(scheduler));
	}

}
