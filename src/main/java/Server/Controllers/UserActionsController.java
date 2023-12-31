package Server.Controllers;

import Server.Database.DatabaseController;
import Server.StorageUnit.StorageManager;
import Server.Utils.Snowflake;
import Server.Utils.TweetAnalyzer;
import com.twitter.common.Exceptions.AttachmentError;
import com.twitter.common.Models.Messages.Textuals.*;
import com.twitter.common.Models.Messages.Visuals.Image;
import com.twitter.common.Models.User;

import java.util.List;


//Singleton class
public class UserActionsController {
    private static UserActionsController instance;
    private final DatabaseController DBConnection = DatabaseController.getInstance();
    protected final Snowflake ID_GEN = new Snowflake();

    public static UserActionsController getInstance() {
        if(instance != null)
            return instance;
        
        instance = new UserActionsController();
        return instance;
    }

    private UserActionsController() {}

    public boolean signUp(User newUser) {
        return DBConnection.addUser(newUser);
    }

    public User signIn(String username, String passwordHash) {
        User user = DBConnection.getUser(username, passwordHash);
        if(user != null && user.getUserId() != 0)
            user.setProfilePic(StorageManager.loadProfilePhoto(user.getUserId()));
        return user;
    }

    public boolean follow(int followerId,int followedId) {
        return DBConnection.addFollower(followerId,followedId);
    }

    public boolean unfollow(int usernameFollower,int usernameFollowed) {
        return DBConnection.removeFollowing(usernameFollower,usernameFollowed);
    }

    public boolean tweet(Tweet tweet) {
        tweet.setTweetId(ID_GEN.nextId());
        try {
            StorageManager.saveAttachments(tweet);
        } catch (AttachmentError e) {
            return false;
        }
        DBConnection.addHashtags(TweetAnalyzer.getHashtags(tweet));
        return DBConnection.addTweet(tweet);
    }

    public boolean quote(Quote quote) {
        quote.setTweetId(ID_GEN.nextId());
        try {
            StorageManager.saveAttachments(quote);
        } catch (AttachmentError e) {
            return false;
        }
        DBConnection.addHashtags(TweetAnalyzer.getHashtags(quote));
        return DBConnection.addQuote(quote);
    }

    public boolean mention(Mention mention) {
        mention.setTweetId(ID_GEN.nextId());
        try {
            StorageManager.saveAttachments(mention);
        } catch (AttachmentError e) {
            return false;
        }
        DBConnection.addHashtags(TweetAnalyzer.getHashtags(mention));
        return DBConnection.addMention(mention);
    }

    public boolean retweet(Retweet retweet) {
        retweet.setTweetId(ID_GEN.nextId());
        DBConnection.addHashtags(TweetAnalyzer.getHashtags(retweet.getRetweeted()));
        return DBConnection.addRetweet(retweet);
    }
    public boolean like(int userId , long tweetId ) {
        return DBConnection.addLike(userId,tweetId);
    }

    public boolean unlike(int userId , long tweetId ) {
        return DBConnection.removeLike(userId,tweetId);
    }

    public boolean block(int blockerId, int blockedId) {
        DBConnection.removeFollower(blockedId, blockerId);
        DBConnection.removeFollowing(blockedId, blockerId);
        return DBConnection.addBlocked(blockerId, blockedId);

    }

    public boolean unblock(int blockerId, int blockedId) {
        return DBConnection.removeBlocked(blockerId, blockedId);
    }


    public void sendMassage(Direct directMessage) {
        DBConnection.sendMassage(directMessage);
    }

    public boolean setProfile(int userId, Image newProfile) {
        try {
            StorageManager.saveProfilePhoto(userId, newProfile);
        } catch (AttachmentError e) {
            return false;
        }
        return true;
    }

    public boolean setHeader(int userId, Image newHeader) {
        try {
            StorageManager.saveHeaderPhoto(userId, newHeader);
        } catch (AttachmentError e) {
            return false;
        }
        return true;
    }

    public boolean setDisplayName(int userId, String newDisplayName) {
        return DBConnection.setDisplayName(userId, newDisplayName);
    }

    public boolean setBio(int userId, String newBio) {
        return DBConnection.setBio(userId, newBio);
    }

    public boolean setLocation(int userId, String newLocation) {
        return DBConnection.setLocation(userId, newLocation);
    }
}
