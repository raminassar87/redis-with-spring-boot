package com.learnredis.learnredis.service;

import com.learnredis.learnredis.bean.Event;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventService {

    // In-memory store simulating a database
    private final Map<String, Event> db = new HashMap<>(Map.of(
            "1", new Event("1", "Spring Boot Conf", "Berlin", "2026-05-10"),
            "2", new Event("2", "Redis Summit",     "London", "2026-06-20"),
            "3", new Event("3", "JavaOne",           "SF",     "2026-09-15")
    ));

    // Cache the result. On subsequent calls with the same id, the method body
    // is skipped and the value is returned directly from Redis.
    @Cacheable(value = "events", key = "#id")
    public Event getEvent(String id) {
        System.out.println(">> DB hit for event: " + id); // won't print on cache hits
        return db.get(id);
    }

    // Always executes the method AND updates the cache with the returned value.
    // Use this for create/update so the cache stays in sync.
    @CachePut(value = "events", key = "#event.id")
    public Event saveEvent(Event event) {
        db.put(event.getId(), event);
        return event;
    }

    // Removes the entry from the cache. Use this on delete.
    @CacheEvict(value = "events", key = "#id")
    public void deleteEvent(String id) {
        db.remove(id);
    }

    // Clears every entry under the "events" cache name.
    @CacheEvict(value = "events", allEntries = true)
    public void clearAllEvents() {
        // cache cleared; db untouched in this example
    }
}
