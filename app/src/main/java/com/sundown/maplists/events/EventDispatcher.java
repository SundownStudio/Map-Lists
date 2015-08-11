package com.sundown.maplists.events;


import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Sundown on 5/20/2015.
 *
 * Most of the code for this package is opensource I grabbed it from: https://github.com/RonBendele/MVC
 */
public class EventDispatcher extends Dispatcher {

    private HashMap<String, CopyOnWriteArrayList<EventListener>> listenerMap;
    private Dispatcher target;


    public EventDispatcher() {
        this(null);
    }

    public EventDispatcher(Dispatcher target) {
        listenerMap = new HashMap<String, CopyOnWriteArrayList<EventListener>>();
        this.target = (target != null) ? target : this;
    }


    @Override
    void addListener(String type, EventListener listener) {
        synchronized (listenerMap) {
            CopyOnWriteArrayList<EventListener> list = listenerMap.get(type);
            if (list == null) {
                list = new CopyOnWriteArrayList<EventListener>();
                listenerMap.put(type, list);
            }
            list.add(listener);
        }
    }

    @Override
    void removeListener(String type, EventListener listener) {
        synchronized (listenerMap) {
            CopyOnWriteArrayList<EventListener> list = listenerMap.get(type);
            if (list == null) return;
            list.remove(listener);
            if (list.size() == 0) {
                listenerMap.remove(type);
            }
        }
    }

    @Override
    boolean hasListener(String type, EventListener listener) {
        synchronized (listenerMap) {
            CopyOnWriteArrayList<EventListener> list = listenerMap.get(type);
            if (list == null) return false;
            return list.contains(listener);
        }
    }

    @Override
    void dispatchEvent(Event event) {
        if (event != null) {
            String type = event.getType();
            event.setSource(target);
            CopyOnWriteArrayList<EventListener> list;
            synchronized (listenerMap) {
                list = listenerMap.get(type);
            }

            if (list == null) return;
            for (EventListener l : list) {
                l.onEvent(event);
            }
        }
    }
}
