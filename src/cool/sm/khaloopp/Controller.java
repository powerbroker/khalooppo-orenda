package cool.sm.khaloopp;

import java.io.IOException;
import java.util.logging.LogManager;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Controller{
    public static final String TARGET_URL = "http://www.avito.ru/catalog/kvartiry-24/sankt-peterburg-653240/params.201_1060.504_5256?user=1&view=list";

    private static SessionFactory sessionFactory = buildSessionFactory();

    protected EventDispatcher eventManager;
    protected DataCollector collector;
    protected DataAnalyzer analyzer;
    protected Notifier notifier;

    protected static Controller instance;

    public static void main(String[] args) throws SecurityException, IOException {

        LogManager.getLogManager().readConfiguration(Controller.class.getResourceAsStream("/logging.properties"));

        sessionFactory = buildSessionFactory();

        instance = new Controller();

        instance.eventManager = new EventDispatcher();

        instance.collector = new DataCollector(instance.eventManager, 10000);
        instance.collector.setUrl(TARGET_URL);

        instance.analyzer = new DataAnalyzer(instance.eventManager);
        instance.eventManager.addListener(DataCollector.EVT_DATA_COLLECTED, instance.analyzer);
        instance.analyzer.start();

        instance.notifier = new Notifier(instance.eventManager);
        instance.eventManager.addListener(DataAnalyzer.EVT_DATA_FILTERED, instance.notifier);
        instance.notifier.start();

        instance.collector.start();
    }

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}