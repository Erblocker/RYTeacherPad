package org.kobjects.pim;

public class VCard extends PimItem {
    public VCard(VCard orig) {
        super(orig);
    }

    public String getType() {
        return "vcard";
    }

    public int getArraySize(String name) {
        if (name.equals("n")) {
            return 5;
        }
        if (name.equals("adr")) {
            return 6;
        }
        return -1;
    }
}
