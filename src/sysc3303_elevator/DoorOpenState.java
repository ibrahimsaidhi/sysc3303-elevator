package sysc3303_elevator;

public class DoorOpenState implements ElevatorState {
	public DoorOpenState(Elevator elevator) {
		elevator.setStatus(ElevatorStatus.DoorOpen);
	}

	@Override
	public void advance(Elevator elevator) throws InterruptedException {

		var queue = elevator.getDestinationFloors();
		var response = new ElevatorResponse(queue.getCurrentFloor(), elevator.getStatus(), elevator.getDirection());
		elevator.notifyObservers(response);

		Thread.sleep(elevator.getTimeLoadUnload()); //simulate loading or unloading an elevator

		Logger.debugln("Closing doors");

		elevator.startTimer(ElevatorStatus.DoorOpen);

		Thread.sleep(elevator.getTimeDoorOpenClose());

		if (elevator.checkAndDealWithFaults(ElevatorStatus.DoorOpen)) {
			return;
		}
		elevator.setDoorState(DoorState.CLOSED);

		elevator.setState(new DoorClosedState(elevator));
	}
}
