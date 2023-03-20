package sysc3303_elevator;

public class DoorClosedState implements ElevatorState {
	public DoorClosedState(Elevator elevator) {
		elevator.setStatus(ElevatorStatus.DoorClose);
	}

	@Override
	public void advance(Elevator elevator) {
		var queue = elevator.getDestinationFloors();
		if (queue.peek().isEmpty()) {
			elevator.setState(new IdleState(elevator));

			var response = new ElevatorResponse(queue.getCurrentFloor(), elevator.getStatus());
			elevator.notifyObservers(response);
			return;
		}
		if (queue.getCurrentFloor() != queue.peek().get()) {
			elevator.setState(new MovingState(elevator));
			var response = new ElevatorResponse(queue.getCurrentFloor(), elevator.getStatus());
			elevator.notifyObservers(response);
		} else {
			elevator.getButtonLampStates()[queue.peek().get()] = ButtonLampState.OFF;
			Logger.debugln("Opening doors");
			elevator.setDoorState(DoorState.OPEN);
			queue.next();
			elevator.setState(new DoorOpenState(elevator));
		}
	}
}