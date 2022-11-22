package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;


/**
 * The class Apache db, which is created to access and store SQl content in the Apache
 * database server.
 */
public class ApacheDB {
    /**
     * The constant conn.
     */
//class variables
    public static Connection conn;
    /**
     * The constant statement.
     */
    public static Statement statement;

    /**
     * The class constructor, which generates anew the tables 'deliveries' and 'flightpath'
     * after erasing the previous ones if they existed.
     * It will throw an SQL exception if the apache server is inaccessible.
     *
     * @param name the Machine name.
     * @param por  the database's port.
     */
//class constructor
    public ApacheDB(String name, String por) {
        try {
            conn = DriverManager.getConnection("jdbc:derby://"+name+":"+por+"/derbyDB");
            statement = conn.createStatement();

            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }
            statement.execute(
                    "create table deliveries(" +
                            "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");

            //creates flightpath table
            ResultSet resultSet1 =
                    databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet1.next()) {
                statement.execute("drop table flightpath");
            }
            statement.execute(
                    "create table flightpath(" +
                            "orderNo char(8), " + "fromLongitude double, " +
                            "fromLatitude double, " + "angle integer, " + "toLongitude double, " + "toLatitude double)");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * This method searches in the apache database all the orders for a given day.
     * It then adds the order's details into the @Database data table using a @row
     * object.
     * It will throw an SQL exception if the apache server is inaccessible.
     *
     * @param date        the date of the day we want all the orders
     * @param newDatabase the @Database data table where the order details are stored.
     */
    public void getDayOrders(java.sql.Date date, Database newDatabase) {
        try {
            final String dataQuery = "select * from orders where deliveryDate=(?)";
            final String itemsQuery = "select * from orderDetails where orderNo=(?)";
            PreparedStatement psDataQuery = conn.prepareStatement(dataQuery);
            PreparedStatement psItemsQuery = conn.prepareStatement(itemsQuery);
            psDataQuery.setDate(1, date);

            ResultSet rs = psDataQuery.executeQuery();
            while (rs.next()) {
                String num = rs.getString("orderNo");
                String To = rs.getString("deliverTo");
                ArrayList<String> foodItems = new ArrayList<>();

                psItemsQuery.setString(1, num);
                ResultSet itms = psItemsQuery.executeQuery();
                while (itms.next()) {
                    String itm = itms.getString("item");
                    foodItems.add(itm);
                }

                newDatabase.addToDataTable(num, To, foodItems);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * This is the method which inserts, into the 'deliveries' table we've created on the apache db, the necessary
     * information (order number, the "ThreeWords" location and the order's cost) of all the orders
     * undergone in the day.
     * It will throw an SQL exception if the apache server is inaccessible.
     *
     * @param row , a row from the @Database's data table.
     */
    public void writeDeliveries(Database.row row) {
        try {
            PreparedStatement psDeliveries = conn.prepareStatement("insert into deliveries values (?, ?, ?)");

            psDeliveries.setString(1, row.getNo());
            psDeliveries.setString(2, row.getThreeWords());
            psDeliveries.setInt(3, row.getDeliveryCost());
            psDeliveries.execute();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     This is the method which inserts, into the 'flightpath' table we've created on the apache db, all the different
     moves the drone has made to deliver the orders of the day.
     It does so by, after completing a given order, updating the apache table with the coordinates
     of all the steps taken during the process.
     * It will throw an SQL exception if the apache server is inaccessible.
     *
     * @param row     , a row from the @Database's data table.
     * @param moves   , the array list of @LongLat coordinates which describe the drone's movement that day
     * @param initPos , the init position at the beginning of delivering the order.
     */
    public void writeFlightpath(Database.row row, ArrayList<LongLat> moves, LongLat initPos) {
        try {
            PreparedStatement psFlightpath = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");

            String orderNo = row.getNo();
            LongLat previousCoordinate = initPos;
            for (LongLat move : moves) {
                int angle = previousCoordinate.getAngle(move);
                psFlightpath.setString(1, orderNo);
                psFlightpath.setDouble(2, previousCoordinate.longitude);
                psFlightpath.setDouble(3, previousCoordinate.latitude);
                psFlightpath.setInt(4, angle);
                psFlightpath.setDouble(5, move.longitude);
                psFlightpath.setDouble(6, move.latitude);
                previousCoordinate = move;
                psFlightpath.execute();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}
