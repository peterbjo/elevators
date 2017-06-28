package com.tingco.codechallenge.elevator.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.tingco.codechallenge.elevator.api.Elevator;
import com.tingco.codechallenge.elevator.api.ElevatorListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simple elevator algorithm
 * If a elevator is going up, it continues to do so as long there are requests above the elevator and vice verse.
 * If the elevator does not receive any more requests it doesn't move.
 * When a request comes for an elevator on a specific floor, the elevator with the estimated shortest time to reach the
 * floor is chosen.
 *
 * @author Peter Björklund <mailto:peter.bjorklund@joors.com>
 * @since 1.0.0
 */
public class ElevatorImpl implements Elevator, Runnable {

    private static final Logger LOGGER = Logger.getLogger(ElevatorImpl.class.getName());

    private int id;
    private int nrOfPassengers = 0;
    private Direction direction;
    private int currentFloor;
    private int nrOfFloors;
    private long speedBetweenFloorsMs;
    private long avgWaitingTimePerStopMs;
    private int[] elevatorStops;
    private List<ElevatorListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private boolean isRunning = false;

    public ElevatorImpl(int id, Direction direction, int currentFloor, int nrOfFloors, long speedBetweenFloorsMs, long avgWaitingTimePerStopMs) {
        this.id = id;
        this.direction = direction;
        this.currentFloor = currentFloor;
        this.nrOfFloors = nrOfFloors;
        this.speedBetweenFloorsMs = speedBetweenFloorsMs;
        this.avgWaitingTimePerStopMs = avgWaitingTimePerStopMs;
        this.elevatorStops = new int[nrOfFloors];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElevatorImpl elevator = (ElevatorImpl) o;
        return id == elevator.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nrOfPassengers", nrOfPassengers)
                .add("direction", direction)
                .add("currentFloor", currentFloor)
                .add("nrOfFloors", nrOfFloors)
                .add("speedBetweenFloorsMs", speedBetweenFloorsMs)
                .add("avgWaitingTimePerStopMs", avgWaitingTimePerStopMs)
                .add("isRunning", isRunning)
                .toString();
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public synchronized int getAddressedFloor() {
        int addressedFloor;
        switch (direction) {
            case UP:
                if ((addressedFloor = nextStopUp()) > -1) {
                    return addressedFloor;
                }

                if ((addressedFloor = nextStopDown()) > -1) {
                    return addressedFloor;

                }
                return currentFloor;
            case DOWN:
                if ((addressedFloor = nextStopDown()) > -1) {
                    return addressedFloor;
                }

                if ((addressedFloor = nextStopUp()) > -1) {
                    return addressedFloor;
                }

                return currentFloor;
            case NONE:
                int nextStopUp = nextStopUp();
                int nextStopDown = nextStopDown();

                if (nextStopUp > -1) {
                    return (nextStopDown == -1) ? nextStopUp : (nextStopDown < nextStopUp) ? nextStopDown : nextStopUp;
                }

                if (nextStopDown > -1) {
                    return nextStopDown;
                }
                return currentFloor;

        }
        return currentFloor;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public synchronized void moveElevator(int toFloor) {
        elevatorStops[toFloor] = elevatorStops[toFloor] + 1;
    }

    @Override
    public synchronized long calculateTimeToFloor(int toFloor, Direction direction) {
        switch (this.direction) {
            case UP:
                if (direction.equals(Direction.DOWN)) {
                    return calculateTimeGoingUp(currentFloor, lastStopUp()) + calculateTimeGoingDown(lastStopUp(), toFloor);
                }
                return calculateTimeGoingUp(currentFloor, toFloor);
            case DOWN:
                if (direction.equals(Direction.UP)) {
                    return calculateTimeGoingDown(currentFloor, lastStopDown()) + calculateTimeGoingUp(lastStopDown(), toFloor);
                }
                return calculateTimeGoingDown(currentFloor, toFloor);
            case NONE:
                if (toFloor >= currentFloor) {
                    return calculateTimeGoingUp(currentFloor, toFloor);
                }
                return calculateTimeGoingDown(currentFloor, toFloor);
        }
        return Long.MAX_VALUE;
    }

    @Override
    public synchronized int moveToNextFloor() {

        int addressedFloor = getAddressedFloor();

        if (isBelow(addressedFloor)) {
            this.direction = Direction.UP;
            currentFloor++;
        } else if (isAbove(addressedFloor)) {
            this.direction = Direction.DOWN;
            currentFloor--;
        }

        if (isOnSameFloor(addressedFloor)) {
            if (direction == Direction.UP && nextStopUp() > -1) {
                this.direction = Direction.UP;
            } else if (direction == Direction.DOWN && nextStopDown() > -1) {
                this.direction = Direction.DOWN;
            } else {
                this.direction = Direction.NONE;
            }

            List<ElevatorListener> toRemove = new ArrayList<>();
            for (ElevatorListener listener : listeners) {
                if (listener.onStopEvent(new StopEvent(currentFloor, this))) {
                    toRemove.add(listener);
                }
            }
            listeners.removeAll(toRemove);
        }

        LOGGER.info(String.format("Elevator=%s, is on floor=%d with direction=%s and nr of passengers=%d", id, currentFloor, direction, nrOfPassengers));
        return currentFloor;
    }

    @Override
    public int currentFloor() {
        return currentFloor;
    }

    @Override
    public void addElevatorListener(ElevatorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeElevatorListener(ElevatorListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void leave() {
        elevatorStops[currentFloor] = elevatorStops[currentFloor] - 1;
        nrOfPassengers--;
    }

    @Override
    public synchronized void enter(int toFloor) {
        elevatorStops[currentFloor] = elevatorStops[currentFloor] - 1;
        elevatorStops[toFloor] = elevatorStops[toFloor] + 1;
        nrOfPassengers++;
    }

    @Override
    public int getNrOfPassengers() {
        return nrOfPassengers;
    }

    @Override
    public boolean isBusy() {
        return isRunning;
    }


    @Override
    public void start() {
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        try {
            while (isRunning) {

                int addressedFloor = getAddressedFloor();
                int nextFloor = moveToNextFloor();
                TimeUnit.MILLISECONDS.sleep(speedBetweenFloorsMs);
                if (nextFloor == addressedFloor) {
                    TimeUnit.MILLISECONDS.sleep(avgWaitingTimePerStopMs);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isBelow(int floor) {
        return currentFloor < floor;
    }

    private boolean isAbove(int floor) {
        return currentFloor > floor;
    }

    private boolean isOnSameFloor(int floor) {
        return currentFloor == floor;
    }

    private int nextStopUp() {
        for (int i = currentFloor; i < nrOfFloors; i++) {
            if (elevatorStops[i] > 0) {
                return i;
            }
        }
        return -1;
    }

    private int lastStopUp() {
        int lastStop = 0;
        for (int i = currentFloor; i < nrOfFloors; i++) {
            if (elevatorStops[i] > 0) {
                lastStop = i;
            }
        }
        return lastStop;
    }

    private int nextStopDown() {
        for (int i = currentFloor; i >= 0; i--) {
            if (elevatorStops[i] > 0) {
                return i;
            }
        }
        return -1;
    }

    private int lastStopDown() {
        int lastStop = 0;
        for (int i = currentFloor; i >= 0; i--) {
            if (elevatorStops[i] > 0) {
                lastStop = i;
            }
        }
        return lastStop;
    }

    private long calculateTimeGoingUp(int fromFloor, int toFloor) {
        int[] stops = Arrays.copyOf(elevatorStops, elevatorStops.length);
        int lastStopUp = lastStopUpStartingFrom(fromFloor, stops);

        lastStopUp = (toFloor > lastStopUp) ? toFloor : lastStopUp;

        long totalTime = 0;

        if (stops[toFloor] == 0) {
            totalTime = avgWaitingTimePerStopMs;
        }

        totalTime += totalTimeUp(fromFloor, lastStopUp, stops);

        if (toFloor > fromFloor) {
            return totalTime;
        }

        int lastStopDown = lastStopDownStartingFrom(lastStopUp, stops);
        lastStopDown = (toFloor < lastStopDown) ? toFloor : lastStopDown;

        totalTime += totalTimeDown(lastStopUp, lastStopDown, stops);

        return totalTime;
    }

    private long totalTimeUp(int from, int to, int[] stops) {
        long totalTime = 0;
        for (int i = from; i <= to; i++) {
            if (stops[i] > 0) {
                totalTime += avgWaitingTimePerStopMs;
                stops[i] = stops[i] - 1;
            }
            totalTime += speedBetweenFloorsMs;
        }
        return totalTime;
    }

    private long totalTimeDown(int from, int to, int[] stops) {
        long totalTime = 0;
        for (int i = from; i >= to; i--) {
            if (stops[i] > 0) {
                totalTime += avgWaitingTimePerStopMs;
                stops[i] = stops[i] - 1;
            }
            totalTime += speedBetweenFloorsMs;
        }

        return totalTime;
    }

    private long calculateTimeGoingDown(int fromFloor, int toFloor) {
        int[] stops = Arrays.copyOf(elevatorStops, elevatorStops.length);
        int lastStopDown = lastStopDownStartingFrom(fromFloor, stops);

        lastStopDown = (toFloor < lastStopDown) ? toFloor : lastStopDown;

        long totalTime = 0;

        if (stops[toFloor] == 0) {
            totalTime = avgWaitingTimePerStopMs;
        }

        totalTime += totalTimeDown(fromFloor, lastStopDown, stops);

        if (toFloor < fromFloor) {
            return totalTime;
        }

        int lastStopUp = lastStopUpStartingFrom(lastStopDown, stops);
        lastStopUp = (toFloor > lastStopUp) ? toFloor : lastStopUp;

        totalTime += totalTimeUp(lastStopDown, lastStopUp, stops);

        return totalTime;
    }

    private int lastStopUpStartingFrom(int startingFrom, int[] stops) {
        int lastStop = startingFrom;

        for (int i = startingFrom; i < nrOfFloors; i++) {
            if (stops[i] > 0 && i != startingFrom) {
                lastStop = i;
            }
        }
        return lastStop;
    }

    private int lastStopDownStartingFrom(int startingFrom, int[] stops) {
        int lastStop = startingFrom;
        for (int i = startingFrom; i >= 0; i--) {
            if (stops[i] > 0 && i != startingFrom) {
                lastStop = i;
            }
        }
        return lastStop;
    }
}
