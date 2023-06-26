package Server.Database;

import org.apache.commons.dbutils.QueryRunner;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Table {

    public static final QueryRunner queryRunner = new QueryRunner();
    protected final Connection conn = DatabaseController.getConnection();
    public abstract void createTable() throws SQLException;
}
