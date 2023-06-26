package Server.Database.UserData;


import Server.Database.Table;
import Server.Utils.DBUtils;
import com.twitter.common.Models.User;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static com.twitter.common.Utils.SafeCall.safe;

public class Followers extends Table {
    public final static String TABLE_NAME = "Followers";
    public final static String COL_FOLLOWED = "followed";
    public final static String COL_FOLLOWER = "follower";
    //private final static Connection conn = DatabaseController.getConnection();
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
            int affectedRows = queryRunner.update(
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
            int affectedRows = queryRunner.update(
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
        String sqlQuery = "SELECT "+ COL_FOLLOWER + " FROM " + TABLE_NAME + " WHERE " + COL_FOLLOWED + "= (?)";
        return selectHelper(sqlQuery, userID);
    }

    public List<User> selectFollowings(int userID) {
        String sqlQuery = "SELECT "+COL_FOLLOWED+" FROM " + TABLE_NAME + " WHERE " + COL_FOLLOWER + "= (?)";
        return selectHelper(sqlQuery, userID);
    }


    private List<User> selectHelper(String sqlQuery, int userID) {
        try(PreparedStatement selector = conn.prepareStatement(sqlQuery)) {
            selector.setInt(1,userID);
            ResultSet rs = selector.executeQuery();
            List<User> users = new LinkedList<>();

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
}
