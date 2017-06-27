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
    private Elevator elevator;

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
     * @return
     */
    public boolean isInElevator() {
        return inElevator;
    }

    /**
     * Returns true if a passenger waits for an elevator
     * @return
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * Return next target floor
     * @return target floor
     */
    public  Integer getNextFloor() {
        if (stops.size() < 2) {
            return null;
        }
        return stops.get(1);
    }

    /**
     * Returns current floor passenger is on, if he's not in the elevator
     * @return curren floor
     */
    public Integer getCurrentFloor() {
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
    public void assignElevator(Elevator elevator) {
        if(this.elevator != null){
            elevator.removeElevatorListener(this);
        }
        this.elevator = elevator;
        this.elevator.addElevatorListener(this);
        waiting = true;
    }

    @Override
    public void onStopEvent(StopEvent event) {
        Elevator elevator = event.getElevator();
        if (inElevator && event.getFloor().equals(getNextFloor())) {
            elevator.leave();
            stops.remove(0);
            inElevator = false;
            waiting = false;
            LOGGER.info(String.format("Passenger %d, leaves elevator %d on floor %d", id, elevator.getId(), event.getFloor()));
            return;
        } else if (!inElevator && event.getFloor().equals(getCurrentFloor())) {
            inElevator = true;
            waiting = false;
            Integer nextFloor = getNextFloor();
            if (nextFloor == null) {
                inElevator = false;
                stops.remove(0);
                return;
            }
            elevator.enter(nextFloor);
            LOGGER.info(String.format("Passenger %d, enters elevator %d on floor %d", id, elevator.getId(), event.getFloor()));
        }
        if (inElevator) {
            LOGGER.info(String.format("Passenger %d is in elevator %d, on floor %d", id, elevator.getId(), event.getFloor()));
        }else{
            LOGGER.info(String.format("Passenger %d is %s on floor %d", id, (waiting? "waiting" : "hanging around"), getCurrentFloor()));
        }
    }
}
