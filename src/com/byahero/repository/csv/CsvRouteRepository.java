package com.byahero.repository.csv;

import com.byahero.model.RouteStop;
import com.byahero.repository.RouteRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Saves and loads route stops using routes.csv.
 * The "id" of a stop is destination + "#" + stop number (example: Manila#1).
 */
public class CsvRouteRepository extends CsvRepository<RouteStop> implements RouteRepository {

    public static final String HEADER = "destination,stopOrder,stopName,road";

    public CsvRouteRepository(String filePath) {
        super(filePath, HEADER, 4, "route");
    }

    @Override
    protected RouteStop fromRow(String[] row) {
        return new RouteStop(row[0], Integer.parseInt(row[1]), row[2], row[3]);
    }

    @Override
    protected String toLine(RouteStop stop) {
        return CsvUtil.clean(stop.getDestination()) + "," + stop.getStopOrder() + ","
                + CsvUtil.clean(stop.getStopName()) + "," + CsvUtil.clean(stop.getRoad());
    }

    @Override
    protected String idOf(RouteStop stop) {
        return stop.getId();
    }

    @Override
    protected String alreadyExistsMessage(RouteStop stop) {
        return "Stop " + stop.getStopOrder() + " already exists for " + stop.getDestination() + ".";
    }

    @Override
    protected String notFoundMessage(RouteStop stop) {
        return "No stop " + stop.getStopOrder() + " found for " + stop.getDestination() + ".";
    }

    @Override
    public synchronized List<RouteStop> findByDestination(String destination) {
        List<RouteStop> result = new ArrayList<RouteStop>();
        if (destination == null) {
            return result;
        }

        List<RouteStop> stops = readFile();
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getDestination().equalsIgnoreCase(destination.trim())) {
                result.add(stops.get(i));
            }
        }
        result.sort(Comparator.comparingInt(RouteStop::getStopOrder));
        return result;
    }
}
