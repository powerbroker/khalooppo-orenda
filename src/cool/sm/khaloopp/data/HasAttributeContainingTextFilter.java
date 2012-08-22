package cool.sm.khaloopp.data;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;

public class HasAttributeContainingTextFilter extends HasAttributeFilter {

    private static final long serialVersionUID = 1L;

    public HasAttributeContainingTextFilter(String attrName, String attrValueSubstring){
        super(attrName, attrValueSubstring);
    }

    @Override
    public boolean accept(Node node) {
        Tag tag;
        Attribute attribute;
        boolean ret;

        ret = false;
        if (node instanceof Tag)
        {
            tag = (Tag)node;
            attribute = tag.getAttributeEx (mAttribute);
            ret = null != attribute;
            String tmp;
            if (ret && (null != mValue) && (tmp = attribute.getValue()) != null)
                ret = tmp.contains(mValue);
        }

        return (ret);
    }
}
