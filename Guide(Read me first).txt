BYAHERO - HOW TO READ AND CHANGE THE CODE

Run:    run.sh (or run.bat), then open http://localhost:8080/board
        (or in your IDE: run src/com/byahero/Main.java)
Tests:  test.sh (or test.bat)
        (or in your IDE: run test/com/byahero/TestRunner.java)
        The tests use temporary folders, so your real CSV files are never touched.


THE FEATURES (these 7 are the only features of ByaHero)
-------------------------------------------------------------------------------
 1. Live Bus Board     /board    buses grouped by destination (PITX, Cubao...).
                                 Reloads itself every 10 seconds, so every new
                                 check-in appears.                (BoardPage, board.js)
 2. Fare Display       /board    the fare of each bus: base fare of the destination
                                 plus the bus type's own formula. (Bus subclasses)
 3. Bus Status         /board    ARRIVED / LOADING / DEPARTED, with the estimated
                                 departure time and the bay.
 4. Route Page         /route    ordered stops, the road used, and an embedded map.
                                 (the map needs internet in the browser)
 5. Staff Check-in     /checkin  registers a bus: plate, destination, operator, ...
 6. Status Updates     /status   the button on each bus moves it forward; a DEPARTED
                                 bus is removed from the board.
 7. Admin Page         /admin    edit fares, routes, buses, operators and admin accounts.
                                 Needs a login (see "ADMIN LOGIN" below).

 The data lives in resources/data: buses.csv, fares.csv, routes.csv, operators.csv, admins.csv.
 An operator must be registered on the Admin page before a bus can use it.


ADMIN LOGIN
-------------------------------------------------------------------------------
 Only /admin is protected. The board, check-in, status and route pages stay public.
 First start: if admins.csv has no accounts, the app creates one and prints it:
        username: admin     password: admin1234
 Log in at /login, then change that password in "Admin accounts" at the bottom of
 the Admin page (type the username, a new password, press "Change password").
   - Passwords are never saved as text: only a salted PBKDF2 hash (util/PasswordHasher).
   - Login = a random token in an HttpOnly cookie, kept in memory for 2 hours.
     Restarting the program logs everyone out.
   - 5 wrong passwords for a username lock it for 5 minutes.
   - The last admin account cannot be deleted. Forgot every password? Stop the app,
     delete resources/data/admins.csv, start again: the default admin is recreated.
 Code: AuthGuard (the gate in front of AdminController), LoginController,
 LogoutController, SessionCookie, DefaultAuthService, InMemorySessionService,
 DefaultAdminAccountService, AdminAccountSection, model/AdminAccount.
 To protect another page, wrap it in AppConfig: new AuthGuard(sessions, controller).
 Note: this is plain HTTP on localhost. If you ever put the site on the internet,
 put it behind HTTPS and add "; Secure" to the cookie in SessionCookie.

1. THE FOLDERS (one folder = one job)
-------------------------------------------------------------------------------
 src/com/byahero/
   Main.java              starts the app (10 lines of real work)
   config/AppConfig       the ONLY place that connects everything together
   model/                 Bus (+2 types), BusFactory, BusStatus, Fare, RouteStop, Operator
   repository/            the interfaces: ReadRepository, WriteRepository, Repository, ...
   repository/csv/        the CSV versions of those interfaces + CsvUtil
   service/               the interfaces: one small interface per use case
   service/impl/          the Default... classes with the real rules
   controller/            reads the browser request, answers with a page
   controller/admin/      one class per admin part (fares, route stops, operators, buses)
   view/                  builds HTML: BoardPage, HtmlUtil, TemplateLoader
   util/                  small helpers: Validator, InputParser, FormParser, DateUtil
   exception/             our own exceptions
 test/                    TestRunner + fake repositories for testing
 resources/               data/ (CSV), templates/ (HTML), static/ (CSS, JS)

How one request travels:
   Browser -> Controller -> Service -> Repository -> CSV file
   (the Controller may use a View to build the HTML)


2. SUGGESTED READING ORDER
-------------------------------------------------------------------------------
 1. model/BusStatus.java, model/Bus.java, model/OrdinaryBus.java (+ ProvincialBus)
 2. model/BusFactory.java            how a bus type name becomes an object
 3. repository/Repository.java       the interfaces (read the 4 small ones first)
 4. repository/csv/CsvRepository.java   the shared "read file, change list, write file"
    repository/csv/CsvFareRepository.java   a tiny child (look how short it is)
 5. service/CheckInService.java      then service/impl/DefaultCheckInService.java
 6. controller/BaseController.java, controller/CheckInController.java
 7. view/BoardPage.java              builds the board
 8. config/AppConfig.java            see how all the pieces are plugged together

You do NOT need to read: util/, controller/StaticController.java, resources/static/.


3. SOLID - WHERE EACH PRINCIPLE IS IN THE CODE
-------------------------------------------------------------------------------
 S  Single Responsibility - every class has one job
      CsvRepository      only CSV storage          BusFactory     only builds buses
      DefaultCheckInService only check-in rules    DefaultStatusService only status moves
      BoardPage          only board HTML           BoardController only handles /board
      (the old BusService did 7 jobs; the old repositories repeated the same code 3 times)

 O  Open/Closed - add things by ADDING classes, not by editing old ones
      New bus type    -> new Bus subclass + 1 register(...) line in BusFactory
      New admin part  -> new AdminSection class + 1 add(...) line in AppConfig
                         (the operators section was added exactly this way)
      New storage     -> new class that implements Repository<T>

 L  Liskov Substitution - a child can be used wherever its parent is expected
      OrdinaryBus / ProvincialBus both work as a Bus and all reject a
      negative fare (the tests loop over every type to prove it).
      Every Csv...Repository works as a Repository<T>.

 I  Interface Segregation - small interfaces, so a class only sees what it needs
      Repository<T> is split into ReadRepository<T> + WriteRepository<T>.
        The board only gets ReadRepository, so it CANNOT save or delete.
      One small service interface per page: CheckInService, StatusService,
        BoardService, FareService, DestinationService, OperatorService, RouteService,
        and FareAdminService / RouteAdminService / OperatorAdminService / BusAdminService.
        (RouteController used to receive the huge BusService just to list destinations.)

 D  Dependency Inversion - depend on interfaces, not on concrete classes
      Services and controllers only mention interfaces (Repository, CheckInService,
      TemplateLoader...). They never write "new CsvBusRepository".
      AppConfig is the only class that does, so switching from CSV to a database
      means changing 3 lines in AppConfig.
      The tests prove it: DefaultCheckInService runs on an InMemoryRepository
      (test/.../fakes) with no file at all.


4. HOW DO I...
-------------------------------------------------------------------------------
 ...add a new bus type (example: LUXURY, fare = base x 2)?
     1. Copy model/OrdinaryBus.java -> LuxuryBus.java, set TYPE = "LUXURY", write calculateFare.
     2. In BusFactory.withDefaultTypes() add:  factory.register(LuxuryBus.TYPE, LuxuryBus::new);
     3. Done. The check-in form, the admin form, the CSV and the board pick it up.
        (Optional: add a .type-luxury color in resources/static/style.css.)

 ...add a new admin section?
     1. Write a service interface + Default... class for it.
     2. Copy controller/admin/FareAdminSection.java and change it.
     3. Add the section to the list in AppConfig, and a form + {{...Rows}} table in admin.html.

 ...change a rule (plate format, fare formula, status order)?
     plate format  -> model/Bus.java (PLATE_REGEX)
     fare formula  -> the bus subclass in model/
     status order  -> model/BusStatus.java
     check-in rules-> service/impl/DefaultCheckInService.java

 ...change a page's look?  resources/templates/*.html and resources/static/style.css.
     Placeholders look like {{name}}; they are filled in by the controller or BoardPage.

 ...swap CSV for a database?
     Write classes that implement Repository<Bus>, Repository<Fare> and RouteRepository,
     then use them in AppConfig. Nothing else changes.


5. OOP WORDS AND WHERE THEY ARE
-------------------------------------------------------------------------------
   Abstraction     abstract class Bus, interface Repository<T>, interface TemplateLoader
   Inheritance     the 2 bus types extend Bus; CsvRepository -> Csv...Repository;
                   controllers extend BaseController; admin sections extend BaseAdminSection
   Polymorphism    bus.calculateFare(...) runs the formula of the real bus type
   Encapsulation   private fields + getters/setters with checks
   Enum            BusStatus
   Own exceptions  package exception
