package Server.Database.MessageData;

import Server.Database.*;
import Server.Database.UserData.Followers;
import Server.Database.UserData.Users;
import Server.Utils.DBUtils;
import com.twitter.common.Models.Messages.Textuals.Mention;
import com.twitter.common.Models.Messages.Textuals.Quote;
import com.twitter.common.Models.Messages.Textuals.Retweet;
import com.twitter.common.Models.Messages.Textuals.Tweet;

import java.sql.*;
import java.util.List;


public class Tweets extends Table implements Insertable<Tweet> { //TODO: this class needs further investigation for improving code quality
    public final static String TABLE_NAME = "Tweets";
    public final static String COL_TWEET_ID = "tweetId";
    public final static String COL_SENDER_ID = "senderId";
    public final static String COL_TEXT = "Tweettext";
    public final static String COL_FAV_COUNT = "favCount";
    public final static String COL_RETWEET_COUNT = "retweetCount";
    public final static String COL_MENTION_COUNT = "mentionCount";
    public final static String COL_SENT_AT = "sentAt";
    public final static String COL_PARENT_TWEET = "parentTweet";
    public final static String COL_TWEET_TYPE = "tweetType";
    public final static String TWEET_TYPE_ENUM = "ENUM('Tweet', 'Retweet', 'Mention', 'Quote')";
    public final static String MENTIONER_ALIAS = "mentioner";
    public final static String MENTIONED_ALIAS = "mentioned";
    public final static String QUOTER_ALIAS = "quoter";
    public final static String QUOTED_ALIAS = "quoted";
    //private final static Connection conn = DatabaseController.getConnection();
    enum TweetType {Tweet, Retweet, Mention, Quote}

    @Override
    public void createTable() throws SQLException{
        queryRunner.execute(conn,
            "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + "("
                    + COL_TWEET_ID + " BIGINT NOT NULL UNIQUE PRIMARY KEY,"
                    + COL_SENDER_ID + " INT NOT NULL, "
                    + "FOREIGN KEY(" + COL_SENDER_ID + ") REFERENCES "+ Users.TABLE_NAME+"("+ Users.COL_USERID + "),"
                    + COL_TEXT + " VARCHAR("+Tweet.MAX_TWEET_LENGTH+") ,"
                    + COL_FAV_COUNT + " INT DEFAULT 0,"   //maybe add NOT NULL some day?
                    + COL_RETWEET_COUNT + " INT DEFAULT 0,"
                    + COL_MENTION_COUNT + " INT DEFAULT 0,"
                    + COL_SENT_AT + " DATETIME NOT NULL, "
                    + COL_PARENT_TWEET + " BIGINT REFERENCES "+COL_TWEET_ID + ", "
                    + COL_TWEET_TYPE + " " + TWEET_TYPE_ENUM +" NOT NULL "+")");
    }


    public boolean insert(Tweet tweet) {
        return insertWithParams(
                tweet,
                0,
                0,
                0,
                0L,
                TweetType.Tweet);
    }

    public boolean insert(Mention mention) {
        return insertWithParams(
                mention,
                0,
                0,
                0,
                mention.getMentionedTo().getTweetId(),
                TweetType.Mention);
    }

    public boolean insert(Quote quote) {
        return insertWithParams(quote,
                0,
                0,
                0,
                quote.getQuoted().getTweetId(),
                TweetType.Quote);
    }

    public boolean insert(Retweet retweet) {
        return insertWithParams(
                retweet,
                retweet.getRetweeted().getFavCount(),
                retweet.getRetweeted().getRetweetCount(),
                retweet.getRetweeted().getMentionCount(),
                retweet.getRetweeted().getTweetId(),
                TweetType.Retweet);
    }

    private boolean insertWithParams(Tweet tweet,
                                     int favCount,
                                     int retweetCount,
                                     int mentionCount,
                                     long parentTweet,
                                     TweetType tweetType) {
        try {
            int rowsUpdated = queryRunner.update(conn,
            "INSERT INTO " + TABLE_NAME + "("
                    + COL_TWEET_ID + ","
                    + COL_SENDER_ID + ","
                    + COL_TEXT + ","
                    + COL_FAV_COUNT + ","
                    + COL_RETWEET_COUNT + ","
                    + COL_MENTION_COUNT + ","
                    + COL_SENT_AT + ", "
                    + COL_PARENT_TWEET + ", "
                    + COL_TWEET_TYPE + ") "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        tweet.getTweetId(),
                        tweet.getSender().getUserId(),
                        tweet.getText(),
                        favCount,
                        retweetCount,
                        mentionCount,
                        tweet.getFormattedSentAt(),
                        parentTweet,
                        tweetType.name());

            return rowsUpdated == 1;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }

    private int findParent(int retweetId)
    {
        try (PreparedStatement pStmt = conn.prepareStatement("SELECT " + COL_PARENT_TWEET + " FROM " + TABLE_NAME + " WHERE "+ COL_TWEET_ID + "= (?)"))
        {
            int parentId;
            pStmt.setInt(1,retweetId);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next())
            {
                parentId = rs.getInt(COL_PARENT_TWEET);
                return parentId;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
        }
        return 0;
    }
    private String findTweetType(int tweetId)
    {
        try (PreparedStatement pStmt = conn.prepareStatement("SELECT " + COL_TWEET_TYPE + " FROM " + TABLE_NAME + " WHERE "+ COL_TWEET_ID + "= (?)"))
        {
            String type ;
            pStmt.setInt(1,tweetId);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next())
            {
                type = rs.getString(COL_TWEET_TYPE);
                return type;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
        }
        return null;
    }

    public synchronized void reduceLikes(int tweetId) {
        if (findTweetType(tweetId).equals(TweetType.Retweet.name())) {
            updateCounter(findParent(tweetId), COL_FAV_COUNT, -1);
        } else {
            updateCounter(tweetId, COL_FAV_COUNT, -1);
        }
    }

    public synchronized void incrementLikes(int tweetId) {
        if (findTweetType(tweetId).equals(TweetType.Retweet.name())) {
            updateCounter(findParent(tweetId), COL_RETWEET_COUNT, 1);
        } else {
            updateCounter(tweetId, COL_FAV_COUNT, 1);
        }
    }

    public synchronized boolean incrementRetweets(Retweet retweet) {
        return updateCounter(retweet.getRetweeted().getTweetId(), COL_RETWEET_COUNT, 1);
    }

    public synchronized boolean incrementMentions(Mention mention) {
        return updateCounter(mention.getTweetId(), COL_MENTION_COUNT,1);
    }


    private boolean updateCounter(long tweetId, String columnName, int value) {
        try {
            int affectedRow = queryRunner.update(
            "UPDATE " + TABLE_NAME + " SET " +
                    columnName + " = " + columnName + " + (?)  WHERE " + COL_TWEET_ID + " = (?)",
                    value,
                    tweetId);
            return affectedRow == 1;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }
    public synchronized List<Tweet> selectTimelineTweets(int userId, int MAX_COUNT) {
        try(PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT "
                    + Users.TABLE_NAME + "." + Users.COL_USERID + ", "
                    + Users.TABLE_NAME + "." + Users.COL_DISPLAY_NAME + ", "
                    + Users.TABLE_NAME + "." + Users.COL_USERNAME + ", "
                    + TABLE_NAME + "." + COL_TEXT + ", "
                    + TABLE_NAME + "." + COL_FAV_COUNT + ", "
                    + TABLE_NAME + "." + COL_RETWEET_COUNT + ", "
                    + TABLE_NAME + "." + COL_MENTION_COUNT + ", "
                    + TABLE_NAME + "." + COL_SENT_AT
                    + " FROM " + TABLE_NAME
                    + " INNER JOIN " + Followers.TABLE_NAME
                    + " ON " + Followers.TABLE_NAME + "." + Followers.COL_FOLLOWED
                    + " = " + TABLE_NAME + "." + COL_SENDER_ID
                    + " AND " + Followers.TABLE_NAME + "." + Followers.COL_FOLLOWER + " = (?)"
                    + " INNER JOIN " + Users.TABLE_NAME
                    + " ON " + TABLE_NAME + "." + COL_SENDER_ID + " = " + Users.TABLE_NAME + "." + Users.COL_USERID
                    + " WHERE " + TABLE_NAME + "." + COL_TWEET_TYPE + " = '" + TweetType.Tweet.name() + "'"
                    + " ORDER BY " +COL_SENT_AT+ " LIMIT " + MAX_COUNT)) {
            pStmt.setInt(1, userId);

            ResultSet resultSet =  pStmt.executeQuery();
            return DBUtils.timelineTweetsHandler(DBUtils.resultSetToList(resultSet));

        } catch (SQLException e ) {
            System.out.println(e.getSQLState());
        }

        return null;
    }

    public synchronized List<Retweet> selectTimelineRetweets(int userId, int MAX_COUNT) {
        final String USERS_TABLE_ALIAS_RT = "retweeter";
        final String USERS_TABLE_ALIAS_OS = "original_sender";
        final String TWEETS_TABLE_ALIAS_OG = "original";
        final String TWEETS_TABLE_ALIAS_RT = "retweeted";

        try(PreparedStatement pStmt = conn.prepareStatement(
                "SELECT "
                + USERS_TABLE_ALIAS_RT + "." + Users.COL_USERID + " AS retweeter_user_id, "
                + USERS_TABLE_ALIAS_RT + "." + Users.COL_DISPLAY_NAME + " AS retweeter_display_name, "
                + USERS_TABLE_ALIAS_OS + "." + Users.COL_USERID + ", "
                + USERS_TABLE_ALIAS_OS + "." + Users.COL_DISPLAY_NAME + ", "
                + USERS_TABLE_ALIAS_OS + "." + Users.COL_USERNAME + ", "
                + TWEETS_TABLE_ALIAS_RT + "." + COL_SENDER_ID + ", "
                + TWEETS_TABLE_ALIAS_OG + "." + COL_TEXT + ", "
                + TWEETS_TABLE_ALIAS_OG + "." + COL_FAV_COUNT + ", "
                + TWEETS_TABLE_ALIAS_OG + "." + COL_RETWEET_COUNT + ", "
                + TWEETS_TABLE_ALIAS_OG + "." + COL_MENTION_COUNT + ", "
                + TWEETS_TABLE_ALIAS_OG + "." + COL_SENT_AT
                + " FROM " + TABLE_NAME + " " + TWEETS_TABLE_ALIAS_RT
                + " INNER JOIN " + Users.TABLE_NAME + " AS " + USERS_TABLE_ALIAS_RT + " ON "
                        + TWEETS_TABLE_ALIAS_RT +"."+COL_SENDER_ID + "=" + USERS_TABLE_ALIAS_RT+"."+Users.COL_USERID
                + " INNER JOIN " + Followers.TABLE_NAME + " ON "
                        + Followers.TABLE_NAME +"."+Followers.COL_FOLLOWED +"="+ TWEETS_TABLE_ALIAS_RT+"."+COL_SENDER_ID
                + " INNER JOIN " + TABLE_NAME + " AS " + TWEETS_TABLE_ALIAS_OG + " ON "
                        + TWEETS_TABLE_ALIAS_RT+"."+COL_PARENT_TWEET +"="+ TWEETS_TABLE_ALIAS_OG+"."+COL_TWEET_ID +" AND "+ TWEETS_TABLE_ALIAS_RT+"."+COL_TWEET_TYPE +"= 'Retweet'"
                + " INNER JOIN " + Users.TABLE_NAME + " AS " + USERS_TABLE_ALIAS_OS + " ON "
                        + TWEETS_TABLE_ALIAS_OG+"."+COL_SENDER_ID +"="+ USERS_TABLE_ALIAS_OS+"."+Users.COL_USERID
                + " WHERE " + Followers.TABLE_NAME+"."+Followers.COL_FOLLOWER +"= (?)" +
                        " ORDER BY " + COL_SENT_AT + " LIMIT " + MAX_COUNT)) {

            pStmt.setInt(1, userId);

            ResultSet resultSet =  pStmt.executeQuery();
            return DBUtils.timelineRetweetsHandler(DBUtils.resultSetToList(resultSet));

        } catch (SQLException e ) {
            System.out.println(e.getSQLState());
        }

        return null;
    }


    public synchronized List<Quote> selectTimelineQuotes(int userId, int MAX_COUNT) {
        try (PreparedStatement pStmt = conn.prepareStatement(
                mentionQuoteHelper(
                        QUOTER_ALIAS,
                        QUOTED_ALIAS,
                        TweetType.Quote,
                        MAX_COUNT
                )
        )) {

            pStmt.setInt(1, userId);

            ResultSet resultSet = pStmt.executeQuery();
            return DBUtils.timelineQuoteHandler(DBUtils.resultSetToList(resultSet));

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }

        return null;
    }


    public synchronized List<Mention> selectTimelineMentions(int userId, int MAX_COUNT) {
        try (PreparedStatement pStmt = conn.prepareStatement(
                mentionQuoteHelper(
                        MENTIONER_ALIAS,
                        MENTIONED_ALIAS,
                        TweetType.Mention,
                        MAX_COUNT
                )
        )) {

            pStmt.setInt(1, userId);

            ResultSet resultSet = pStmt.executeQuery();
            return DBUtils.timelineMentionHandler(DBUtils.resultSetToList(resultSet));

        } catch (SQLException e ) {
            System.out.println(e.getSQLState());
        }

        return null;
    }


    private String mentionQuoteHelper(
            String users_alias_original,
            String tweets_alias_secondary,
            TweetType type,
            int MAX_COUNT
    ) {
        String person = (type == TweetType.Quote) ? ("quoter") : ("mentioner");

        return "SELECT " +
                users_alias_original + "." + Users.COL_USERID + " AS original_user_id, " +
                users_alias_original + "." + Users.COL_DISPLAY_NAME + " AS original_display_name, " +
                users_alias_original + "." + Users.COL_USERNAME + " AS original_username, " +
                "original_sender" + "." + Users.COL_USERID + " AS " + person + "_user_id, " +
                "original_sender" + "." + Users.COL_DISPLAY_NAME + " AS " + person + "_display_name, " +
                "original_sender" + "." + Users.COL_USERNAME + " AS " + person + "_username, " +
                "original" + "." + COL_TEXT + " AS original_text, " +
                "original" + "." + COL_FAV_COUNT + " AS original_fav_count, " +
                "original" + "." + COL_RETWEET_COUNT + " AS original_retweet_count, " +
                "original" + "." + COL_MENTION_COUNT + " AS original_mention_count, " +
                "original" + "." + COL_SENT_AT + " AS original_sent_at, " +
                tweets_alias_secondary + "." + COL_TEXT + " AS " + person + "_text, " +
                tweets_alias_secondary + "." + COL_FAV_COUNT + " AS " + person + "_fav_count, " +
                tweets_alias_secondary + "." + COL_RETWEET_COUNT + " AS " + person + "_retweet_count, " +
                tweets_alias_secondary + "." + COL_MENTION_COUNT + " AS " + person + "_mention_count, " +
                tweets_alias_secondary + "." + COL_SENT_AT + " AS " + person + "_sent_at"
                + " FROM " + Tweets.TABLE_NAME +" "+ tweets_alias_secondary
                + 	" INNER JOIN " + Users.TABLE_NAME + " as " + "original_sender" +" ON "
                + tweets_alias_secondary+"."+COL_SENDER_ID +"="+ "original_sender" +"."+Users.COL_USERID
                + 	" INNER JOIN " + Followers.TABLE_NAME +" on "
                + Followers.TABLE_NAME+"."+Followers.COL_FOLLOWED +"="+ tweets_alias_secondary+"."+COL_SENDER_ID
                + 	" inner JOIN " + Tweets.TABLE_NAME + " as " + "original" + " on "
                + tweets_alias_secondary+"."+COL_PARENT_TWEET +"="+ "original" +"."+COL_TWEET_ID +" and "+ tweets_alias_secondary+"."+COL_TWEET_TYPE +"= 'Mention'"
                + 	" INNER JOIN " + Users.TABLE_NAME + " as " + users_alias_original +" on "
                + "original" +"."+COL_SENDER_ID +"="+ users_alias_original+"."+Users.COL_USERID
                + " WHERE " + Followers.TABLE_NAME+"."+Followers.COL_FOLLOWER +"= (?)"
                + "ORDER BY " + tweets_alias_secondary+"."+COL_SENT_AT + " LIMIT " + MAX_COUNT;
    }
}
