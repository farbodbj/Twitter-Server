package Server.Database.UserData;

import Server.Database.DatabaseController;
import Server.Database.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class Countries extends Table {
    public final static String TABLE_NAME = "Countries";
    public final static String COL_ID = "id";
    public final static String COL_COUNTRY_NAME = "countryName";
    public final static String COL_COUNTRY_CODE = "countryCode";
    //private final static Connection conn = DatabaseController.getConnection();

    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                        + COL_ID + " INT UNIQUE PRIMARY KEY NOT NULL AUTO_INCREMENT,"
                        + COL_COUNTRY_NAME + " CHAR(64) UNIQUE ,"
                        + COL_COUNTRY_CODE + " CHAR(8))");
        //initializeTable();
    }

    private void initializeTable() throws SQLException {
        String initQuery = "INSERT IGNORE INTO " + TABLE_NAME + " (" + COL_COUNTRY_NAME + ", " + COL_COUNTRY_CODE + ") VALUES (?, ?)";
        queryRunner.batch(
               conn,
               initQuery,
               generateCountriesList());
    }

    private String[][] generateCountriesList() {
        String[] countryCodes = Locale.getISOCountries();
        String[][] countriesList = new String[countryCodes.length][2];
        for (int i = 0; i < countryCodes.length; i++) {
            Locale locale = new Locale("", countryCodes[i]);

            countriesList[i][0] = locale.getDisplayCountry();
            countriesList[i][1] = locale.getCountry();
        }

        return countriesList;
    }
}
