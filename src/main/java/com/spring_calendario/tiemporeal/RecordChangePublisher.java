package com.spring_calendario.tiemporeal;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RecordChangePublisher {

    private final EventBus bus;

    public RecordChangePublisher(EventBus bus) {
        this.bus = bus;
    }

    public void recordsChanged(UUID userId, Recurso recurso) {
        Runnable publish = () -> bus.publish(userId, RealtimeEvent.recordsChanged(recurso));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }
}
