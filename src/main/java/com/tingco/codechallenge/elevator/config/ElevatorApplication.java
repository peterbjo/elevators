package com.tingco.codechallenge.elevator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tingco.codechallenge.elevator.api.Elevator;
import com.tingco.codechallenge.elevator.api.ElevatorController;
import com.tingco.codechallenge.elevator.domain.ElevatorImpl;
import com.tingco.codechallenge.elevator.service.ElevatorControllerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * Preconfigured Spring Application boot class.
 *
 */
@Configuration
@ComponentScan(basePackages = { "com.tingco.codechallenge.elevator" })
@EnableAutoConfiguration
@PropertySources({ @PropertySource("classpath:application.properties") })
public class ElevatorApplication {

    @Value("${com.tingco.elevator.numberofelevators}")
    private int numberOfElevators;

    @Value("${com.tingco.elevator.number.of.floors}")
    private int numberOfFloors;

    @Value("${com.tingco.elevator.speed.between.floors.ms}")
    private long speedBetweenFloorsMs;

    @Value("${com.tingco.elevator.average.waiting.time.per.stop.ms}")
    private int averageWaitingTimePerStopMs;
    /**
     * Start method that will be invoked when starting the Spring context.
     *
     * @param args
     *            Not in use
     */
    public static void main(final String[] args) {
        SpringApplication.run(ElevatorApplication.class, args);
    }

    /**
     * Create a default thread pool for your convenience.
     *
     * @return Executor thread pool
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(numberOfElevators);
    }

    /**
     * Create an event bus for your convenience.
     *
     * @return EventBus for async task execution
     */
    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(Executors.newCachedThreadPool());
    }

    @Bean
    ElevatorController elevatorController(){
        List<Elevator> elevators = new ArrayList<>();
        for(int i = 0; i < numberOfElevators; i++){
            elevators.add(new ElevatorImpl(i, Elevator.Direction.NONE, 0, numberOfFloors, speedBetweenFloorsMs, averageWaitingTimePerStopMs));
        }
        return  new ElevatorControllerImpl(taskExecutor(), elevators);
    }

}
