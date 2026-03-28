package com.learn.redis.controller;

import com.learn.redis.bean.Event;
import com.learn.redis.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // First call hits the "DB" and caches the result.
    // Subsequent calls return from Redis (check console — no ">> DB hit" log).
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable String id) {
        Event event = eventService.getEvent(id);
        if (event == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(event);
    }

    // Creates or updates an event and refreshes the cache entry.
    @PostMapping
    public ResponseEntity<Event> saveEvent(@RequestBody Event event) {
        return ResponseEntity.ok(eventService.saveEvent(event));
    }

    // Deletes the event and evicts it from the cache.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // Evicts all entries from the "events" cache.
    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearCache() {
        eventService.clearAllEvents();
        return ResponseEntity.noContent().build();
    }
}
