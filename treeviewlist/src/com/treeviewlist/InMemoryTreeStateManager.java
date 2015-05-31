package com.treeviewlist;

import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * In-memory manager of tree state.
 *
 * @param <T> type of identifier
 */
public class InMemoryTreeStateManager<T> implements TreeStateManager<T> {
    private static final String TAG = InMemoryTreeStateManager.class
            .getSimpleName();
    private static final long serialVersionUID = 1L;
    private final Map<T, InMemoryTreeNode<T>> allNodes = new ConcurrentSkipListMap<T, InMemoryTreeNode<T>>();
    private final InMemoryTreeNode<T> topSentinel;
    private transient List<T> visibleListCache = null; // lasy initialised
    private transient List<T> unmodifiableVisibleList = null;
    private boolean visibleByDefault = true;
    private final transient Set<DataSetObserver> observers = new HashSet<DataSetObserver>();
    private Map<T, Integer> sizeMap;
    private HashMap<String, String> expandMap;
    private Handler handler = new Handler();
    private int childTypeCount = 1;
    private DataTreeRefresher dataTreeRefresher;
    private BaseAdapter currentAdapter;

    public synchronized void internalDataSetChanged() {
        final Map<T, InMemoryTreeNode<T>> nodeMap = getNodeMap();
        final List<T> visibleList = new ArrayList<T>(nodeMap.keySet());

        handler.post(new Runnable() {
            @Override
            public void run() {
                visibleListCache = null;
                unmodifiableVisibleList = null;
                if (dataTreeRefresher != null) { // do refresh listview data here
                    dataTreeRefresher.refreshSourceData(nodeMap, visibleList);
                }
                for (final DataSetObserver observer : observers) {
                    observer.onChanged();
                }
                if (currentAdapter != null) {
                    currentAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * If true new nodes are visible by default.
     *
     * @param visibleByDefault if true, then newly added nodes are expanded by default
     */
    public void setVisibleByDefault(final boolean visibleByDefault) {
        this.visibleByDefault = visibleByDefault;
    }

    public InMemoryTreeStateManager() {
        this(1);
    }

    public InMemoryTreeStateManager(int childTypeCount) {
        topSentinel = new InMemoryTreeNode<T>(
                null, null, -1, true, childTypeCount, 0);
        this.childTypeCount = childTypeCount;
    }

    private InMemoryTreeNode<T> getNodeFromTreeOrThrow(final T id) {
        if (id == null) {
            throw new NodeNotInTreeException("(null)");
        }
        final InMemoryTreeNode<T> node = allNodes.get(id);
        return node;
    }

    private InMemoryTreeNode<T> getNodeFromTreeOrThrowAllowRoot(final T id) {
        if (id == null) {
            return topSentinel;
        }
        return getNodeFromTreeOrThrow(id);
    }

    private void expectNodeNotInTreeYet(final T id) {
        final InMemoryTreeNode<T> node = allNodes.get(id);
        if (node != null) {
            throw new NodeAlreadyInTreeException(id.toString(), node.toString());
        }
    }

    @Override
    public synchronized TreeNodeInfo<T> getNodeInfo(final T id) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrow(id);
        final List<InMemoryTreeNode<T>> children = node.getChildren();
        boolean expanded = false;
        if (!children.isEmpty() && children.get(0).isVisible()) {
            expanded = true;
        }
        return new TreeNodeInfo<T>(id, node.getLevel(), !children.isEmpty(),
                node.isVisible(), expanded, node.getData());
    }

    @Override
    public List<T> getChildren(final T id) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        return node.getChildIdList();
    }

    public List<T> getChildren(final T id, int childType) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        if (node == null) {
            return null;
        }
        return node.getChildIdList(childType);
    }

    @Override
    public T getParent(final T id) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        return node.getParent();
    }

    private boolean getChildrenVisibility(final InMemoryTreeNode<T> node) {
        boolean visibility;
        final List<InMemoryTreeNode<T>> children = node.getChildren();
        if (children.isEmpty()) {
            visibility = visibleByDefault;
        } else {
            visibility = children.get(0).isVisible();
        }
        return visibility;
    }

    @Override
    public synchronized void addBeforeChild(final T parent, final T newChild,
                                            final T beforeChild) {
        addBeforeChild(parent, newChild, beforeChild, null, false);
    }

    @Override
    public synchronized void addBeforeChild(T parent, T newChild, T afterChild, Object data, boolean isShow) {
        addBeforeChild(parent, newChild, afterChild, data, isShow, 0, true);
    }


    public synchronized void addBeforeChild(T parent, T newChild, T beforeChild, Object data, boolean isShow,
                                            int childType, boolean needNotify) {
        expectNodeNotInTreeYet(newChild);
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(parent);
        if (node == null) {
            return;
        }
        final boolean visibility = parent == null ? getChildrenVisibility(node) : isShow;
        if (beforeChild == null) {
            final InMemoryTreeNode<T> added = node.add(0, newChild, visibility, data, childType);
            allNodes.put(newChild, added);
        } else {
            final int index = node.indexOf(beforeChild);
            final InMemoryTreeNode<T> added = node.add(index == -1 ? 0 : index,
                    newChild, visibility, data, childType);
            allNodes.put(newChild, added);
        }
        if (visibility && needNotify) {
            internalDataSetChanged();
        }
    }

    @Override
    public synchronized void addAfterChild(final T parent, final T newChild,
                                           final T afterChild) {
        addAfterChild(parent, newChild, afterChild, null, false);
    }

    @Override
    public synchronized void addAfterChild(T parent, T newChild, T afterChild, Object data, boolean isShow) {
        addAfterChild(parent, newChild, afterChild, data, isShow, 0, true);
    }

    public synchronized void addAfterChild(T parent, T newChild, T afterChild, Object data, boolean isShow,
                                           int childType, boolean needNotify) {
        expectNodeNotInTreeYet(newChild);
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(parent);
        if (node == null) {
            return;
        }
        final boolean visibility = parent == null ? getChildrenVisibility(node) : isShow;
        if (afterChild == null) {
            final InMemoryTreeNode<T> added = node.add(
                    node.getChildrenListSize(childType), newChild, visibility, data, childType);
            allNodes.put(newChild, added);
        } else {
            final int index = node.indexOf(afterChild);
            final InMemoryTreeNode<T> added = node.add(
                    index == -1 ? node.getChildrenListSize(childType) : index + 1, newChild,
                    visibility, data, childType);
            allNodes.put(newChild, added);
        }

        if (visibility && needNotify) {
            internalDataSetChanged();
        }
    }

    @Override
    public synchronized void removeNodeRecursively(final T id) {
        removeNodeRecursively(id, 0, true);
    }

    public synchronized void removeNodeRecursively(final T id, int childType, boolean needNotify) {
        removeExpandMapRecord((String) id + childType);
        getSizeMap().remove(id);
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        final boolean visibleNodeChanged = removeNodeRecursively(node);
        final T parent = node.getParent();
        final InMemoryTreeNode<T> parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
        parentNode.removeChild(id, childType);
        if (visibleNodeChanged && needNotify) {
            internalDataSetChanged();
        }
    }

    private synchronized boolean removeNodeRecursively(final InMemoryTreeNode<T> node) {
        boolean visibleNodeChanged = false;
        for (int i = 0; i < childTypeCount; i++) {
            for (final InMemoryTreeNode<T> child : node.getChildren(i)) {
                if (removeNodeRecursively(child)) {
                    visibleNodeChanged = true;
                }
            }
        }
        node.clearChildren();
        if (node.getId() != null) {
            removeExpandMapRecord((String) node.getId() + node.getNodeType());
            getSizeMap().remove(node.getId());
            allNodes.remove(node.getId());
            if (node.isVisible()) {
                visibleNodeChanged = true;
            }
        }
        return visibleNodeChanged;
    }

    private synchronized void setChildrenVisibility(final InMemoryTreeNode<T> node,
                                                    final boolean visible, final boolean recursive) {
        setChildrenVisibility(node, visible, recursive, 0);
    }

    private synchronized void setChildrenVisibility(final InMemoryTreeNode<T> node, final boolean visible,
                                                    final boolean recursive, int childType) {
        for (final InMemoryTreeNode<T> child : node.getChildren(childType)) {
            child.setVisible(visible);
            if (!visible) {
                getSizeMap().remove(child.getId());
            }
            if (recursive) {
                for (int i = 0; i < childTypeCount; i++) {
                    setChildrenVisibility(child, visible, true, i);
                }
            }
        }
    }

    @Override
    public synchronized void expandDirectChildren(final T id) {
        expandDirectChildren(id, 0, true);
    }

    public synchronized void expandDirectChildren(final T id, int childType, boolean needNotify) {
        Log.d(TAG, "Expanding direct children of " + id);
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        if (childType == 0) {
            putExpandMapRecord((String) id + childType, node.getParent() == null ? "" : (String) node.getParent());
        }
        setChildrenVisibility(node, true, false, childType);
        if (needNotify) {
            internalDataSetChanged();
        }
    }

    @Override
    public synchronized void expandEverythingBelow(final T id) {
        Log.d(TAG, "Expanding all children below " + id);
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        setChildrenVisibility(node, true, true);
        internalDataSetChanged();
    }

    @Override
    public synchronized void collapseChildren(final T id) {
        collapseChildren(id, 0, true);
    }

    public synchronized void collapseChildren(final T id, int childType, boolean needNotify) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        removeExpandMapRecord((String) id + childType);
        if (node == topSentinel) {
            for (final InMemoryTreeNode<T> n : topSentinel.getChildren()) {
                setChildrenVisibility(n, false, true, childType);
            }
        } else {
            setChildrenVisibility(node, false, true, childType);
        }
        if (needNotify) {
            internalDataSetChanged();
        }
    }

    @Override
    public synchronized T getNextSibling(final T id) {
        final T parent = getParent(id);
        final InMemoryTreeNode<T> parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
        boolean returnNext = false;
        for (int i = 0; i < childTypeCount; i++) {
            for (final InMemoryTreeNode<T> child : parentNode.getChildren(i)) {
                if (returnNext) {
                    if (!child.isVisible()) {
                        return null;
                    }
                    return child.getId();
                }
                if (child.getId().equals(id)) {
                    returnNext = true;
                }
            }
        }
        return null;
    }

    @Override
    public synchronized T getPreviousSibling(final T id) {
        final T parent = getParent(id);
        final InMemoryTreeNode<T> parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
        T previousSibling = null;
        for (final InMemoryTreeNode<T> child : parentNode.getChildren()) {
            if (child.getId().equals(id)) {
                return previousSibling;
            }
            previousSibling = child.getId();
        }
        return null;
    }

    @Override
    public synchronized boolean isInTree(final T id) {
        return allNodes.containsKey(id);
    }

    @Override
    public synchronized int getVisibleCount() {
        return getVisibleList().size();
    }

    public synchronized Map<T, InMemoryTreeNode<T>> getNodeMap() {
        T currentId = null;
        Map<T, InMemoryTreeNode<T>> nodeMap = new LinkedHashMap<T, InMemoryTreeNode<T>>();
        do {
            currentId = getNextVisible(currentId);
            if (currentId == null) {
                break;
            } else {
                InMemoryTreeNode<T> nodeInfo = getNodeFromTreeOrThrow(currentId);
                if (nodeInfo != null) {
                    nodeMap.put(currentId, nodeInfo);
                }
            }
        } while (true);
        return nodeMap;
    }

    public synchronized List<T> getVisibleList() {
        T currentId = null;
        if (visibleListCache == null) {
            visibleListCache = new ArrayList<T>(allNodes.size());
            do {
                currentId = getNextVisible(currentId);
                if (currentId == null) {
                    break;
                } else {
                    visibleListCache.add(currentId);
                }
            } while (true);
        }
        if (unmodifiableVisibleList == null) {
            unmodifiableVisibleList = Collections
                    .unmodifiableList(visibleListCache);
        }
        return unmodifiableVisibleList;
    }

    public synchronized T getNextVisible(final T id) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        if (!node.isVisible()) {
            return null;
        }
        for (int i = 0; i < childTypeCount; i++) {
            final List<InMemoryTreeNode<T>> children = node.getChildren(i);
            if (!children.isEmpty()) {
                final InMemoryTreeNode<T> firstChild = children.get(0);
                if (firstChild.isVisible()) {
                    return firstChild.getId();
                }
            }
        }
        final T sibl = getNextSibling(id);
        if (sibl != null) {
            return sibl;
        }
        T parent = node.getParent();
        do {
            if (parent == null) {
                return null;
            }

            final T parentSibling = getNextSibling(parent);
            if (parentSibling != null) {
                return parentSibling;
            }
            parent = getNodeFromTreeOrThrow(parent).getParent();
        } while (true);
    }

    @Override
    public synchronized void registerDataSetObserver(
            final DataSetObserver observer) {
        observers.add(observer);
    }

    @Override
    public synchronized void unregisterDataSetObserver(
            final DataSetObserver observer) {
        observers.remove(observer);
    }

    @Override
    public int getLevel(final T id) {
        return getNodeFromTreeOrThrow(id).getLevel();
    }

    @Override
    public Integer[] getHierarchyDescription(final T id) {
        final int level = getLevel(id);
        final Integer[] hierarchy = new Integer[level + 1];
        int currentLevel = level;
        T currentId = id;
        T parent = getParent(currentId);
        while (currentLevel >= 0) {
            hierarchy[currentLevel--] = getChildren(parent).indexOf(currentId);
            currentId = parent;
            parent = getParent(parent);
        }
        return hierarchy;
    }

    private void appendToSb(final StringBuilder sb, final T id) {
        if (id != null) {
            final TreeNodeInfo<T> node = getNodeInfo(id);
            final int indent = node.getLevel() * 4;
            final char[] indentString = new char[indent];
            Arrays.fill(indentString, ' ');
            sb.append(indentString);
            sb.append(node.toString());
            sb.append(Arrays.asList(getHierarchyDescription(id)).toString());
            sb.append("\n");
        }
        final List<T> children = getChildren(id);
        for (final T child : children) {
            appendToSb(sb, child);
        }
    }

    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder();
        appendToSb(sb, null);
        return sb.toString();
    }

    @Override
    public synchronized void clear() {
        sizeMap.clear();
        allNodes.clear();
        topSentinel.clearChildren();
        internalDataSetChanged();
    }

    @Override
    public void refresh() {
        internalDataSetChanged();
    }

    @Override
    public boolean isLastChild(T nodeID) {
        T parentID = getParent(nodeID);
        InMemoryTreeNode parentNode = getNodeFromTreeOrThrowAllowRoot(parentID);
        int lastVisibleType = 0;
        for (int i = 0; i < childTypeCount; i++) {
            List<InMemoryTreeNode<T>> childNodeList = parentNode.getChildren(i);
            if (childNodeList.size() > 0 && childNodeList.get(0).isVisible()) {
                lastVisibleType = i;
            }
        }
        List<T> childList = getChildren(parentID, lastVisibleType);
        if (childList.size() > 0 && childList.indexOf(nodeID) == childList.size() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLastVisibleItem(T nodeID) {
        if (getVisibleList().indexOf(nodeID) == getVisibleList().size() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isFirstVisibleItem(T nodeID) {
        if (getVisibleList().indexOf(nodeID) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public TreeNodeInfo<T> getPreviousVisibleItem(T nodeID) {
        int index = getVisibleList().indexOf(nodeID);
        if (index > 0) {
            return getNodeInfo(getVisibleList().get(index - 1));
        } else
            return null;
    }

    @Override
    public TreeNodeInfo<T> getNextVisibleItem(T nodeID) {
        int index = getVisibleList().indexOf(nodeID);
        if (index < getVisibleCount() - 1) {
            return getNodeInfo(getVisibleList().get(index + 1));
        } else return null;
    }

    public Map<T, Integer> getSizeMap() {
        if (sizeMap == null) {
            sizeMap = new HashMap<T, Integer>();
        }
        return sizeMap;
    }

    public void setExpandMap(HashMap<String, String> expandMap) {
        this.expandMap = expandMap;
    }

    public void updateNodeData(T nodeID, Object data, boolean needNotify) {
        if (data == null) {
            return;
        }
        InMemoryTreeNode memoryTreeNode = getNodeFromTreeOrThrowAllowRoot(nodeID);
        if (memoryTreeNode == null) {
            return;
        }
        memoryTreeNode.setData(data);
        if (memoryTreeNode.isVisible() && needNotify) {
            internalDataSetChanged();
        }
    }

    public Object getNodeData(T nodeID) {
        InMemoryTreeNode memoryTreeNode = getNodeFromTreeOrThrowAllowRoot(nodeID);

        if (memoryTreeNode == null) {
            return null;
        }
        return memoryTreeNode.getData();
    }

    public HashMap<String, String> getExpandMap() {
        return expandMap;
    }

    public void setChildTypeNumber(int childTypeCount) {
        this.childTypeCount = childTypeCount;
    }

    public int getChildTypeCount() {
        return childTypeCount;
    }

    public boolean hasChildrenExpand(T nodeID) {
        InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(nodeID);
        for (int i = 0; i < childTypeCount; i++) {
            List<InMemoryTreeNode<T>> childList = node.getChildren(i);
            if (childList.size() > 0 && childList.get(0).isVisible()) {
                return true;
            }
        }
        return false;
    }

    public InMemoryTreeNode<T> getTopSentinel() {
        return topSentinel;
    }

    public InMemoryTreeNode<T> getInmemoryTreeNode(T nodeID) {
        return getNodeFromTreeOrThrow(nodeID);
    }

    public DataTreeRefresher getDataTreeRefresher() {
        return dataTreeRefresher;
    }

    public void setDataTreeRefresher(DataTreeRefresher dataTreeRefresher) {
        this.dataTreeRefresher = dataTreeRefresher;
    }

    public BaseAdapter getCurrentAdapter() {
        return currentAdapter;
    }

    public void setCurrentAdapter(BaseAdapter currentAdapter) {
        this.currentAdapter = currentAdapter;
    }

    public synchronized void removeExpandMapRecord(String mapKey) {
        if (expandMap == null) {
            return;
        }
        expandMap.remove(mapKey);
    }

    public synchronized void putExpandMapRecord(String mapKey, String value) {
        if (expandMap == null) {
            return;
        }
        expandMap.put(mapKey, value);
    }

}
