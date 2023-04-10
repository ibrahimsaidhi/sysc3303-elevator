package sysc3303_elevator;

public class StuckState implements ElevatorState{
	public StuckState(Elevator elevator) {
		
		Logger.println("ALERT!!! Elevator Stuck");
		
		if(elevator.isdoorStuck()) {
			elevator.setStatus(ElevatorStatus.DoorStuck);
		}else {
			elevator.setStatus(ElevatorStatus.StuckBtwnFloors);
		}
	}

	@Override
	public void advance(Elevator elevator) throws InterruptedException {		
		if(elevator.isdoorStuck()) {
			elevator.setdoorStuck(false);
			elevator.setState(new DoorClosedState(elevator));
			return;
		}else {
			elevator.setState(new ShutDownState(elevator));
		}
	}
}
