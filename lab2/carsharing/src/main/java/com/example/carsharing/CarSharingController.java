package com.example.carsharing;// CarSharingController.java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

@RestController
@RequestMapping("/carsharing")
public class CarSharingController {

    /*private final RenterService renterService;*/
    @Autowired
    private CarConnectionHandler carConnectionHandler;

    private final Set<String> registeredRenters = new HashSet<>();
    private final Set<String> registeredOwners = new HashSet<>();
    private final Map<String, String> registeredCars = new HashMap<>();
    private final List<String> operations = new ArrayList<>();
    private final Map<String, Car> availableCars = new HashMap<>();

    //I decided to remove the renterService, for this lab it's enough to have the data in the controller, and not have a separate service for renter, owner, car, etc.
    //Is it required to have a separate service for renter, owner, car, etc.? or logic implemented for the car sharing operations? as if client can't access same car, if it's occupied, etc.
    /*@Autowired
    public CarSharingController(RenterService renterService) {
        this.renterService = renterService;
    }*/

    public void removeAvailableCar(String carId) {
        availableCars.remove(carId);
    }

    public Map<String,String> getRegisteredCars() {
        return registeredCars;
    }

    public void addAvailableCar(String carId, int fuelLevel, boolean isLocked) {
        Car car = new Car(carId, fuelLevel, isLocked);
        availableCars.put(carId, car);
    }

    public Map<String, Car> getAvailableCars() {
        return availableCars;
    }

    @PostMapping("/")
    public ResponseEntity<String> processMessage(@RequestBody CarSharingMessage message) {
        if (message.getClientId() == null || message.getClientType() < 0 || message.getMessageId() < 0 || message.getMessageId() > 5) {
            return ResponseEntity.badRequest().body("Invalid message payload");
        }
        ArrayList<String> operations = new ArrayList<String>();

        switch (message.getMessageId()) {
            case 0:
                if (message.getClientType() != 1) {
                    return ResponseEntity.badRequest().body("Invalid client type for registerRenter operation");
                }
                return registerRenter(message);
            case 1:
                if (message.getClientType() != 0) {
                    return ResponseEntity.badRequest().body("Invalid client type for registerOwner operation");
                }
                return registerOwner(message);
            case 2:
                if (message.getClientType() != 0) {
                    return ResponseEntity.badRequest().body("Invalid client type for postCar operation");
                }
                return postCar(message);
            case 3:
                if (message.getClientType() != 1) {
                    return ResponseEntity.badRequest().body("Invalid client type for requestCar operation");
                }
                return requestCar(message);
            case 4:
                if (message.getClientType() != 1) {
                    return ResponseEntity.badRequest().body("Invalid client type for startRental operation");
                }
                return startRental(message);
            case 5:
                if (message.getClientType() != 1) {
                    return ResponseEntity.badRequest().body("Invalid client type for endRental operation");
                }
                return endRental(message);
            default:
                return ResponseEntity.badRequest().body("Invalid message ID");
        }

    }

    private ResponseEntity<String> registerRenter(CarSharingMessage message) {
        if (registeredRenters.contains(message.getClientId())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Renter already registered"); //202
        }
        if (registeredOwners.contains(message.getClientId())) {
            return ResponseEntity.badRequest().body("Renter already registered as Owner with same Client ID\"");
        }

        registeredRenters.add(message.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Renter registered successfully"); //201
    }


    private ResponseEntity<String> registerOwner(CarSharingMessage message) {

        if (registeredOwners.contains(message.getClientId())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Owner already registered"); //202
        }
        if(registeredRenters.contains(message.getClientId())) {
            return ResponseEntity.badRequest().body("Owner already registered as Renter with same Client ID");
        }

        registeredOwners.add(message.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Owner registered successfully"); //201
    }

    private ResponseEntity<String> postCar(CarSharingMessage message) {
        String ownerId = message.getPayload();
        String carId = message.getClientId();
        if (registeredCars.get(carId) != null) {
            return ResponseEntity.badRequest().body("Car already posted");
        }
        if (registeredCars.containsKey(ownerId)) {
            if(registeredCars.get(ownerId).equals(carId)) {
                return ResponseEntity.badRequest().body("Owner already posted a car with the same ID");
            }
        }
        registeredCars.put(carId, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Car posted successfully");
    }

    private ResponseEntity<String> requestCar(CarSharingMessage message) {
        // Get the list of available cars
        Map<String, Car> availableCarsList = getAvailableCars();
        //skip the first entry in the map - bug
        availableCarsList.remove("100,true");

        if (availableCarsList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No available cars"); //  204 No Content
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(availableCarsList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error converting available cars to JSON");
        }

        return ResponseEntity.ok(jsonString); //ghetto solution, but it works
    }
    private ResponseEntity<String> startRental(CarSharingMessage message) {
        // isRented - setam pe true - daca clientul face request printr-un buton de start rental
        // renter car id - setam idul masinii - de ex pt idul 1 1234 - setam 3 3456 - pt renter 1, setam masina 3
        //set de date - clientId -> carId
        String carId = message.getPayload();

        if (!availableCars.containsKey(carId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Car not available for rental");
        }

        Car car = availableCars.remove(carId);

        WebSocketSession session = carConnectionHandler.getSession(carId);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find WebSocket session for car");
        }

        try {
            session.sendMessage(new TextMessage("start rental"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send start rental message to car");
        }

        return ResponseEntity.ok("Rental started successfully");
    }

    private ResponseEntity<String> endRental(CarSharingMessage message) {
        // isRented - setam pe false - daca clientul face request printr-un buton de end rental
        // stergem rented car id - dam clear la id ul masinii setate pt renter
        //set de date - clientId -/> carId

        String carId = message.getPayload();

        WebSocketSession session = carConnectionHandler.getSession(carId);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not find WebSocket session for car");
        }

        try {
            session.sendMessage(new TextMessage("stop rental"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send stop rental message to car");
        }

        Car car = carConnectionHandler.getCar(carId);
        //bug - after i put it back in motion, the getFuelLevel() is 0
        /*if (car != null && car.getFuelLevel() > 0) {
            availableCars.put(carId, car);
        }*/
        if (car != null) {
            availableCars.put(carId, car);
        }

        return ResponseEntity.ok("Rental ended successfully");
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAllData() {
        Map<String, Object> data = new HashMap<>();
        data.put("renters", registeredRenters);
        data.put("owners", registeredOwners);
        data.put("cars", registeredCars);
        data.put("operations", operations);
        return ResponseEntity.ok(data);
    }

}
