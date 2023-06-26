package Server.StorageUnit;

import com.twitter.common.Exceptions.AttachmentError;
import com.twitter.common.Models.Messages.Textuals.Tweet;
import com.twitter.common.Models.Messages.Visuals.Image;
import com.twitter.common.Models.Messages.Visuals.Visual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

public class StorageManager {
    //public final static String STORAGE_UNIT_PATH = "src\\main\\java\\server\\StorageUnit\\";
    public final static String STORAGE_UNIT_PATH = "src/main/java/Server/StorageUnit/";
    public final static String TWEET_ATTACHMENTS_PATH = STORAGE_UNIT_PATH + "tweet_attachments/";
    public final static String USER_PICTURES_PATH = STORAGE_UNIT_PATH + "UserPictures/";

    public static void saveToFile(Path path, byte[] fileBytes) throws IOException {
        Files.write(path, fileBytes, StandardOpenOption.CREATE);
    }

    public static byte[] loadFromFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public static void saveAttachments(Tweet tweet) throws AttachmentError {
        try {
            int position = 0;
            for (Visual att : tweet.getAttachments()) {
                StorageManager.saveToFile(getAttachmentPath(att, tweet.getTweetId(), position++), att.getFileBytes());
            }
        } catch (IOException e) {
            throw new AttachmentError("saving attachments failed");
        }
    }


    public static LinkedList<Visual> loadAttachments(Tweet tweet) throws AttachmentError {
        try {
            LinkedList<Visual> attachments = (LinkedList<Visual>) tweet.getAttachments();
            for (Visual attachment : attachments)
                attachment.setFileBytes(loadFromFile(attachment.getPathInStorage()));
            return attachments;
        } catch (IOException e) {
            throw new AttachmentError("error loading attachments");
        }
    }

    public static void saveProfilePhoto(int userId, Image profilePic) throws AttachmentError {
        try {
            saveToFile(
                    getProfilePicPath(userId, profilePic),
                    profilePic.getFileBytes());

        } catch (IOException e) {
            throw new AttachmentError("saving profile picture failed");
        }
    }

    public static void saveHeader(int userId, Image profilePic) throws AttachmentError {
        try {
            saveToFile(
                    getHeaderPath(userId, profilePic),
                    profilePic.getFileBytes());

        } catch (IOException e) {
            throw new AttachmentError("saving header picture failed");
        }
    }

    private static Path getAttachmentPath(Visual visual, long tweetId, int position) {
        String URI = TWEET_ATTACHMENTS_PATH + tweetId + "_" + position + "." + visual.getFileFormat();
        visual.setPathInStorage(URI);
        return Paths.get(URI);
    }


    private static Path getUserPicPath(int userId, Image image, String suffix) {
        String URI = USER_PICTURES_PATH + userId + "_" + suffix + "." + image.getFileFormat();
        image.setPathInStorage(URI);
        return Paths.get(URI);
    }

    private static Path getProfilePicPath(int userId, Image profilePic) {
        final String profilePicSuffix = "_PP";
        return getUserPicPath(userId, profilePic, profilePicSuffix);
    }

    private static Path getHeaderPath(int userId, Image header) {
        final String headerSuffix = "_H";
        return getUserPicPath(userId, header, headerSuffix);
    }


}
