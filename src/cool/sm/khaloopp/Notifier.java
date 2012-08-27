package cool.sm.khaloopp;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import cool.sm.khaloopp.EventDispatcher.Event;
import cool.sm.khaloopp.data.entity.KhalooppoEntity;
import cool.sm.khaloopp.data.entity.NotifierTarget;

public class Notifier extends Thread implements EventDispatcher.Subscriber {

    protected static final Logger LOG = Logger.getLogger(Notifier.class.getName());

    protected EventDispatcher eventManager;
    protected Object semaphore;

    protected Collection<KhalooppoEntity> data;

    public Notifier(EventDispatcher em){
        this.eventManager = em;
    }

    protected void spam(){
        Session session = Controller.getSessionFactory().getCurrentSession();
        Transaction ts = session.beginTransaction();

        try{
            for(KhalooppoEntity khalooppa : this.data){
                System.out.printf(">%d: %s, Hotelka: %s, ID: %s\n", khalooppa.getNumber(), khalooppa.getTitle(), khalooppa.getYaHachuuu(), khalooppa.getLink());

                khalooppa.setState(NotifierTarget.State.Active);
                session.update(khalooppa);
            }
            ts.commit();
        } catch(Exception ex){
            ts.rollback();
            LOG.log(Level.SEVERE, "Spamming failed", ex);
        }
        this.data = null;
    }

    @Override
    public void run() {
        LOG.info("Notifier thread started...");
        this.semaphore = new Object();
        try {
            synchronized(this.semaphore){
                for(;;){
                    this.semaphore.wait();
                    this.spam();
                }
            }
        } catch (InterruptedException ex) {
            // fuck it
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processEvent(Event event) {
        this.data = (Collection<KhalooppoEntity>)event.getParams()[0];
        synchronized(this.semaphore){
            this.semaphore.notifyAll();
        }
    }
}
