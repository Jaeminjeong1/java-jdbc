package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    /**
     * 현재 스레드의 트랜잭션에 묶인 커넥션이 있으면 그것을, 없으면 새 커넥션을 반환한다.
     * 새 커넥션은 바인딩하지 않는다. 트랜잭션을 시작하는 쪽이 {@link TransactionSynchronizationManager#bindResource}로 직접 묶는다.
     */
    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    /**
     * 트랜잭션에 묶인 커넥션은 트랜잭션을 시작한 쪽이 닫으므로 닫지 않고, 그 외의 커넥션만 닫는다.
     */
    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}
