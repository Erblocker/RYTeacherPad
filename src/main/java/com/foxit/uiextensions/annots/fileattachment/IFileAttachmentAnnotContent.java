package com.foxit.uiextensions.annots.fileattachment;

import com.foxit.uiextensions.annots.AnnotContent;

public interface IFileAttachmentAnnotContent extends AnnotContent {
    String getFileName();

    String getPath();
}
