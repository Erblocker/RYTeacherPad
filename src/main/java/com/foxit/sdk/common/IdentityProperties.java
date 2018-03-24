package com.foxit.sdk.common;

public class IdentityProperties {
    private String a = null;
    private String b = null;
    private String c = null;
    private String d = null;

    public void set(String str, String str2, String str3, String str4) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
    }

    public void setCorporation(String str) {
        this.a = str;
    }

    public String getCorporation() {
        return this.a;
    }

    public void setEmail(String str) {
        this.b = str;
    }

    public String getEmail() {
        return this.b;
    }

    public void setLoginName(String str) {
        this.c = str;
    }

    public String getLoginName() {
        return this.c;
    }

    public void setName(String str) {
        this.d = str;
    }

    public String getName() {
        return this.d;
    }
}
