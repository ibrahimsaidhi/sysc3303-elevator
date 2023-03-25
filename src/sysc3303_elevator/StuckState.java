package sysc3303_elevator;

public class StuckState implements ElevatorState{
	public StuckState(Elevator elevator) {
		
		Logger.println("ALERT!!! Elevator Stuck");
		
		if(elevator.isdoorStuck()) {
			elevator.setStatus(ElevatorStatus.DoorStuck);
		}else {
			elevator.setStatus(ElevatorStatus.StuckBtwFloors);
		}
	}

	@Override
	public void advance(Elevator elevator) throws InterruptedException {		
		if(elevator.isdoorStuck()) {
			elevator.setState(new DoorClosedState(elevator));
			elevator.setdoorStuck(false);
		}else {
			elevator.setState(new ShutDownState(elevator));
		}
	}
}
