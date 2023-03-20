package sysc3303_elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ElevatorQueue {
    private ArrayList<Integer> queue;
    private int currentFloor;

    public ElevatorQueue(int currentFloor) {
        this.queue = new ArrayList<>();
        this.currentFloor = currentFloor;
    }

    public synchronized Direction getDirection() {
        return currentFloor < this.peek().orElse(currentFloor) ? Direction.Up : Direction.Down;
    }

    public synchronized void add(int floor) {
        var direction = this.getDirection();

        var destinations = new HashSet<>(this.queue);
        destinations.add(floor);
        var dest = new ArrayList<>(destinations);
        Collections.sort(dest);
        if (direction.equals(Direction.Down)) {
            Collections.reverse(dest);
        }

        // Bump some values to the back if not continguous in the same direction
        var bumped = new ArrayList<Integer>();
        while (dest.size() > 0) {
            var headValue = dest.get(0);
            if (direction.equals(Direction.Up) ? headValue < this.currentFloor : headValue > this.currentFloor) {
                bumped.add(dest.remove(0));
            } else {
                break;
            }
        }

        Collections.reverse(bumped);
        dest.addAll(bumped);


        this.queue = dest;
    }

    public synchronized Optional<Integer> peek() {
        if (this.queue.size() > 0) {
            return Optional.of(this.queue.get(0));
        }
        return Optional.empty();
    }

    public synchronized void next() {
        if (this.queue.size() > 0) {
            this.currentFloor = this.queue.remove(0);
        }
    }

    public synchronized int getCurrentFloor() {
        return this.currentFloor;
    }

    public synchronized List<Integer> getQueue() {
        return Collections.unmodifiableList(this.queue);
    }

    public synchronized void advance() {
        if (peek().isEmpty()) {
            return;
        }

        if (getDirection().equals(Direction.Up)) {
            this.currentFloor = Math.min(peek().get(), currentFloor + 1);
        } else {
            this.currentFloor = Math.max(peek().get(), currentFloor - 1);
        }
    }
}
