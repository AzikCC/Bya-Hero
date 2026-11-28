package com.byahero.config;

import com.byahero.controller.AdminController;
import com.byahero.controller.AuthGuard;
import com.byahero.controller.BoardController;
import com.byahero.controller.CheckInController;
import com.byahero.controller.LoginController;
import com.byahero.controller.LogoutController;
import com.byahero.controller.RootController;
import com.byahero.controller.RouteController;
import com.byahero.controller.StaticController;
import com.byahero.controller.StatusController;
import com.byahero.controller.admin.AdminSection;
import com.byahero.controller.admin.AdminAccountSection;
import com.byahero.controller.admin.BusAdminSection;
import com.byahero.controller.admin.FareAdminSection;
import com.byahero.controller.admin.OperatorAdminSection;
import com.byahero.controller.admin.RouteAdminSection;
import com.byahero.model.AdminAccount;
import com.byahero.model.Bus;
import com.byahero.model.BusFactory;
import com.byahero.model.Fare;
import com.byahero.model.Operator;
import com.byahero.repository.Repository;
import com.byahero.repository.RouteRepository;
import com.byahero.repository.csv.CsvAdminAccountRepository;
import com.byahero.repository.csv.CsvBusRepository;
import com.byahero.repository.csv.CsvFareRepository;
import com.byahero.repository.csv.CsvOperatorRepository;
import com.byahero.repository.csv.CsvRouteRepository;
import com.byahero.service.AdminAccountService;
import com.byahero.service.AuthService;
import com.byahero.service.BoardService;
import com.byahero.service.BusAdminService;
import com.byahero.service.CheckInService;
import com.byahero.service.DestinationService;
import com.byahero.service.FareAdminService;
import com.byahero.service.FareService;
import com.byahero.service.OperatorAdminService;
import com.byahero.service.OperatorService;
import com.byahero.service.RouteAdminService;
import com.byahero.service.RouteService;
import com.byahero.service.SessionService;
import com.byahero.service.StatusService;
import com.byahero.service.impl.DefaultAdminAccountService;
import com.byahero.service.impl.DefaultAuthService;
import com.byahero.service.impl.DefaultBoardService;
import com.byahero.service.impl.DefaultBusAdminService;
import com.byahero.service.impl.DefaultCheckInService;
import com.byahero.service.impl.DefaultDestinationService;
import com.byahero.service.impl.DefaultFareAdminService;
import com.byahero.service.impl.DefaultFareService;
import com.byahero.service.impl.DefaultOperatorAdminService;
import com.byahero.service.impl.DefaultOperatorService;
import com.byahero.service.impl.DefaultRouteAdminService;
import com.byahero.service.impl.DefaultRouteService;
import com.byahero.service.impl.DefaultStatusService;
import com.byahero.service.impl.InMemorySessionService;
import com.byahero.view.BoardPage;
import com.byahero.view.FileTemplateLoader;
import com.byahero.view.TemplateLoader;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * DEPENDENCY INVERSION: this is the ONE place that picks concrete classes
 * (CSV files, Default... services) and plugs them into the classes that only
 * know interfaces. To switch from CSV to a database you would change the
 * three "repositories" lines below, and nothing else.
 *
 * Wiring order:  repositories -> services -> views -> controllers -> web server
 */
public class AppConfig {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin1234";

    private final String resourceFolder;
    private final int port;

    public AppConfig(String resourceFolder, int port) {
        this.resourceFolder = resourceFolder;
        this.port = port;
    }

    /** Creates every object, connects them, and returns a server that is ready to start(). */
    public HttpServer createServer() throws IOException {

        // 1. repositories (they read and write the CSV files)
        BusFactory busFactory = BusFactory.withDefaultTypes();
        Repository<Bus> busRepo = new CsvBusRepository(resourceFolder + "/data/buses.csv", busFactory);
        Repository<Fare> fareRepo = new CsvFareRepository(resourceFolder + "/data/fares.csv");
        RouteRepository routeRepo = new CsvRouteRepository(resourceFolder + "/data/routes.csv");
        Repository<Operator> operatorRepo = new CsvOperatorRepository(resourceFolder + "/data/operators.csv");
        Repository<AdminAccount> accountRepo = new CsvAdminAccountRepository(resourceFolder + "/data/admins.csv");

        // 2. services (each one gets only the interfaces it needs)
        DestinationService destinations = new DefaultDestinationService(fareRepo, routeRepo);
        OperatorService operators = new DefaultOperatorService(operatorRepo);
        FareService fareService = new DefaultFareService(fareRepo);
        BoardService boardService = new DefaultBoardService(busRepo);
        StatusService statusService = new DefaultStatusService(busRepo);
        CheckInService checkInService = new DefaultCheckInService(busRepo, destinations, operators, busFactory);
        RouteService routeService = new DefaultRouteService(routeRepo);
        FareAdminService fareAdmin = new DefaultFareAdminService(fareRepo);
        RouteAdminService routeAdmin = new DefaultRouteAdminService(routeRepo);
        OperatorAdminService operatorAdmin = new DefaultOperatorAdminService(operatorRepo);
        BusAdminService busAdmin = new DefaultBusAdminService(busRepo, checkInService, destinations, operators);
        DefaultAdminAccountService accountAdmin = new DefaultAdminAccountService(accountRepo);
        AuthService authService = new DefaultAuthService(accountRepo);
        SessionService sessions = new InMemorySessionService();

        // first start: no admin exists yet, so create a default one (change its password after logging in!)
        if (accountAdmin.createFirstAdminIfNone(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD)) {
            System.out.println("No admin account found. Created the default one:");
            System.out.println("    username: " + DEFAULT_ADMIN_USERNAME + "    password: " + DEFAULT_ADMIN_PASSWORD);
            System.out.println("    Log in at /login and change this password on the Admin page.");
        }

        // 3. views (build HTML)
        TemplateLoader templates = new FileTemplateLoader(resourceFolder + "/templates");
        BoardPage boardPage = new BoardPage(templates, boardService, fareService);

        // 4. admin sections (add a new one to this list to get a new admin feature)
        List<AdminSection> adminSections = new ArrayList<AdminSection>();
        adminSections.add(new FareAdminSection(fareAdmin));
        adminSections.add(new RouteAdminSection(routeAdmin));
        adminSections.add(new OperatorAdminSection(operatorAdmin));
        adminSections.add(new BusAdminSection(busAdmin));
        adminSections.add(new AdminAccountSection(accountAdmin));

        // 5. create the web server and connect each address to its controller
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootController());
        server.createContext("/board", new BoardController(boardPage));
        server.createContext("/checkin", new CheckInController(checkInService, destinations, operators, templates));
        server.createContext("/status", new StatusController(statusService, boardPage));
        server.createContext("/route", new RouteController(routeService, destinations, templates));
        server.createContext("/login", new LoginController(authService, sessions, templates));
        server.createContext("/logout", new LogoutController(sessions));
        // the AuthGuard is the gate: only a logged-in admin gets through to AdminController
        server.createContext("/admin", new AuthGuard(sessions, new AdminController(adminSections, templates)));
        server.createContext("/static", new StaticController(resourceFolder + "/static"));

        // let the server handle up to 8 requests at the same time
        server.setExecutor(Executors.newFixedThreadPool(8));
        return server;
    }
}
