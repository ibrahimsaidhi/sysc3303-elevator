package sysc3303_elevator;

public record ElevatorSettings(
        int numberOfFloors,
        int betweenFloorsMs, // = 7383; // milliseconds
        int betweenFloorsThresholdOffsetMs, // = 200; // maximum time for moving between floors or closing door in
        int doorOpeningClosingMs, // = 1500; // milliseconds
        int doorOpeningClosingThresholdOffsetMs, // = 200;
        int loadUnloadMs // = 6483; // milliseconds
) {
}
