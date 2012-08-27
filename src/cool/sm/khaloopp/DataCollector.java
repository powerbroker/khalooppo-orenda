package cool.sm.khaloopp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import cool.sm.khaloopp.data.DataListVisitor;
import cool.sm.khaloopp.data.PaginatorVisitor;
import cool.sm.khaloopp.data.entity.KhalooppoEntity;

public class DataCollector extends Thread {
    public static final Object EVT_DATA_COLLECTED = DataCollector.class;

    protected static final Logger LOG = Logger.getLogger(DataCollector.class.getName());

    protected EventDispatcher eventManager;
    protected long period;
    protected boolean active;

    protected String url;
    protected HttpClient client;
    protected HttpGet request;
    protected Parser parser;
    protected Lexer lexer;

    protected List<KhalooppoEntity> data;
    protected int pages;

    public DataCollector(EventDispatcher dispatcher, long period) {
        this.eventManager = dispatcher;
        this.period = period;
    }

    protected void init(){
        this.client = new DefaultHttpClient();
        this.request = new HttpGet(this.url);
    }

    protected Parser constructParser(InputStream iStream) throws UnsupportedEncodingException {
        Page page = new Page(iStream, "utf-8");
        if(this.parser == null){
            this.lexer = new Lexer(page);
            this.parser = new Parser(this.lexer);
        } else {
            this.lexer.setPage(page);
            this.parser.reset();
        }
        return this.parser;
    }

    protected boolean collectData() throws ClientProtocolException, IOException, ParserException{

        if(this.client == null){
            this.init();
        }

        LOG.log(Level.INFO, "Getting {0}", this.url);

        HttpResponse response = this.client.execute(this.request);
        HttpEntity entity = response.getEntity();

        Parser parser = this.constructParser(entity.getContent());

        NodeFilter filter = new OrFilter(DataListVisitor.DataBlockFilter, PaginatorVisitor.DataBlockFilter);

        NodeList pg = parser.extractAllNodesThatMatch(filter);

        Node dataBlock = pg.extractAllNodesThatMatch(DataListVisitor.DataBlockFilter).elementAt(0);
        Node paginator = pg.extractAllNodesThatMatch(PaginatorVisitor.DataBlockFilter).elementAt(0);

        DataListVisitor dataVisitor = null;

        if(dataBlock != null){
            NodeList data = new NodeList();
            dataBlock.collectInto(data, DataListVisitor.DataEntryFilter);

            dataVisitor = new DataListVisitor();
            for(int i = 0; i < data.size(); i++) {
                dataVisitor.visitTag((Tag)data.elementAt(i));
            }

            this.data = dataVisitor.getPageData();
            LOG.log(Level.INFO, "Parsed {0} entries", this.data.size());
        } else {
            LOG.severe("General failure: No data found!");
        }

        PaginatorVisitor pv = new PaginatorVisitor();
        if(paginator != null){
            NodeList pgdata = new NodeList();
            paginator.collectInto(pgdata, PaginatorVisitor.PaginatorFilter);
            pgdata.visitAllNodesWith(pv);
        }

        parser.getLexer().getPage().close();

        this.pages = pv.getPages();
        LOG.log(Level.INFO, "List on {0} page(s)", this.pages);

        return dataBlock != null;
    }

    @Override
    public void run() {
        LOG.info("Collector thread started...");
        try {
            for (;;) {
                if(this.collectData()){
                    this.eventManager.fireEvent(this, EVT_DATA_COLLECTED, this.data);
                }
                Thread.sleep(this.period);
            }
        } catch (InterruptedException ex) {
            // fuck it
        } catch (ClientProtocolException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (ParserException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } finally {
            if(this.client != null){
                this.client.getConnectionManager().shutdown();
            }
        }
    }

    public void setDelay(long delay) {
        this.period = delay;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
