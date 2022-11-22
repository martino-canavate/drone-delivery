package uk.ac.ed.inf;

/**
 * This class is created in order to be used in @Website's getPosition method
 * as a parser of the .json documents containing the coordinates of the "ThreeWords".
 */
public class coordinates {
    /**
     * The type Latlong, which parses the actual coordinates.
     */
    public static class Latlong{
        private double lng;
        private double lat;

        /**
         * Getter that returns the longitude.
         *
         * @return the lng
         */
//getters to access the private attributes
        public double getLng() {
            return lng;
        }

        /**
         * Getter that returns the latitude.
         *
         * @return the lat
         */
        public double getLat() {
            return lat;
        }
    }

    /**
     * The type Square.
     */
    public static class Square{
        private Latlong southwest;
        private Latlong northeast;
    }

    /**
     * The Country.
     */
    String country;
    private Square square;
    private String nearestPlace;
    private Latlong coordinates;

    /**
     * Gets coordinates.
     *
     * @return the coordinates
     */
//getter to access the private attribute
    public Latlong getCoordinates() {
        return coordinates;
    }
    private String words;
    private String language;
    private String map;
}
