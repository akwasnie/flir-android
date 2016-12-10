package com.example.ala.eventaggregator;

/**
 * Created by ala on 14/07/16.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ala
 */
public class EventAggregator {

    private final Map<Event, List<EventListener>> listeners
            = Collections.synchronizedMap(new EnumMap<Event, List<EventListener>>(Event.class));

    public void addEventListener(Event event, EventListener eventListener) {
        initForEvent(event);

        listeners.get(event).add(eventListener);
    }

    public void removeEventListener(Event event, EventListener eventListener) {
        initForEvent(event);

        listeners.get(event).remove(eventListener);
    }

    public void triggerEvent(Event event, Object parameter, Object parameter2) {
        initForEvent(event);

        for (EventListener listener : listeners.get(event)) {
            listener.onEventOccurred(event, parameter, parameter2);
        }
    }

    private synchronized void initForEvent(Event event) {
        if (!listeners.containsKey(event)) {
            listeners.put(event, Collections.synchronizedList(new ArrayList<EventListener>()));
        }
    }

}

