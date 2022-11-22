package uk.ac.ed.inf;

import org.junit.Test;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class AppTest {





    @Test
    public void displayNo1() throws SQLException, IOException {
        App trial = new App("12","12", "2022", 80, 1527);
        trial.flightPlanner();
        trial.gsonDocGenerator();
    }
}
