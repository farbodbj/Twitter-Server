package Server.Database.MessageData;


import Server.Database.Insertable;
import Server.Database.Table;
import Server.Database.UserData.Users;
import com.twitter.common.Models.Messages.Textuals.Direct;
import java.sql.*;


public class Messages extends Table implements Insertable<Direct> {

    public final static String TABLE_NAME = "Messages";
    public final static String COL_CHAT_ID = "chatId";
    public final static String COL_SENDER_ID = "senderId";
    public final static String COL_MESSAGE_TEXT = "messageText";
    public final static String COL_SENT_AT = "sentAt";
    public final static String COL_IS_RECEIVED = "isReceived";
    //private final static Connection conn = DatabaseController.getConnection();

    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                        + COL_CHAT_ID + " BIGINT,"
                        + COL_SENDER_ID + " INT, "
                        + COL_MESSAGE_TEXT + " VARCHAR("+Direct.MAX_MESSAGE_LENGTH+"), "
                        + COL_SENT_AT + " DATETIME,  "
                        + COL_IS_RECEIVED + " BOOLEAN ,"
                        + "FOREIGN KEY("+COL_CHAT_ID+") REFERENCES " + Chats.TABLE_NAME+"("+Chats.COL_CHAT_ID+") ,"
                        + "FOREIGN KEY("+COL_SENDER_ID+") REFERENCES " + Users.TABLE_NAME +"("+Users.COL_USERID +"))");
    }

    @Override
    public boolean insert(Direct  directMessage) {
        try {
            int insertCount =
                    queryRunner.update(conn,
                       "INSERT INTO " +TABLE_NAME+ "(" + COL_SENDER_ID + "," +
                            COL_MESSAGE_TEXT + "," + COL_SENT_AT +"," + COL_IS_RECEIVED +")"
                            + "VALUES (?,?,?,?)",
                            directMessage.getSender().getUserId(),
                            directMessage.getMessageText(),
                            directMessage.getFormattedSentAt(),
                            false);

            return insertCount == 1;
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }

}
