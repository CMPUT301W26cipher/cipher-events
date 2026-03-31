package com.example.cipher_events.user;

import com.example.cipher_events.database.Event;

import java.util.ArrayList;
import java.util.List;

/*
US 01.01.05
 */
public class EventSearchService {

    public List<Event> searchEvents(List<Event> allEvents, String keyword) {

        List<Event> result = new ArrayList<>();

        if (allEvents == null || keyword == null) {
            return result;
        }

        String lower = keyword.toLowerCase();

        for (Event event : allEvents) {

            if (event == null) continue;

            // private event
            if (!event.isPublicEvent()) continue;

            if ((event.getName() != null && event.getName().toLowerCase().contains(lower)) ||
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(lower))) {

                result.add(event);
            }
        }

        return result;
    }
}
