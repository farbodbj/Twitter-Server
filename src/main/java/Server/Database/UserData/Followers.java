package Server.Database.UserData;


import Server.Database.Table;
import Server.Utils.DBUtils;
import com.twitter.common.Models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Followers extends Table {
    public final static String TABLE_NAME = "Followers";
    public final static String COL_FOLLOWED = "followed";
    public final static String COL_FOLLOWER = "follower";

    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + COL_FOLLOWED + " INT, "
                + COL_FOLLOWER + " INT, "
                + "FOREIGN KEY("+COL_FOLLOWED+") REFERENCES " + Users.TABLE_NAME +"("+Users.COL_USERID +"), "
                + "FOREIGN KEY("+COL_FOLLOWER+") REFERENCES " + Users.TABLE_NAME + "(" + Users.COL_USERID + "),"
                +" UNIQUE  KEY follow_id ("+COL_FOLLOWED+", "+COL_FOLLOWER+"))");
    }

    public boolean insert(int followerId,int followedId) {
        try {
            int affectedRows = queryRunner.update(conn,
            "INSERT INTO " +TABLE_NAME+ "(" + COL_FOLLOWED + "," + COL_FOLLOWER +")" + "VALUES (?,?)",
                followedId,
                followerId);

            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }

    public boolean delete(int followerId, int followedId) {
        try {
            int affectedRows = queryRunner.update(conn,
                    "DELETE FROM " +TABLE_NAME+ " WHERE " + COL_FOLLOWED + "= (?) AND " + COL_FOLLOWER + "= (?)",
                    followedId,
                    followerId);

            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }

    public List<User> selectFollowers(int userID) {
        String sqlQuery = generateUserSelectQuery(userID, COL_FOLLOWED, COL_FOLLOWER);
        return selectHelper(sqlQuery, userID);
    }

    public List<User> selectFollowings(int userID) {
        String sqlQuery = generateUserSelectQuery(userID, COL_FOLLOWER, COL_FOLLOWED);
        return selectHelper(sqlQuery, userID);
    }

    public int selectFollowersCount(int userId) {
        return selectCount(COL_FOLLOWER, userId);
    }

    public int selectFollowingsCount(int userId) {
        return selectCount(COL_FOLLOWED, userId);
    }


    public int selectCount(String columnName, int userId) {
        try(PreparedStatement pStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + columnName + " = ?")) {
            pStmt.setInt(1, userId);

            ResultSet resultSet = pStmt.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                resultSet.close();
                return count;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return -1;
    }

    private List<User> selectHelper(String sqlQuery, int userID) {
        try(PreparedStatement selector = conn.prepareStatement(sqlQuery)) {
            selector.setInt(1,userID);
            ResultSet rs = selector.executeQuery();
            List<User> users = new ArrayList<>();

            while (rs.next()) {
                users.add(DBUtils.resultSetToUser(rs));
            }
            return users;
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
        }
        return null;
    }

    private String generateUserSelectQuery(int userID, String targetColumn, String conditionColumn) {
        return "SELECT "
                + Users.TABLE_NAME + "." + Users.COL_USERID + ", "
                + Users.TABLE_NAME + "." + Users.COL_DISPLAY_NAME + ", "
                + Users.TABLE_NAME + "." + Users.COL_USERNAME + ", "
                + Users.TABLE_NAME + "." + Users.COL_BIO
                + " FROM " + TABLE_NAME
                + " JOIN " + Users.TABLE_NAME
                + " ON " + targetColumn + "=" + Users.TABLE_NAME + "." + Users.COL_USERID
                + " WHERE " + conditionColumn + "= (?)";
    }
}
