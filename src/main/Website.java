package uk.ac.ed.inf;

import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;


/**
 * The class website, which generates all the information needed to deliver each order, using the
 * methods from the @Http client class.\
 * It produces the location where each item from the order must be collected, the location where
 * the order must be delivered, the price of the order, and the no-fly zone's perimeter.
 */
public class Website {

    /**
     * The Machine name.
     */
//class variables
    public final String machineName;
    /**
     * The website's port.
     */
    public final String port;

    /**
     * Class constructor for the Website.
     *
     * @param name the machine name
     * @param por  the website port
     */
//class constructor
    public Website(String name, String por){
        this.machineName = name;
        this.port = por;
    }

    /**
     * The inner class info, whichj generates object which contain the specific
     * information of an order (locations of pick-up and delivery places and the price)
     */
    public static class info{
        /**
         * The Cost.
         */
        public final int cost;
        /**
         * The Locations.
         */
        public final ArrayList<String> locations;

        /**
         * Class constructor of Info.
         *
         * @param cost      the order cost
         * @param locations the order pick-up and drop-off locations
         */
//class builder
        public info(int cost, ArrayList<String> locations){
            this.cost = cost;
            this.locations = locations;
        }
    }

    /**
     * Method that given a Delivery, returns an @info object with all the
     * delivery's pick-up and drop-off locations and the delivery's cost.
     *
     * It does so by finding throughout all the menus ( gotten by parsing,
     * using the @shop class, the menus.json file gotten from the web using
     * the @Http class' method @getMenus) the items that match those in
     * the order. Then it returns
     *
     * @param delivery a list of strings that compose the delivery order's items
     * @return the info object with all the delivery's pick-up and drop-off
     * locations and the delivery's cost.
     */
//returns the cost and the pickup locations of a delivery
    public info getInfo(ArrayList<String> delivery){

        //gets the menu.json file from the server
        String menus = Http.getMenus(this.machineName, this.port);
        //creates a parser template
        Type listType = new TypeToken<ArrayList<shop>>() {}.getType();
        //parses the menu.json file into a list of shop class objects
        ArrayList<shop> shopList = new Gson().fromJson(menus, listType);

        //Default cost of deliveries
        int cost = 50;
        ArrayList<String> locations = new ArrayList<>();

        //for each item in delivery, it loops throughout all the shops and items in menus
        for (String deliver : delivery){
            boolean Break = false;
            for (shop shop : shopList){
                ArrayList<shop.food> menu = shop.getMenu();
                for (shop.food food : menu){
                    if (food.getItem().equals(deliver)){
                        //when item is found on menu, it adds the price to the delivery cost
                        cost = cost + food.getPence();
                        locations.add(shop.getLocation());
                        Break = true;
                        break;
                    }
                }
                if (Break){
                    break;
                }
            }
        }
        return new info(cost, locations);
    }

    /**
     * This method, given the "ThreeWords", finds the .json file that contains
     * the coordinates that correspond to those "Three words", and parses (using
     * the @coordinates class) the document to find and return the @LongLat location.
     *
     * @param words the 3 words that compose a "ThreeWords" location
     * @return the @LongLat location that corresponds to those "ThreeWords"
     */
//Gets the coordinates from the 3 words.
    public LongLat getPosition(String words){
        String[] wordsArray = words.split("\\.");
        String coords = Http.getCoordinates(this.machineName, this.port, wordsArray[0], wordsArray[1], wordsArray[2]);
        coordinates coordinat = new Gson().fromJson(coords, coordinates.class);

        return new LongLat(coordinat.getCoordinates().getLng(), coordinat.getCoordinates().getLat());
    }

    /**
     * This method, uses the @getBuildings method from the @Http class to get the
     * no-fly-zones.geojson document. It then uses the built-in methods in the geojson class
     * to parse out all the different no-fly areas.
     *
     * It then will decompose every no-fly zone polygon into all the different lines that form it,
     * returning them as @LongLat.line objects.
     *
     * @return the array list with the @LongLat.line objects that represent the lines that form
     * all the different no-fly-zone's polygons
     */
    public ArrayList<LongLat.line> getNoFlyPerimeter(){
        String gsonArray = Http.getBuildings(this.machineName, this.port);
        FeatureCollection fc = FeatureCollection.fromJson(gsonArray);
        List<Feature> buildings = fc.features();
        ArrayList<List<Point>> polygons = new ArrayList<>();
        assert buildings != null;
        for (Feature f : buildings){
            assert f.geometry() != null;
            polygons.add(((Polygon)f.geometry()).coordinates().get(0));
        }
        ArrayList<LongLat.line> perimeter = new ArrayList<>();

        for (List<Point> polygon :polygons){
            Point initialPos = polygon.get(0);
            Point previous = initialPos;
            for (int p = 1; p<polygon.size(); p++){
                Point corner = polygon.get(p);
                LongLat start = new LongLat(previous.longitude(),previous.latitude());
                LongLat end = new LongLat(corner.longitude(),corner.latitude());
                perimeter.add(new LongLat.line(start,end));
                previous = corner;
            }
            LongLat finalStart = new LongLat(previous.longitude(),previous.latitude());
            LongLat finalEnd = new LongLat(initialPos.longitude(),initialPos.latitude());
            perimeter.add(new LongLat.line(finalStart,finalEnd));
        }
        return perimeter;
    }
}


