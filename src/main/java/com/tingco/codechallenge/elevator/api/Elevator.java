package com.tingco.codechallenge.elevator.api;

/**
 * Interface for an elevator object.
 *
 * @author Sven Wesley
 *
 */
public interface Elevator extends Runnable{

    int getNrOfPassengers();

    boolean isRunning();

    /**
     * Enumeration for describing elevator's direction.
     */
    enum Direction {
        UP, DOWN, NONE
    }

    /**
     * Tells which direction is the elevator going in.
     *
     * @return Direction Enumeration value describing the direction.
     */
    Direction getDirection();

    /**
     * If the elevator is moving. This is the target floor.
     *
     * @return primitive integer number of floor
     */
    int getAddressedFloor();

    /**
     * Get the Id of this elevator.
     *
     * @return primitive integer representing the elevator.
     */
    int getId();

    /**
     * Command to move the elevator to the given floor.
     *
     * @param toFloor
     *            int where to go.
     */
    void moveElevator(int toFloor);

    /**
     * Calculates the time for a elevator to reach a certain floor
     * @param toFloor the target floot
     * @param direction
     * @return time in milliseconds
     */
    long calculateTimeToFloor(int toFloor, Direction direction);

    /**
     * Moves the elevator to next floor
     * @return the next floor
     */
    int moveToNextFloor();

    /**
     * Check if the elevator is occupied at the moment.
     *
     * @return true if busy.
     */
    boolean isBusy();

    /**
     * Reports which floor the elevator is at right now.
     *
     * @return int actual floor at the moment.
     */
    int currentFloor();

    /**
     * Ads a listener to elevator events
     * @param listener a ElevatorListener
     */
    void addElevatorListener(ElevatorListener listener);

    /**
     * Removes a listener to elevator events
     * @param listener a ElevatorListener
     */
    void removeElevatorListener(ElevatorListener listener);

    /**
     * Leaves the elevator on current floor
     */
    void leave();

    /**
     * Enters the elevator at current floor and press target floor
     * @param toFloor the target floor
     */
    void enter(int toFloor);

    /**
     * Starts elevator
     */
    void start();

    /**
     * Stops elevator
     */
    void stop();

}
