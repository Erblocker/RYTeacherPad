package com.foxit.uiextensions.modules;

import com.foxit.uiextensions.utils.AppDmUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnnotNode implements Comparable<AnnotNode> {
    private String author;
    private boolean canDelete;
    private boolean canReply;
    private boolean checked;
    private List<AnnotNode> children;
    private CharSequence contents;
    int counter;
    private String creationDate;
    private int index;
    private String intent;
    private String modifiedDate;
    private final boolean pageDivider;
    private AnnotNode parent;
    private final String replyTo;
    private String type;
    private final String uid;

    public AnnotNode(int index, String uid, String replyTo) {
        this.index = index;
        this.uid = uid;
        this.replyTo = replyTo;
        this.pageDivider = false;
    }

    public AnnotNode(int index) {
        this.index = index;
        this.uid = null;
        this.replyTo = null;
        this.pageDivider = true;
    }

    public boolean isPageDivider() {
        return this.pageDivider;
    }

    public int getPageIndex() {
        return this.index;
    }

    public void setPageIndex(int index) {
        this.index = index;
    }

    public String getUID() {
        return this.uid == null ? "" : this.uid;
    }

    public String getReplyTo() {
        return this.replyTo == null ? "" : this.replyTo;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthor() {
        return this.author == null ? "" : this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(CharSequence contents) {
        this.contents = contents;
    }

    public CharSequence getContent() {
        return this.contents == null ? "" : this.contents;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedDate() {
        return this.modifiedDate == null ? AppDmUtil.dateOriValue : this.modifiedDate;
    }

    public String getCreationDate() {
        return this.creationDate == null ? AppDmUtil.dateOriValue : this.creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setParent(AnnotNode parent) {
        this.parent = parent;
    }

    public AnnotNode getParent() {
        return this.parent;
    }

    public boolean isRootNode() {
        return this.parent == null;
    }

    public boolean isLeafNode() {
        return this.children == null || this.children.size() == 0;
    }

    public void addChildNode(AnnotNode note) {
        if (this.children == null) {
            this.children = new ArrayList();
        }
        if (!this.children.contains(note)) {
            this.children.add(note);
        }
    }

    public void removeChildren() {
        if (this.children != null) {
            for (int i = 0; i < this.children.size(); i++) {
                ((AnnotNode) this.children.get(i)).removeChildren();
                ((AnnotNode) this.children.get(i)).setParent(null);
            }
            this.children.clear();
        }
    }

    public void removeChild(AnnotNode node) {
        if (this.children != null && this.children.contains(node)) {
            node.removeChildren();
            node.setParent(null);
            this.children.remove(node);
        }
    }

    public List<AnnotNode> getChildren() {
        return this.children;
    }

    public int getLevel() {
        if (this.pageDivider) {
            return -1;
        }
        return this.parent == null ? 0 : this.parent.getLevel() + 1;
    }

    public void setChecked(boolean isChecked) {
        if (!this.pageDivider) {
            this.checked = isChecked;
        }
    }

    public boolean isChecked() {
        return this.checked;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AnnotNode)) {
            return false;
        }
        AnnotNode another = (AnnotNode) o;
        if (this.pageDivider == another.pageDivider && getPageIndex() == another.getPageIndex() && getUID().equals(another.getUID())) {
            return true;
        }
        return false;
    }

    public int compareTo(AnnotNode another) {
        if (another == null) {
            return 0;
        }
        if (getPageIndex() != another.getPageIndex()) {
            return getPageIndex() - another.getPageIndex();
        }
        if (getLevel() != another.getLevel()) {
            return getLevel() - another.getLevel();
        }
        try {
            Date lDate = AppDmUtil.documentDateToJavaDate(AppDmUtil.parseDocumentDate(getCreationDate()));
            Date rDate = AppDmUtil.documentDateToJavaDate(AppDmUtil.parseDocumentDate(another.getCreationDate()));
            if (lDate == null && rDate == null) {
                return 0;
            }
            if (lDate.before(rDate)) {
                return -1;
            }
            if (lDate.after(rDate)) {
                return 1;
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isRedundant() {
        return !getReplyTo().equals("") && (this.parent == null || this.parent.isRedundant());
    }

    public boolean canDelete() {
        return this.canDelete;
    }

    public void setDeletable(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean canReply() {
        return this.canReply;
    }

    public void setCanReply(boolean canReply) {
        this.canReply = canReply;
    }

    public String getIntent() {
        return this.intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }
}
