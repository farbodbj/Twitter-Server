package Server.Database.UserData;

import Server.Database.DatabaseController;
import Server.Database.Table;

import java.sql.*;

public class BlockList extends Table
{
    public final static String TABLE_NAME = "BlockList";
    public final static String COL_BLOCKED = "blocked";
    public final static String COL_BLOCKER = "blocker";
    //private final static Connection conn = DatabaseController.getConnection();
    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + COL_BLOCKED + " INT, "
                + COL_BLOCKER + " INT, "
                +"FOREIGN KEY("+COL_BLOCKED+") REFERENCES " + Users.TABLE_NAME +"("+Users.COL_USERID +"), "
                +"FOREIGN KEY("+COL_BLOCKER+") REFERENCES " + Users.TABLE_NAME + "(" + Users.COL_USERID + "), "
                +" UNIQUE KEY block_id ("+COL_BLOCKER+", "+COL_BLOCKED+"))");

    }


    public boolean insert(int blockerId,int blockedId) {
        try {
            int insertCount =
                    queryRunner.update(conn,
                        "INSERT INTO " +TABLE_NAME+
                            "(" + COL_BLOCKED + "," + COL_BLOCKER +")"
                                + "VALUES (?,?)",
                                blockedId,
                                blockerId);

            return insertCount == 1;
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }

    public boolean remove(int blockerId,int blockedId) {
        try {
            int rowsAffected = queryRunner.update(conn,
            "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL_BLOCKED + "= (?) AND " + COL_BLOCKER + "= (?)",
                    blockedId,
                    blockerId);

            return rowsAffected == 1;

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }
}
