package io.vov.vitamio;

public interface MediaScannerClient {
    void addNoMediaFolder(String str);

    void handleStringTag(String str, byte[] bArr, String str2);

    void scanFile(String str, long j, long j2);

    void setMimeType(String str);
}
