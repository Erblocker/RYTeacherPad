package com.foxit.uiextensions.annots.fileattachment;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.FileRead;
import com.foxit.sdk.common.FileSpec;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FileAttachment;
import com.foxit.uiextensions.utils.Event.Callback;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class FileAttachmentUtil {

    /* renamed from: com.foxit.uiextensions.annots.fileattachment.FileAttachmentUtil$1 */
    class AnonymousClass1 implements CallBack {
        private final /* synthetic */ Callback val$callback;

        AnonymousClass1(Callback callback) {
            this.val$callback = callback;
        }

        public void result(Task task) {
            if (this.val$callback != null) {
                this.val$callback.result(null, true);
            }
        }
    }

    /* renamed from: com.foxit.uiextensions.annots.fileattachment.FileAttachmentUtil$2 */
    class AnonymousClass2 extends Task {
        private final /* synthetic */ Annot val$annot;
        private final /* synthetic */ String val$newFile;

        AnonymousClass2(CallBack $anonymous0, Annot annot, String str) {
            this.val$annot = annot;
            this.val$newFile = str;
            super($anonymous0);
        }

        protected void execute() {
            try {
                FileSpec fileSpec = ((FileAttachment) this.val$annot).getFileSpec();
                if (fileSpec != null) {
                    FileRead fileRead = fileSpec.getFileData();
                    if (fileRead != null) {
                        FileOutputStream fileOutputStream = new FileOutputStream(this.val$newFile);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        int offset = 0;
                        long fileSize = fileRead.getFileSize();
                        while (true) {
                            byte[] buf;
                            if (fileSize < ((long) (4096 + offset))) {
                                buf = fileRead.read((long) offset, fileSize - ((long) offset));
                            } else {
                                buf = fileRead.read((long) offset, (long) 4096);
                            }
                            if (buf.length != 4096) {
                                bufferedOutputStream.write(buf, 0, buf.length);
                                bufferedOutputStream.flush();
                                bufferedOutputStream.close();
                                fileOutputStream.close();
                                return;
                            }
                            bufferedOutputStream.write(buf, 0, 4096);
                            offset += 4096;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] getIconNames() {
        return new String[]{"Graph", "Paperclip", "PushPin", "Tag"};
    }

    public static int getIconType(String name) {
        String[] iconNames = getIconNames();
        for (int i = 0; i < iconNames.length; i++) {
            if (iconNames[i].contentEquals(name)) {
                return i;
            }
        }
        return 0;
    }

    public static void saveAttachment(PDFViewCtrl pdfViewCtrl, String newFile, Annot annot, Callback callback) {
        pdfViewCtrl.addTask(new AnonymousClass2(new AnonymousClass1(callback), annot, newFile));
    }
}
