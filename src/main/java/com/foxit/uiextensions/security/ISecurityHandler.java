package com.foxit.uiextensions.security;

public interface ISecurityHandler {
    boolean canAddAnnot(int i);

    boolean canAssemble(int i);

    boolean canCopy(int i);

    boolean canCopyForAssess(int i);

    boolean canFillForm(int i);

    boolean canModifyContents(int i);

    boolean canPrint(int i);

    boolean canPrintHighQuality(int i);

    boolean canSigning(int i);

    String getName();

    int getSupportedTypes();

    boolean isOwner(int i);
}
