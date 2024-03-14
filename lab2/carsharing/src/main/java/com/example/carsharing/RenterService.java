package com.example.carsharing;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class RenterService {

    private Map<String, CarSharingMessage> registeredRenters = new HashMap<>();

    public List<CarSharingMessage> getAllRegisteredRenters() {
        return new ArrayList<>(registeredRenters.values());
    }

    public void registerRenter(CarSharingMessage renter) {
        registeredRenters.put(renter.getClientId(), renter);
    }

    public boolean isRenterRegistered(String clientId) {
        return registeredRenters.containsKey(clientId);
    }
}