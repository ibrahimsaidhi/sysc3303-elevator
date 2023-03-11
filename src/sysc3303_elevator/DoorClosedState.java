package sysc3303_elevator;

public class DoorClosedState implements ElevatorState {
	@Override
	public void advance(Elevator elevator) {
		if (elevator.getDestinationFloors().isEmpty()) {
			elevator.setState(new IdleState());

			var response = new ElevatorResponse(elevator.getCurrentFloor(), ElevatorStatus.Idle);
			elevator.notifyObservers(response);
			return;
		}
		if (elevator.getCurrentFloor() != elevator.getDestinationFloors().get(0)) {
			elevator.setState(new MovingState());
			var direction = elevator.getDirection();
			var response = new ElevatorResponse(elevator.getCurrentFloor(), direction.equals(Direction.Up) ? ElevatorStatus.Up : ElevatorStatus.Down);
			elevator.notifyObservers(response);
		} else {
			Logger.println("Opening doors");
			elevator.setDoorState(DoorState.OPEN);
			elevator.getDestinationFloors().remove(0);
			elevator.setState(new DoorOpenState());
		}
	}
}