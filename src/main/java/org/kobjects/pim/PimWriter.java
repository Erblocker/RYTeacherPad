package org.kobjects.pim;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

public class PimWriter {
    Writer writer;

    public PimWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeEntry(PimItem item) throws IOException {
        this.writer.write("begin:");
        this.writer.write(item.getType());
        this.writer.write("\r\n");
        Enumeration e = item.fieldNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            for (int i = 0; i < item.getFieldCount(name); i++) {
                PimField field = item.getField(name, i);
                this.writer.write(name);
                this.writer.write(58);
                this.writer.write(field.getValue().toString());
                this.writer.write("\r\n");
            }
        }
        this.writer.write("end:");
        this.writer.write(item.getType());
        this.writer.write("\r\n\r\n");
    }
}
