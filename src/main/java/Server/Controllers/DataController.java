package Server.Controllers;

import Server.Database.DatabaseController;
import com.twitter.common.Models.Chat;
import com.twitter.common.Models.Timeline;
import com.twitter.common.Models.User;

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

    public List<User> getFollowers(int userId)
    {
        return DBController.getFollowers(userId);
    }

    public List<User> getFollowings(int userId)
    {
        return DBController.getFollowings(userId);
    }

    public Timeline getTimeline(int userId, int MAX_COUNT) {
        Timeline timeline = new Timeline();
        timeline.setForUser(userId);
        timeline.addAll(DBController.generateTimeline(userId, MAX_COUNT));
        return timeline;
    }
}
