/**
 * 
 */
package sysc3303_elevator;

public class IdleState implements ElevatorState {
    @Override
    public void advance(Elevator elevator) {
        // Do nothing, elevator is idle
    }
}
