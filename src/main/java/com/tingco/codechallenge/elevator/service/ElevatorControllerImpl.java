package com.tingco.codechallenge.elevator.service;

import com.tingco.codechallenge.elevator.api.Elevator;
import com.tingco.codechallenge.elevator.api.ElevatorController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Björklund <mailto:peter.bjorklund@joors.com>
 * @since 1.0.0
 */
public class ElevatorControllerImpl implements ElevatorController {

    private final ExecutorService taskExecutor;

    private final List<Elevator> elevators;

    public ElevatorControllerImpl(ExecutorService taskExecutor, List<Elevator> elevators) {
        this.taskExecutor = taskExecutor;
        this.elevators = Collections.synchronizedList(elevators);
    }

    @Override
    public synchronized Elevator requestElevator(int toFloor) {

        Elevator fastest = elevators.get(0);
        long calculatedTime = fastest.calculateTimeToFloor(toFloor);
        for (int i = 1; i < elevators.size(); i++) {
            Elevator elevator = elevators.get(i);
            long time;
            if ((time = elevator.calculateTimeToFloor(toFloor)) < calculatedTime) {
                calculatedTime = time;
                fastest = elevator;
            }
            if (time == calculatedTime) {
                fastest = (elevator.getNrOfPassengers() < fastest.getNrOfPassengers()) ? elevator : fastest;
            }

        }
        if(!fastest.isRunning()) {
            startElevator(fastest);
        }
        fastest.moveElevator(toFloor);
        return fastest;
    }

    @Override
    public List<Elevator> getElevators() {
        return elevators;
    }

    @Override
    public void releaseElevator(Elevator elevator) {
        elevator.stop();

    }

    private void startElevator(Elevator elevator) {
        elevator.start();
        taskExecutor.execute(elevator);
    }

    @Override
    public synchronized void stop() {
        try {
            for (Elevator elevator : elevators){
                elevator.stop();
            }
            System.out.println("attempt to shutdown executor");
            taskExecutor.shutdown();
            taskExecutor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        } finally {
            if (!taskExecutor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            taskExecutor.shutdownNow();
            System.out.println("shutdown finished");
        }
    }

}
