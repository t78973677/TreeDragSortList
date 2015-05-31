package org.jain.treedragsortlist;

public interface CommonListener {
    void showAddParent(String dialogTitle);
    boolean checkParent(String addName);
    void deleteParent();
    void addParent(String addName);
}
