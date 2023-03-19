package sysc3303_elevator;

public class DoorClosedState implements ElevatorState {
	public DoorClosedState(Elevator elevator) {
		elevator.setStatus(ElevatorStatus.DoorClose);
	}

	@Override
	public void advance(Elevator elevator) {
		if (elevator.getDestinationFloors().isEmpty()) {
			elevator.setState(new IdleState(elevator));

			var response = new ElevatorResponse(elevator.getCurrentFloor(), elevator.getStatus());
			elevator.notifyObservers(response);
			return;
		}
		if (elevator.getCurrentFloor() != elevator.getDestinationFloors().get(0)) {
			elevator.setState(new MovingState(elevator));
			var response = new ElevatorResponse(elevator.getCurrentFloor(), elevator.getStatus());
			elevator.notifyObservers(response);
		} else {
			elevator.getButtonLampStates()[elevator.getDestinationFloors().get(0)] = ButtonLampState.OFF;
			Logger.debugln("Opening doors");
			elevator.setDoorState(DoorState.OPEN);
			elevator.getDestinationFloors().remove(0);
			elevator.setState(new DoorOpenState(elevator));
		}
	}
}