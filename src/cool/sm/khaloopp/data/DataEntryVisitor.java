package cool.sm.khaloopp.data;

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.visitors.NodeVisitor;

public class DataEntryVisitor extends NodeVisitor {

    public static final NodeFilter DataBlockFilter = new AndFilter(new NodeFilter[]{
        new NodeClassFilter(Div.class),
        new HasAttributeContainingTextFilter("class", "g_92")
    });

    public static final NodeFilter KhaloopperFilter = new HasAttributeFilter("id", "seller");

    public static final NodeFilter LocationFilter = new AndFilter(new NodeFilter[]{
        new TagNameFilter("DD"),
        new HasAttributeContainingTextFilter("class", "b_d_c")
    });

    public static final NodeFilter DescriptionFilter = new HasAttributeFilter("id", "desc_text");

    public static final NodeFilter IdentifierFilter = new HasAttributeFilter("id", "item_id");
}
