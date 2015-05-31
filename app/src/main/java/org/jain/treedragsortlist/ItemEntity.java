package org.jain.treedragsortlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItemEntity implements Serializable, Comparable, Cloneable {
    private String groupName;
    private int icon;
    private int itemId;

    private int order;
    private int titleID;
    private String showText;
    private Boolean visible = true;
    private Boolean enabled = true;
    private Boolean manualVisible = true;
    private List<ItemEntity> child;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getGroupName() {
        return groupName;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    public int getItemId() {
        return itemId;
    }
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public int getIcon() {
        return icon;
    }
    public void setOrder(int order) {
        this.order = order;
    }
    public int getOrder() {
        return order;
    }
    public void setTitleID(int titleID) {
        this.titleID = titleID;
    }
    public int getTitleID() {
        return titleID;
    }
    public void setShowText(String showText) {
        this.showText = showText;
    }
    public String getShowText() {
        return showText;
    }
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    public Boolean getVisible() {
        return visible;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public Boolean isEnabled() {
        return enabled;
    }
    public void setChild(List<ItemEntity> child) {
        this.child = child;
    }
    public List<ItemEntity> getChild() {
        if (child == null) {
            child = new ArrayList<>();
        }
        return child;
    }
    public void setManualVisible(Boolean manualVisible) {
        this.manualVisible = manualVisible;
    }
    public Boolean getManualVisible() {
        return manualVisible;
    }

    @Override
    public int compareTo(Object o) {
        ItemEntity entity = (ItemEntity)o;

        if (getOrder() > entity.getOrder()) {
            return 1;
        }
        else if (getOrder() < entity.getOrder()) {
            return -1;
        }
        else {
            return 0;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
