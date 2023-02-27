package sysc3303_elevator;

public class DoorClosedState implements ElevatorState {
	@Override
	public void advance(Elevator elevator) {
		if (elevator.getDestinationFloors().isEmpty()) {
			elevator.setState(new IdleState());
			
			Message message = new Message("Processing FloorEvent : Done");
			elevator.notifyObservers(message);
			return;
		}
		if (elevator.getCurrentFloor() != elevator.getDestinationFloors().get(0)) {
			elevator.setState(new MovingState());
		} else {
			Logger.println("Opening doors");
			elevator.setDoorState(DoorState.OPEN);
			elevator.getDestinationFloors().remove(0);
			elevator.setState(new DoorOpenState());
		}
	}
}