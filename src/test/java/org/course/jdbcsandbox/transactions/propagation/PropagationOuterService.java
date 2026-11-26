package org.course.jdbcsandbox.transactions.propagation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropagationOuterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            PropagationOuterService.class);

    private final PropagationDemoRepository repository;
    private final PropagationInnerService innerService;

    public PropagationOuterService(
            PropagationDemoRepository repository,
            PropagationInnerService innerService
    ) {
        this.repository = repository;
        this.innerService = innerService;
    }

    @Transactional
    public void requiredScenario() {
        repository.save(1L, "A");
        try {
            innerService.requiredFails();
        } catch (RuntimeException e) {
            LOGGER.info("Catch runtime exception from requiredFails()");
        }
        repository.save(3L, "C");
    }

    @Transactional
    public void requiresNewScenario() {
        repository.save(1L, "A");
        innerService.requiresNewSaves();
        throw new IllegalStateException();
    }

    @Transactional
    public void nestedScenario() {
        repository.save(1L, "A");
        try {
            innerService.nestedFails();
        } catch (RuntimeException e) {
            LOGGER.info("Catch runtime exception from nestedFails()");
        }
        repository.save(3L, "C");
    }
}
