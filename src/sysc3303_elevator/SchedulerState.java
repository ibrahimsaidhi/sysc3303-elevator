package sysc3303_elevator;

public abstract class SchedulerState {
	protected Scheduler scheduler;
	public SchedulerState(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public abstract void dealWithMessage();
}
