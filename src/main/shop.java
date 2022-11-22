package uk.ac.ed.inf;


import java.util.ArrayList;

/**
 * This class is created in order to be used in @Website's getInfo method
 * as a parser of menus.json
 */
//class to help parse the menus.json file
public class shop {
    /**
     * The name of the shop.
     */
    String name;
    /**
     * The location of the shop.
     */
    private String location;

    /**
     * The inner class Food, used to parse the food items in each menu
     */
//class to help parse each item of the menu section
    public static class food{
        private String item;
        private int pence;

        /**
         * Getter that gives you the name of the corresponding food item.
         *
         * @return the item
         */
//getters to access the private attributes
        public String getItem() {
            return item;
        }

        /**
         * Getter that gives you the price of the corresponding food item.
         *
         * @return the pence
         */
        public int getPence() {
            return pence;
        }
    }

    /**
     * Array list with all the food items in the menu section
     */
    private ArrayList<food> menu;

    /**
     * Getter that returns all the food items of the corresponding menu.
     *
     * @return the menu
     */
    public ArrayList<food> getMenu() {
        return menu;
    }

    /**
     * Getter that returns the location of the menu's corresponding shop.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }
}
