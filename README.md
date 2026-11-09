# Bya-Hero: A Live Bus Board at the Batangas Grand Terminal
## Project Description
Bya-Hero is a Java-based web application that gives commuters a live view of the buses currently at the Batangas Grand Terminal. A dispatcher checks in each bus when it arrives by entering its plate number, operator, and destination. The Java back-end saves each check-in as a row in a CSV file, which serves as the database. The public web page, built with HTML and CSS, shows every bus in the terminal grouped by destination (for example PITX, Buendia, and Cubao), along with its fare and status, and refreshes every minute. Each bus links to a route page that lists its stops.
## Problems being addressed
- Bus fares are not posted in one place, so passengers can be unsure what they should pay before boarding.
- There is no way to tell which buses in the terminal are still loading or about to leave. Routes differ between buses, and passengers cannot compare stops, drop-off points, or the roads used.
- Waiting times are uncertain, which is hardest on first-time commuters, students, and senior citizens.
## Project Objectives
General Objective To develop a Java-based application that gives commuters and terminal staff a live, organized view of buses, fares, statuses, and routes at the Batangas Grand Terminal.
### Specific Objectives
- Allow dispatchers to register (check in) each arriving bus with its plate number, operator, and destination.
Display all buses at the terminal grouped by destination, with their fare and current status.
- Show bus status (arrived, loading, or departed) so passengers know which bus is about to leave.
- Provide a route page with an ordered list of stops for each bus.
- Store check-in data persistently in a CSV file.
- Apply the four OOP principles (abstraction, encapsulation, inheritance, polymorphism) in the design.
- Validate user input and handle errors without crashing
## Target Users
| Target Users | Description |
| --- | --- |
| Commuters | View the live board to find buses by destination, check fares and status, and open a route page to see the stops. |
| Dispatcher | Uses the staff check-in page to register arriving buses and update their status (arrived, loading, departed). |
## Main Features
* Live Bus Board - Lists buses by destination (ie: PITX, Cubao) which updates every check-in
* Fare Display - Shows the fare per bus
* Bus Status - Show arrived, loading, or departed along with estimated departure time and bay.
* Route Page - Shows ordered stops, the road used, and an embedded map.
* Staff area-in page - Registers a bus and fills in destination, operator, and plate number.
* Status updates - Bus station changes and removes departed bus from the board
* Admin Page - Edits fares, routes, buses, and operators
## Technologies Used
| Technology | Description |
| --- | --- |
| Front-end | HTML & CSS |
| Back-end | Java |
| Database | CSV File |
| APIs | Routes API |
## Team Members and Roles
| Member | Roles |
| --- | --- |
| Azik Carandang | Project Leader |
| Alliah Mae Mitra | System/Design Lead |
| Tristan Lei Quinto | Lead Programmer |
## OOP Concepts Demonstrated
tba
## Instructions for Running The Program
tba
## Sample Screenshots
tba
## Known Limitations
tba
## Future Improvements
tba
## Project Contributors
tba
