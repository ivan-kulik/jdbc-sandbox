package org.course.jdbcsandbox.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class TransactionIsolationDemoTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testIsolationLevels() throws SQLException {
        TransactionIsolationDemo demo = new TransactionIsolationDemo(dataSource);

        demo.demonstrateNonRepeatableRead(
                Connection.TRANSACTION_READ_COMMITTED,
                "READ COMMITTED:"
        );

        demo.demonstrateNonRepeatableRead(
                Connection.TRANSACTION_REPEATABLE_READ,
                "REPEATABLE READ:"
        );
    }
}
