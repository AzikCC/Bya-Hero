package com.byahero.repository.csv;

import com.byahero.model.Fare;

/** Saves and loads fares using fares.csv. The "id" of a fare is its destination name. */
public class CsvFareRepository extends CsvRepository<Fare> {

    public static final String HEADER = "destination,baseFare";

    public CsvFareRepository(String filePath) {
        super(filePath, HEADER, 2, "fare");
    }

    @Override
    protected Fare fromRow(String[] row) {
        return new Fare(row[0], Double.parseDouble(row[1]));
    }

    @Override
    protected String toLine(Fare fare) {
        return CsvUtil.clean(fare.getDestination()) + "," + fare.getBaseFare();
    }

    @Override
    protected String idOf(Fare fare) {
        return fare.getDestination();
    }

    @Override
    protected String alreadyExistsMessage(Fare fare) {
        return "A fare for " + fare.getDestination() + " already exists.";
    }

    @Override
    protected String notFoundMessage(Fare fare) {
        return "No fare found for " + fare.getDestination() + ".";
    }
}
