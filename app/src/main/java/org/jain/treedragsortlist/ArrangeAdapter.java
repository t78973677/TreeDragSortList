package org.jain.treedragsortlist;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.treeviewlist.AbstractTreeViewAdapter;
import com.treeviewlist.InMemoryTreeNode;
import com.treeviewlist.InMemoryTreeStateManager;

import org.jain.utils.StyleUtil;

import java.util.List;
import java.util.Map;

public class ArrangeAdapter extends AbstractTreeViewAdapter<String> {
    private Activity mActivity;
    private InMemoryTreeStateManager treeStateManager;
    private ItemEvent event;

    public ArrangeAdapter(Activity mActivity, InMemoryTreeStateManager treeStateManager,
                          int numberOfLevels, ItemEvent event) {
        super(mActivity, treeStateManager, numberOfLevels);
        this.mActivity = mActivity;
        this.treeStateManager = treeStateManager;
        this.event = event;
        treeStateManager.setDataTreeRefresher(this);
    }

    public List<String> getVisibleList() {
        return visibleList;
    }

    public Map<String, InMemoryTreeNode<String>> getNodeMap() {
        return nodeMap;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getNewChildView(InMemoryTreeNode<String> treeNodeInfo) {
        View contentView = LayoutInflater.from(mActivity).inflate(
            R.layout.action_arrange_item, null);
        return updateView(contentView, treeNodeInfo);
    }

    @Override
    public View updateView(View contentView, InMemoryTreeNode<String> treeNodeInfo) {
        ArrangeHolder holder;
        if (contentView == null || contentView.getTag() == null) {
            contentView = LayoutInflater.from(mActivity).inflate(
                    R.layout.action_arrange_item, null);
            holder = new ArrangeHolder(contentView);
            contentView.setTag(holder);
        } else {
            holder = (ArrangeHolder) contentView.getTag();
        }
        ItemEntity data = (ItemEntity) treeNodeInfo.getData();
        holder.setViewItemId(data.getItemId());
        //holder.OverflowItemImage.setImageResource(data.getIcon());
        holder.itemImage.setVisibility(View.GONE);
        if (data.getGroupName() != null) {
            holder.itemText.setText(data.getGroupName());
        } else {
            holder.itemText.setText(mActivity.getString(data.getTitleID()));
        }
        holder.itemText.setTag(treeNodeInfo.getId());
        holder.itemText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                event.addParentNode((String) view.getTag());
                return false;
            }
        });
        holder.arrange_move.setVisibility(View.VISIBLE);
        if (treeNodeInfo.getId().equals(String.valueOf(TreeDragSortListActivity.SETTING_ID))) {
            holder.arrange_move.setVisibility(View.GONE);
        }
        layoutConnectionLine(holder, treeNodeInfo.getId());
        return contentView;
    }

    @Override
    protected Drawable getDrawable(InMemoryTreeNode<String> nodeInfo) {
        ItemEntity item = (ItemEntity)nodeInfo.getData();
        Drawable expandable = mActivity.getResources().getDrawable(R.drawable.datatree_open);
        Drawable collapseAble = mActivity.getResources().getDrawable(R.drawable.datatree_close);
        Drawable dottedLine;

        if (isLastChild(nodeInfo.getId())) {
            dottedLine = mActivity.getResources().getDrawable(R.drawable.datatree_bottom);
        } else {
            dottedLine = mActivity.getResources().getDrawable(R.drawable.datatree_mid);
        }

        if (nodeInfo.isChildrenExpand(0)) {
            return collapseAble;
        }

        if (nodeInfo.hasChildren()) {
            return expandable;
        } else {
            return dottedLine;
        }
    }

    protected void layoutConnectionLine(ArrangeHolder holder, String nodeID) {
        holder.connectionLine.setImageResource(R.drawable.datatree_line);
        if (nodeMap.get(nodeID).hasChildrenExpand()) {
            holder.connectionLine.setVisibility(View.VISIBLE);
        } else {
            holder.connectionLine.setVisibility(View.GONE);
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.connectionLine.getLayoutParams();
        params.height = StyleUtil.getPixelByDP(mActivity, 5f);
        holder.connectionLine.setLayoutParams(params);
    }

    @Override
    protected View generateConnectionView() {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.datatree_connection_view, null);
        ImageView connectionView = (ImageView) contentView.findViewById(R.id.datatree_connection_imageview);
        contentView.removeAllViews();
        return connectionView;
    }

    @Override
    protected View getLoadingView(String nodeID) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(mActivity).inflate(
                getTreeListItemWrapperId(), null);
        FrameLayout frameLayout = (FrameLayout) layout
                .findViewById(com.treeviewlist.R.id.treeview_list_item_frame);
        View loadingView = LayoutInflater.from(mActivity).inflate(R.layout.loading_words_layout, null);
        frameLayout.addView(loadingView);
        LinearLayout indicatorView = (LinearLayout) layout.findViewById(com.treeviewlist.R.id.treeview_list_indent_view);
        doAddConnectionLineLayout(indicatorView, nodeID);
        return layout;
    }

    @Override
    protected void doAddConnectionLineLayout(ViewGroup layoutView, String nodeID) {
        String parentID = getParent(nodeID);
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

    public interface ItemEvent {
        void setVisibility(String id, boolean visibility);
        void addParentNode(String id);
    }
}
