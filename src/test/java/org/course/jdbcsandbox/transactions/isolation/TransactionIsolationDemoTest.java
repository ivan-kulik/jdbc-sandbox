package org.course.jdbcsandbox.transactions.isolation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class TransactionIsolationDemoTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testIsolationLevels() throws SQLException {
        TransactionIsolationDemo demo = new TransactionIsolationDemo(dataSource);

        BigDecimal readCommited = demo.demonstrateNonRepeatableRead(
                Connection.TRANSACTION_READ_COMMITTED,
                "READ COMMITTED:"
        );

        assertEquals(new BigDecimal("700.00"), readCommited);

        BigDecimal repeatableRead = demo.demonstrateNonRepeatableRead(
                Connection.TRANSACTION_REPEATABLE_READ,
                "REPEATABLE READ:"
        );

        assertEquals(new BigDecimal("1000.00"), repeatableRead);
    }
}
