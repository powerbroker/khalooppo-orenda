package cool.sm.khaloopp;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;

import cool.sm.khaloopp.EventDispatcher.Event;
import cool.sm.khaloopp.EventDispatcher.Subscriber;
import cool.sm.khaloopp.data.entity.KhalooppoEntity;

public class DataAnalyzer extends Thread implements Subscriber {

    public static final Object EVT_DATA_FILTERED = DataAnalyzer.class;

    protected static final Logger LOG = Logger.getLogger(DataAnalyzer.class.getName());

    protected EventDispatcher eventManager;
    protected Object semaphore;

    protected List<KhalooppoEntity> data;

    public DataAnalyzer(EventDispatcher em){
        this.eventManager = em;
    }

    @SuppressWarnings("unchecked")
    protected void analyze(){
        Session session = Controller.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try {
            List<Long> existent = session.createCriteria(KhalooppoEntity.class)
                    .setProjection(Projections.property("number"))
                    .list();

            long number;
            Set<KhalooppoEntity> fresh = new HashSet<KhalooppoEntity>();
            for(KhalooppoEntity khalooppa : this.data){
                number = khalooppa.getNumber();
                if(!existent.contains(number))
                fresh.add(khalooppa);
            }

            LOG.log(Level.INFO, "{0} fresh records discovered", fresh.size());

            for(KhalooppoEntity khalooppa : fresh){
                session.save(khalooppa);
            }

            this.data = Collections.EMPTY_LIST;
            transaction.commit();

            this.eventManager.fireEvent(this, EVT_DATA_FILTERED, fresh);
        } catch(Exception ex){
            transaction.rollback();
            LOG.log(Level.SEVERE, "Data analysis failed", ex);
        }
    }

    @Override
    public void run() {
        LOG.info("Analyzer thread started...");
        this.semaphore = new Object();
        try {
            synchronized(this.semaphore){
                for(;;){
                    this.semaphore.wait();
                    this.analyze();
                }
            }
        } catch (InterruptedException ex) {
            // fuck it
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processEvent(Event event) {
        this.data = (List<KhalooppoEntity>)event.getParams()[0];
        synchronized(this.semaphore){
            this.semaphore.notifyAll();
        }
    }
}