package com.foxit.uiextensions;

public interface Module {
    public static final String MODULE_MORE_MENU = "More Menu Module";
    public static final String MODULE_NAME_ANNOTPANEL = "Annotations Module";
    public static final String MODULE_NAME_BOOKMARK = "Bookmark Module";
    public static final String MODULE_NAME_BRIGHTNESS = "Brightness Module";
    public static final String MODULE_NAME_CARET = "Caret Module";
    public static final String MODULE_NAME_CIRCLE = "Circle Module";
    public static final String MODULE_NAME_DEFAULT = "Default";
    public static final String MODULE_NAME_DIGITALSIGNATURE = "Digital Signature Module";
    public static final String MODULE_NAME_DOCINFO = "DocumentInfo Module";
    public static final String MODULE_NAME_ERASER = "Eraser Module";
    public static final String MODULE_NAME_FILEATTACHMENT = "FileAttachment Module";
    public static final String MODULE_NAME_FORMFILLER = "FormFiller Module";
    public static final String MODULE_NAME_FORM_NAVIGATION = "Navigation Module";
    public static final String MODULE_NAME_HIGHLIGHT = "Highlight Module";
    public static final String MODULE_NAME_INK = "Ink Module";
    public static final String MODULE_NAME_LINE = "Line Module";
    public static final String MODULE_NAME_LINK = "Link Module";
    public static final String MODULE_NAME_NOTE = "Note Module";
    public static final String MODULE_NAME_OUTLINE = "Outline Module";
    public static final String MODULE_NAME_PAGENAV = "Page Navigation Module";
    public static final String MODULE_NAME_PASSWORD = "Password Module";
    public static final String MODULE_NAME_PSISIGNATURE = "PSI Signature Module";
    public static final String MODULE_NAME_REPLY = "Reply Module";
    public static final String MODULE_NAME_SEARCH = "Search Module";
    public static final String MODULE_NAME_SELECTION = "TextSelect Module";
    public static final String MODULE_NAME_SQUARE = "Rectangle Module";
    public static final String MODULE_NAME_SQUIGGLY = "Squiggly Module";
    public static final String MODULE_NAME_STAMP = "Stamp Module";
    public static final String MODULE_NAME_STRIKEOUT = "Strikeout Module";
    public static final String MODULE_NAME_THUMBNAIL = "Thumbnail Module";
    public static final String MODULE_NAME_TYPEWRITER = "Typewriter Module";
    public static final String MODULE_NAME_UNDERLINE = "Underline Module";
    public static final String MODULE_NAME_UNDO = "Undo Redo Module";
    public static final String SECURITY_NAME_PASSWORD = "Standard";

    String getName();

    boolean loadModule();

    boolean unloadModule();
}
