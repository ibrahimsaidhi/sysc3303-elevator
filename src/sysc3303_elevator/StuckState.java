package sysc3303_elevator;

public class StuckState implements ElevatorState {
	public StuckState(Elevator elevator) {

		Logger.println("ALERT!!! Elevator Stuck");

		if (elevator.isdoorStuck()) {
			elevator.setStatus(ElevatorStatus.DoorStuck);
		} else {
			elevator.setStatus(ElevatorStatus.StuckBtwFloors);
		}
	}

	@Override
	public void advance(Elevator elevator) throws InterruptedException {
		var queue = elevator.getDestinationFloors();
		
		if (elevator.isdoorStuck()) {
			elevator.setdoorStuck(false);
			if (elevator.getDoorState().equals(DoorState.CLOSED)) {

				elevator.setDoorState(DoorState.OPEN);
				elevator.setState(new DoorOpenState(elevator));

			} else {
				elevator.setDoorState(DoorState.CLOSED);
				elevator.setState(new DoorClosedState(elevator));
			}
			return;
		} else {
			elevator.setState(new ShutDownState(elevator));
			var response = new ElevatorResponse(queue.getCurrentFloor(), elevator.getStatus(), elevator.getDirection());
			elevator.notifyObservers(response);
			return;
		}
	}
}
