package Server.Controllers;

import Server.Database.DatabaseController;
import Server.StorageUnit.StorageManager;
import com.twitter.common.Exceptions.AttachmentError;
import com.twitter.common.Models.Chat;
import com.twitter.common.Models.Messages.Textuals.Mention;
import com.twitter.common.Models.Messages.Textuals.Quote;
import com.twitter.common.Models.Messages.Textuals.Retweet;
import com.twitter.common.Models.Messages.Textuals.Tweet;
import com.twitter.common.Models.Timeline;
import com.twitter.common.Models.User;

import java.io.IOException;
import java.util.List;

public class DataController //gets timeline and profile and etc.
{
    private final DatabaseController DBController = DatabaseController.getInstance();
    private static DataController instance;

    public static DataController getInstance() {
        if(instance != null)
            return instance;

        instance = new DataController();
        return instance;
    }

    public Chat getChat(Chat chat) {
        return DBController.getChat(chat);
    }

    public User getUser(int userId) {
        User user =  DBController.getUser(userId);
        if(user == null) return null;

        user.setHeaderPic(StorageManager.loadHeaderPhoto(userId));
        user.setProfilePic(StorageManager.loadProfilePhoto(userId));
        return user;
    }

    public int getFollowersCount(int userId) {return DBController.getFollowersCount(userId);}

    public List<User> getFollowers(int userId)
    {
        return DBController.getFollowers(userId);
    }

    public int getFollowingsCount(int userId) {return DBController.getFollowingsCount(userId);}

    public List<User> getFollowings(int userId)
    {
        return DBController.getFollowings(userId);
    }

    public Timeline getTimeline(int userId, int MAX_COUNT) {
        Timeline timeline = new Timeline();
        timeline.setForUser(userId);
        timeline.addAll(DBController.generateTimeline(userId, MAX_COUNT));
        loadTimelineAttachments(timeline);
        return timeline;
    }

    private void loadTimelineAttachments(Timeline timeline) {
        try {
            for (Tweet tweet : timeline.getTimelineTweets()) {
                StorageManager.loadAttachments(tweet);
            }
        } catch (AttachmentError e) {
            System.out.println("loading timeline attachments failed.");
            //Error handling logic
        }
    }

    private void loadTimelineProfilePhotos(Timeline timeline) { //TODO: complete this method

        for(Tweet tweet: timeline.getTimelineTweets()) {
             if (tweet instanceof Mention) {
                StorageManager.loadProfilePhoto(((Mention) tweet).getMentionedTo().getSender().getUserId());

            } else if (tweet instanceof Quote) {

            } else if (tweet instanceof Retweet) {

            } else {

             }


        }

    }
}
