package org.course.jdbcsandbox.transactions.propagation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropagationInnerService {

    private final PropagationDemoRepository repository;

    public PropagationInnerService(
            PropagationDemoRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredFails() {
        repository.save(2L, "B");
        throw new IllegalStateException();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewSaves() {
        repository.save(2L, "B");
    }

    @Transactional(propagation = Propagation.NESTED)
    public void nestedFails() {
        repository.save(2L, "B");
        throw new IllegalStateException();
    }
}
