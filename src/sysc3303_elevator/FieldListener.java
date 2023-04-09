package sysc3303_elevator;

public interface FieldListener<I> {
	void updateSchedulerFieldArea(String str);
	void updateElevatorFieldArea(I channelId, String floor, String direction, String state);
	
}
