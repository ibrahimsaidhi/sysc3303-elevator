package sysc3303_elevator;

public class DoorOpenState implements ElevatorState {
	@Override
	public void advance(Elevator elevator) {
		Logger.println("Closing doors");
		elevator.setDoorState(DoorState.CLOSED);

		elevator.setState(new DoorClosedState());
	}
}
