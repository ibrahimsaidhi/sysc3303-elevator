package sysc3303_elevator;

public class ShutDownState implements ElevatorState{

	public ShutDownState(Elevator elevator) {
		elevator.setStatus(ElevatorStatus.ShutDown);
	}
	@Override
	public void advance(Elevator elevator) throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
