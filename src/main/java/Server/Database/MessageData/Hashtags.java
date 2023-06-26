package Server.Database.MessageData;


import Server.Database.Table;
import Server.Database.Insertable;
import com.twitter.common.Models.Messages.Textuals.Tweet;

import java.sql.SQLException;

public class Hashtags extends Table implements Insertable<String> {
    public final static String TABLE_NAME = "Hashtags";
    public final static String COL_HASHTAG = "hashtag";
    public final static String COL_HASHTAG_COUNT = "hashtagCount";
    //private final static Connection conn = DatabaseController.getConnection();
    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + COL_HASHTAG + " VARCHAR("+ Tweet.MAX_TWEET_LENGTH +") UNIQUE ,"
                + COL_HASHTAG_COUNT + " INT)");
    }

    @Override
    public boolean insert(String toAdd) {
        try {
            int rowsAffected =
                    queryRunner.update(conn,
                    "INSERT INTO "+TABLE_NAME+"("+COL_HASHTAG+", "+COL_HASHTAG_COUNT+")" +
                        " VALUES (?, ?) ON DUPLICATE KEY UPDATE " + COL_HASHTAG_COUNT + "=" + COL_HASHTAG_COUNT + "+1",
            toAdd,1);

            return rowsAffected != 0;

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }
}
