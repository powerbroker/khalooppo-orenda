package cool.sm.khaloopp;

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
import org.htmlparser.filters.CssSelectorNodeFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import cool.sm.khaloopp.data.DataListVisitor;
import cool.sm.khaloopp.data.HasAttributeContainingTextFilter;
import cool.sm.khaloopp.data.PaginatorVisitor;
import cool.sm.khaloopp.data.entity.KhalooppoEntity;

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

            TagNameFilter tnf = new TagNameFilter("DIV");
            HasAttributeContainingTextFilter attf = new HasAttributeContainingTextFilter("class", "t_i_i");

            AndFilter filter = new AndFilter();
            filter.setPredicates(new NodeFilter[]{
                tnf,
                attf
            });

            NodeList data = parser.extractAllNodesThatMatch(filter);

            DataListVisitor visitor = new DataListVisitor();
            for (int i = 0; i < data.size(); i++) {
                visitor.visitTag((Tag)data.elementAt(i));
            }

            for(KhalooppoEntity de : visitor.getPageData()){
                System.out.printf("> %s, Hotelka: %s, ID: %s\n", de.getTitle(), de.getYaHachuuu(), de.getIdref());
            }
/*
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
*/
            parser.reset();
            NodeList pgdata = parser.extractAllNodesThatMatch(PaginatorVisitor.PaginatorFilter);
            PaginatorVisitor pv = new PaginatorVisitor();
            pgdata.visitAllNodesWith(pv);

            System.out.printf("List on %d page(s)", pv.getPages());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }
}