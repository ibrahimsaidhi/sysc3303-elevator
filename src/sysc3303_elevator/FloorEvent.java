package sysc3303_elevator;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * @author Quinn Parrott
 *
 */
public record FloorEvent(LocalTime time, int floor, Direction direction, int carButton)implements Serializable { }
