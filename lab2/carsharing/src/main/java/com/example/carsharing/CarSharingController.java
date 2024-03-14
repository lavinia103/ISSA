package com.example.carsharing;// CarSharingController.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.*;

@RestController
@RequestMapping("/carsharing")
public class CarSharingController {

    /*private final RenterService renterService;*/

    private final Set<String> registeredRenters = new HashSet<>();
    private final Set<String> registeredOwners = new HashSet<>();
    private final Set<String> registeredCars = new HashSet<>();
    private final List<String> operations = new ArrayList<>();

    //I decided to remove the renterService, for this lab it's enough to have the data in the controller, and not have a separate service for renter, owner, car, etc.
    //Is it required to have a separate service for renter, owner, car, etc.? or logic implemented for the car sharing operations? as if client can't access same car, if it's occupied, etc.
    /*@Autowired
    public CarSharingController(RenterService renterService) {
        this.renterService = renterService;
    }*/

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
            return ResponseEntity.badRequest().body("Renter already registered");
        }
        if (registeredOwners.contains(message.getClientId())) {
            return ResponseEntity.badRequest().body("Renter already registered as Owner with same Client ID\"");
        }

        registeredRenters.add(message.getClientId());
        return ResponseEntity.ok("Renter registered successfully");
    }

    private ResponseEntity<String> registerOwner(CarSharingMessage message) {

        if (registeredOwners.contains(message.getClientId())) {
            return ResponseEntity.badRequest().body("Owner already registered");
        }
        if(registeredRenters.contains(message.getClientId())) {
            return ResponseEntity.badRequest().body("Owner already registered as Renter with same Client ID");
        }

        registeredOwners.add(message.getClientId());
        return ResponseEntity.ok("Owner registered successfully");
    }

    private ResponseEntity<String> postCar(CarSharingMessage message) {
        if (registeredCars.contains(message.getClientId())) {
            return ResponseEntity.badRequest().body("Car already posted");
        }

        registeredCars.add(message.getClientId());

        return ResponseEntity.ok("Car posted successfully");
    }

    private ResponseEntity<String> requestCar(CarSharingMessage message) {
        // ...

        return ResponseEntity.ok("Car requested successfully");
    }

    private ResponseEntity<String> startRental(CarSharingMessage message) {
        // ...

        return ResponseEntity.ok("Rental started successfully");
    }

    private ResponseEntity<String> endRental(CarSharingMessage message) {
        // ...

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
