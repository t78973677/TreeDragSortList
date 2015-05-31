package com.treeviewlist;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Node. It is package protected so that it cannot be used outside.
 *
 * @param <T> type of the identifier used by the tree
 */
public class InMemoryTreeNode<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final T id;
    private final T parent;
    private final int level;
    private boolean visible = true;
    private final int childTypeCount;
    private final List<InMemoryTreeNode<T>>[] childrens;
    private final List<InMemoryTreeNode<T>> children = new LinkedList<InMemoryTreeNode<T>>();
    private List<T>[] childIdListCache = null;
    private Object data;
    private int nodeType = 0;

    public InMemoryTreeNode(final T id, final T parent, final int level,
                            final boolean visible) {
        this(id, parent, level, visible, 1, 0);
    }

    public InMemoryTreeNode(final T id, final T parent, final int level,
                            final boolean visible, final int childTypeCount, final int nodeType) {
        super();
        this.id = id;
        this.parent = parent;
        this.level = level;
        this.visible = visible;
        this.childTypeCount = childTypeCount;
        this.nodeType = nodeType;
        this.childrens = new List[childTypeCount];
        this.childIdListCache = new List[childTypeCount];
        initChildrens();
    }

    private void initChildrens() {
        for (int i = 0; i < childTypeCount; i++) {
            if (i == 0) {
                childrens[i] = children;
                continue;
            }
            childrens[i] = new LinkedList<InMemoryTreeNode<T>>();
        }
    }

    public int indexOf(final T id) {
        return getChildIdList().indexOf(id);
    }

    public int indexOf(final T id, int childType) {
        return getChildIdList(childType).indexOf(id);
    }

    /**
     * Cache is built lasily only if needed. The cache is cleaned on any
     * structure change for that node!).
     *
     * @return list of ids of children
     */
    public synchronized List<T> getChildIdList() {
        return getChildIdList(0);
    }

    public synchronized List<T> getChildIdList(int childType) {
        if (childIdListCache[childType] != null) {
            return childIdListCache[childType];
        }
        childIdListCache[childType] = new LinkedList<T>();
        for (final InMemoryTreeNode<T> n : childrens[childType]) {
            childIdListCache[childType].add(n.getId());
        }
        return childIdListCache[childType];
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public int getChildrenListSize() {
        return children.size();
    }

    public int getChildrenListSize(int childType) {
        return childrens[childType].size();
    }

    public synchronized InMemoryTreeNode<T> add(final int index, final T child,
                                                final boolean visible) {
        return add(index, child, visible, null);
    }

    public synchronized InMemoryTreeNode<T> add(final int index, final T child,
                                                final boolean visible, Object data) {
        childIdListCache[0] = null;
        // Note! top levell children are always visible (!)
        final InMemoryTreeNode<T> newNode = new InMemoryTreeNode<T>(child,
                getId(), getLevel() + 1, getId() == null ? true : visible);
        newNode.setData(data);
        children.add(index, newNode);
        return newNode;
    }

    public synchronized InMemoryTreeNode<T> add(final int index, final T child,
                                                final boolean visible, Object data, int childType) {
        childIdListCache[childType] = null;
        // Note! top levell children are always visible (!)
        final InMemoryTreeNode<T> newNode = new InMemoryTreeNode<T>(child,
                getId(), getLevel() + 1, getId() == null ? true : visible, childTypeCount, childType);
        newNode.setData(data);
        childrens[childType].add(index, newNode);
        return newNode;
    }

    /**
     * Note. This method should technically return unmodifiable collection, but
     * for performance reason on small devices we do not do it.
     *
     * @return children list
     */
    public List<InMemoryTreeNode<T>> getChildren() {
        return children;
    }

    public List<InMemoryTreeNode<T>> getChildren(int childType) {
        if (childType >= childTypeCount || childType < 0) {
            return null;
        }
        return childrens[childType];
    }

    public synchronized void clearChildren() {
        for (int i = 0; i < childTypeCount; i++) {
            childrens[i].clear();
            childIdListCache[i] = null;
        }

    }

    public synchronized void removeChild(final T child) {
        final int childIndex = indexOf(child);
        if (childIndex != -1) {
            children.remove(childIndex);
            childIdListCache[0] = null;
        }
    }

    public synchronized void removeChild(final T child, int childType) {
        final int childIndex = indexOf(child, childType);
        if (childIndex != -1) {
            childrens[childType].remove(childIndex);
            childIdListCache[childType] = null;
        }
    }

    public synchronized boolean hasChildren() {
        for (int i = 0; i < childTypeCount; i++) {
            if (getChildren(i).size() > 0) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean hasChildren(int childType) {
        if (getChildren(childType) != null && getChildren(childType).size() > 0) {
            return true;
        }
        return false;
    }

    public synchronized boolean hasChildrenExpand() {
        for (int i = 0; i < childTypeCount; i++) {
            List<InMemoryTreeNode<T>> childList = getChildren(i);
            if (childList != null && childList.size() > 0 && childList.get(0).isVisible()) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isChildrenExpand(int childType) {
        List<InMemoryTreeNode<T>> childList = getChildren(childType);
        if (childList != null && childList.size() > 0 && childList.get(0).isVisible()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "InMemoryTreeNode [id=" + getId() + ", parent=" + getParent()
                + ", level=" + getLevel() + ", visible=" + visible
                + ", children=" + children + ", childIdListCache="
                + childIdListCache[0] + "]";
    }

    public T getId() {
        return id;
    }

    public T getParent() {
        return parent;
    }

    public int getLevel() {
        return level;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getNodeType() {
        return nodeType;
    }


}
