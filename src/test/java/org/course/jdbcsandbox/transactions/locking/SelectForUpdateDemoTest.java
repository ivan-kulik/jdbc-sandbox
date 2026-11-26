package org.course.jdbcsandbox.transactions.locking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class SelectForUpdateDemoTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void rowLockPreventsConcurrentLocking() {
        SelectForUpdateDemo demo = new SelectForUpdateDemo(dataSource);

        SQLException exception = assertThrows(
                SQLException.class,
                demo::demonstrateRowLock
        );

        assertEquals("55P03", exception.getSQLState());
    }

}
