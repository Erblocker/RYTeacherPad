package org.kxml2.wap.syncml;

import com.foxit.sdk.pdf.annots.Stamp;
import io.vov.vitamio.MediaMetadataRetriever;
import org.ksoap2.serialization.MarshalHashtable;
import org.kxml2.wap.WbxmlParser;
import org.kxml2.wap.WbxmlSerializer;

public abstract class SyncML {
    public static final String[] TAG_TABLE_0 = new String[]{"Add", "Alert", "Archive", "Atomic", "Chal", "Cmd", "CmdID", "CmdRef", "Copy", "Cred", "Data", "Delete", "Exec", Stamp.STANDARDICONNAME_FINAL, "Get", "Item", "Lang", "LocName", "LocURI", MarshalHashtable.NAME, "MapItem", "Meta", "MsgID", "MsgRef", "NoResp", "NoResults", "Put", "Replace", "RespURI", "Results", "Search", "Sequence", "SessionID", "SftDel", "Source", "SourceRef", "Status", "Sync", "SyncBody", "SyncHdr", "SyncML", "Target", "TargetRef", "Reserved for future use", "VerDTD", "VerProto", "NumberOfChanged", "MoreData", "Field", "Filter", "Record", "FilterType", "SourceParent", "TargetParent", "Move", "Correlator"};
    public static final String[] TAG_TABLE_1 = new String[]{"Anchor", "EMI", "Format", "FreeID", "FreeMem", "Last", "Mark", "MaxMsgSize", "Mem", "MetInf", "Next", "NextNonce", "SharedMem", "Size", "Type", "Version", "MaxObjSize", "FieldLevel"};
    public static final String[] TAG_TABLE_2_DM = new String[]{"AccessType", "ACL", "Add", "b64", "bin", "bool", "chr", "CaseSense", "CIS", "Copy", "CS", MediaMetadataRetriever.METADATA_KEY_DATE, "DDFName", "DefaultValue", "Delete", "Description", "DDFFormat", "DFProperties", "DFTitle", "DFType", "Dynamic", "Exec", "float", "Format", "Get", "int", "Man", "MgmtTree", "MIME", "Mod", "Name", "Node", "node", "NodeName", "null", "Occurence", "One", "OneOrMore", "OneOrN", "Path", "Permanent", "Replace", "RTProperties", "Scope", "Size", "time", "Title", "TStamp", "Type", "Value", "VerDTD", "VerNo", "xml", "ZeroOrMore", "ZeroOrN", "ZeroOrOne"};

    public static WbxmlParser createParser() {
        WbxmlParser p = new WbxmlParser();
        p.setTagTable(0, TAG_TABLE_0);
        p.setTagTable(1, TAG_TABLE_1);
        return p;
    }

    public static WbxmlSerializer createSerializer() {
        WbxmlSerializer s = new WbxmlSerializer();
        s.setTagTable(0, TAG_TABLE_0);
        s.setTagTable(1, TAG_TABLE_1);
        return s;
    }

    public static WbxmlParser createDMParser() {
        WbxmlParser p = createParser();
        p.setTagTable(2, TAG_TABLE_2_DM);
        return p;
    }

    public static WbxmlSerializer createDMSerializer() {
        WbxmlSerializer s = createSerializer();
        s.setTagTable(2, TAG_TABLE_2_DM);
        return s;
    }
}
