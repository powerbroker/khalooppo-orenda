package cool.sm.khaloopp;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

    protected String formatMessage(){
        StringBuilder sb = new StringBuilder();

        for(KhalooppoEntity khalooppa : this.data){
            sb.append(String.format("%d: %s, Hotelka: %s, url: www.avito.ru/%s\n", khalooppa.getNumber(), khalooppa.getTitle(), khalooppa.getYaHachuuu(), khalooppa.getLink()));
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected void spam(){
        Session session = Controller.getSessionFactory().getCurrentSession();
        Transaction ts = session.beginTransaction();

        try{
            javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(System.getProperties(), null);

            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress("dev-box@yandex.ru"));
            // "spamcollector@yandex.ru"
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("shelkovkinsn@gmail.com", false));
            msg.setSubject(String.format("Avito-Khalooppa: %d new record(s) discovered", this.data.size()));
            msg.setText(this.formatMessage());
            msg.setHeader("X-Mailer", "AvitoSpammer");
            msg.setSentDate(new Date());


            Transport transport = mailSession.getTransport("smtp");
            transport.connect("smtp.yandex.ru", 25, "dev-box", "password");
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();

            LOG.info(String.format("Pee runs though pipes!"));
            for(Address addr : msg.getAllRecipients()){
                LOG.fine(addr.toString());
            }

            for(KhalooppoEntity khalooppa : this.data){
                khalooppa.setState(NotifierTarget.State.Active);
                session.update(khalooppa);
            }

            ts.commit();

            this.data = Collections.EMPTY_LIST;
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
                    if(this.data != null && this.data.size() > 0){
                        this.spam();
                    }
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
