package com.netspace.library.parser;

import android.content.Context;
import com.netspace.library.utilities.Utilities;
import io.vov.vitamio.MediaMetadataRetriever;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LessonsPrepareResourceParser extends ResourceBase {
    private String mAuthor;
    private String mGUID;
    private int mSubjectID;
    private String mSubjectName;
    private String mTitle;
    private ArrayList<LessonPrepareResourceItem> marrItems;

    public static class LessonPrepareResourceItem {
        public int nType = 0;
        public int nUsageType = 0;
        public String szGUID = "";
        public String szResourceType = "";
        public String szTitle = "";
    }

    public LessonsPrepareResourceParser() {
        this.marrItems = new ArrayList();
        this.mObjectName = "LessonsPrepare";
    }

    public boolean initialize(Context Context, String szXMLContent) {
        if (!super.initialize(Context, szXMLContent)) {
            return false;
        }
        NodeList NodeList = this.mRootDocument.getElementsByTagName("Resource");
        Element LessonsPrepareElement = getXMLNode("LessonsPrepare");
        if (LessonsPrepareElement != null) {
            this.mGUID = LessonsPrepareElement.getAttribute("guid");
            this.mTitle = LessonsPrepareElement.getAttribute("title");
            this.mAuthor = LessonsPrepareElement.getAttribute(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            this.mSubjectName = LessonsPrepareElement.getAttribute("subject");
            this.mSubjectID = Utilities.getSubjectID(this.mSubjectName);
            if (this.mSubjectID == -1) {
                this.mSubjectID = Integer.valueOf(LessonsPrepareElement.getAttribute("numberSubject")).intValue();
            }
        }
        this.marrItems.clear();
        for (int i = 0; i < NodeList.getLength(); i++) {
            Node OneResource = NodeList.item(i);
            LessonPrepareResourceItem OneData = new LessonPrepareResourceItem();
            OneData.szGUID = OneResource.getAttributes().getNamedItem("guid").getTextContent();
            OneData.szTitle = OneResource.getAttributes().getNamedItem("title").getTextContent();
            OneData.szResourceType = OneResource.getAttributes().getNamedItem("resourceType").getTextContent();
            if (OneResource.getAttributes().getNamedItem("usageType") != null) {
                OneData.nUsageType = Integer.valueOf(OneResource.getAttributes().getNamedItem("usageType").getTextContent()).intValue();
            }
            if (OneResource.getAttributes().getNamedItem("type") != null) {
                OneData.nType = Integer.valueOf(OneResource.getAttributes().getNamedItem("type").getTextContent()).intValue();
            }
            this.marrItems.add(OneData);
        }
        return true;
    }

    public LessonPrepareResourceItem getItem(int nItem) {
        return (LessonPrepareResourceItem) this.marrItems.get(nItem);
    }

    public int getCount() {
        return this.marrItems.size();
    }

    public void clear() {
        this.marrItems.clear();
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getSubjectName() {
        return this.mSubjectName;
    }

    public int getSubjectID() {
        return this.mSubjectID;
    }
}
