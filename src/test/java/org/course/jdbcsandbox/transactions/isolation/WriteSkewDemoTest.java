package org.course.jdbcsandbox.transactions.isolation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class WriteSkewDemoTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void repeatableReadAllowsWriteSkew() throws Exception {
        WriteSkewDemo demo = new WriteSkewDemo(dataSource);

        WriteSkewDemo.Result result = demo.demonstrateWriteSkewDemo(
                Connection.TRANSACTION_REPEATABLE_READ
        );

        assertAll(
                () -> assertEquals(2, result.committedCount()),
                () -> assertEquals(0, result.serializationFailureCount()),
                () -> assertEquals(0, result.onCallCount())
        );
    }

    @Test
    void serializablePreventsWriteSkew() throws Exception {
        WriteSkewDemo demo = new WriteSkewDemo(dataSource);

        WriteSkewDemo.Result result = demo.demonstrateWriteSkewDemo(
                Connection.TRANSACTION_SERIALIZABLE
        );

        assertAll(
                () -> assertEquals(1, result.committedCount()),
                () -> assertEquals(1, result.serializationFailureCount()),
                () -> assertEquals(1, result.onCallCount())
        );
    }
}
