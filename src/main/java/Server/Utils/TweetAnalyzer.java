package Server.Utils;

import com.twitter.common.Models.Messages.Textuals.Tweet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//this class is not meant to be instantiated, therefor the constructor is private
public class TweetAnalyzer {
    public static final String HASHTAG_RECOGNIZER = "(#+[a-zA-Z0-9(_)]+)";

    private TweetAnalyzer() {}

    public static String[] getHashtags(Tweet tweet) {
        String tweetText = tweet.getText();
        ArrayList<String> hashtags = new ArrayList<>();

        Pattern pattern = Pattern.compile(HASHTAG_RECOGNIZER, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tweetText);

        while(matcher.find())
            hashtags.add(matcher.group());

        return hashtags.toArray(new String[0]);
    }
}
