package com.treeviewlist;

import java.util.List;
import java.util.Map;

public interface DataTreeRefresher<T> {
    public void refreshSourceData(Map<T, InMemoryTreeNode<T>> nodeMap, List<T> visibleList);
}
