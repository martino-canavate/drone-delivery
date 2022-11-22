package uk.ac.ed.inf;


/**
 * This class creates objects which store latitude and longitude. It is used in all the
 * drone's motion-related methods.
 * It also helps to calculate distance's between points, calculate angles and check if two
 * line segments intersect.
 */
public class LongLat
{
    /**
     * The Longitude.
     */
//Class variables
    public final double longitude;
    /**
     * The Latitude.
     */
    public final double latitude;
    /**
     * The size of the step the drone takes with every move.
     */
    static public final double step = 0.00015;

    /**
     * The LongLat object constructor.
     *
     * @param ln the longitude.
     * @param lt the latitude.
     */
//class constructor
    public LongLat(double ln, double lt) {
        this.longitude = ln;
        this.latitude = lt;
    }

    /**
     * This method checks if the corresponding location of the @LongLat object is contained
     * inside the 'legal' space.
     *
     * @return the boolean true or false.
     */
//Checks that longitude and latitude are in the proper ranges
    boolean isConfined(){
        return ((this.longitude >= -3.192473d) && (this.longitude <= -3.184319d) && (this.latitude >= 55.942617d) && (this.latitude <=55.946233));
    }

    /**
     * This method checks if the location of two different @LongLat objects is the same.
     *
     * @param obj the @LongLat obj
     * @return the boolean true or false.
     */
    boolean isEqual(LongLat obj){
        boolean latitude = this.latitude == obj.latitude;
        boolean longitude = this.longitude == obj.longitude;

        return latitude && longitude;
    }

    /**
     * This method rounds doubles to one decimal place.
     * @param value , the double value
     * @return   , the rounded value.
     */
    private static double round(double value) {
        int scale = (int) Math.pow(10, 1);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * This method calculates the positive angle that generates the line given by two @LongLat
     * object coordinates with respect to the latitude axis.
     *
     * @param obj the @LongLat obj
     * @return the angle in degrees(int)
     */
    int getAngle(LongLat obj){
        double angle;
        if ((obj.longitude - this.longitude) < 0) {
            angle = (180 - Math.toDegrees(Math.asin((obj.latitude - this.latitude)/this.distanceTo(obj)))) / 100;
        } else {
            angle = Math.toDegrees(Math.asin((obj.latitude - this.latitude)/this.distanceTo(obj))) / 100;
        }
        return (int)(round(angle)*100);
    }

    /**
     * This is a helper method for the @doesIntersect method below.
     *
     * @param A the a
     * @param B the b
     * @param C the c
     * @return the boolean
     */
    static boolean ccw(LongLat A,LongLat B, LongLat C){
        return (C.latitude-A.latitude) * (B.longitude-A.longitude)
                > (B.latitude-A.latitude) * (C.longitude-A.longitude);
    }

    /**
     * This method is used to calculate if two lines, given by 4 @LongLat object
     * coordinates, intersect.
     *
     * @param lineBeginning  the line beginning
     * @param lineEnd        the line end
     * @param line1Beginning the line 1 beginning
     * @param line1End       the line 1 end
     * @return the boolean
     */
    static boolean doesIntersect(LongLat lineBeginning, LongLat lineEnd, LongLat line1Beginning, LongLat line1End){
        if (lineBeginning.isEqual(line1Beginning) || lineBeginning.isEqual(line1End)){
            return true;
        }
        if (lineEnd.isEqual(line1Beginning) || lineEnd.isEqual(line1End)){
            return true;
        }
        return ccw(lineBeginning,line1Beginning,line1End) != ccw(lineEnd,line1Beginning,line1End)
                && ccw(lineBeginning,lineEnd,line1Beginning) != ccw(lineBeginning,lineEnd,line1End);
    }

    /**
     * This method calculates the distance between two @LongLat objects.
     *
     * @param obj the obj
     * @return the double
     */
//Uses pythagoras to calculate distance between two pairs of coordinates
    double distanceTo(LongLat obj){
        return Math.sqrt((this.longitude - obj.longitude)*(this.longitude - obj.longitude) + (this.latitude - obj.latitude)*(this.latitude - obj.latitude));
    }

    /**
     * This object calculates if the distance between two @LongLat objects is
     * less than the 'step' value (0.00015).
     *
     * @param obj the obj
     * @return the boolean
     */
//Checks if the distance between two objects is smaller than 'step'
    boolean closeTo(LongLat obj){
        return (this.distanceTo(obj) < step);
    }

    /**
     * This method calculates the position the drone has after taking a 'step' from a certain location.
     * in a certain direction.
     *
     * @param angle the angle which indicates the direction
     * @return the @LongLat object coordinates.
     */
//Calculates by using trigonometry the position after travelling a 'step' in a certain direction
    LongLat nextPosition(int angle){
        //if angle -999 is used it means that the drone doesn't move
        if (angle == -999){
            return this;
        }
        double radians = Math.toRadians(angle);
        return new LongLat(Math.cos(radians)*step +this.longitude, Math.sin(radians)*step +this.latitude);
    }

    /**
     * This inner class generates objects that represent line segments.
     * These line segments are represented by two @LongLat objects corresponding to
     * the locations of the line segment's end points.
     */
    public static class line{

        private final LongLat startPoint;
        private final LongLat endPoint;

        /**
         * The line object constructor.
         *
         * @param startPoint the start point
         * @param endPoint   the end point
         */
        public line(LongLat startPoint, LongLat endPoint) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        /**
         * Getter that returns the segments start point @LongLat object.
         *
         * @return the startPoint attribute
         */
        public LongLat getStartPoint() {
            return startPoint;
        }

        /**
         * Getter that returns the segments end point @LongLat object.
         *
         * @return the endPoint attribute
         */
        public LongLat getEndPoint() {
            return endPoint;
        }
    }

}
