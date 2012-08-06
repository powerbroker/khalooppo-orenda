package cool.sm.khaloopp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

/**
 * This example demonstrates the use of the {@link ResponseHandler} to simplify
 * the process of processing the HTTP response and releasing associated resources.
 */
public class ClientWithResponseHandler {

    //http://www.avito.ru/catalog/kvartiry-24/sankt-peterburg-653240/params.201_1060.504_5256?user=1&view=list
    //http://www.avito.ru/catalog/kvartiry-24/sankt-peterburg-653240/params.201_1060.504_5256.550_5703.567_5830?metro_id=187&view=list
    public static final String TARGET_URL = "http://www.avito.ru/catalog/kvartiry-24/sankt-peterburg-653240/params.201_1060.504_5256?user=1&view=list";

    public final static void main(String[] args) throws Exception {

        HttpClient httpclient = new DefaultHttpClient();

        try {

            HttpGet httpget = new HttpGet(TARGET_URL);

            System.out.println("executing request " + httpget.getURI());

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            
            Page page = new Page(entity.getContent(), "utf-8");

            Parser parser = new Parser(new Lexer(page));

            TagNameFilter tnf = new TagNameFilter();
            tnf.setName ("DIV");

            HasAttributeFilter attf = new HasAttributeFilter();
            attf.setAttributeName ("class");
            attf.setAttributeValue ("t_i_i");

            AndFilter filter = new AndFilter();
            filter.setPredicates(new NodeFilter[]{
                tnf,
                attf
            });

            NodeList data = parser.extractAllNodesThatMatch(filter);

            //System.out.println(data.toHtml());
            DataVisitor visitor = new DataVisitor();
            for (int i = 0; i < data.size(); i++) {
                visitor.visitTag((Tag)data.elementAt(i));
            }

            for(DataEntity de : visitor.getData()){
                System.out.printf("%s, Hotelka: %s, ID: %s\n", de.title, de.yaHachuuu, de.idref);
            }

            NodeClassFilter pgnf = new NodeClassFilter();
            pgnf.setMatchClass(LinkTag.class);

            HasAttributeFilter pgatt1f = new HasAttributeFilter();
            pgatt1f.setAttributeName ("class");
            pgatt1f.setAttributeValue ("page");

            HasAttributeFilter pgatt2f = new HasAttributeFilter();
            pgatt2f.setAttributeName ("rel");
            pgatt2f.setAttributeValue ("last");

            AndFilter pgnfilter = new AndFilter();
            pgnfilter.setPredicates(new NodeFilter[]{
                pgnf,
                pgatt1f,
                pgatt2f
            });

            NodeList pgdata = parser.extractAllNodesThatMatch(pgatt2f);
            if(pgdata.size() > 0){
                LinkTag pgntag = (LinkTag)pgdata.elementAt(0);
                System.out.println(pgntag.getLinkText());
                System.out.println(pgntag.getLink());
            } else {
                System.out.println("List on single page");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }

    protected static class DataVisitor extends NodeVisitor {

        protected List<DataEntity> data = new ArrayList<DataEntity>();

        @Override
        public void visitTag(Tag tag) {
            DataEntity entity = new DataEntity();
            data.add(entity);
            entity.populate(tag);
        }

        public List<DataEntity> getData() {
            return this.data;
        }
    }

    protected static class DataEntity {
        protected String title;
        protected String idref;
        protected String yaHachuuu;

        protected static final NodeFilter DateFilter = new AndFilter(new NodeFilter[]{
            new NodeClassFilter(Div.class),
            new HasAttributeFilter("class", "t_i_date")
        });

        protected static final NodeFilter TimeFilter = new AndFilter(new NodeFilter[]{
            new NodeClassFilter(Span.class),
            new HasAttributeFilter("class", "t_i_time")
        });

        protected static final NodeFilter KhalooppoHotelkaFilter = new AndFilter(new NodeFilter[]{
            new NodeClassFilter(Div.class),
            new HasAttributeFilter("class", "l_i_price")
        });

        protected static final NodeFilter KhalooppoHotelkaValueFilter = new NodeClassFilter(Span.class);

        protected static final NodeFilter LinkFilter = new AndFilter(new NodeFilter[]{
            new NodeClassFilter(LinkTag.class),
            new HasParentFilter(new HasAttributeFilter("class", "t_i_title"), true)
        });

        public void populate(Tag tag){

            NodeList nl = new NodeList();

            tag.collectInto(nl, LinkFilter);
            if(nl.size() > 0){
                LinkTag link = (LinkTag)nl.elementAt(0);
                this.title = link.getLinkText();
                this.idref = link.getLink();
                nl.removeAll();
            }

            tag.collectInto(nl, KhalooppoHotelkaFilter);
            if(nl.size() > 0){
                Span s = (Span)nl.extractAllNodesThatMatch(KhalooppoHotelkaValueFilter, true).elementAt(0);
                if(s != null){
                    this.yaHachuuu = s.getStringText().replace("&nbsp;", "");
                }
            }
        }
    }
}