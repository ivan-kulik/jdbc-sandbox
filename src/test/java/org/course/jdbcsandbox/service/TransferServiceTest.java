package org.course.jdbcsandbox.service;

import org.course.jdbcsandbox.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TransferServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferService transferService;
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        String sqlQuery = """
                INSERT INTO accounts (id, balance)
                VALUES (1, 1000.00), (2, 500.00)
                ON CONFLICT (id)
                DO UPDATE SET balance = EXCLUDED.balance;
                """;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(sqlQuery);
        }
    }

    @Test
    void successfulTransfer() {
        transferService.transfer(
                1L, 2L, new BigDecimal("300.00")
        );

        assertEquals(
                0,
                accountRepository.findBalanceById(1L)
                        .orElseThrow()
                        .compareTo(new BigDecimal("700.00"))
        );

        assertEquals(
                0,
                accountRepository.findBalanceById(2L)
                        .orElseThrow()
                        .compareTo(new BigDecimal("800.00"))
        );
    }

    @Test
    void rollbackOnFailedDeposit() {
        assertThrows(
                IllegalStateException.class,
                () -> transferService.transfer(
                        1L, 999999L, new BigDecimal("300.00")
                )
        );

        assertEquals(
                0,
                accountRepository.findBalanceById(1L)
                        .orElseThrow()
                        .compareTo(new BigDecimal("1000.00"))
        );

        assertEquals(
                0,
                accountRepository.findBalanceById(2L)
                        .orElseThrow()
                        .compareTo(new BigDecimal("500.00"))
        );
    }

}
