package sysc3303_elevator;

public class DoorOpenState implements ElevatorState {
	public DoorOpenState(Elevator elevator) {
		elevator.setStatus(ElevatorStatus.DoorOpen);
	}

	@Override
	public void advance(Elevator elevator) throws InterruptedException {
		Logger.debugln("Closing doors");

		elevator.startTimer(ElevatorStatus.DoorOpen);

		Thread.sleep(elevator.getDOOR_CLOSING_TIME());

		if (elevator.checkAndDealWithFaults()) {
			return;
		}
		elevator.setDoorState(DoorState.CLOSED);

		elevator.setState(new DoorClosedState(elevator));
	}
}
