package sysc3303_elevator;

public class ElevatorListeningState extends SchedulerState {

	public ElevatorListeningState(Scheduler scheduler) {
		super(scheduler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dealWithMessage() {
		scheduler.setState(new ElevatorSendingState(scheduler));
		scheduler.listenToElevator();
	}

}
