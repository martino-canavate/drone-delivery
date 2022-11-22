package uk.ac.ed.inf;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * The Http class, it will create the client that helps access the webserver.
 */
//Class to create the http client to get data from the server
public class Http {
    //http client object
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Method that gets the .json file from the selected directory (url), throwing
     * the appropiate exceptions.
     *
     * @param url the url
     * @return the response
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
//method that gets the .json file from the selected directory(url)
    public static String getResponse(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()!=200){
            System.exit(1);
        }
        return response.body();
    }

    /**
     * Get menus.json file from the webserver.
     *
     * @param name the name of the machine
     * @param port the webserver port
     * @return a string composed of the content of menus.json
     */
//method that gets the menus.json file from the server
    public static String getMenus(String name, String port){
        String url = "http://"+name+":"+port+"/menus/menus.json";
        String menus = null;

        //uses try and catches to handle the possible errors
        try{
            menus = getResponse(url);
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to "+name+" at port "+port+".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal IO Exception Error: \n" + e);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Fatal Interrupted Exception Error: \n" + e);
            System.exit(1);
        }
        return menus;

    }

    /**
     * Method that returns the .json file where the coordinates of the corresponding
     * What three words are stored.
     *
     * @param name the machine name
     * @param port the webserver port
     * @param w1   the first word
     * @param w2   the second word
     * @param w3   the third word
     * @return a string composed of the contents of the .json document
     */
    public static String getCoordinates(String name, String port, String w1, String w2, String w3){
        String url = "http://"+name+":"+port+"/words/"+w1+"/"+w2+"/"+w3+"/details.json";
        String coordinates = null;

        //uses try and catches to handle the possible errors
        try{
            coordinates = getResponse(url);
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to "+name+" at port "+port+".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal IO Exception Error: \n" + e);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Fatal Interrupted Exception Error: \n" + e);
            System.exit(1);
        }
        return coordinates;

    }

    /**
     * Method that gets the .json document containing the coordinates that
     * delimitate the boundaries of the buildings that compromise the no-fly zone
     *
     * @param name the machine name
     * @param port the of the website port
     * @return a string composed of the contents of .json documents
     */
    public static String getBuildings(String name, String port){
        String url = "http://"+name+":"+port+"/buildings/no-fly-zones.geojson";
        String buildings = null;

        //uses try and catches to handle the possible errors
        try{
            buildings = getResponse(url);
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to "+name+" at port "+port+".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal IO Exception Error: \n" + e);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Fatal Interrupted Exception Error: \n" + e);
            System.exit(1);
        }
        return buildings;

    }

}
