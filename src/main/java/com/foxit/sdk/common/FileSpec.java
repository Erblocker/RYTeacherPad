package com.foxit.sdk.common;

import com.foxit.sdk.pdf.PDFDoc;
import java.io.File;

public class FileSpec {
    private transient long a;
    protected transient boolean swigCMemOwn;

    class a extends FileRead {
        long a;
        final /* synthetic */ FileSpec b;

        private a(FileSpec fileSpec, long j) {
            this.b = fileSpec;
            this.a = 0;
            this.a = j;
        }

        public long getFileSize() {
            long j = 0;
            try {
                if (this.b.a == j) {
                    throw new PDFException(4);
                }
                j = CommonJNI.FileSpec_getFileSize(this.b.a, this.b);
                return j;
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }

        public byte[] read(long j, long j2) {
            try {
                if (this.b.a != 0) {
                    return CommonJNI.FileSpec_getFileData(this.b.a, this.b, this.a, j, j2);
                }
                throw new PDFException(4);
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void release() {
            this.a = 0;
        }
    }

    protected FileSpec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(FileSpec fileSpec) {
        return fileSpec == null ? 0 : fileSpec.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                CommonJNI.delete_FileSpec(this.a);
            }
            this.a = 0;
        }
    }

    public static FileSpec create(PDFDoc pDFDoc) throws PDFException {
        if (pDFDoc == null) {
            throw new PDFException(8);
        }
        long longValue = ((Long) a.a(PDFDoc.class, "getCPtr", pDFDoc)).longValue();
        if (longValue == 0) {
            throw new PDFException(8);
        }
        long FileSpec_create = CommonJNI.FileSpec_create(longValue, pDFDoc);
        return FileSpec_create == 0 ? null : new FileSpec(FileSpec_create, false);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        delete();
    }

    public String getFileName() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_getFileName(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setFileName(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() < 1) {
            throw new PDFException(8);
        } else {
            CommonJNI.FileSpec_setFileName(this.a, this, str);
        }
    }

    public long getFileSize() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_getFileSize(this.a, this);
        }
        throw new PDFException(4);
    }

    public FileRead getFileData() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long FileSpec_getFileRead = CommonJNI.FileSpec_getFileRead(this.a, this);
        return FileSpec_getFileRead != 0 ? new a(FileSpec_getFileRead) : null;
    }

    public boolean embed(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() < 1) {
            throw new PDFException(8);
        } else if (new File(str).exists()) {
            return CommonJNI.FileSpec_embed(this.a, this, str);
        } else {
            throw new PDFException(1);
        }
    }

    public boolean isEmbedded() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_isEmbedded(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getDescription() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_getDescription(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setDescription(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() < 1) {
            throw new PDFException(8);
        } else {
            CommonJNI.FileSpec_setDescription(this.a, this, str);
        }
    }

    public DateTime getCreationDateTime() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_getCreationDateTime(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setCreationDateTime(DateTime dateTime) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (dateTime == null) {
            throw new PDFException(8);
        } else {
            CommonJNI.FileSpec_setCreationDateTime(this.a, this, dateTime);
        }
    }

    public DateTime getModifiedDateTime() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_getModifiedDateTime(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setModifiedDateTime(DateTime dateTime) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (dateTime == null) {
            throw new PDFException(8);
        } else {
            CommonJNI.FileSpec_setModifiedDateTime(this.a, this, dateTime);
        }
    }

    public byte[] getChecksum() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.FileSpec_getChecksum(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setChecksum(byte[] bArr) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (bArr == null || bArr.length < 1) {
            throw new PDFException(8);
        } else {
            CommonJNI.FileSpec_setChecksum(this.a, this, bArr);
        }
    }
}
