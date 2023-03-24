package sysc3303_elevator;

public class MovingState implements ElevatorState {
	@Override
	public void advance(Elevator elevator) throws InterruptedException {
		int destinationFloor = elevator.getDestinationFloors().get(0);

		// Start moving
		elevator.setMoving(true);
		Logger.debugln("Elevator doors are " + elevator.getDoorState() + ", motor is ON. Car button " + destinationFloor + " lamp is " + elevator.getButtonLampStates()[destinationFloor]
				+ " Elevator is moving " + elevator.getDirection());
		while (elevator.getCurrentFloor() != destinationFloor && elevator.getfialed()) {
			Thread.sleep(1000);
			int nextFloor = elevator.getCurrentFloor() + (elevator.getDirection() == Direction.Up ? 1 : -1);
			elevator.setCurrentFloor(nextFloor);
			Logger.println("Floor: " + nextFloor);
		}
		
		if(elevator.getfialed()) {
			elevator.setState(new ElevatorFailState());
			return;
		}
		
		elevator.getButtonLampStates()[elevator.getDestinationFloors().get(0)] = ButtonLampState.OFF;
		Logger.debugln("Elevator reached destination floor: " + destinationFloor + ". Car button lamp is " + elevator.getButtonLampStates()[destinationFloor]
				+ ". Motor is OFF. Elevator is not moving... Opening doors");
		elevator.getDestinationFloors().remove(0);
		elevator.setMotorOn(false);
		elevator.setMoving(false);
		elevator.setDoorState(DoorState.OPEN);

		elevator.setState(new DoorOpenState());
	}
}