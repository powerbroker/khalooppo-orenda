package cool.sm.khaloopp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.w3c.dom.NodeList;

/**
 * This example demonstrates the use of the {@link ResponseHandler} to simplify
 * the process of processing the HTTP response and releasing associated resources.
 */
public class ClientWithResponseHandler {

    public static final String TARGET_URL = "http://www.avito.ru/catalog/kvartiry-24/sankt-peterburg-653240/params.201_1060.504_5256.550_5703.567_5830?metro_id=187&view=list";

    public final static void main(String[] args) throws Exception {
/*
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setExpandEntityReferences(false);
        
        DocumentBuilder builder = null;
*/
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpget = new HttpGet(TARGET_URL);

            System.out.println("executing request " + httpget.getURI());

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            
            Page page = new Page(entity.getContent(), "utf-8");

            Lexer lexer = new Lexer(page);
            Parser parser = new Parser(lexer);

//            for (org.htmlparser.Node n = null;(n = lexer.nextNode()) != null;){
//                System.out.println(n.getText());
//            }

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

            org.htmlparser.util.NodeList data = parser.extractAllNodesThatMatch(filter);

            System.out.println(data.toHtml());

            //FilterBean bean = new FilterBean();
            //bean.setFilters (array1);

            //bean.setURL(TARGET_URL);
            //System.out.println (bean.getNodes().toHtml());

/*
            builder = dbf.newDocumentBuilder();
            Document document = builder.parse(entity.getContent());

            NodeList nodes = document.getElementsByTagName("div");
            Node n;
            for(int i = 0; i < nodes.getLength(); i++){
                n = nodes.item(i);
                
                if (n.hasAttributes() && n.getAttributes().getNamedItem("class").getNodeValue().contains("l_i")) {
                    System.out.println(n.getNodeValue());
                }
            }
*/
            // Create a response handler
/*
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
*/
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
