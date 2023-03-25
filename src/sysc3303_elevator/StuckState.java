package sysc3303_elevator;

public class StuckState implements ElevatorState{
	public StuckState(Elevator elevator) {
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
		}else {
			elevator.setState(new ShutDownState(elevator));
		}
	}
}
