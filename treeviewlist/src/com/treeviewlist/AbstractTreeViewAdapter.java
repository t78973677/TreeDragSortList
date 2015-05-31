package com.treeviewlist;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adapter used to feed the table view.
 *
 * @param <T> class for ID of the tree
 */
public abstract class AbstractTreeViewAdapter<T> extends BaseAdapter implements
        ListAdapter, DataTreeRefresher<T> {
    private static final String TAG = AbstractTreeViewAdapter.class
            .getSimpleName();
    protected final String LOADING_VIEW_KEY = "?LOADING_VIEW_KEY";
    private final InMemoryTreeStateManager<T> treeStateManager;
    private final int numberOfLevels;
    private final LayoutInflater layoutInflater;
    private int indentWidth = 20;
    private int indicatorGravity = 0;
    private Drawable collapsedDrawable;
    private Drawable expandedDrawable;
    private Drawable indicatorBackgroundDrawable;
    private Drawable rowBackgroundDrawable;
    private OnClickListener wrapViewClickListener;
    private View.OnLongClickListener wrapViewLongClickListener;
    protected Map<T, InMemoryTreeNode<T>> nodeMap;
    protected List<T> visibleList;
    private InMemoryTreeNode<T> topNode;
    protected ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected Handler handler = new Handler();

    private final OnClickListener indicatorClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            @SuppressWarnings("unchecked")
            final T id = (T) v.getTag();
            expandCollapse(id, true);
        }
    };

    private boolean collapsible;
    private final Activity activity;

    public Activity getActivity() {
        return activity;
    }

    protected TreeStateManager<T> getManager() {
        return treeStateManager;
    }

    protected void expandCollapse(final T id, boolean needToggle) {
        final InMemoryTreeNode<T> info = nodeMap.get(id);
        if (!info.hasChildren()) {
            // ignore - no default action
            return;
        }
        if (info.isChildrenExpand(0)) {
            treeStateManager.collapseChildren(id);
        } else {
            treeStateManager.expandDirectChildren(id);
        }
    }

    private void calculateIndentWidth() {
        if (expandedDrawable != null) {
            indentWidth = Math.max(getIndentWidth(),
                    expandedDrawable.getIntrinsicWidth());
        }
        if (collapsedDrawable != null) {
            indentWidth = Math.max(getIndentWidth(),
                    collapsedDrawable.getIntrinsicWidth());
        }
    }

    public AbstractTreeViewAdapter(final Activity activity,
                                   final InMemoryTreeStateManager<T> treeStateManager, final int numberOfLevels) {
        this.activity = activity;
        this.treeStateManager = treeStateManager;
        this.layoutInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.numberOfLevels = numberOfLevels;
        this.collapsedDrawable = null;
        this.expandedDrawable = null;
        this.rowBackgroundDrawable = null;
        this.indicatorBackgroundDrawable = null;
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        treeStateManager.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        treeStateManager.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        if (nodeMap == null) {
            return 0;
        }
        return visibleList.size();
    }

    @Override
    public Object getItem(final int position) {
        return nodeMap.get(visibleList.get(position));
    }

    @Override
    public boolean hasStableIds() { // NOPMD
        return true;
    }

    @Override
    public int getItemViewType(final int position) {
        return nodeMap.get(visibleList.get(position)).getLevel();
    }

    @Override
    public int getViewTypeCount() {
        return numberOfLevels;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() { // NOPMD
        return true;
    }

    @Override
    public boolean isEnabled(final int position) { // NOPMD
        return true;
    }

    protected int getTreeListItemWrapperId() {
        return R.layout.tree_list_item_wrapper;
    }

    @Override
    public final View getView(final int position, final View convertView,
                              final ViewGroup parent) {
        Log.d(TAG, "Creating a view based on " + convertView
                + " with position " + position);
        final InMemoryTreeNode<T> nodeInfo = nodeMap.get(visibleList.get(position));

        if (nodeInfo.getId().equals(getParent(nodeInfo.getId()) + LOADING_VIEW_KEY)) {
            return getLoadingView(nodeInfo.getId());
        }

        if (convertView == null || convertView.getTag() == null) {
            Log.d(TAG, "Creating the view a new");
            final LinearLayout layout = (LinearLayout) layoutInflater.inflate(
                    getTreeListItemWrapperId(), null);
            return populateTreeItem(layout, getNewChildView(nodeInfo),
                    nodeInfo, true);
        } else {
            Log.d(TAG, "Reusing the view");
            final LinearLayout linear = (LinearLayout) convertView;
            final FrameLayout frameLayout = (FrameLayout) linear
                    .findViewById(R.id.treeview_list_item_frame);
            final View childView = frameLayout.getChildAt(0);
            updateView(childView, nodeInfo);
            return populateTreeItem(linear, childView, nodeInfo, false);
        }
    }

    /**
     * Called when new view is to be created.
     *
     * @param treeNodeInfo node info
     * @return view that should be displayed as tree content
     */
    public abstract View getNewChildView(InMemoryTreeNode<T> treeNodeInfo);

    /**
     * Called when new view is going to be reused. You should update the view
     * and fill it in with the data required to display the new information. You
     * can also create a new view, which will mean that the old view will not be
     * reused.
     *
     * @param view         view that should be updated with the new values
     * @param treeNodeInfo node info used to populate the view
     * @return view to used as row indented content
     */
    public abstract View updateView(View view, InMemoryTreeNode<T> treeNodeInfo);

    /**
     * Retrieves background drawable for the node.
     *
     * @param treeNodeInfo node info
     * @return drawable returned as background for the whole row. Might be null,
     * then default background is used
     */
    public Drawable getBackgroundDrawable(final InMemoryTreeNode<T> treeNodeInfo) { // NOPMD
        return null;
    }

    private Drawable getDrawableOrDefaultBackground(final Drawable r) {
        if (r == null) {
            return activity.getResources()
                    .getDrawable(R.drawable.list_selector_background).mutate();
        } else {
            return r;
        }
    }

    public final LinearLayout populateTreeItem(final LinearLayout layout,
                                               final View childView, final InMemoryTreeNode<T> nodeInfo,
                                               final boolean newChildView) {

        ImageView imgTopStretchView = (ImageView) layout.findViewById(R.id.datatree_top_stretch_view);
        ImageView imgBottomStretchView = (ImageView) layout.findViewById(R.id.datatree_bottom_stretch_view);
        layoutStretchView(imgTopStretchView, imgBottomStretchView, nodeInfo);

        final LinearLayout indicatorView = (LinearLayout) layout.findViewById(R.id.treeview_list_indent_view);
        indicatorView.removeAllViews();
        doAddConnectionLineLayout(indicatorView, nodeInfo.getId());

        final ImageView image = (ImageView) layout.findViewById(R.id.treeview_list_item_image);
        image.setImageDrawable(getDrawable(nodeInfo));

        final LinearLayout imageLayout = (LinearLayout) layout.findViewById(R.id.datatree_image_layout);
        imageLayout.setTag(nodeInfo.getId());
        if (collapsible) {
            imageLayout.setOnClickListener(indicatorClickListener);
        } else {
            imageLayout.setOnClickListener(null);
        }
        layout.setTag(nodeInfo.getId());

        final FrameLayout frameLayout = (FrameLayout) layout
                .findViewById(R.id.treeview_list_item_frame);
        final FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if (newChildView) {
            frameLayout.addView(childView, childParams);
        }
        frameLayout.setTag(nodeInfo.getId());

        if (wrapViewClickListener != null) {
            layout.setOnClickListener(wrapViewClickListener);
        }
        if (wrapViewLongClickListener != null) {
            layout.setOnLongClickListener(wrapViewLongClickListener);
        }

        return layout;
    }

    protected int calculateIndentation(final TreeNodeInfo<T> nodeInfo) {
        return getIndentWidth() * (nodeInfo.getLevel());
    }

    protected Drawable getDrawable(final InMemoryTreeNode<T> nodeInfo) {
        if (!nodeInfo.hasChildren() || !collapsible) {
            return getDrawableOrDefaultBackground(indicatorBackgroundDrawable);
        }
        if (nodeInfo.hasChildrenExpand()) {
            return expandedDrawable;
        } else {
            return collapsedDrawable;
        }
    }

    public void setIndicatorGravity(final int indicatorGravity) {
        this.indicatorGravity = indicatorGravity;
    }

    public void setCollapsedDrawable(final Drawable collapsedDrawable) {
        this.collapsedDrawable = collapsedDrawable;
        calculateIndentWidth();
    }

    public void setExpandedDrawable(final Drawable expandedDrawable) {
        this.expandedDrawable = expandedDrawable;
        calculateIndentWidth();
    }

    public void setIndentWidth(final int indentWidth) {
        this.indentWidth = indentWidth;
        calculateIndentWidth();
    }

    public void setRowBackgroundDrawable(final Drawable rowBackgroundDrawable) {
        this.rowBackgroundDrawable = rowBackgroundDrawable;
    }

    public void setIndicatorBackgroundDrawable(
            final Drawable indicatorBackgroundDrawable) {
        this.indicatorBackgroundDrawable = indicatorBackgroundDrawable;
    }

    public void setCollapsible(final boolean collapsible) {
        this.collapsible = collapsible;
    }

    public void refresh() {
        treeStateManager.refresh();
    }

    private int getIndentWidth() {
        return indentWidth;
    }

    @SuppressWarnings("unchecked")
    public void handleItemClick(final View view, final Object id) {
        expandCollapse((T) id, true);
    }

    protected void layoutStretchView(ImageView imgTopDottedView, ImageView imgBottomDottedView, InMemoryTreeNode<T> nodeInfo) {
        if (isLastChild(nodeInfo.getId()) || isLastVisibleItem(nodeInfo.getId())) {
            imgBottomDottedView.setVisibility(View.INVISIBLE);
        } else {
            imgBottomDottedView.setVisibility(View.VISIBLE);
        }

        if (isFirstVisibleItem(nodeInfo.getId())) {
            imgTopDottedView.setVisibility(View.INVISIBLE);
        } else {
            imgTopDottedView.setVisibility(View.VISIBLE);
        }
    }

    public void setWrapViewClickListener(OnClickListener wrapViewClickListener) {
        this.wrapViewClickListener = wrapViewClickListener;
    }

    public void setWrapViewLongClickListener(View.OnLongClickListener wrapViewLongClickListener) {
        this.wrapViewLongClickListener = wrapViewLongClickListener;
    }

    // InMemoryTreeNode Tool Function

    protected T getParent(final T id) {
        final InMemoryTreeNode<T> node = nodeMap.get(id);
        if (node == null) {
            return null;
        }
        return node.getParent();
    }

    public boolean isFirstChild(T nodeID) {
        T parentID = getParent(nodeID);
        InMemoryTreeNode parentNode = getNodeFromTreeOrThrowAllowRoot(parentID);

        if (parentNode == null) {
            return false;
        }

        int firstVisibleType = 0;
        for (int i = treeStateManager.getChildTypeCount() - 1; i >= 0; i--) {
            List<InMemoryTreeNode<T>> childNodeList = parentNode.getChildren(i);
            if (childNodeList.size() > 0 && childNodeList.get(0).isVisible()) {
                firstVisibleType = i;
            }
        }

        List<T> childList = parentNode.getChildIdList(firstVisibleType);
        if (childList.size() > 0 && childList.indexOf(nodeID) == 0) {
            return true;
        }
        return false;
    }

    public boolean isLastChild(T nodeID) {
        T parentID = getParent(nodeID);
        InMemoryTreeNode parentNode = getNodeFromTreeOrThrowAllowRoot(parentID);

        if (parentNode == null) {
            return false;
        }

        int lastVisibleType = 0;
        for (int i = 0; i < treeStateManager.getChildTypeCount(); i++) {
            List<InMemoryTreeNode<T>> childNodeList = parentNode.getChildren(i);
            if (childNodeList.size() > 0 && childNodeList.get(0).isVisible()) {
                lastVisibleType = i;
            }
        }
        List<T> childList = parentNode.getChildIdList(lastVisibleType);
        if (childList.size() > 0 && childList.indexOf(nodeID) == childList.size() - 1) {
            return true;
        }
        return false;
    }

    public boolean isLastVisibleItem(T nodeID) {
        if (visibleList.indexOf(nodeID) == visibleList.size() - 1) {
            return true;
        }
        return false;
    }

    public boolean isFirstVisibleItem(T nodeID) {
        if (visibleList.indexOf(nodeID) == 0) {
            return true;
        }
        return false;
    }

    public List<T> getChildren(final T id, int childType) {
        final InMemoryTreeNode<T> node = getNodeFromTreeOrThrowAllowRoot(id);
        return node.getChildIdList(childType);
    }

    protected InMemoryTreeNode<T> getNodeFromTreeOrThrowAllowRoot(final T id) {
        if (id == null) {
            return topNode;
        }
        return getNodeFromTreeOrThrow(id);
    }

    protected InMemoryTreeNode<T> getNodeFromTreeOrThrow(final T id) {
        if (id == null) {
            throw new NodeNotInTreeException("(null)");
        }
        final InMemoryTreeNode<T> node = nodeMap.get(id);
        return node;
    }

    public List<T> getVisibleList() {
        readWriteLock.readLock().lock();
        try {
            return visibleList;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Map<T, InMemoryTreeNode<T>> getNodeMap() {
        readWriteLock.readLock().lock();
        try {
            return nodeMap;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    protected void doAddConnectionLineLayout(ViewGroup layoutView, T nodeID) {
        T parentID = getParent(nodeID);
        if (parentID == null) {
            return;
        }

        View connectionView = generateConnectionView();
        if (isLastChild(parentID)) {
            connectionView.setVisibility(View.INVISIBLE);
        } else {
            connectionView.setVisibility(View.VISIBLE);
        }
        layoutView.addView(connectionView, 0);
        doAddConnectionLineLayout(layoutView, parentID);
    }

    protected View generateConnectionView() {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.datatree_connection_view, null);
        ImageView connectionView = (ImageView) contentView.findViewById(R.id.datatree_connection_imageview);
        contentView.removeAllViews();
        return connectionView;
    }

    @Override
    public void refreshSourceData(Map<T, InMemoryTreeNode<T>> nodeMap, List<T> visibleList) {
        readWriteLock.writeLock().lock();
        try {
            this.nodeMap = nodeMap;
            topNode = treeStateManager.getTopSentinel();
            if (nodeMap == null) {
                return;
            }
            this.visibleList = visibleList;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    abstract protected View getLoadingView(T nodeID);

}
