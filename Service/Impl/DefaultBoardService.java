package com.byahero.service.impl;

import com.byahero.model.Bus;
import com.byahero.repository.ReadRepository;
import com.byahero.service.BoardService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultBoardService implements BoardService {

    private final ReadRepository<Bus> busRepo;

    public DefaultBoardService(ReadRepository<Bus> busRepo) {
        this.busRepo = busRepo;
    }

    /**
     * Example: {"Manila" -> [bus1, bus2], "Baguio" -> [bus3]}
     * A LinkedHashMap keeps the groups in the order we added them.
     */
    @Override
    public Map<String, List<Bus>> getActiveBusesByDestination() {
        Map<String, List<Bus>> groups = new LinkedHashMap<String, List<Bus>>();
        List<Bus> allBuses = busRepo.findAll();

        for (int i = 0; i < allBuses.size(); i++) {
            Bus bus = allBuses.get(i);
            if (bus.isActive()) {
                String destination = bus.getDestination();

                // create the list for this destination the first time we see it
                if (!groups.containsKey(destination)) {
                    groups.put(destination, new ArrayList<Bus>());
                }
                groups.get(destination).add(bus);
            }
        }
        return groups;
    }
}
