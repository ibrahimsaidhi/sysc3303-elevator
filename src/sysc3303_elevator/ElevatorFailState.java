package sysc3303_elevator;

public class ElevatorFailState implements ElevatorState {
	@Override
	public void advance(Elevator elevator) {
		Logger.debugln("Elevator Failed");
		
		var response = new ElevatorResponse(elevator.getCurrentFloor(), ElevatorStatus.failed);
		elevator.notifyObservers(response);
		
		elevator.setState(new ElevatorShutdownState());
		return;

	}
}
