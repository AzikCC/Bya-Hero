package com.byahero.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the right kind of Bus from its type name ("PROVINCIAL" -> ProvincialBus).
 *
 * OPEN/CLOSED PRINCIPLE: to add a new bus type you write a new Bus subclass and
 * add ONE register(...) line in withDefaultTypes(). No other class changes:
 * the repository, services and HTML forms all ask this factory for the type list.
 *
 * (Before, this logic was a chain of if-statements inside CsvBusRepository,
 * so the storage class had to change whenever a bus type was added.)
 */
public class BusFactory {

    /** One way to build a bus. A constructor like ProvincialBus::new fits this shape. */
    public interface BusCreator {
        Bus create(String plate, String operator, String destination, BusStatus status,
                   String bay, String checkInTime, String estDeparture);
    }

    // LinkedHashMap keeps the types in the order they were registered (used by dropdown lists)
    private final Map<String, BusCreator> creators = new LinkedHashMap<String, BusCreator>();

    /** A factory that already knows the three standard bus types. */
    public static BusFactory withDefaultTypes() {
        BusFactory factory = new BusFactory();
        factory.register(OrdinaryBus.TYPE, OrdinaryBus::new);
        factory.register(ProvincialBus.TYPE, ProvincialBus::new);
        return factory;
    }

    /** Teaches the factory a new bus type. */
    public BusFactory register(String typeName, BusCreator creator) {
        creators.put(typeName.trim().toUpperCase(), creator);
        return this;
    }

    /** Returns the names of all known types, like [ORDINARY, PROVINCIAL]. */
    public List<String> getTypeNames() {
        return new ArrayList<String>(creators.keySet());
    }

    /** Creates a bus of the given type. Throws if the type is unknown. */
    public Bus create(String busType, String plate, String operator, String destination,
                      BusStatus status, String bay, String checkInTime, String estDeparture) {
        String type = "";
        if (busType != null) {
            type = busType.trim().toUpperCase();
        }

        BusCreator creator = creators.get(type);
        if (creator == null) {
            throw new IllegalArgumentException("Unknown bus type '" + busType + "'. Use one of: "
                    + String.join(", ", creators.keySet()) + ".");
        }
        return creator.create(plate, operator, destination, status, bay, checkInTime, estDeparture);
    }
}
