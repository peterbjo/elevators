package com.tingco.codechallenge.elevator.domain;

import com.google.common.base.MoreObjects;
import com.tingco.codechallenge.elevator.api.Elevator;
import com.tingco.codechallenge.elevator.api.ElevatorListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A simulated passenger that uses the elevators
 *
 * @author Peter Björklund <mailto:peter.bjorklund@joors.com>
 * @since 1.0.0
 */
public class Passenger implements ElevatorListener {
    private static final Logger LOGGER = Logger.getLogger(Passenger.class.getName());

    private int id;
    private List<Integer> stops = new ArrayList<>();
    private boolean inElevator = false;
    private boolean waiting = false;

    public Passenger(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return id == passenger.id;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("inElevator", inElevator)
                .add("waiting", waiting)
                .toString();
    }

    /**
     * Returns true if a passenger is inside an elevator
     * @return a boolean
     */
    public boolean isInElevator() {
        return inElevator;
    }

    /**
     * Returns true if a passenger waits for an elevator
     * @return a boolean
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * Return next target floor
     * @return target floor
     */
    public synchronized Integer getNextFloor() {
        if (stops.size() < 2) {
            return null;
        }
        return stops.get(1);
    }

    /**
     * Returns current floor passenger is on, if he's not in the elevator
     * @return curren floor
     */
    public synchronized Integer getCurrentFloor() {
        if (stops.isEmpty()) {
            return null;
        }
        return stops.get(0);
    }

    /**
     * Ads a elevator stop for passenger
     * @param floor where to stop
     */
    public void addStop(Integer floor) {
        Objects.requireNonNull(floor);
        stops.add(floor);
    }

    /**
     * Assigns an elevator to the passenger
     * @param elevator the elevator
     */
    public synchronized void assignElevator(Elevator elevator) {
        elevator.addElevatorListener(this);
        waiting = true;
    }

    @Override
    public synchronized boolean onStopEvent(StopEvent event) {
        Elevator elevator = event.getElevator();

        if (inElevator && event.getFloor().equals(getNextFloor())) {
            stops.remove(0);
            inElevator = false;
            waiting = false;
            elevator.leave();
            LOGGER.info(String.format("Passenger=%d, leaves elevator=%d on floor=%d", id, elevator.getId(), event.getFloor()));
            return true;
        }

        if (!inElevator && event.getFloor().equals(getCurrentFloor())) {
            inElevator = true;
            waiting = false;
            Integer nextFloor = getNextFloor();
            if (nextFloor == null) {
                inElevator = false;
                stops.remove(0);
                return true;
            }
            elevator.enter(nextFloor);
            LOGGER.info(String.format("Passenger=%d, enters elevator=%d on floor=%d", id, elevator.getId(), event.getFloor()));
            return false;
        }

        if (inElevator) {
            LOGGER.info(String.format("Passenger=%d is in elevator=%d, on floor=%d", id, elevator.getId(), event.getFloor()));
        }else if (getCurrentFloor() != null){
            LOGGER.info(String.format("Passenger=%d is %s on floor=%d", id, (waiting? "waiting" : "hanging around"), getCurrentFloor()));
        }else {
            LOGGER.info(String.format("Passenger=%d has left the building", id));
        }

        return false;
    }
}
