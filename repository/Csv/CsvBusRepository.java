package com.byahero.repository.csv;

import com.byahero.exception.DuplicateBusException;
import com.byahero.model.Bus;
import com.byahero.model.BusFactory;
import com.byahero.model.BusStatus;

import java.util.List;

/**
 * Saves and loads buses using buses.csv. The "id" of a bus is its plate number.
 *
 * Important: buses that already DEPARTED stay in the file as history.
 * So the same plate can appear many times, but only ONE of them can be active.
 * The newest row for a plate is always the last one in the file.
 * That is why this class overrides findById, save, update and delete.
 *
 * It no longer decides which Bus subclass to build: the BusFactory does that.
 */
public class CsvBusRepository extends CsvRepository<Bus> {

    public static final String HEADER = "plate,busType,operator,destination,status,bay,checkInTime,estDeparture";

    private final BusFactory busFactory;

    public CsvBusRepository(String filePath, BusFactory busFactory) {
        super(filePath, HEADER, 8, "bus");
        this.busFactory = busFactory;
    }

    @Override
    protected Bus fromRow(String[] row) {
        // column order: plate, busType, operator, destination, status, bay, checkInTime, estDeparture
        BusStatus status = BusStatus.fromString(row[4]);
        return busFactory.create(row[1], row[0], row[2], row[3], status, row[5], row[6], row[7]);
    }

    @Override
    protected String toLine(Bus bus) {
        return bus.getPlate() + ","
                + bus.getBusType() + ","
                + CsvUtil.clean(bus.getOperator()) + ","
                + CsvUtil.clean(bus.getDestination()) + ","
                + bus.getStatus() + ","
                + CsvUtil.clean(bus.getBay()) + ","
                + CsvUtil.clean(bus.getCheckInTime()) + ","
                + CsvUtil.clean(bus.getEstDeparture());
    }

    @Override
    protected String idOf(Bus bus) {
        return bus.getPlate();
    }

    @Override
    protected String notFoundMessage(Bus bus) {
        return "No bus found with plate " + bus.getPlate() + ".";
    }

    /** Finds the LAST row with this plate (the newest one). Returns -1 if none. */
    private int indexOfNewest(List<Bus> buses, String plate) {
        if (plate == null) {
            return -1;
        }
        int found = -1;
        for (int i = 0; i < buses.size(); i++) {
            if (buses.get(i).getPlate().equalsIgnoreCase(plate.trim())) {
                found = i; // keep going, so the last match wins
            }
        }
        return found;
    }

    /** Returns the newest row for this plate, or null if there is none. */
    @Override
    public synchronized Bus findById(String plate) {
        List<Bus> buses = readFile();
        int index = indexOfNewest(buses, plate);
        if (index == -1) {
            return null;
        }
        return buses.get(index);
    }

    /** Adds a new bus. Not allowed if the same plate is still active in the terminal. */
    @Override
    public synchronized void save(Bus bus) {
        List<Bus> buses = readFile();
        for (int i = 0; i < buses.size(); i++) {
            Bus other = buses.get(i);
            if (other.getPlate().equalsIgnoreCase(bus.getPlate()) && other.isActive()) {
                throw new DuplicateBusException("Bus " + bus.getPlate() + " is already in the terminal.");
            }
        }
        buses.add(bus);
        writeFile(buses);
    }

    /** Replaces the newest row that has the same plate. */
    @Override
    public synchronized void update(Bus bus) {
        List<Bus> buses = readFile();
        int index = indexOfNewest(buses, bus.getPlate());
        if (index == -1) {
            throw new IllegalArgumentException(notFoundMessage(bus));
        }
        buses.set(index, bus);
        writeFile(buses);
    }

    /** Removes EVERY row with this plate, including its history. Returns true if something was removed. */
    @Override
    public synchronized boolean delete(String plate) {
        if (plate == null) {
            return false;
        }
        List<Bus> buses = readFile();
        boolean removedSomething = false;

        // go from the end to the start so removing an item does not shift the ones we still need to check
        for (int i = buses.size() - 1; i >= 0; i--) {
            if (buses.get(i).getPlate().equalsIgnoreCase(plate.trim())) {
                buses.remove(i);
                removedSomething = true;
            }
        }

        if (removedSomething) {
            writeFile(buses);
        }
        return removedSomething;
    }
}
