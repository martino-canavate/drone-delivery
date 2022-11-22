package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;


/**
 * Main class from where the application is run. It contains all the methods
 * that calculate the flightpath.
 */
public class App {
    /**
     * This will be the sql database server initiated using the port.
     */
    private final ApacheDB apacheData;
    /**
     * This is a database containing all the orders, coordinates of pick-up and drop-off locations,
     * and the price of all the  deliveries for the day.
     * The database's orders will be ordered from most expensive to least expensive in order to
     * increase the "sampled average percentage monetary value" by following a greedy-like algorithm.
     */
    public Database theDatabase;
    /**
     * Coordinates of a location where the drone is directed to optimise the flightpath.
     */
    public static LongLat northEast = new LongLat(-3.1879, 55.9452);
    /**
     * Coordinates of a location where the drone is directed to optimise the flightpath.
     */
    public static LongLat southWest = new LongLat(-3.1916, 55.9437);
    /**
     * Array list of coordinates that stores the calculated flightpath.
     */
    public static ArrayList<LongLat> finalCoordinates = new ArrayList<>();
    /**
     * Counter that keeps track of the moves left.
     */
    public static int movesLeft = 1500;
    /**
     * The Return address (AppletonTower).
     */
    public final LongLat returnAddress = new LongLat(-3.186874,55.944494);
    /**
     * The Current position of the drone.
     */
    public LongLat currentPosition;
    /**
     * All the lines that form the no-fly zone's perimeter are stored here
     */
    public static ArrayList<LongLat.line> noFlyPerimeter = new ArrayList<>();
    /**
     * The date of the delivery day is stored here.
     */
    public static String fileDate;
    /**
     * This helps access the webpage, which is initialized using the web port.
     */
    public static Website actualWeb;

    /**
     * Instantiates the app for a new day.
     *
     * @param day          the day
     * @param month        the month
     * @param year         the year
     * @param webPort      the web port
     * @param databasePort the database port
     */
    public App(String day, String month, String year, int webPort, int databasePort) {
        String machineName = "localhost"; //Change this parameter to mach your machine name
        this.apacheData = new ApacheDB(machineName, String.valueOf(databasePort));
        actualWeb = new Website(machineName, String.valueOf(webPort));
        noFlyPerimeter = actualWeb.getNoFlyPerimeter();
        this.currentPosition = returnAddress;
        this.theDatabase = new Database(actualWeb);
        fileDate = day+"-"+month+"-"+year;
        apacheData.getDayOrders(Date.valueOf(year+"-"+month+"-"+day), theDatabase);
        theDatabase.greedyTable();
    }

    /**
     * Checks if the move is allowed, i.e. if the move makes the drone enter
     * a no-fly zone or if it makes the drone get outside the allowed area it
     * will return false.
     *
     * It checks if the drone is getting into the no-fly area by checking if the drone
     * move will cross any of the lines that compose the perimeter in @noFlyPerimeter
     *
     * @param initialPos the input arguments
     * @param nextPos the input arguments
     * @return true if the move is legal and false if it is illegal
     */
    private static boolean isMoveGood(LongLat initialPos, LongLat nextPos){
        if (!nextPos.isConfined()){
            return false;
        }
        for (LongLat.line l : noFlyPerimeter){
            if (LongLat.doesIntersect(initialPos, nextPos,l.getStartPoint(),l.getEndPoint())){
                return false;
            }
        }
        return true;
    }

    /**
     * It calculates the desired step direction to reach the destination (finalPos).
     * If the desired angle makes the move not legal it will select the next best angle
     * until the move is legal.
     *
     * @param initialPos the input arguments
     * @param finalPos the input arguments
     * @return the most appropriate angle value
     */
    private static int goodAngle(LongLat initialPos, LongLat finalPos){
        int angleInt = initialPos.getAngle(finalPos);

        for (int i=0; i<18;i++){
            if (isMoveGood(initialPos, initialPos.nextPosition(angleInt + i*10))){
                return angleInt+(i*10);
            }
            if (isMoveGood(initialPos, initialPos.nextPosition(angleInt - i*10))){
                return angleInt-(i*10);
            }
        }
        return angleInt;
    }

    /**
     * If we detect that the drone will take a non-optimum path (i.e. if the drone
     * starts making moves that will make him take longer to get to the destination or
     * even start looping), this function will provide the drone with a more appropriate
     * guideline for the path using the intermediate points @southwest or @northeast
     *
     * @param initialPos the input arguments
     * @param finalPos the input arguments
     * @return the most appropriate optimal path to the destination.
     */
    private static ArrayList<LongLat> willDetour(LongLat initialPos, LongLat finalPos){
        if (initialPos.distanceTo(southWest) < initialPos.distanceTo(northEast)){
            ArrayList<LongLat> total = movementsCalculator(initialPos, southWest);
            total.addAll(movementsCalculator(southWest, finalPos));
            return total;
        }else{
            ArrayList<LongLat> total = movementsCalculator(initialPos, northEast);
            total.addAll(movementsCalculator(northEast, finalPos));
            return total;
        }
    }

    /**
     * This function recursively finds the steps for the flightpath of the drone by using the
     * function @goodAngle. It will detect if the drone is taking too long to get to the desired location,
     * and if so, it will select another path using willDetour.
     * When the drone arrives to @finalPos (it's 'close') it will add the hovering step and conclude the
     * flightpath.
     *
     * @param initialPos the input arguments
     * @param finalPos the input arguments
     * @param movements empty list where the movements will be stored
     * @return the flightpath that takes the drone to @finalPos
     */
    private static ArrayList<LongLat> movementRecursion(LongLat initialPos,
                                                        LongLat finalPos, ArrayList<LongLat> movements){
        if (movements.size()>100){
            return willDetour(movements.get(0),finalPos);
        }
        if(!initialPos.closeTo(finalPos)) {
            int Angle = goodAngle(initialPos,finalPos);
            LongLat nextPosition = initialPos.nextPosition(Angle);
            movements.add(nextPosition);
            return movementRecursion(nextPosition, finalPos, movements);
        }else{
            movements.add(movements.get(movements.size()-1));
            return movements;
        }
    }

    /**
     * Method needed to initiate the @movementRecursion function, it
     * provides the empty list where the movements are stored.
     *
     * @param initialPos the input arguments
     * @param finalPos the input arguments
     * @return the flightpath plotted by movementRecursion
     */
    private static ArrayList<LongLat> movementsCalculator(LongLat initialPos, LongLat finalPos){
        ArrayList<LongLat> movements = new ArrayList<>();
        movements.add(initialPos);
        return movementRecursion(initialPos, finalPos, movements);
    }

    /**
     * If the drone has to pickup food from two places, it calculates the
     * most optimal order in which the drone should pick up the food. It will pick
     * lastly the food from the shop that is closest to the drop-off location.
     *
     * @param pickups list of locations from where the drone has to pick up food
     * @param deliveryPlace the place where the drone has to drop-of
     * @return list of ordered location where the drone has to go
     */
    private static ArrayList<LongLat> distanceOrder(ArrayList<LongLat> pickups, LongLat deliveryPlace){
        ArrayList<LongLat> orderedDistances = new ArrayList<>();
        if (pickups.size() == 2){
            if (deliveryPlace.distanceTo(pickups.get(0))< deliveryPlace.distanceTo(pickups.get(1))){
                orderedDistances.add(pickups.get(1));
                orderedDistances.add(pickups.get(0));
            }else {
                orderedDistances.add(pickups.get(0));
                orderedDistances.add(pickups.get(1));
            }
        }else{
            orderedDistances.addAll(pickups);
        }
        return orderedDistances;
    }

    /**
     * This method finds from the database the pickup places and the drop-off
     * places the drone should go for each order. It decides the order of the pick-up
     * places using @distanceOrder
     *
     * @param row the input arguments
     * @return the places the drone must go to pick-up and drop-off in optimal order.
     */
    private ArrayList<LongLat> orderFlight(Database.row row){
        ArrayList<LongLat> orderedPickups = distanceOrder(row.getPickups(), row.getDeliverTo());
        ArrayList<LongLat> movements = new ArrayList<>();
        if (orderedPickups.size() == 2){
            movements.addAll(movementsCalculator(currentPosition,orderedPickups.get(0)));
            movements.addAll(movementsCalculator(orderedPickups.get(0),orderedPickups.get(1)));
            movements.addAll(movementsCalculator(orderedPickups.get(1),row.getDeliverTo()));
        }else{
            movements.addAll(movementsCalculator(currentPosition, orderedPickups.get(0)));
            movements.addAll(movementsCalculator(orderedPickups.get(0),row.getDeliverTo()));
        }
        return movements;
    }

    /**
     * This method will use all the previous methods in this class to store into @finalCoordinates
     * the flightpath for each order, and it will update the @movesLeft.
     * Before completing each order it will check if the drone will have enough moves left to go
     * back to Appleton Tower. When there aren't any more orders that can be delivered with the
     * moves left, or after completing all the orders, it sends the drone back to @returnAddress.
     *
     * This function also updates the apache database with the new orders and moves the drone undertakes.
     *
     */
    public void flightPlanner() {
        int orders = theDatabase.getSize();
        ArrayList<LongLat> moves;
        int movesSpent;
        LongLat finalPosition;
        LongLat previousFinalPosition = returnAddress;
        ArrayList<LongLat> movesNeededToReturn = new ArrayList<>();
        for (int i=0;i<orders;i++){
            moves = orderFlight(theDatabase.getDataTableRow(i));
            movesSpent = moves.size();
            finalPosition = moves.get(movesSpent - 1);
            movesNeededToReturn = movementsCalculator(finalPosition,returnAddress);
            int remainingMoves = movesLeft - movesSpent;
            if (movesNeededToReturn.size() < remainingMoves){
                finalCoordinates.addAll(moves);
                currentPosition = finalPosition;
                movesLeft = movesLeft - movesSpent;
                apacheData.writeDeliveries(theDatabase.getDataTableRow(i));
                apacheData.writeFlightpath(theDatabase.getDataTableRow(i), moves, previousFinalPosition);
                previousFinalPosition = finalPosition;
                System.out.println(theDatabase.getDataTableRow(i).getDeliveryCost() + " pence collected");
            }
        }
        finalCoordinates.addAll(movesNeededToReturn);
        System.out.println(theDatabase.getSize() + " orders completed");
        System.out.println(movesLeft + " moves left");
    }

    /**
     * This method generates the .geojson file which helps visualize the flightpath of the drone
     *
     * It will catch the IO exception.
     */
    public void gsonDocGenerator() {
        try {
            ArrayList<Point> pointsList = new ArrayList<>();
            for (LongLat point : finalCoordinates) {
                pointsList.add(Point.fromLngLat(point.longitude, point.latitude));
            }
            LineString line = LineString.fromLngLats(pointsList);
            Feature feature = Feature.fromGeometry(line);
            FeatureCollection collection = FeatureCollection.fromFeature(feature);
            String flightPath = collection.toJson();
            FileWriter file = new FileWriter("drone-" + fileDate + ".geojson");
            file.write(flightPath);
            file.close();
        } catch (IOException e) {
            System.err.println("Fatal IO Exception Error: \n" + e);
            System.exit(1);
        }
    }

    /**
     * The entry point of the application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        App trial = new App(args[0],args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        trial.flightPlanner();
        trial.gsonDocGenerator();
    }

}
