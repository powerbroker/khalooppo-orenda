package cool.sm.khaloopp.data;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

import cool.sm.khaloopp.data.entity.KhalooppoEntity;

public class DataListVisitor extends NodeVisitor {

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

    protected List<KhalooppoEntity> data = new ArrayList<KhalooppoEntity>();

    @Override
    public void visitTag(Tag tag) {
        KhalooppoEntity entity = new KhalooppoEntity();

        NodeList nl = new NodeList();

        tag.collectInto(nl, LinkFilter);
        if(nl.size() > 0){
            LinkTag link = (LinkTag)nl.elementAt(0);
            entity.setTitle(link.getLinkText().trim());
            entity.setIdref(link.getLink());
            nl.removeAll();
        }

        tag.collectInto(nl, KhalooppoHotelkaFilter);
        if(nl.size() > 0){
            Span s = (Span)nl.extractAllNodesThatMatch(KhalooppoHotelkaValueFilter, true).elementAt(0);
            if(s != null){
                entity.setYaHachuuu(s.getStringText().replace("&nbsp;", ""));
            }
        }

        data.add(entity);
    }

    public List<KhalooppoEntity> getPageData() {
        return this.data;
    }
}