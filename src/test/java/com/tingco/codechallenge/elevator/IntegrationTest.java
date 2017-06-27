package com.tingco.codechallenge.elevator;

import com.tingco.codechallenge.elevator.api.Elevator;
import com.tingco.codechallenge.elevator.api.ElevatorController;
import com.tingco.codechallenge.elevator.domain.Passenger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tingco.codechallenge.elevator.config.ElevatorApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Boiler plate test class to get up and running with a test faster.
 *
 * @author Sven Wesley
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ElevatorApplication.class)
public class IntegrationTest {

    private static int NR_OF_PASSENGERS = 20;

    @Autowired
    private ElevatorController elevatorController;

    @Value("${com.tingco.elevator.number.of.floors}")
    private int numberOfFloors;


    @Test
    public void simulateAnElevatorShaft() throws InterruptedException {

        Random random = new Random();
        List<Passenger> passengers = Collections.synchronizedList(new ArrayList<>());
        for(int i = 0 ; i < NR_OF_PASSENGERS; i++){
            Passenger passenger = new Passenger(i);
            passenger.addStop(0);
            passenger.addStop(random.nextInt(numberOfFloors - 1) + 1);
            passenger.addStop(0);
            passengers.add(passenger);
        }
        while (!passengers.isEmpty()){
            Passenger passenger = passengers.get(random.nextInt(passengers.size()));

            if(passenger.isWaiting() || passenger.isInElevator()){
                continue;
            }
            Integer nextFloor = passenger.getNextFloor();
            if(nextFloor == null) {
                passengers.remove(passenger);
                continue;
            }
            Elevator elevator = elevatorController.requestElevator(passenger.getCurrentFloor());

            passenger.assignElevator(elevator);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
        elevatorController.stop();
    }

}
