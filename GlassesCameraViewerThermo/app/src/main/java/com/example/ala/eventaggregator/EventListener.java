package com.example.ala.eventaggregator;

/**
 * Created by ala on 14/07/16.
 */
public interface EventListener {

    void onEventOccurred(Event event, Object parameter, Object parameter2);
}
