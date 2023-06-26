package Server.Database.UserData;


import Server.Database.Table;
import Server.Database.MessageData.Tweets;

import java.sql.SQLException;


public class Likes extends Table
{
    public final static String TABLE_NAME = "Likes";
    public final static String COL_TWEET_ID = "tweet";//tweet Id
    public final static String COL_LIKER_ID = "Liker";//ID of user that has liked
    //private final static Connection conn = DatabaseController.getConnection();
    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
        "CREATE TABLE IF NOT EXISTS " +TABLE_NAME+"("+
                COL_TWEET_ID + " BIGINT," +
                COL_LIKER_ID + " INT, " +
                "FOREIGN KEY ("+COL_TWEET_ID+") REFERENCES "+ Tweets.TABLE_NAME +"("+Tweets.COL_TWEET_ID+"), "
                + "FOREIGN KEY ("+COL_LIKER_ID+") REFERENCES "+ Users.TABLE_NAME + "("+Users.COL_USERID+"), "
                + "UNIQUE  KEY like_id ("+COL_LIKER_ID+", "+COL_TWEET_ID+"))");
    }

    public boolean insert(int tweetId ,int likerId) {
        try {
            int insertCount =
                    queryRunner.update(
                        "INSERT INTO " + TABLE_NAME +
                            "(" + COL_TWEET_ID + "," + COL_LIKER_ID + ")"+
                            "VALUES (?,?)",
                                tweetId,
                                likerId);

            return insertCount == 1;
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }


    }
    public boolean remove(int tweetId ,int likerId) {
        try {
            int rowsAffected = queryRunner.update(conn,
                "DELETE FROM " + TABLE_NAME + " WHERE " + COL_TWEET_ID + "= ? AND " + COL_LIKER_ID + "= ?",
                    tweetId,
                    likerId);

            return rowsAffected == 1;
        }
        catch (SQLException e)
        {
            System.out.println(e.getSQLState());
            return false;
        }
    }
}


