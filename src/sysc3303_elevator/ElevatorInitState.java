
package sysc3303_elevator;

public class ElevatorInitState implements ElevatorState {
    @Override
    public void advance(Elevator elevator) {
        var response = new ElevatorResponse(elevator.getCurrentFloor(), ElevatorStatus.Idle);
        elevator.notifyObservers(response);

        elevator.setState(new IdleState());
    }
}
