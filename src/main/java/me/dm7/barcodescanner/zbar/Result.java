package me.dm7.barcodescanner.zbar;

public class Result {
    private BarcodeFormat mBarcodeFormat;
    private String mContents;

    public void setContents(String contents) {
        this.mContents = contents;
    }

    public void setBarcodeFormat(BarcodeFormat format) {
        this.mBarcodeFormat = format;
    }

    public BarcodeFormat getBarcodeFormat() {
        return this.mBarcodeFormat;
    }

    public String getContents() {
        return this.mContents;
    }
}
