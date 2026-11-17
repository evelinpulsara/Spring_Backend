package com.spring_calendario.tiemporeal;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class StreamTicketService {

    static final Duration TTL = Duration.ofSeconds(30);

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final Supplier<Instant> clock;

    public StreamTicketService() {
        this(Instant::now);
    }

    StreamTicketService(Supplier<Instant> clock) {
        this.clock = clock;
    }

    private record Ticket(UUID userId, Instant expiresAt) {}

    public String issue(UUID userId) {
        Instant now = clock.get();
        tickets.values().removeIf(t -> t.expiresAt().isBefore(now));
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tickets.put(ticket, new Ticket(userId, now.plus(TTL)));
        return ticket;
    }

    public Optional<UUID> redeem(String ticket) {
        if (ticket == null) return Optional.empty();
        Ticket t = tickets.remove(ticket);
        if (t == null || t.expiresAt().isBefore(clock.get())) return Optional.empty();
        return Optional.of(t.userId());
    }
}
