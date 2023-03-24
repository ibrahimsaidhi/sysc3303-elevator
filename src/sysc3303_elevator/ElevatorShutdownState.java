package sysc3303_elevator;

public class ElevatorShutdownState implements ElevatorState {
    @Override
    public void advance(Elevator elevator) {
        // Do nothing, elevator is shutdown
    }
}