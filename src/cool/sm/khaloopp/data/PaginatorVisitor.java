package cool.sm.khaloopp.data;

import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.visitors.NodeVisitor;

public class PaginatorVisitor extends NodeVisitor {

    public static final NodeFilter DataBlockFilter = new AndFilter(new NodeFilter[]{
        new NodeClassFilter(ParagraphTag.class),
        new HasAttributeContainingTextFilter("class", "p_l_p")
    });

    public static final AndFilter PaginatorFilter = new AndFilter(new NodeFilter[]{
        new NodeClassFilter(LinkTag.class),
        new HasAttributeContainingTextFilter("class", "p_l_n")
    });

    protected int pages = 1;

    @Override
    public void visitTag(Tag tag) {
        LinkTag pgntag = (LinkTag)tag;
        try{
            int tmp = Integer.decode(pgntag.getLinkText());
            if(this.pages < tmp){
                this.pages = tmp;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getPages() {
        return this.pages;
    }
}