package org.kxml2.wap.wml;

import android.net.http.Headers;
import com.netspace.library.fragment.RESTLibraryFragment;
import org.apache.http.cookie.ClientCookie;
import org.kxml2.wap.WbxmlParser;
import org.kxml2.wap.WbxmlSerializer;

public abstract class Wml {
    public static final String[] ATTR_START_TABLE = new String[]{"accept-charset", "align=bottom", "align=center", "align=left", "align=middle", "align=right", "align=top", "alt", "content", null, ClientCookie.DOMAIN_ATTR, "emptyok=false", "emptyok=true", "format", "height", "hspace", "ivalue", "iname", null, "label", "localsrc", "maxlength", "method=get", "method=post", "mode=nowrap", "mode=wrap", "multiple=false", "multiple=true", RESTLibraryFragment.ARGUMENT_NAME_SUFFIX, "newcontext=false", "newcontext=true", "onpick", "onenterbackward", "onenterforward", "ontimer", "optimal=false", "optimal=true", "path", null, null, null, "scheme", "sendreferer=false", "sendreferer=true", "size", "src", "ordered=true", "ordered=false", "tabindex", "title", "type", "type=accept", "type=delete", "type=help", "type=password", "type=onpick", "type=onenterbackward", "type=onenterforward", "type=ontimer", null, null, null, null, null, "type=options", "type=prev", "type=reset", "type=text", "type=vnd.", "href", "href=http://", "href=https://", "value", "vspace", "width", "xml:lang", null, "align", "columns", "class", "id", "forua=false", "forua=true", "src=http://", "src=https://", "http-equiv", "http-equiv=Content-Type", "content=application/vnd.wap.wmlc;charset=", "http-equiv=Expires", null, null};
    public static final String[] ATTR_VALUE_TABLE = new String[]{".com/", ".edu/", ".net/", ".org/", "accept", "bottom", "clear", "delete", "help", "http://", "http://www.", "https://", "https://www.", null, "middle", "nowrap", "onpick", "onenterbackward", "onenterforward", "ontimer", "options", "password", "reset", null, TestHandler.TEXT, "top", "unknown", "wrap", "www."};
    public static final String[] TAG_TABLE = new String[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "a", "td", "tr", "table", "p", "postfield", "anchor", "access", "b", "big", "br", "card", "do", "em", "fieldset", "go", "head", "i", "img", "input", "meta", "noop", "prev", "onevent", "optgroup", "option", Headers.REFRESH, "select", "small", "strong", null, "template", "timer", "u", "setvar", "wml"};

    public static WbxmlParser createParser() {
        WbxmlParser p = new WbxmlParser();
        p.setTagTable(0, TAG_TABLE);
        p.setAttrStartTable(0, ATTR_START_TABLE);
        p.setAttrValueTable(0, ATTR_VALUE_TABLE);
        return p;
    }

    public static WbxmlSerializer createSerializer() {
        WbxmlSerializer s = new WbxmlSerializer();
        s.setTagTable(0, TAG_TABLE);
        s.setAttrStartTable(0, ATTR_START_TABLE);
        s.setAttrValueTable(0, ATTR_VALUE_TABLE);
        return s;
    }
}
