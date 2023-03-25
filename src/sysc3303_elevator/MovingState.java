package sysc3303_elevator;

public class MovingState implements ElevatorState {
	public MovingState(Elevator elevator) {
		elevator.setStatus(ElevatorStatus.Moving);
	}

	@Override
	public void advance(Elevator elevator) throws InterruptedException {
		var queue = elevator.getDestinationFloors();
		int destinationFloor = queue.peek().get();

		// Start moving
		elevator.setMoving(true);
		Logger.debugln("Elevator doors are " + elevator.getDoorState() + ", motor is ON. Car button " + destinationFloor
				+ " lamp is " + elevator.getButtonLampStates()[destinationFloor]
				+ " Elevator is moving " + elevator.getDirection());
		while (queue.getCurrentFloor() != destinationFloor) {
			var response = new ElevatorResponse(queue.getCurrentFloor(), elevator.getStatus(), elevator.getDirection());
			elevator.notifyObservers(response);
			
			
			
			Thread.sleep(elevator.getTIME_BTW_FLOORS());
			queue.advance();
			Logger.println("Floor: " + queue.getCurrentFloor());
			
			elevator.checkAndDealWIthFaults();
		}
		var response = new ElevatorResponse(queue.getCurrentFloor(), elevator.getStatus(), elevator.getDirection());
		elevator.notifyObservers(response);
		elevator.getButtonLampStates()[queue.peek().get()] = ButtonLampState.OFF;
		Logger.debugln("Elevator reached destination floor: " + destinationFloor + ". Car button lamp is "
				+ elevator.getButtonLampStates()[destinationFloor]
				+ ". Motor is OFF. Elevator is not moving... Opening doors");
		queue.next();
		elevator.setMotorOn(false);
		elevator.setMoving(false);
		elevator.setDoorState(DoorState.OPEN);

		elevator.setState(new DoorOpenState(elevator));
	}
}