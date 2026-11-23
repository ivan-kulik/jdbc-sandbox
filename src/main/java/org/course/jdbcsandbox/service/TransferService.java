package org.course.jdbcsandbox.service;

import org.course.jdbcsandbox.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount
    ) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException(
                    "FromAccountID and toAccountID must be different"
            );
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount to transfer must be greater than zero"
            );
        }
        if (Math.max(0, amount.stripTrailingZeros().scale()) > 2) {
            throw new IllegalArgumentException(
                    "Amount to transfer must have no more than 2 decimal places"
            );
        }

        int affectedRows = accountRepository.withdraw(fromAccountId, amount);
        if (affectedRows != 1) {
            throw new IllegalStateException("SQL query UPDATE went wrong.");
        }

        affectedRows = accountRepository.deposit(toAccountId, amount);
        if (affectedRows != 1) {
            throw new IllegalStateException("SQL query UPDATE went wrong.");
        }
    }
}
