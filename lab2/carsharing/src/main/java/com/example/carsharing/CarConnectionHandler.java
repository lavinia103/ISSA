package com.example.carsharing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@Component
public class CarConnectionHandler extends TextWebSocketHandler {

    private final CarSharingController carSharingController;
    private static final Logger logger = LoggerFactory.getLogger(CarConnectionHandler.class);
    private ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Car> cars = new ConcurrentHashMap<>();
    public CarConnectionHandler(@Lazy CarSharingController carSharingController) {
        this.carSharingController = carSharingController;
    }
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        logger.info("Received message: {}", message.getPayload());

        String[] carDetails = message.getPayload().split(",");

        String carId = carDetails[0];
        int fuelLevel = 0;
        boolean isLocked = false;

        if (carDetails.length > 2) {
            fuelLevel = Integer.parseInt(carDetails[1]);
            isLocked = Boolean.parseBoolean(carDetails[2]);
        } else if (!carSharingController.getRegisteredCars().containsKey(carId)) {
            logger.info("Received carId is not registered: {}", carId);
            return;
        }
        if (!sessions.containsKey(carId)) {
            sessions.put(carId, session);
        }

        logger.info("Registered cars: {}", carSharingController.getRegisteredCars());
        logger.info("Available cars: {}", carSharingController.getAvailableCars());

        //bug - in sendAvailableCar, there is fuel info and other garbage data, which is not required
        if (carSharingController.getRegisteredCars().keySet().contains(carId)) {
            try {
                logger.info("Sending back message connected");
                session.sendMessage(new TextMessage("connected"));

                cars.putIfAbsent(carId, new Car(carId, fuelLevel, isLocked));
                carSharingController.addAvailableCar(carId, fuelLevel, isLocked);
            }
            catch(IOException exp) {
                logger.error("IO Exception caught: {}", exp.getMessage());
            }
        } else if (sessions.containsKey(carId)) {
            // This is a response from a car client
            logger.info("Getting response from client");

            Car car = cars.get(carId);
            if (car != null) {
                car.setFuelLevel(fuelLevel);
            }

            // Add the car to the list of available cars
            carSharingController.addAvailableCar(carId, fuelLevel, isLocked);
        }
        // Handle incoming messages from the client here
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Connection closed: {}, status: {}", session.getId(), status.getCode());

        String carId = (String) session.getAttributes().get("carId");
        if (carId != null) {
            sessions.remove(carId);
            cars.remove(carId);
            carSharingController.removeAvailableCar(carId);
        }
    }

    public WebSocketSession getSession(String carId) {
        return sessions.get(carId);
    }

    public Car getCar(String carId) {
        return cars.get(carId);
    }
}