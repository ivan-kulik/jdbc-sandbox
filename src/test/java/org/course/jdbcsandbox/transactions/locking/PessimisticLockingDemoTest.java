package org.course.jdbcsandbox.transactions.locking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PessimisticLockingDemoTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void pessimisticLockingPreventsWriteSkew() throws Exception {
        PessimisticLockingDemo demo = new PessimisticLockingDemo(dataSource);

        PessimisticLockingDemo.Result result = demo.demonstratePessimisticLocking();

        assertAll(
                () -> assertEquals(1, result.removedCount()),
                () -> assertEquals(1, result.keptOnCallCount()),
                () -> assertEquals(1, result.onCallCount())
        );
    }
}
