package org.kobjects.rss;

import io.vov.vitamio.MediaMetadataRetriever;
import io.vov.vitamio.provider.MediaStore.Video.VideoColumns;
import java.io.IOException;
import java.io.Reader;
import org.kobjects.xml.XmlReader;

public class RssReader {
    public static final int AUTHOR = 4;
    public static final int DATE = 3;
    public static final int DESCRIPTION = 2;
    public static final int LINK = 1;
    public static final int TITLE = 0;
    XmlReader xr;

    public RssReader(Reader reader) throws IOException {
        this.xr = new XmlReader(reader);
    }

    void readText(StringBuffer buf) throws IOException {
        while (this.xr.next() != 3) {
            switch (this.xr.getType()) {
                case 2:
                    readText(buf);
                    break;
                case 4:
                    buf.append(this.xr.getText());
                    break;
                default:
                    break;
            }
        }
    }

    public String[] next() throws IOException {
        String[] item = new String[5];
        while (this.xr.next() != 1) {
            if (this.xr.getType() == 2) {
                String n = this.xr.getName().toLowerCase();
                if (n.equals("item") || n.endsWith(":item")) {
                    while (this.xr.next() != 3) {
                        if (this.xr.getType() == 2) {
                            String name = this.xr.getName().toLowerCase();
                            int cut = name.indexOf(":");
                            if (cut != -1) {
                                name = name.substring(cut + 1);
                            }
                            StringBuffer buf = new StringBuffer();
                            readText(buf);
                            String text = buf.toString();
                            if (name.equals("title")) {
                                item[0] = text;
                            } else if (name.equals("link")) {
                                item[1] = text;
                            } else if (name.equals(VideoColumns.DESCRIPTION)) {
                                item[2] = text;
                            } else if (name.equals(MediaMetadataRetriever.METADATA_KEY_DATE)) {
                                item[3] = text;
                            } else if (name.equals(MediaMetadataRetriever.METADATA_KEY_AUTHOR)) {
                                item[4] = text;
                            }
                        }
                    }
                    return item;
                }
            }
        }
        return null;
    }
}
