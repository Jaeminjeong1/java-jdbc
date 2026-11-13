package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceUtilsTest {

    private JdbcConnectionPool dataSource;

    @BeforeEach
    void setUp() {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "", "");
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        dataSource.dispose();
    }

    @Test
    void getsNewUnboundConnectionAndClosesItOnRelease() throws SQLException {
        final var connection = DataSourceUtils.getConnection(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        DataSourceUtils.releaseConnection(connection, dataSource);

        assertThat(connection.isClosed()).isTrue();
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void getsBoundConnectionAndDoesNotCloseItOnRelease() throws SQLException {
        try (final var bound = dataSource.getConnection()) {
            TransactionSynchronizationManager.bindResource(dataSource, bound);

            final var connection = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);

            assertThat(connection).isSameAs(bound);
            assertThat(bound.isClosed()).isFalse();
        }
    }
}
