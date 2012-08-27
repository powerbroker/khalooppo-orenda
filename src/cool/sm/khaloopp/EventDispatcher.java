package cool.sm.khaloopp;

import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class EventDispatcher extends Thread {
    protected static final Logger LOG = Logger.getLogger(EventDispatcher.class.getName());

    protected Map<Object, Set<EventListener>> registry;
    protected BlockingQueue<Event> events;

    public synchronized boolean addListener(Object eventId, EventListener listener){
        return this.getListeners(eventId, true).add(listener);
    }

    public synchronized boolean removeListener(Object eventId, EventListener listener){
        Set<EventListener> listeners = this.getListeners(eventId, false);
        if(listeners != null){
            return listeners.remove(listener);
        }
        return false;
    }

    protected synchronized Set<EventListener> getListeners(Object eventId, boolean autoConstruct){
        if(autoConstruct && this.registry == null){
            this.registry = new HashMap<Object, Set<EventListener>>();
        }
        Set<EventListener> listeners = this.registry != null ? this.registry.get(eventId) : null;
        if(autoConstruct && listeners == null){
            listeners = new HashSet<EventListener>();
            this.registry.put(eventId, listeners);
        }
        return listeners;
    }

    public synchronized void fireEvent(Object source, Object eventId, Object ... params){
        if(this.events == null){
            this.events = new LinkedBlockingQueue<EventDispatcher.Event>();
            this.start();
        }
        this.events.add(new Event(source, eventId, params));
    }

    protected void dispathEvent(Event event){
        Set<EventListener> listeners = this.getListeners(event.id, false);
        if(listeners != null){
            for(EventListener l : listeners){
                if(l instanceof Subscriber){
                    ((Subscriber)l).processEvent(event);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    public void finalize(){
        this.interrupt();
    }

    public void run() {
        LOG.info("Event dispatcher started");
        Event event;
        try {
            for(;;){
                event = this.events.take();
                this.dispathEvent(event);
            }
        } catch (InterruptedException ex) {
            // fuck it
        }
    }

    public static interface Subscriber extends EventListener {
        public void processEvent(Event event);
    }

    public static class Event extends EventObject{
        private static final long serialVersionUID = 1L;

        public final Object params[];
        public final Object id;

        public Event(Object source, Object eventId, Object ... params) {
            super(source);
            this.id = eventId;
            this.params = params;
        }

        public Object getId(){
            return this.id;
        }

        public Object[] getParams(){
            return this.params;
        }
    }
}
