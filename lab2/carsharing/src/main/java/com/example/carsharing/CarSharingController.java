package com.example.carsharing;// CarSharingController.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        if (registeredCars.contains(message.getClientId())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Car already posted/registered"); //202
        }

        registeredCars.add(message.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Car posted successfully"); //201
    }

    private ResponseEntity<String> requestCar(CarSharingMessage message) {
        //sa imi iau o lista de masini disponibile in oras -- de ex un buton de refresh care face un get la /carsharing/ si imi da toate masinile disponibile
        //TODO: daca nu are fuel, trimit inapoi 400 ca nu se poate face request car
        //TODO: daca e deja ocupata, trimit inapoi 400 ca isRented is true

        return ResponseEntity.ok("Car requested successfully");
    }

    private ResponseEntity<String> startRental(CarSharingMessage message) {
        // isRented - setam pe true - daca clientul face request printr-un buton de start rental
        // renter car id - setam idul masinii - de ex pt idul 1 1234 - setam 3 3456 - pt renter 1, setam masina 3
        //set de date - clientId -> carId
        return ResponseEntity.ok("Rental started successfully");
    }

    private ResponseEntity<String> endRental(CarSharingMessage message) {
        // isRented - setam pe false - daca clientul face request printr-un buton de end rental
        // stergem rented car id - dam clear la id ul masinii setate pt renter
        //set de date - clientId -/> carId
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
