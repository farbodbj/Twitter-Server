package Server.StorageUnit;

import com.twitter.common.Exceptions.AttachmentError;
import com.twitter.common.Models.Messages.Textuals.Tweet;
import com.twitter.common.Models.Messages.Visuals.Image;
import com.twitter.common.Models.Messages.Visuals.Video;
import com.twitter.common.Models.Messages.Visuals.Visual;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.apache.commons.io.FilenameUtils;

import static com.twitter.common.Models.Messages.Visuals.Visual.ALLOWED_IMAGE_FORMAT_EXTENSIONS;

public class StorageManager {
    public final static String STORAGE_UNIT_PATH = "src/main/java/Server/StorageUnit/";
    public final static String TWEET_ATTACHMENTS_PATH = STORAGE_UNIT_PATH + "tweet_attachments/";
    public final static String USER_PICTURES_PATH = STORAGE_UNIT_PATH + "UserPictures/";

    public static void saveToFile(Path path, byte[] fileBytes) throws IOException {
        Files.write(path, fileBytes, StandardOpenOption.CREATE);
    }

    public static byte[] loadFromFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public static File[] directoryScan(String query, String dir) {
        File root = new File(dir);
        FilenameFilter beginswithm = (directory, filename) -> filename.startsWith(query);

        return root.listFiles(beginswithm);
    }

    public static boolean deleteFileIfExists(String query, String dir) {
        File[] matchingFiles = directoryScan(query, dir);
        boolean deletedAnyFile = false;

        if (matchingFiles != null) {
            for (File file : matchingFiles) {
                if (file.delete()) {
                    deletedAnyFile = true;
                }
            }
        }

        return deletedAnyFile;
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


    public static void loadAttachments(Tweet tweet) throws AttachmentError {
        try {
            File[] attachmentFiles = directoryScan(String.valueOf(tweet.getTweetId()), TWEET_ATTACHMENTS_PATH);
            LinkedList<Visual> attachments = new LinkedList<>();
            String fileFormat;
            Visual tmp;
            for (File file: attachmentFiles) {
                fileFormat = FilenameUtils.getExtension(file.getName());
                tmp = (ALLOWED_IMAGE_FORMAT_EXTENSIONS.contains(fileFormat)) ? (new Image()) : (new Video());


                tmp.setFileFormat(fileFormat);
                tmp.setFileBytes(loadFromFile(file.toPath()));
                attachments.add(tmp);
            }

            tweet.setAttachments(attachments);

        } catch (IOException e) {
            throw new AttachmentError("error loading attachments");
        }
    }

    public static void saveProfilePhoto(int userId, Image profilePic) throws AttachmentError {
        try {
            deleteFileIfExists(userId + "_PP", USER_PICTURES_PATH);
            saveToFile(
                    getProfilePicPath(userId, profilePic),
                    profilePic.getFileBytes());

        } catch (IOException e) {
            throw new AttachmentError("saving profile picture failed");
        }
    }

    public static void saveHeaderPhoto(int userId, Image profilePic) throws AttachmentError {
        try {
            deleteFileIfExists(userId + "_H", USER_PICTURES_PATH);
            saveToFile(
                    getHeaderPath(userId, profilePic),
                    profilePic.getFileBytes());

        } catch (IOException e) {
            throw new AttachmentError("saving header picture failed");
        }
    }

    public static Image loadProfilePhoto(int userId) {
        try {
            File[] profilePictureMatches = directoryScan(userId + "_PP", USER_PICTURES_PATH);
            if(profilePictureMatches.length == 0) return null;

            return  new Image(profilePictureMatches[0]);
        }
        catch (Exception e) {
            System.out.println("file error in loadProfilePhoto");
        }
        return null;
    }


    public static Image loadHeaderPhoto(int userId) {
        try {
            File[] headerPictureMatches = directoryScan(userId + "_H", USER_PICTURES_PATH);
            if(headerPictureMatches.length == 0) return null;

            return  new Image(headerPictureMatches[0]);
        }
        catch (Exception e) {
            System.out.println("file error in loadProfilePhoto");
        }
        return null;
    }



    private static Path getAttachmentPath(Visual visual, long tweetId, int position) {
        String URI = TWEET_ATTACHMENTS_PATH + tweetId + "_" + position + "." + visual.getFileFormat();
        visual.setPathInStorage(URI);
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

    private static Path getUserPicPath(int userId, Image image, String suffix) {
        String URI = USER_PICTURES_PATH + userId + suffix + image.getFileFormat();
        image.setPathInStorage(URI);
        return Paths.get(URI);
    }
}
