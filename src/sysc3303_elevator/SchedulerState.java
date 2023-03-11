package sysc3303_elevator;

import java.io.Serializable;

public abstract class SchedulerState implements Serializable{
	protected Scheduler scheduler;
	public SchedulerState(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public abstract void dealWithMessage();
}
