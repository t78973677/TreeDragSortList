package widgets.dragsortlist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.treeviewlist.AbstractTreeViewAdapter;
import com.treeviewlist.TreeConfigurationException;

public class TreeDragSortListView extends DragSortListView {
    /** Data Tree*/
    private static final int DEFAULT_COLLAPSED_RESOURCE = com.treeviewlist.R.drawable.collapsed;
    private static final int DEFAULT_EXPANDED_RESOURCE = com.treeviewlist.R.drawable.expanded;
    private static final int DEFAULT_INDENT = 0;
    private static final int DEFAULT_GRAVITY = Gravity.LEFT
            | Gravity.CENTER_VERTICAL;
    private Drawable expandedDrawable;
    private Drawable collapsedDrawable;
    private Drawable rowBackgroundDrawable;
    private Drawable indicatorBackgroundDrawable;
    private int indentWidth = 0;
    private int indicatorGravity = 0;
    private AbstractTreeViewAdapter< ? > treeAdapter;
    private boolean collapsible;
    private boolean handleTrackballPress;

    public TreeDragSortListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    private void parseAttributes(final Context context, final AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs,
                com.treeviewlist.R.styleable.TreeViewList);
        expandedDrawable = a.getDrawable(com.treeviewlist.R.styleable.TreeViewList_src_expanded);
        if (expandedDrawable == null) {
            expandedDrawable = context.getResources().getDrawable(
                    DEFAULT_EXPANDED_RESOURCE);
        }
        collapsedDrawable = a
                .getDrawable(com.treeviewlist.R.styleable.TreeViewList_src_collapsed);
        if (collapsedDrawable == null) {
            collapsedDrawable = context.getResources().getDrawable(
                    DEFAULT_COLLAPSED_RESOURCE);
        }
        indentWidth = a.getDimensionPixelSize(
                com.treeviewlist.R.styleable.TreeViewList_indent_width, DEFAULT_INDENT);
        indicatorGravity = a.getInteger(
                com.treeviewlist.R.styleable.TreeViewList_indicator_gravity, DEFAULT_GRAVITY);
        indicatorBackgroundDrawable = a
                .getDrawable(com.treeviewlist.R.styleable.TreeViewList_indicator_background);
        rowBackgroundDrawable = a
                .getDrawable(com.treeviewlist.R.styleable.TreeViewList_row_background);
        collapsible = a.getBoolean(com.treeviewlist.R.styleable.TreeViewList_collapsible, true);
        handleTrackballPress = a.getBoolean(
                com.treeviewlist.R.styleable.TreeViewList_handle_trackball_press, true);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof AbstractTreeViewAdapter)) {
            throw new TreeConfigurationException(
                    "The adapter is not of TreeViewAdapter type");
        }
        treeAdapter = (AbstractTreeViewAdapter<?>) adapter;
        super.setAdapter(treeAdapter);
        if (mAdapterWrapper != null){
            syncAdapter();
        }
    }

    private void syncAdapter() {
        treeAdapter.setCollapsedDrawable(collapsedDrawable);
        treeAdapter.setExpandedDrawable(expandedDrawable);
        treeAdapter.setIndicatorGravity(indicatorGravity);
        treeAdapter.setIndentWidth(indentWidth);
        treeAdapter.setIndicatorBackgroundDrawable(indicatorBackgroundDrawable);
        treeAdapter.setRowBackgroundDrawable(rowBackgroundDrawable);
        treeAdapter.setCollapsible(collapsible);
        if (handleTrackballPress) {
            setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView< ? > parent,
                                        final View view, final int position, final long id) {
                    treeAdapter.handleItemClick(view, view.getTag());
                }
            });
        } else {
            setOnClickListener(null);
        }
    }
}
