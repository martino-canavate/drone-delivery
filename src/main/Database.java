package uk.ac.ed.inf;

import java.util.*;


/**
 * The Database class. This class is used to create a data table that stores in every row
 * all the necessary information of an order(the order's number, the coordinates of
 * pick-up and drop-off locations, and the delivery price), for all the orders
 * of a given day.
 */
public class Database{

    /**
     * The @Website object that helps access the website being used.
     */
    final Website ActualWeb;

    /**
     * The ArrayList of rows that contain the information of the day's orders.
     */
    private final ArrayList<row> DataTable = new ArrayList<>();

    /**
     * Database class constructor.
     *
     * @param ActualWeb the actual web
     */
    public Database(Website ActualWeb){
        this.ActualWeb = ActualWeb;
    }

    /**
     * Getter that returns the requested row from the data table.
     *
     * @param i the number of the requested row
     * @return the row
     */
    public  row getDataTableRow(int i) {
        return DataTable.get(i);
    }

    /**
     * Setter that adds the information of an order to a new row in the data table.
     *
     * @param No        the no of the delivery
     * @param DeliverTo the order's drop-off location
     * @param foodItems the order's food items
     */
    public void addToDataTable(String No, String DeliverTo, ArrayList<String> foodItems) {
        row newRow = new row(No, DeliverTo, foodItems);
        DataTable.add(newRow);
    }

    /**
     * Method that returns the number of rows (number of deliveries), stored in the data table
     *
     * @return the number (int).
     */
    public int getSize(){
        return DataTable.size();
    }

    /**
     * Inner class which is used to compare @row based on the cost of the order stored in each row.
     */
    private static class CustomComparator implements Comparator<row> {
        @Override
        public int compare(row o1, row o2) {
            return o1.getDeliveryCost() - o2.getDeliveryCost();
        }
    }

    /**
     * Method that orders the rows of the data table based on the price (using the @CustomComparator class)
     */
    public void greedyTable(){
        ArrayList<row> list = this.DataTable;
        list.sort(new CustomComparator());
        Collections.reverse(list);
    }

    /**
     * The inner class Row, which generates a row of the data table for each delivery.
     */
    public class row{
        /**
         * The order number.
         */
        private final String No;
        /**
         * The drop-off location.
         */
        private final LongLat DeliverTo;
        /**
         * The pick-up locations.
         */
        private final ArrayList<LongLat> pickups;
        /**
         * The order cost.
         */
        private final int deliveryCost;
        /**
         * The drop-off "ThreeWords" location.
         */
        private final String threeWords;

        /**
         * Class constructor for row.
         *
         * @param No        the order number (string).
         * @param DeliverTo the drop-off location of the order.
         * @param foodItems a list of strings which are all the food items of the order.
         */
//class constructor
        public row(String No, String DeliverTo, ArrayList<String> foodItems) {
            this.No = No;
            this.DeliverTo = ActualWeb.getPosition(DeliverTo);
            this.pickups = getPositionFromAll(ActualWeb.getInfo(foodItems).locations);
            this.deliveryCost = ActualWeb.getInfo(foodItems).cost;
            this.threeWords = DeliverTo;
        }

        /**
         * Method that, given an array of "ThreeWords" strings, returns an array of the
         * corresponding @LongLat locations.
         *
         * @param locations the "ThreeWords" locations.
         * @return the @LongLat array list
         */
        public ArrayList<LongLat> getPositionFromAll(ArrayList<String> locations){
            Set<String> set = new HashSet<>(locations);
            ArrayList<LongLat> pickup = new ArrayList<>();
            for (String c : set) {
                pickup.add(ActualWeb.getPosition(c));
            }
            return pickup;
        }

        /**
         * Getter that returns the Order Number.
         *
         * @return the No attribute.
         */
//getters to access the private attributes
        public String getNo() {
            return this.No;
        }

        /**
         * Getter that returns the order's drop-off location.
         *
         * @return the DeliverTo attribute.
         */
        public LongLat getDeliverTo() {
            return this.DeliverTo;
        }

        /**
         * Getter that returns the order's pick-up locations.
         *
         * @return the pickups attribute.
         */
        public ArrayList<LongLat> getPickups() {
            return this.pickups;
        }

        /**
         * Getter that returns the order's delivery cost.
         *
         * @return the deliveryCost attribute.
         */
        public int getDeliveryCost() {
            return this.deliveryCost;
        }

        /**
         * Getter that return's the order's "ThreeWords" location.
         *
         * @return the threeWords attribute.
         */
        public String getThreeWords() {
            return this.threeWords;
        }
    }

}

