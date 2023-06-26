package Server.Database.UserData;


import Server.Database.Table;
import Server.Database.Insertable;
import Server.Utils.DBUtils;
import com.twitter.common.Models.User;

import java.sql.*;

public class Users extends Table implements Insertable<User> {

    public final static String TABLE_NAME = "Users";

    public final static String COL_USERID = "userId";
    public final static String COL_USERNAME = "username";
    public final static String COL_DISPLAY_NAME = "displayName";
    public final static String COL_PASSWORD_HASH = "passwordHash";
    public final static String COL_REFRESH_TOKEN = "accessToken";
    public final static String COL_EMAIL = "email";
    public final static String COL_DATE_OF_BIRTH = "dateOfBirth";
    public final static String COL_ACCOUNT_MADE = "accountMade";
    public final static String COL_PROFILE_PIC_PATH = "profilePicPath";
    public final static String COL_HEADER_PIC_PATH = "headerPath";
    public final static String COL_BIO = "bio";
    public final static String COL_LOCATION = "location";
    public final static String COL_COUNTRY_ID = "countryId";
    public final static String COL_PHONE_NUMBER = "phonenumber";
    //private final static Connection conn = DatabaseController.getConnection();
    @Override
    public synchronized void createTable() throws SQLException {
        queryRunner.execute(conn,
    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COL_USERID + " INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT ,"
            + COL_USERNAME + " CHAR(255),"
            + COL_DISPLAY_NAME + " CHAR(50),"
            + COL_PASSWORD_HASH + " CHAR(64),"
            + COL_REFRESH_TOKEN + " VARCHAR(2048),"
            + COL_EMAIL + " CHAR(255),"
            + COL_DATE_OF_BIRTH + " DATE,"
            + COL_ACCOUNT_MADE + " DATE,"
            + COL_BIO + " CHAR(160),"
            + COL_LOCATION + " CHAR(50),"
            + COL_COUNTRY_ID + " INT, "
            + "FOREIGN KEY ("+COL_COUNTRY_ID+") REFERENCES "+ Countries.TABLE_NAME +" ("+Countries.COL_ID+"),"
            + COL_PHONE_NUMBER + " CHAR(32))");
    }

    @Override
    public synchronized boolean insert(User toAdd) {
        try {
            int insertCount =
                queryRunner.update(conn,
            "INSERT INTO " + TABLE_NAME + "("
                    + COL_DISPLAY_NAME + ", " + COL_USERNAME + ", "
                    + COL_PASSWORD_HASH + ", " + COL_EMAIL + ", "
                    + COL_DATE_OF_BIRTH + ", " + COL_ACCOUNT_MADE + ")" +
                " VALUES (?, ?, ?, ?, ?, ?)",
                    toAdd.getDisplayName(),
                    toAdd.getUsername(),
                    toAdd.getPasswordHash(),
                    toAdd.getEmail(),
                    toAdd.getDateOfBirth(),
                    toAdd.getAccountMade());

            return insertCount == 1;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }

    public synchronized User selectUser(int userId) {
        try(PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM " + Users.TABLE_NAME + " WHERE " + COL_USERID + "= (?)")) {
            pStmt.setInt(1, userId);

            ResultSet  rs = pStmt.executeQuery();

            if(rs.next())
                return DBUtils.resultSetToUser(rs);
            return null;

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return null;
        }
    }


    public synchronized User selectUser(String username, String passwordHash) {
        try(PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE (" + COL_USERNAME + " = (?) OR " + COL_EMAIL + "= (?)) AND " + COL_PASSWORD_HASH + " = (?)")) {
            pStmt.setString(1, username);
            pStmt.setString(2, username);
            pStmt.setString(3, passwordHash);

            ResultSet rs = pStmt.executeQuery();
            if(rs.next())
                return DBUtils.resultSetToUser(rs);
            return null;

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return null;
        }
    }

    public synchronized boolean exists(String columnName, String value) {
        try (PreparedStatement pStmt = conn.prepareStatement("SELECT COUNT(*) "+columnName+" FROM " + TABLE_NAME + " WHERE " + columnName + " = (?)")) {
            pStmt.setString(1 ,value);
            try (ResultSet rs = pStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(columnName)>0;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }

        return false;
    }

}
