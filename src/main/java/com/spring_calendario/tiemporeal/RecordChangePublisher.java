package com.spring_calendario.tiemporeal;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Avisa a la app que cambiaron los registros de una usuaria.
 *
 * <p>El aviso sale DESPUÉS de guardar en la base de datos (after commit): si
 * saliera antes, la otra pestaña podría recargar y leer los datos viejos.
 */
@Component
public class RecordChangePublisher {

    private final EventBus bus;

    public RecordChangePublisher(EventBus bus) {
        this.bus = bus;
    }

    public void recordsChanged(UUID userId, String resource) {
        Runnable publish = () -> bus.publish(userId, RealtimeEvent.recordsChanged(resource));
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
