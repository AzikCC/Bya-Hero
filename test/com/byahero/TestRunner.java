package com.byahero;

import com.byahero.config.AppConfig;
import com.byahero.controller.admin.AdminAccountSection;
import com.byahero.controller.admin.AdminSection;
import com.byahero.controller.admin.FareAdminSection;
import com.byahero.controller.admin.OperatorAdminSection;
import com.byahero.fakes.InMemoryRepository;
import com.byahero.model.AdminAccount;
import com.byahero.model.Bus;
import com.byahero.model.BusFactory;
import com.byahero.model.BusStatus;
import com.byahero.model.Fare;
import com.byahero.model.Operator;
import com.byahero.model.OrdinaryBus;
import com.byahero.model.ProvincialBus;
import com.byahero.model.RouteStop;
import com.byahero.repository.Repository;
import com.byahero.repository.csv.CsvAdminAccountRepository;
import com.byahero.repository.csv.CsvBusRepository;
import com.byahero.repository.csv.CsvFareRepository;
import com.byahero.repository.csv.CsvOperatorRepository;
import com.byahero.repository.csv.CsvRouteRepository;
import com.byahero.service.AuthService;
import com.byahero.service.BoardService;
import com.byahero.service.CheckInService;
import com.byahero.service.DestinationService;
import com.byahero.service.FareAdminService;
import com.byahero.service.FareService;
import com.byahero.service.OperatorAdminService;
import com.byahero.service.SessionService;
import com.byahero.service.StatusService;
import com.byahero.service.impl.DefaultAdminAccountService;
import com.byahero.service.impl.DefaultAuthService;
import com.byahero.service.impl.DefaultBoardService;
import com.byahero.service.impl.DefaultCheckInService;
import com.byahero.service.impl.DefaultDestinationService;
import com.byahero.service.impl.DefaultFareAdminService;
import com.byahero.service.impl.DefaultFareService;
import com.byahero.service.impl.DefaultOperatorAdminService;
import com.byahero.service.impl.DefaultOperatorService;
import com.byahero.service.impl.DefaultStatusService;
import com.byahero.service.impl.InMemorySessionService;
import com.byahero.view.TemplateLoader;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple tests without JUnit. Run this class and read the PASS / FAIL lines.
 * Every test uses a temporary folder, so your real CSV files are never touched.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    // ---------- helper methods ----------

    /** Prints PASS or FAIL for one check. */
    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    /** Compares two decimal numbers (they can differ by a tiny bit). */
    private static boolean sameNumber(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }

    /** Creates a new empty temporary folder and returns its path. */
    private static String newTempFolder() throws IOException {
        return Files.createTempDirectory("byahero_test").toString();
    }

    /** All the services of one test setup, wired to CSV files in a temp folder. */
    private static class Setup {
        CsvBusRepository busRepo;
        CheckInService checkIn;
        StatusService status;
        BoardService board;
        FareService fares;
    }

    /** Creates services that have fares for Manila and Baguio and one route stop. */
    private static Setup newSetup(String folder) {
        BusFactory factory = BusFactory.withDefaultTypes();
        Setup s = new Setup();
        s.busRepo = new CsvBusRepository(folder + "/buses.csv", factory);
        CsvFareRepository fareRepo = new CsvFareRepository(folder + "/fares.csv");
        CsvRouteRepository routeRepo = new CsvRouteRepository(folder + "/routes.csv");
        CsvOperatorRepository operatorRepo = new CsvOperatorRepository(folder + "/operators.csv");

        operatorRepo.save(new Operator("Op", ""));
        operatorRepo.save(new Operator("Victory", ""));
        fareRepo.save(new Fare("Manila", 80));
        fareRepo.save(new Fare("Baguio", 450));
        routeRepo.save(new RouteStop("Manila", 1, "Bacoor", "Aguinaldo Highway"));

        DestinationService destinations = new DefaultDestinationService(fareRepo, routeRepo);
        s.checkIn = new DefaultCheckInService(s.busRepo, destinations, new DefaultOperatorService(operatorRepo), factory);
        s.status = new DefaultStatusService(s.busRepo);
        s.board = new DefaultBoardService(s.busRepo);
        s.fares = new DefaultFareService(fareRepo);
        return s;
    }

    /** Tries a check-in. Returns "OK" or the name of the error that was thrown. */
    private static String tryCheckIn(CheckInService service, String plate, String busType, String operator,
                                     String destination, String bay, String estDeparture) {
        try {
            service.checkIn(plate, busType, operator, destination, bay, estDeparture);
            return "OK";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName();
        }
    }

    /** Creates a file with the given text inside. */
    private static void writeFile(String path, String text) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(path));
        out.print(text);
        out.close();
    }

    /** Returns the first line of a file. */
    private static String firstLine(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        reader.close();
        return line;
    }

    // ---------- test 1: fares of the different bus types ----------

    private static void testFares() {
        System.out.println("\n--- Fares (polymorphism / Liskov: every bus type works as a Bus) ---");

        // one array holds three different kinds of bus
        Bus[] buses = new Bus[2];
        buses[0] = new OrdinaryBus("ABC-123", "Op", "Manila", BusStatus.ARRIVED, "1", "2026-01-01 08:00:00", "09:00");
        buses[1] = new ProvincialBus("GHI-789", "Op", "Manila", BusStatus.ARRIVED, "3", "2026-01-01 08:00:00", "09:00");

        check("OrdinaryBus: base 100 -> 100", sameNumber(buses[0].calculateFare(100), 100));
        check("ProvincialBus: base 100 -> 150 (+50 fee)", sameNumber(buses[1].calculateFare(100), 150));
        check("AIRCON is no longer a bus type", !BusFactory.withDefaultTypes().getTypeNames().contains("AIRCON"));

        // Liskov: the same rule must hold for EVERY subclass
        boolean allRejectNegative = true;
        for (int i = 0; i < buses.length; i++) {
            try {
                buses[i].calculateFare(-5);
                allRejectNegative = false;
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
        check("Negative base fare is rejected by every bus type", allRejectNegative);
    }

    // ---------- test 2: check-in rules ----------

    private static void testCheckIn() throws IOException {
        System.out.println("\n--- Check-in validation ---");
        Setup s = newSetup(newTempFolder());
        CheckInService service = s.checkIn;

        check("Blank plate is rejected",
                tryCheckIn(service, "", "ORDINARY", "Op", "Manila", "1", "09:00").equals("IllegalArgumentException"));
        check("Blank operator is rejected",
                tryCheckIn(service, "ABC-123", "ORDINARY", "", "Manila", "1", "09:00").equals("IllegalArgumentException"));
        check("Blank bay is rejected",
                tryCheckIn(service, "ABC-123", "ORDINARY", "Op", "Manila", "", "09:00").equals("IllegalArgumentException"));
        check("Bad plate (12-ABC) is rejected",
                tryCheckIn(service, "12-ABC", "ORDINARY", "Op", "Manila", "1", "09:00").equals("InvalidPlateException"));
        check("Bad plate (AB-123) is rejected",
                tryCheckIn(service, "AB-123", "ORDINARY", "Op", "Manila", "1", "09:00").equals("InvalidPlateException"));
        check("Unknown destination is rejected",
                tryCheckIn(service, "ABC-123", "ORDINARY", "Op", "Atlantis", "1", "09:00").equals("IllegalArgumentException"));
        check("Operator that is not registered is rejected",
                tryCheckIn(service, "ABC-123", "ORDINARY", "Nobody Liner", "Manila", "1", "09:00").equals("IllegalArgumentException"));
        check("Unknown bus type is rejected",
                tryCheckIn(service, "ABC-123", "FLYING", "Op", "Manila", "1", "09:00").equals("IllegalArgumentException"));
        check("Good check-in is accepted",
                tryCheckIn(service, "ABC-123", "PROVINCIAL", "Victory", "Manila", "1", "09:00").equals("OK"));
        check("Same plate again (still active) is rejected",
                tryCheckIn(service, "ABC-123", "PROVINCIAL", "Victory", "Manila", "1", "09:00").equals("DuplicateBusException"));
        check("New bus starts as ARRIVED",
                s.board.getActiveBusesByDestination().get("Manila").get(0).getStatus() == BusStatus.ARRIVED);

        // the bus leaves, then the same plate comes back later
        s.status.updateStatus("ABC-123", "LOADING");
        s.status.updateStatus("ABC-123", "DEPARTED");
        check("Same plate can check in again after it DEPARTED",
                tryCheckIn(service, "ABC-123", "PROVINCIAL", "Victory", "Manila", "2", "11:00").equals("OK"));
        check("History is kept: 2 rows in buses.csv", s.busRepo.findAll().size() == 2);
        check("Board shows only the 1 active bus", s.board.getActiveBusesByDestination().get("Manila").size() == 1);

        Bus activeBus = s.board.getActiveBusesByDestination().get("Manila").get(0);
        double fare = activeBus.calculateFare(s.fares.getBaseFare(activeBus.getDestination()));
        check("Provincial fare on the board: base 80 + 50 -> 130", sameNumber(fare, 130));
        check("Operator spelling is fixed to the registered one (op -> Op)",
                tryCheckIn(service, "QRS-777", "ORDINARY", "op", "Manila", "3", "12:00").equals("OK")
                        && s.busRepo.findById("QRS-777").getOperator().equals("Op"));
        check("Destination without a fare gives -1", sameNumber(s.fares.getBaseFare("Atlantis"), -1));
    }

    // ---------- test 3: status order ----------

    private static void testStatus() throws IOException {
        System.out.println("\n--- Status order ---");

        check("ARRIVED -> LOADING is allowed", BusStatus.ARRIVED.canMoveTo(BusStatus.LOADING));
        check("LOADING -> DEPARTED is allowed", BusStatus.LOADING.canMoveTo(BusStatus.DEPARTED));
        check("ARRIVED -> DEPARTED (skipping) is NOT allowed", !BusStatus.ARRIVED.canMoveTo(BusStatus.DEPARTED));
        check("LOADING -> ARRIVED (backward) is NOT allowed", !BusStatus.LOADING.canMoveTo(BusStatus.ARRIVED));
        check("DEPARTED -> LOADING (backward) is NOT allowed", !BusStatus.DEPARTED.canMoveTo(BusStatus.LOADING));
        check("DEPARTED has no next status", BusStatus.DEPARTED.next() == null);

        // the same rules, checked on a real Bus object
        Bus bus = new OrdinaryBus("ABC-123", "Op", "Manila", BusStatus.DEPARTED, "1", "2026-01-01 08:00:00", "09:00");
        boolean threw = false;
        try {
            bus.changeStatus(BusStatus.LOADING);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("bus.changeStatus(DEPARTED -> LOADING) throws an error", threw);

        // the same rules, checked through the service
        Setup s = newSetup(newTempFolder());
        s.checkIn.checkIn("ABC-123", "ORDINARY", "Op", "Manila", "1", "09:00");

        boolean skipRejected = false;
        try {
            s.status.updateStatus("ABC-123", "DEPARTED");
        } catch (IllegalArgumentException e) {
            skipRejected = true;
        }
        check("Service rejects ARRIVED -> DEPARTED", skipRejected);

        s.status.updateStatus("ABC-123", "LOADING");
        s.status.updateStatus("ABC-123", "DEPARTED");

        boolean backRejected = false;
        try {
            s.status.updateStatus("ABC-123", "LOADING");
        } catch (IllegalArgumentException e) {
            backRejected = true;
        }
        check("Service rejects DEPARTED -> LOADING", backRejected);

        boolean unknownPlateRejected = false;
        try {
            s.status.updateStatus("ZZZ-999", "LOADING");
        } catch (IllegalArgumentException e) {
            unknownPlateRejected = true;
        }
        check("Service rejects a plate that does not exist", unknownPlateRejected);
    }

    // ---------- test 4: CSV files that are missing, empty or broken ----------

    private static void testCsv() throws IOException {
        System.out.println("\n--- CSV problems (warnings printed on System.err are expected) ---");
        String folder = newTempFolder();
        BusFactory factory = BusFactory.withDefaultTypes();

        // missing files (and missing folders) must be created automatically
        String busPath = folder + "/new/sub/buses.csv";
        String farePath = folder + "/new/sub/fares.csv";
        String routePath = folder + "/new/sub/routes.csv";
        CsvBusRepository busRepo = new CsvBusRepository(busPath, factory);
        CsvFareRepository fareRepo = new CsvFareRepository(farePath);
        CsvRouteRepository routeRepo = new CsvRouteRepository(routePath);

        check("buses.csv is created automatically", new File(busPath).exists());
        check("fares.csv is created automatically", new File(farePath).exists());
        check("routes.csv is created automatically", new File(routePath).exists());
        check("buses.csv has the header row", firstLine(busPath).equals(CsvBusRepository.HEADER));
        check("fares.csv has the header row", firstLine(farePath).equals(CsvFareRepository.HEADER));
        check("routes.csv has the header row", firstLine(routePath).equals(CsvRouteRepository.HEADER));
        check("New files give empty lists",
                busRepo.findAll().isEmpty() && fareRepo.findAll().isEmpty() && routeRepo.findAll().isEmpty());

        // a completely empty file
        String emptyPath = folder + "/empty.csv";
        writeFile(emptyPath, "");
        CsvBusRepository emptyRepo = new CsvBusRepository(emptyPath, factory);
        check("Empty file does not crash (0 buses)", emptyRepo.findAll().size() == 0);

        // a file with good rows mixed with broken rows
        String brokenPath = folder + "/broken.csv";
        writeFile(brokenPath, CsvBusRepository.HEADER + "\n"
                + "ABC-123,ORDINARY,Op,Manila,LOADING,1,2026-01-01 08:00:00,09:00\n"
                + "\n"
                + "this is not csv at all\n"
                + ",,,,,,,\n"
                + "bad-plate,ORDINARY,Op,Manila,LOADING,1,2026-01-01 08:00:00,09:00\n"
                + "XYZ-111,SPACESHIP,Op,Manila,LOADING,1,2026-01-01 08:00:00,09:00\n"
                + "XYZ-222,ORDINARY,Op,Manila,FLYING,1,2026-01-01 08:00:00,09:00\n"
                + "DEF-4567,PROVINCIAL,Op,Baguio,ARRIVED,2,2026-01-01 09:00:00,10:00\n");
        CsvBusRepository brokenRepo = new CsvBusRepository(brokenPath, factory);
        List<Bus> loaded = brokenRepo.findAll();

        check("Broken rows are skipped, 2 good rows are kept", loaded.size() == 2);
        check("Good rows become the correct bus type",
                loaded.size() == 2 && loaded.get(0) instanceof OrdinaryBus && loaded.get(1) instanceof ProvincialBus);

        // broken fares and broken route stops
        String badFarePath = folder + "/badfares.csv";
        writeFile(badFarePath, "destination,baseFare\nManila,80\nBaguio,abc\nBatangas,-5\nOnlyOneColumn\n");
        check("Broken fare rows are skipped, 1 good fare kept", new CsvFareRepository(badFarePath).findAll().size() == 1);

        String badRoutePath = folder + "/badroutes.csv";
        writeFile(badRoutePath, "destination,stopOrder,stopName,road\nManila,1,Bacoor,Highway\nManila,x,Pasay,Roxas\nManila,0,Zero,Road\n");
        check("Broken route rows are skipped, 1 good stop kept", new CsvRouteRepository(badRoutePath).findAll().size() == 1);

        // the repository must still work after reading a broken file
        brokenRepo.update(loaded.get(0));
        check("Repository still works after a broken file", brokenRepo.findAll().size() == 2);
    }

    // ---------- test 5: the CSV repositories (shared base class) ----------

    private static void testRepositories() throws IOException {
        System.out.println("\n--- Repositories (CsvRepository base class) ---");
        String folder = newTempFolder();

        CsvFareRepository fares = new CsvFareRepository(folder + "/fares.csv");
        fares.save(new Fare("Manila", 80));
        check("Fare saved and found by destination (ignoring case)", fares.findById("manila") != null);

        boolean duplicateRejected = false;
        try {
            fares.save(new Fare("MANILA", 90));
        } catch (IllegalArgumentException e) {
            duplicateRejected = true;
        }
        check("Duplicate fare is rejected", duplicateRejected);

        fares.update(new Fare("Manila", 99));
        check("Fare can be updated", sameNumber(fares.findById("Manila").getBaseFare(), 99));
        check("Fare can be deleted", fares.delete("Manila") && fares.findAll().isEmpty());
        check("Deleting a missing fare returns false", !fares.delete("Manila"));

        CsvRouteRepository routes = new CsvRouteRepository(folder + "/routes.csv");
        routes.save(new RouteStop("Manila", 2, "Pasay", "Roxas Boulevard"));
        routes.save(new RouteStop("Manila", 1, "Bacoor", "Aguinaldo Highway"));
        routes.save(new RouteStop("Baguio", 1, "Tarlac", "TPLEX"));
        List<RouteStop> manila = routes.findByDestination("manila");
        check("Route stops of one destination are found", manila.size() == 2);
        check("Route stops are sorted by stop number", manila.get(0).getStopOrder() == 1 && manila.get(1).getStopOrder() == 2);
        check("Route stop found by id Manila#2", routes.findById("Manila#2") != null);

        CsvOperatorRepository operators = new CsvOperatorRepository(folder + "/operators.csv");
        operators.save(new Operator("Victory Liner", ""));
        operators.save(new Operator("JAC Liner", "0917-000-0000"));
        check("Operators are saved and found by name (ignoring case)", operators.findById("victory liner") != null);
        operators.update(new Operator("Victory Liner", "02-123-4567"));
        check("Operator contact number can be updated", operators.findById("Victory Liner").getContactNumber().equals("02-123-4567"));
        check("Operator with an empty contact number is read back correctly", operators.findAll().size() == 2);
        check("Operator can be deleted", operators.delete("JAC Liner") && operators.findAll().size() == 1);

        // bus history rules still work through the base class
        BusFactory factory = BusFactory.withDefaultTypes();
        CsvBusRepository buses = new CsvBusRepository(folder + "/buses.csv", factory);
        buses.save(new OrdinaryBus("ABC-123", "Op", "Manila", BusStatus.DEPARTED, "1", "2026-01-01 08:00:00", "09:00"));
        buses.save(new OrdinaryBus("ABC-123", "Op", "Manila", BusStatus.ARRIVED, "2", "2026-01-02 08:00:00", "09:00"));
        check("findById returns the NEWEST row of a plate", buses.findById("ABC-123").getBay().equals("2"));
        check("delete removes the whole history of a plate", buses.delete("ABC-123") && buses.findAll().isEmpty());
    }

    // ---------- test 6: SOLID checks ----------

    private static void testOpenClosed() {
        System.out.println("\n--- Open/Closed: a new bus type needs no change to existing classes ---");

        // a brand-new bus type, written only for this test
        class LuxuryBus extends Bus {
            LuxuryBus(String plate, String operator, String destination, BusStatus status,
                      String bay, String checkInTime, String estDeparture) {
                super(plate, operator, destination, status, bay, checkInTime, estDeparture);
            }

            @Override
            public double calculateFare(double baseFare) {
                checkBaseFare(baseFare);
                return baseFare * 2;
            }

            @Override
            public String getBusType() {
                return "LUXURY";
            }
        }

        BusFactory factory = BusFactory.withDefaultTypes().register("LUXURY", LuxuryBus::new);
        check("Factory lists the new type", factory.getTypeNames().contains("LUXURY"));
        Bus bus = factory.create("luxury", "ABC-123", "Op", "Manila", BusStatus.ARRIVED, "1", "2026-01-01 08:00:00", "09:00");
        check("Factory builds the new type (case-insensitive)", bus instanceof LuxuryBus);
        check("New type has its own fare formula", sameNumber(bus.calculateFare(100), 200));
        check("Default factory still knows 2 types", BusFactory.withDefaultTypes().getTypeNames().size() == 2);
    }

    private static void testDependencyInversion() {
        System.out.println("\n--- Dependency Inversion: services work with a fake in-memory repository ---");

        // no CSV file anywhere in this test
        InMemoryRepository<Bus> busRepo = new InMemoryRepository<Bus>(Bus::getPlate);
        InMemoryRepository<Fare> fareRepo = new InMemoryRepository<Fare>(Fare::getDestination);
        InMemoryRepository<RouteStop> unusedRoutes = new InMemoryRepository<RouteStop>(RouteStop::getId);
        InMemoryRepository<Operator> operatorRepo = new InMemoryRepository<Operator>(Operator::getName);
        fareRepo.save(new Fare("Manila", 80));
        operatorRepo.save(new Operator("Op", ""));

        BusFactory factory = BusFactory.withDefaultTypes();
        DestinationService destinations = new DefaultDestinationService(fareRepo, new RouteReadAdapter(unusedRoutes));
        CheckInService checkIn = new DefaultCheckInService(busRepo, destinations, new DefaultOperatorService(operatorRepo), factory);
        BoardService board = new DefaultBoardService(busRepo);

        check("Check-in works with an in-memory repository",
                tryCheckIn(checkIn, "ABC-123", "PROVINCIAL", "Op", "manila", "1", "09:00").equals("OK"));
        check("Fake repository holds the bus", busRepo.findAll().size() == 1);
        check("Destination spelling is fixed to the saved one (manila -> Manila)",
                board.getActiveBusesByDestination().containsKey("Manila"));
        check("Duplicate rule still works without CSV",
                tryCheckIn(checkIn, "ABC-123", "PROVINCIAL", "Op", "Manila", "1", "09:00").equals("DuplicateBusException"));
    }

    /** Test helper: lets the in-memory route repository be used as a RouteReadRepository. */
    private static class RouteReadAdapter implements com.byahero.repository.RouteReadRepository {
        private final Repository<RouteStop> inner;

        RouteReadAdapter(Repository<RouteStop> inner) {
            this.inner = inner;
        }

        @Override
        public List<RouteStop> findAll() {
            return inner.findAll();
        }

        @Override
        public RouteStop findById(String id) {
            return inner.findById(id);
        }

        @Override
        public List<RouteStop> findByDestination(String destination) {
            List<RouteStop> result = new java.util.ArrayList<RouteStop>();
            List<RouteStop> all = inner.findAll();
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getDestination().equalsIgnoreCase(destination)) {
                    result.add(all.get(i));
                }
            }
            return result;
        }
    }

    private static void testInterfaceSegregation() {
        System.out.println("\n--- Interface Segregation: read-only clients get read-only objects ---");

        // the board only needs ReadRepository, so a read-only object (not a full repository) is enough
        final Bus bus = new OrdinaryBus("ABC-123", "Op", "Manila", BusStatus.LOADING, "1", "2026-01-01 08:00:00", "09:00");
        com.byahero.repository.ReadRepository<Bus> readOnly = new com.byahero.repository.ReadRepository<Bus>() {
            @Override
            public List<Bus> findAll() {
                return List.of(bus);
            }

            @Override
            public Bus findById(String id) {
                return bus;
            }
        };
        BoardService board = new DefaultBoardService(readOnly);
        check("BoardService runs on a read-only repository", board.getActiveBusesByDestination().get("Manila").size() == 1);

        // the admin section depends only on FareAdminService, not on any repository
        InMemoryRepository<Fare> fareRepo = new InMemoryRepository<Fare>(Fare::getDestination);
        FareAdminService fareAdmin = new DefaultFareAdminService(fareRepo);
        AdminSection section = new FareAdminSection(fareAdmin);

        Map<String, String> form = new HashMap<String, String>();
        form.put("destination", "Batangas");
        form.put("baseFare", "150");
        check("Admin section 'add' returns its message", section.handle("add", form).equals("Fare added."));
        check("Admin section saved through the service", fareRepo.findAll().size() == 1);
        check("Admin section shows its rows", section.getPlaceholders().get("fareRows").contains("Batangas"));

        boolean unknownAction = false;
        try {
            section.handle("explode", form);
        } catch (IllegalArgumentException e) {
            unknownAction = true;
        }
        check("Admin section rejects an unknown action", unknownAction);

        boolean badNumber = false;
        try {
            form.put("baseFare", "abc");
            section.handle("update", form);
        } catch (IllegalArgumentException e) {
            badNumber = true;
        }
        check("Admin section rejects a non-numeric fare", badNumber);
    }

    private static void testOperatorAdmin() {
        System.out.println("\n--- Admin: operators ---");
        InMemoryRepository<Operator> repo = new InMemoryRepository<Operator>(Operator::getName);
        OperatorAdminService service = new DefaultOperatorAdminService(repo);
        AdminSection section = new OperatorAdminSection(service);

        Map<String, String> form = new HashMap<String, String>();
        form.put("name", "Victory Liner");
        form.put("contactNumber", "02-123-4567");
        check("Admin: add operator", section.handle("add", form).equals("Operator added.") && repo.findAll().size() == 1);
        form.put("contactNumber", "02-999-9999");
        check("Admin: update operator", section.handle("update", form).equals("Operator updated.")
                && repo.findById("Victory Liner").getContactNumber().equals("02-999-9999"));
        check("Admin: operator table shows the operator", section.getPlaceholders().get("operatorRows").contains("02-999-9999"));
        check("Admin: delete operator", section.handle("delete", form).equals("Operator deleted.") && repo.findAll().isEmpty());

        boolean blankRejected = false;
        try {
            form.put("name", "  ");
            section.handle("add", form);
        } catch (IllegalArgumentException e) {
            blankRejected = true;
        }
        check("Admin: blank operator name is rejected", blankRejected);

        boolean missingRejected = false;
        try {
            form.put("name", "Ghost Liner");
            section.handle("delete", form);
        } catch (IllegalArgumentException e) {
            missingRejected = true;
        }
        check("Admin: deleting a missing operator is rejected", missingRejected);
    }

    private static void testTemplates() {
        System.out.println("\n--- Templates ---");
        TemplateLoader loader = new TemplateLoader() {
            @Override
            public String load(String fileName) {
                return "Hi {{name}}! {{missing}} {{other}}";
            }
        };
        Map<String, String> values = new HashMap<String, String>();
        values.put("name", "{{other}}");   // a value that looks like a placeholder
        values.put("other", "X");
        String result = loader.render("t.html", values);
        check("Placeholders are filled in", result.startsWith("Hi "));
        check("A value that looks like {{placeholder}} is NOT filled in twice", result.contains("Hi {{other}}!"));
        check("Unknown placeholder stays visible", result.contains("{{missing}}"));
    }

    // ---------- test 7: the real web app, end to end ----------

    private static void testWebApp() throws Exception {
        System.out.println("\n--- Web app smoke test (real server on a free port) ---");

        // copy the real resources to a temp folder so the real CSV files are not changed
        Path temp = Files.createTempDirectory("byahero_web");
        Path source = new File(findResources()).toPath();
        try (var paths = Files.walk(source)) {
            for (Path p : (Iterable<Path>) paths::iterator) {
                Path target = temp.resolve(source.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(p, target);
                }
            }
        }

        HttpServer server = new AppConfig(temp.toString(), 0).createServer();
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        sessionCookie = null; // the test browser starts logged out

        try {
            HttpResponse<String> board = get(client, base + "/board");
            check("GET /board is 200", board.statusCode() == 200);
            check("Board shows a bus from buses.csv", board.body().contains("ABC-123"));
            check("Board shows the provincial fare (450 + 50 = 500.00)", board.body().contains("500.00"));
            check("Board has no leftover {{placeholders}}", !board.body().contains("{{"));
            check("Board lists buses under PITX and Cubao", board.body().contains("<h2>PITX</h2>") && board.body().contains("<h2>Cubao</h2>"));
            check("Board shows each bus's bay and estimated departure", board.body().contains("Bay 4") && board.body().contains("08:30"));
            check("Board shows the status of a bus", board.body().contains("status-loading") && board.body().contains("status-arrived"));
            check("Departed bus (JKL-321) is NOT on the board", !board.body().contains("JKL-321"));
            check("Board page loads the live-refresh script", board.body().contains("/static/board.js")
                    && get(client, base + "/static/board.js").body().contains("window.location.replace(\"/board\")"));

            check("GET / redirects to /board", get(client, base + "/").statusCode() == 303);

            HttpResponse<String> checkin = get(client, base + "/checkin");
            check("GET /checkin is 200", checkin.statusCode() == 200);
            check("Check-in page lists the bus types from the factory",
                    checkin.body().contains("value=\"ORDINARY\">Ordinary</option>")
                            && checkin.body().contains("value=\"PROVINCIAL\">Provincial</option>"));
            check("Check-in page has no leftover {{placeholders}}", !checkin.body().contains("{{"));
            check("Check-in operator is a dropdown of registered operators",
                    checkin.body().contains("<option value=\"Victory Liner\">") && checkin.body().contains("<option value=\"Five Star\">"));

            // ---- login: /admin is closed until the visitor logs in ----
            HttpResponse<String> closedGet = get(client, base + "/admin");
            check("Not logged in: GET /admin redirects to /login",
                    closedGet.statusCode() == 303 && closedGet.headers().firstValue("Location").orElse("").equals("/login"));
            HttpResponse<String> closedPost = post(client, base + "/admin", "entity=fare&action=add&destination=Hackville&baseFare=1");
            check("Not logged in: POST /admin is refused (303 to /login)", closedPost.statusCode() == 303);
            check("Not logged in: the refused POST changed nothing",
                    !new String(Files.readAllBytes(temp.resolve("data/fares.csv"))).contains("Hackville"));
            HttpResponse<String> loginPage = get(client, base + "/login");
            check("GET /login shows the form", loginPage.statusCode() == 200 && loginPage.body().contains("type=\"password\"")
                    && !loginPage.body().contains("{{"));
            check("The board stays public", get(client, base + "/board").statusCode() == 200);
            check("First start created admins.csv", new File(temp.toFile(), "data/admins.csv").exists());
            check("admins.csv does not contain the plain password",
                    !new String(Files.readAllBytes(temp.resolve("data/admins.csv"))).contains("admin1234"));

            HttpResponse<String> wrongLogin = post(client, base + "/login", "username=admin&password=nope-nope");
            check("Wrong password is 401 with a message", wrongLogin.statusCode() == 401 && wrongLogin.body().contains("Wrong username or password."));
            check("Unknown user gets the SAME message (does not reveal which users exist)",
                    post(client, base + "/login", "username=ghost&password=whatever1").body().contains("Wrong username or password."));
            check("Still locked out of /admin after a wrong login", get(client, base + "/admin").statusCode() == 303);

            HttpResponse<String> okLogin = post(client, base + "/login", "username=ADMIN&password=admin1234");
            String cookie = okLogin.headers().firstValue("Set-Cookie").orElse("");
            check("Good login redirects to /admin", okLogin.statusCode() == 303 && okLogin.headers().firstValue("Location").orElse("").equals("/admin"));
            check("Login cookie is HttpOnly and SameSite", cookie.contains("HttpOnly") && cookie.contains("SameSite=Lax"));
            sessionCookie = cookie.split(";")[0]; // the test "browser" now sends this cookie with every request
            check("GET /login while logged in goes to /admin", get(client, base + "/login").statusCode() == 303);

            HttpResponse<String> admin = get(client, base + "/admin");
            check("GET /admin is 200 and lists buses", admin.statusCode() == 200 && admin.body().contains("DEF-4567"));
            check("Admin page has bus type options", admin.body().contains("value=\"ORDINARY\">Ordinary</option>"));
            check("Admin page has an Operators section with the saved operators", admin.body().contains("<h2>Operators</h2>") && admin.body().contains("Saulog Transit"));
            check("Admin page has no leftover {{placeholders}}", !admin.body().contains("{{"));

            HttpResponse<String> route = get(client, base + "/route?destination=PITX");
            check("GET /route shows the stops", route.statusCode() == 200 && route.body().contains("Bacoor"));
            check("Route page shows the road used", route.body().contains("Aguinaldo Highway"));
            check("Route stops are in order (Bacoor before Pasay)", route.body().indexOf("Bacoor") < route.body().indexOf("Pasay"));
            check("Route page has an EMBEDDED map (iframe)", route.body().contains("<iframe") && route.body().contains("output=embed"));
            check("Route page for an unknown destination has no map", !get(client, base + "/route?destination=Atlantis").body().contains("<iframe"));
            check("Unknown route is 404", get(client, base + "/route?destination=Atlantis").statusCode() == 404);

            HttpResponse<String> posted = post(client, base + "/checkin",
                    "plate=NEW-111&busType=PROVINCIAL&operator=Five+Star&destination=pitx&bay=7&estDeparture=15%3A00");
            check("POST /checkin with good data is 200", posted.statusCode() == 200 && posted.body().contains("NEW-111 checked in"));
            HttpResponse<String> bad = post(client, base + "/checkin",
                    "plate=bad&busType=PROVINCIAL&operator=Five+Star&destination=PITX&bay=7&estDeparture=15%3A00");
            check("POST /checkin with a bad plate is 400", bad.statusCode() == 400);
            check("Form keeps what the user typed", bad.body().contains("value=\"bad\"") && bad.body().contains("<option value=\"Five Star\" selected>"));
            HttpResponse<String> badOperator = post(client, base + "/checkin",
                    "plate=NEW-222&busType=ORDINARY&operator=Ghost+Liner&destination=PITX&bay=8&estDeparture=16%3A00");
            check("POST /checkin with an unregistered operator is 400", badOperator.statusCode() == 400 && badOperator.body().contains("Unknown operator"));
            check("A new check-in shows on the board with its fare (PITX 80 + 50 = 130.00)",
                    get(client, base + "/board").body().contains("NEW-111") && get(client, base + "/board").body().contains("130.00"));

            HttpResponse<String> status = post(client, base + "/status", "plate=NEW-111&nextStatus=LOADING");
            check("POST /status moves a bus (redirect)", status.statusCode() == 303);
            post(client, base + "/status", "plate=NEW-111&nextStatus=DEPARTED");
            check("A bus that DEPARTED disappears from the board", !get(client, base + "/board").body().contains("NEW-111"));
            HttpResponse<String> skip = post(client, base + "/status", "plate=DEF-4567&nextStatus=DEPARTED");
            check("POST /status skipping a step is 400", skip.statusCode() == 400);

            HttpResponse<String> addFare = post(client, base + "/admin", "entity=fare&action=add&destination=Tagaytay&baseFare=60");
            check("Admin: add fare works", addFare.statusCode() == 200 && addFare.body().contains("Fare added."));
            HttpResponse<String> addOperator = post(client, base + "/admin", "entity=operator&action=add&name=Genesis+Transport&contactNumber=02-555-0100");
            check("Admin: add operator works", addOperator.statusCode() == 200 && addOperator.body().contains("Operator added."));
            check("Admin: new operator is listed and usable at check-in",
                    addOperator.body().contains("Genesis Transport") && get(client, base + "/checkin").body().contains("<option value=\"Genesis Transport\">"));
            HttpResponse<String> updBus = post(client, base + "/admin", "entity=bus&action=update&plate=DEF-4567&operator=genesis+transport&bay=9");
            check("Admin: edit a bus (operator and bay)", updBus.statusCode() == 200 && updBus.body().contains("Bus updated."));
            HttpResponse<String> updBadOp = post(client, base + "/admin", "entity=bus&action=update&plate=DEF-4567&operator=Ghost+Liner");
            check("Admin: editing a bus to an unregistered operator is 400", updBadOp.statusCode() == 400);
            HttpResponse<String> updRoute = post(client, base + "/admin", "entity=route&action=add&destination=PITX&stopOrder=3&stopName=Sucat&road=SLEX");
            check("Admin: add a route stop works", updRoute.statusCode() == 200 && updRoute.body().contains("Route stop added."));
            HttpResponse<String> badEntity = post(client, base + "/admin", "entity=unicorn&action=add");
            check("Admin: unknown form is 400", badEntity.statusCode() == 400);

            // ---- admin accounts, logout, lockout ----
            check("Admin page has the Admin accounts section", admin.body().contains("<h2>Admin accounts</h2>")
                    && admin.body().contains("<td>admin</td>") && admin.body().contains("action=\"/logout\""));
            HttpResponse<String> shortPw = post(client, base + "/admin", "entity=account&action=add&username=staff1&password=short");
            check("Admin: a short password is rejected (400)", shortPw.statusCode() == 400 && shortPw.body().contains("at least 8"));
            HttpResponse<String> addAcc = post(client, base + "/admin", "entity=account&action=add&username=staff1&password=staff-pass-1");
            check("Admin: add an admin account", addAcc.statusCode() == 200 && addAcc.body().contains("Admin account added.") && addAcc.body().contains("<td>staff1</td>"));
            check("Admin: the table never shows a password or hash", !addAcc.body().contains("staff-pass-1")
                    && !new String(Files.readAllBytes(temp.resolve("data/admins.csv"))).contains("staff-pass-1"));
            check("Admin: change password works", post(client, base + "/admin", "entity=account&action=update&username=staff1&password=new-staff-pass").statusCode() == 200);
            check("Admin: delete the extra account", post(client, base + "/admin", "entity=account&action=delete&username=staff1").statusCode() == 200);
            HttpResponse<String> lastAdmin = post(client, base + "/admin", "entity=account&action=delete&username=admin");
            check("Admin: the LAST account cannot be deleted", lastAdmin.statusCode() == 400 && lastAdmin.body().contains("last admin"));

            String oldCookie = sessionCookie;
            HttpResponse<String> logout = post(client, base + "/logout", "");
            sessionCookie = null;
            check("Logout redirects to /login and clears the cookie", logout.statusCode() == 303
                    && logout.headers().firstValue("Set-Cookie").orElse("").contains("Max-Age=0"));
            check("After logout /admin is closed again", get(client, base + "/admin").statusCode() == 303);
            sessionCookie = oldCookie;
            check("The OLD cookie is dead on the server too (not just removed in the browser)", get(client, base + "/admin").statusCode() == 303);
            sessionCookie = null;

            for (int i = 0; i < 5; i++) {
                post(client, base + "/login", "username=admin&password=wrong-guess" + i);
            }
            HttpResponse<String> locked = post(client, base + "/login", "username=admin&password=admin1234");
            check("5 wrong tries lock the account (even the right password is refused)",
                    locked.statusCode() == 401 && locked.body().contains("Too many wrong tries"));

            check("Static CSS is served", get(client, base + "/static/style.css").statusCode() == 200);
            check("Static path traversal is blocked", get(client, base + "/static/..%2Fdata%2Fbuses.csv").statusCode() == 404);
        } finally {
            server.stop(0);
        }
    }

    private static String findResources() {
        String[] choices = { "resources", "../resources", "ByaHero/resources" };
        for (int i = 0; i < choices.length; i++) {
            if (new File(choices[i] + "/templates/board.html").exists()) {
                return choices[i];
            }
        }
        return "resources";
    }

    /** The cookie the test "browser" sends (null = not logged in). */
    private static String sessionCookie = null;

    private static HttpResponse<String> get(HttpClient client, String url) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).GET();
        if (sessionCookie != null) {
            request.header("Cookie", sessionCookie);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(HttpClient client, String url, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (sessionCookie != null) {
            request.header("Cookie", sessionCookie);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    // ---------- test: login, passwords and sessions ----------

    private static void testLogin() throws IOException {
        System.out.println("\n--- Login: passwords, accounts and sessions ---");

        String salt = com.byahero.util.PasswordHasher.newSalt();
        String hash = com.byahero.util.PasswordHasher.hash("secret-pass", salt);
        check("Hash is not the password", !hash.contains("secret-pass"));
        check("Same password + same salt gives the same hash", hash.equals(com.byahero.util.PasswordHasher.hash("secret-pass", salt)));
        check("Different salt gives a different hash",
                !hash.equals(com.byahero.util.PasswordHasher.hash("secret-pass", com.byahero.util.PasswordHasher.newSalt())));
        check("matches() accepts the right password and rejects a wrong one",
                com.byahero.util.PasswordHasher.matches("secret-pass", salt, hash)
                        && !com.byahero.util.PasswordHasher.matches("Secret-pass", salt, hash));

        // the service works on a fake repository (dependency inversion again: no file needed)
        Repository<AdminAccount> repo = new InMemoryRepository<AdminAccount>(AdminAccount::getUsername);
        DefaultAdminAccountService accounts = new DefaultAdminAccountService(repo);
        AuthService auth = new DefaultAuthService(repo);

        check("First admin is created when there is none", accounts.createFirstAdminIfNone("admin", "first-pass-1"));
        check("No second default admin is created", !accounts.createFirstAdminIfNone("admin2", "first-pass-2"));
        check("Login works with the right password", auth.login("admin", "first-pass-1").equals("admin"));
        check("Login ignores upper/lower case of the username", auth.login("ADMIN", "first-pass-1").equals("admin"));

        boolean wrongRejected = false;
        try {
            auth.login("admin", "wrong-pass");
        } catch (IllegalArgumentException e) {
            wrongRejected = true;
        }
        check("Login rejects a wrong password", wrongRejected);

        boolean blankRejected = false;
        try {
            auth.login("", "");
        } catch (IllegalArgumentException e) {
            blankRejected = true;
        }
        check("Login rejects blank fields", blankRejected);

        boolean badNameRejected = false;
        try {
            accounts.addAccount("a b", "long-enough-pass");
        } catch (IllegalArgumentException e) {
            badNameRejected = true;
        }
        check("Username with spaces is rejected", badNameRejected);

        accounts.changePassword("admin", "changed-pass-1");
        boolean oldRejected = false;
        try {
            auth.login("admin", "first-pass-1");
        } catch (IllegalArgumentException e) {
            oldRejected = true;
        }
        check("After a password change the OLD password stops working", oldRejected && auth.login("admin", "changed-pass-1").equals("admin"));

        // admins.csv round trip
        String file = newTempFolder() + "/admins.csv";
        CsvAdminAccountRepository csv = new CsvAdminAccountRepository(file);
        new DefaultAdminAccountService(csv).addAccount("csvadmin", "csv-password-1");
        check("admins.csv has the header row", firstLine(file).equals(CsvAdminAccountRepository.HEADER));
        check("Account saved in CSV can log in", new DefaultAuthService(csv).login("csvadmin", "csv-password-1").equals("csvadmin"));

        // sessions
        SessionService sessions = new InMemorySessionService();
        String token = sessions.start("admin");
        check("Session token gives back the username", "admin".equals(sessions.getUsername(token)));
        check("Two sessions get different tokens", !token.equals(sessions.start("admin")));
        check("Unknown or missing token is not logged in", sessions.getUsername("nope") == null && sessions.getUsername(null) == null);
        sessions.end(token);
        check("Ended session is no longer valid", sessions.getUsername(token) == null);

        // the admin section shows usernames only
        AdminAccountSection section = new AdminAccountSection(accounts);
        String rows = section.getPlaceholders().get("accountRows");
        check("Account section lists usernames but no hashes", rows.contains("admin") && !rows.contains(hash));
    }

    // ---------- run everything ----------

    public static void main(String[] args) throws Exception {
        testFares();
        testCheckIn();
        testStatus();
        testCsv();
        testRepositories();
        testOpenClosed();
        testDependencyInversion();
        testInterfaceSegregation();
        testOperatorAdmin();
        testLogin();
        testTemplates();
        testWebApp();

        System.out.println("\n==============================");
        System.out.println("Passed: " + passed + "   Failed: " + failed);

        // exit explicitly: the web server's worker threads would keep the program alive otherwise
        System.exit(failed > 0 ? 1 : 0);
    }
}
