package Server.Database;

import Server.Database.MessageData.*;
import Server.Database.UserData.*;
import com.twitter.common.Models.Chat;
import com.twitter.common.Models.Messages.Textuals.*;
import com.twitter.common.Models.User;

import java.sql.*;
import java.util.*;


//Singleton class
public class DatabaseController {
    private final String DB_URL = "jdbc:mysql://localhost/twitter";
    private final String DB_USERNAME = "root";
    private final String DB_PASS = "AP_Twitter";
    private static Connection conn;
    private Tweets tweets;
    private Followers followers ;
    private Users users;
    private BlockList blockList;
    private Hashtags hashtags;
    private Countries countries;
    private Likes likes;
    private Chats chats;
    private Messages messages;
    private static DatabaseController instance;

    public static DatabaseController getInstance() {
        if(instance != null)
            return instance;

        instance = new DatabaseController();
        return instance;
    }

    private DatabaseController() {
        try {
            //establish database connection
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASS);
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }
    }

    public void initializeDB() throws SQLException {
        hashtags = new Hashtags();
        countries = new Countries();
        users = new Users();
        followers = new Followers();
        tweets = new Tweets();
        blockList = new BlockList();
        chats = new Chats();
        messages = new Messages();
        likes = new Likes();

        hashtags.createTable();
        countries.createTable();
        users.createTable();
        followers.createTable();
        tweets.createTable();
        blockList.createTable();
        chats.createTable();
        messages.createTable();
        likes.createTable();
    }

    public static Connection getConnection() {
        return conn;
    }

    public boolean addUser(User user) {
        return users.insert(user);
    }

    public User getUser(String username, String passwordHash) {
        return users.selectUser(username, passwordHash);
    }

    public User getUser(int userId) {
        return users.selectUser(userId);
    }

    public boolean addFollower(int followerId, int followedId) {
        if(users.selectUser(followerId) == null || users.selectUser(followedId) == null)
            return false;
        return followers.insert(followerId, followedId);
    }

    public boolean removeFollowing(int followerId, int followedId) {
        return followers.delete(followerId, followedId);
    }

    public boolean removeFollower(int followerId, int followedId) {
        return followers.delete(followedId, followerId);
    }

    public boolean addTweet(Tweet tweet) {
        return tweets.insert(tweet);
    }

    public boolean addQuote(Quote quote) {
        return tweets.insert(quote);
    }

    public boolean addMention(Mention mention) {
        return tweets.insert(mention) && tweets.incrementMentions(mention);
    }

    public boolean addRetweet(Retweet retweet) {
        return tweets.insert(retweet) && tweets.incrementRetweets(retweet);
    }

    public boolean addLike(int userId , int tweetId) {
        tweets.incrementLikes(tweetId);
        return likes.insert(userId,tweetId);
    }

    public boolean removeLike(int userId , int tweetId) {
        tweets.reduceLikes(tweetId);
        return likes.remove(userId,tweetId);
    }

    public boolean addHashtags(String[] hashtagArray) {
        for (String hashtag: hashtagArray)
            if(!hashtags.insert(hashtag))
                return false;
        return true;
    }

    public boolean addBlocked(int blockerId, int blockedId) {
        return blockList.insert(blockerId, blockedId);
    }

    public boolean removeBlocked(int blockerId, int blockedId) {
        return blockList.remove(blockerId, blockedId);
    }


    public List<Tweet> generateTimeline(int userId, int MAX_COUNT) {
        List<Tweet> timeline = tweets.selectTimelineTweets(userId, MAX_COUNT);
        timeline.addAll(tweets.selectTimelineRetweets(userId, MAX_COUNT));
        timeline.addAll(tweets.selectTimelineQuotes(userId, MAX_COUNT));
        timeline.addAll(tweets.selectTimelineMentions(userId, MAX_COUNT));

        timeline.sort(Comparator.comparing(Tweet::getSentAt));

        return timeline;
    }

    public Map<String, Integer> getHashtagReport() {
        //TODO: just a simple query from db
        return null;
    }

    public boolean emailExists(String email) {
        return users.exists(Users.COL_EMAIL, email);
    }

    public boolean usernameExists(String username) {
        return users.exists(Users.COL_USERNAME, username);
    }

    public void sendMassage(Direct directMessage) {
        messages.insert(directMessage);
    }

    public void addChat(Chat chat) {
        chats.insert(chat);
    }

    public Chat getChat(Chat chat) {
        return chats.selectChat(chat.getChatId());
    }

    public List<User> getFollowers(int userId)
    {
        return followers.selectFollowers(userId);
    }

    public List<User> getFollowings(int userId)
    {
        return followers.selectFollowings(userId);
    }


}
