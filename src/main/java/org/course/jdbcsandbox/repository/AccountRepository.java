package org.course.jdbcsandbox.repository;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository {

    int withdraw(Long accountId, BigDecimal amount);

    int deposit(Long accountId, BigDecimal amount);

    Optional<BigDecimal> findBalanceById(Long accountId);
}
