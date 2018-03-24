package com.foxit.uiextensions.annots.stamp;

import com.foxit.sdk.pdf.annots.Stamp;

public class StampUntil {
    public static int getStampTypeByName(String name) {
        if (name == null) {
            return -1;
        }
        if (name.equals(Stamp.STANDARDICONNAME_APPROVED)) {
            return 0;
        }
        if (name.equals("Completed")) {
            return 1;
        }
        if (name.equals(Stamp.STANDARDICONNAME_CODFIDENTIAL)) {
            return 2;
        }
        if (name.equals(Stamp.STANDARDICONNAME_DRAFT)) {
            return 3;
        }
        if (name.equals("Emergency")) {
            return 4;
        }
        if (name.equals(Stamp.STANDARDICONNAME_EXPIRED)) {
            return 5;
        }
        if (name.equals(Stamp.STANDARDICONNAME_FINAL)) {
            return 6;
        }
        if (name.equals("Received")) {
            return 7;
        }
        if (name.equals("Reviewed")) {
            return 8;
        }
        if (name.equals("Revised")) {
            return 9;
        }
        if (name.equals("Verified")) {
            return 10;
        }
        if (name.equals("Void")) {
            return 11;
        }
        if (name.equals("Accepted")) {
            return 12;
        }
        if (name.equals("Initial")) {
            return 13;
        }
        if (name.equals("Rejected")) {
            return 14;
        }
        if (name.equals("Sign Here")) {
            return 15;
        }
        if (name.equals("Witness")) {
            return 16;
        }
        if (name.equals("DynaApproved")) {
            return 17;
        }
        if (name.equals("DynaConfidential")) {
            return 18;
        }
        if (name.equals("DynaReceived")) {
            return 19;
        }
        if (name.equals("DynaReviewed")) {
            return 20;
        }
        if (name.equals("DynaRevised")) {
            return 21;
        }
        return -1;
    }
}
