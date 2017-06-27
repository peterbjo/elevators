# Elevator Coding Challenge

Create an elevator controller!

This is a skeleton project with two interfaces that you must implement.

You are going to create an Elevator Controller and a number of Elevators that will be managed by the controller. There are a few extra classes already added to the project to get you up and running quickly.

## To Do

There are two interfaces to implement.

 * `Elevator` - this is the elevator itself and has a few public methods to take care of. There can be n number of elevators at the same time

 * `ElevatorController` - this is the elevator manager that keeps track of all the elevators running in the elevator shaft. There should be only one ElevatorController

### Bonus Classes

There are a few classes added to get you faster up and running. It is not mandatory to use these classes in your solution but you can use them to cut time in boiler plate coding.

 * `ElevatorControllerEndPoints` for REST features. If you would like to use them in a test or to support a GUI here is already a basic skeleton class for you

 * `ElevatorApplication` class for starting the Spring container and there are a few beans you can use as well

 * There are two test classes added to get you up and running faster with tests and simulations

## What We Expect

Implement the elevator system and make it as real as possible when it comes to the logic. Which elevator is best suited for a waiting floor, more requests than available elevators and so on.

Write a test or a simulation that runs the system with a number of floors and elevators. The numbers should be flexible and the system must work with one to many elevators.

Document how we start, simulate and monitor your solution. If there is a GUI or logging for monitoring does not matter as long as you describe how it is supposed to be done.

Have fun! This is not a trap. It is a code challenge to check coding style etc. If there are features you don't have time to add or if you have future changes in mind, write comments or document them.

### Deliver Your Solution

Add the code to a github or bitbucket repository. You can also make an archive of the project and e-mail it to us. We would like to see your solution within 7 days.
 
## Build And Run (as is)

As the project is, the Spring app can be started as seen below.

build and run the code with Maven

    mvn package
    mvn spring-boot:run

or start the target JAR file 

    mvn package
    java -jar target/elevator-1.0-SNAPSHOT.jar

