package Server.StorageUnit;


import com.twitter.common.Exceptions.AttachmentError;
import com.twitter.common.Models.Messages.Textuals.Tweet;
import com.twitter.common.Models.Messages.Visuals.Visual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import static Server.StorageUnit.StorageManager.saveToFile;

public class StorageUnitManager {
    public final static String TWEET_ATTACHMENTS_PATH = "src\\main\\java\\server\\StorageUnit\\tweet_attachments\\";

    public static void saveAttachments(Tweet tweet) throws AttachmentError {
        try {
            int position = 0;
            for (Visual att : tweet.getAttachments()) {
                saveToFile(getAttachmentPath(att, tweet.getTweetId(), position++), att.getFileBytes());
            }
        } catch (IOException e) {
            throw new AttachmentError("saving attachments failed");
        }
    }

    public static LinkedList<Visual> loadAttachments(Tweet tweet) throws AttachmentError {
        try {
            LinkedList<Visual> attachments = (LinkedList<Visual>) tweet.getAttachments();
            for (Visual attachment : attachments)
                attachment.setFileBytes(Files.readAllBytes(attachment.getPathInStorage()));
            return attachments;
        } catch (IOException e) {
            throw new AttachmentError("error loading attachments");
        }
    }

    private static Path getAttachmentPath(Visual visual, long tweetId, int position) {
        String URI = TWEET_ATTACHMENTS_PATH + tweetId + "_" + position + visual.getFileFormat();
        visual.setPathInStorage(URI);
        return Paths.get(URI);
    }
}
