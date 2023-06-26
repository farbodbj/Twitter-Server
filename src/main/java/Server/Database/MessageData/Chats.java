package Server.Database.MessageData;


import Server.Database.Insertable;
import Server.Database.Table;
import Server.Database.UserData.Users;
import Server.Utils.DBUtils;
import com.twitter.common.Models.Chat;
import java.sql.*;


public class Chats extends Table implements Insertable<Chat> {
    public final static String TABLE_NAME = "Chats";
    public final static String COL_CHAT_ID = "chatId";
    public final static String COL_FIRST_USER = "User1";
    public final static String COL_SECOND_USER = "User2";
    //private final static Connection conn = DatabaseController.getConnection();

    @Override
    public void createTable() throws SQLException {
        queryRunner.execute(conn,
                        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                            + COL_CHAT_ID + " BIGINT NOT NULL PRIMARY KEY, "
                            + COL_FIRST_USER + " INT, "
                            + COL_SECOND_USER + " INT, "
                            + "FOREIGN KEY("+COL_FIRST_USER+") REFERENCES " + Users.TABLE_NAME +"("+Users.COL_USERID +"), "
                            + "FOREIGN KEY("+COL_SECOND_USER+") REFERENCES " + Users.TABLE_NAME + "(" + Users.COL_USERID + "))"
        );

    }

    @Override
    public boolean insert(Chat chat) {
        try {
            int insertCount =
                    queryRunner.update(conn,
                            "INSERT INTO " + TABLE_NAME +
                            "("+ COL_CHAT_ID+ ", "+ COL_FIRST_USER + "," + COL_SECOND_USER +")"
                            + "VALUES (?, ?, ?)",
                            chat.getChatId(),
                            chat.getUser1().getUserId(),
                            chat.getUser2().getUserId());

            return insertCount == 1;
        }
        catch (SQLException e) {
            System.out.println(e.getSQLState());
            return false;
        }
    }


    public Chat selectChat(long chatId) {
        try(PreparedStatement preparedStatement = conn.prepareStatement(
            "SELECT "+Chats.TABLE_NAME+".*, "+Messages.TABLE_NAME+".* " +
                    "FROM "+Chats.TABLE_NAME+" " +
                    "JOIN "+Messages.TABLE_NAME+
                        " ON "+Chats.TABLE_NAME+"."+COL_CHAT_ID +"="+ Messages.TABLE_NAME+"."+Messages.COL_CHAT_ID + " " +
                    "WHERE "+Chats.TABLE_NAME+"."+COL_CHAT_ID +"= (?)")) {
            preparedStatement.setLong(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();

            return DBUtils.resultSetToChat(resultSet);

        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return null;
        }
    }



}
