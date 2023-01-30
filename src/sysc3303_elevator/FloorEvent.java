package sysc3303_elevator;

import java.time.LocalTime;

public record FloorEvent(LocalTime time, int floor, Direction direction, int carButton) { }
