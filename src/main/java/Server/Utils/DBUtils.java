package Server.Utils;

import Server.Database.MessageData.Chats;
import Server.Database.MessageData.Messages;
import Server.Database.MessageData.Tweets;
import Server.Database.UserData.Users;
import com.twitter.common.Models.Chat;
import com.twitter.common.Models.Messages.Textuals.*;
import com.twitter.common.Models.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class DBUtils {
    public static List<HashMap<String,Object>> resultSetToList(ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            List<HashMap<String, Object>> list = new ArrayList<>();

            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }

            return list;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }

        return null;
    }

    public static Map<String, Object> resultSetToHashMap(ResultSet rs) {
        HashMap<String, Object> row = null;
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            row = new HashMap<>(columns);

            for (int i = 1; i <= columns; ++i)
                row.put(md.getColumnName(i), rs.getObject(i));

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }

        return row;
    }

    public static User resultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        try {
            user.setUserId(rs.getInt(Users.COL_USERID));
            user.setDisplayName(rs.getString(Users.COL_DISPLAY_NAME));
            user.setUsername(rs.getString(Users.COL_USERNAME));
            user.setEmail(rs.getString(Users.COL_EMAIL));
            user.setDateOfBirth(rs.getDate(Users.COL_DATE_OF_BIRTH));
            user.setAccountMade(rs.getDate(Users.COL_ACCOUNT_MADE));
            user.setBio(rs.getString(Users.COL_BIO));
            user.setLocation(rs.getString(Users.COL_LOCATION));
        } catch (SQLException ignore) {
            //column not found should be ignored since not all columns are needed
        }
        return user;
    }

    public static List<User> resultSetToUserList(ResultSet resultSet) {
        List<User> userList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                User user = resultSetToUser(resultSet);
                userList.add(user);
            }

            return userList;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static Chat resultSetToChat(ResultSet rs) throws SQLException {
        Chat chat = new Chat(
                rs.getInt(Chats.COL_FIRST_USER),
                rs.getInt(Chats.COL_SECOND_USER));

        while(rs.next()) {
            chat.addDM(resultSetToMessage(rs));
        }

        return chat;
    }

    public static Direct resultSetToMessage(ResultSet rs) throws SQLException {
        Direct directMessage = new Direct();
        int senderID = Integer.parseInt(rs.getString(Messages.COL_SENDER_ID));
        String messageText = rs.getString(Messages.COL_MESSAGE_TEXT);
        String sentAt = rs.getString(Messages.COL_SENT_AT);
        boolean isReceived = Boolean.parseBoolean(rs.getString(Messages.COL_IS_RECEIVED));

        directMessage.getSender().setUserId(senderID);
        directMessage.setMessageText(messageText);
        directMessage.setFormattedSentAt(sentAt);
        directMessage.setReceived(isReceived);

        return directMessage;
    }


    public static List<Tweet> timelineTweetsHandler(List<HashMap<String, Object>> tweets) {
        List<Tweet> timelineTweets = new ArrayList<>();
        for (HashMap<String, Object> tweetHashMap: tweets) {
            User user = new User();
            Tweet tweet = new Tweet();

            user.setUsername((String) tweetHashMap.get(Users.COL_USERNAME));
            user.setDisplayName((String) tweetHashMap.get(Users.COL_DISPLAY_NAME));
            user.setUserId((int) tweetHashMap.get(Users.COL_USERID));

            tweet.setTweetId((long) tweetHashMap.get(Tweets.COL_TWEET_ID));
            tweet.setText((String) tweetHashMap.get(Tweets.COL_TEXT));
            tweet.setFavCount((int) tweetHashMap.get(Tweets.COL_FAV_COUNT));
            tweet.setRetweetCount((int) tweetHashMap.get(Tweets.COL_RETWEET_COUNT));
            tweet.setMentionCount((int) tweetHashMap.get(Tweets.COL_MENTION_COUNT));
            tweet.setSentAt((LocalDateTime) tweetHashMap.get(Tweets.COL_SENT_AT));

            tweet.setSender(user);
            timelineTweets.add(tweet);
        }
        return timelineTweets;
    }

    public static List<Retweet> timelineRetweetsHandler(List<HashMap<String, Object>> retweets) {
        List<Retweet> retweetList = new ArrayList<>();

        for (HashMap<String, Object> retweetMap : retweets) {
            User retweeter = new User();
            User originalUser = new User();
            Tweet originalTweet = new Tweet();
            Retweet retweet = new Retweet();

            retweeter.setUserId((int) retweetMap.get("retweeter_user_id"));
            retweeter.setDisplayName((String) retweetMap.get("retweeter_display_name"));

            originalUser.setUserId((int) retweetMap.get(Users.COL_USERID));
            originalUser.setDisplayName((String) retweetMap.get(Users.COL_DISPLAY_NAME));
            originalUser.setUsername((String) retweetMap.get(Users.COL_USERNAME));

            originalTweet.setSender(originalUser);
            originalTweet.setTweetId((long) retweetMap.get(Tweets.COL_TWEET_ID));
            originalTweet.setText((String) retweetMap.get(Tweets.COL_TEXT));
            originalTweet.setFavCount((int) retweetMap.get(Tweets.COL_FAV_COUNT));
            originalTweet.setRetweetCount((int) retweetMap.get(Tweets.COL_RETWEET_COUNT));
            originalTweet.setMentionCount((int) retweetMap.get(Tweets.COL_MENTION_COUNT));
            originalTweet.setSentAt(((LocalDateTime) retweetMap.get(Tweets.COL_SENT_AT)));

            retweet.setSentAt((LocalDateTime) retweetMap.get(Tweets.COL_SENT_AT));
            retweet.setRetweeted(originalTweet);
            retweet.setSender(retweeter);


            retweetList.add(retweet);
        }

        return retweetList;
    }

    public static List<Quote> timelineQuoteHandler(List<HashMap<String, Object>> quotes) {
        return timelineHandler(quotes, Quote::new, Tweets.QUOTER_ALIAS);
    }

    public static List<Mention> timelineMentionHandler(List<HashMap<String, Object>> mentions) {
        return timelineHandler(mentions, Mention::new, Tweets.MENTIONER_ALIAS);
    }

    public static <T extends Tweet> List<T> timelineHandler(List<HashMap<String, Object>> rows, BiFunction<User, Tweet, T> tweetConstructor, String senderPrefix) {
        List<T> tweets = new ArrayList<>();

        for (HashMap<String, Object> row : rows) {
            User originalUser = new User();
            User sender = new User();
            Tweet originalTweet = new Tweet();
            T tweet = tweetConstructor.apply(sender, originalTweet);

            originalUser.setUserId((int) row.get("original_user_id"));
            originalUser.setDisplayName((String) row.get("original_display_name"));
            originalUser.setUsername((String) row.get("original_username"));

            sender.setUserId((int) row.get(senderPrefix + "_user_id"));
            sender.setDisplayName((String) row.get(senderPrefix + "_display_name"));
            sender.setUsername((String) row.get(senderPrefix + "_username"));

            originalTweet.setSender(originalUser);
            originalTweet.setTweetId((long) row.get("original_tweet_id"));
            originalTweet.setText((String) row.get("original_text"));
            originalTweet.setFavCount((int) row.get("original_fav_count"));
            originalTweet.setRetweetCount((int) row.get("original_retweet_count"));
            originalTweet.setMentionCount((int) row.get("original_mention_count"));
            originalTweet.setSentAt(((LocalDateTime) row.get("original_sent_at")));

            tweet.setSender(sender);
            tweet.setTweetId((long) row.get(senderPrefix + "_tweet_id"));
            tweet.setText((String) row.get(senderPrefix + "_text"));
            tweet.setFavCount((int) row.get(senderPrefix + "_fav_count"));
            tweet.setRetweetCount((int) row.get(senderPrefix + "_retweet_count"));
            tweet.setMentionCount((int) row.get(senderPrefix + "_mention_count"));
            tweet.setSentAt(((LocalDateTime) row.get(senderPrefix + "_sent_at")));

            tweets.add(tweet);
        }

        return tweets;
    }
}
