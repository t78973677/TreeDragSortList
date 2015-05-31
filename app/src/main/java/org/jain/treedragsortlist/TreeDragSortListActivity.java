package org.jain.treedragsortlist;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.treeviewlist.InMemoryTreeNode;
import com.treeviewlist.InMemoryTreeStateManager;

import org.jain.dialog.AddParentDialog;
import org.jain.dialog.BaseDialog;
import org.jain.dialog.CommonDialog;

import widgets.dragsortlist.DragSortController;
import widgets.dragsortlist.DragSortListView;
import widgets.dragsortlist.TreeDragSortController;
import widgets.dragsortlist.TreeDragSortListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class TreeDragSortListActivity extends Activity {

    public static int SETTING_ID = 8591;
    protected boolean isSave = true;
    private TreeDragSortListView mListView;
    private ArrangeAdapter mAdapter;
    private InMemoryTreeStateManager<String> treeStateManager;
    private List<ItemEntity> currentList;
    private ItemEntity longClickEntity;
    // use for add parent
    private List<String> checkShowName;

    private DragSortListView.DragSortListener mOnDragSortListener = new DragSortListView.DragSortListener() {

        @Override
        public void remove(int which) {
        }

        @Override
        public void drag(int from, int to) {
        }

        @Override
        public void drop(int from, int to) {
            if (mAdapter != null && from != to && to > 0) {
                isSave = false;
                String fromTreeNodeKey = mAdapter.getVisibleList().get(from);
                InMemoryTreeNode<String> fromTreeNode = mAdapter.getNodeMap().get(fromTreeNodeKey);
                List<InMemoryTreeNode<String>> childList = new ArrayList<>(fromTreeNode.getChildren());
                String toTreeNodeKey = mAdapter.getVisibleList().get(to);
                InMemoryTreeNode<String> toTreeNode = mAdapter.getNodeMap().get(toTreeNodeKey);
                if (noMoveItem(from, to, fromTreeNode, toTreeNode)) {
                    return;
                }
                // remove from tree node
                treeStateManager.removeNodeRecursively(fromTreeNodeKey);

                if (from > to) {
                    treeStateManager.addBeforeChild(toTreeNode.getParent(), fromTreeNodeKey,
                            toTreeNodeKey, fromTreeNode.getData(), true);
                } else if (toTreeNode.hasChildrenExpand() && toTreeNode.hasChildren()) {
                    treeStateManager.addBeforeChild(toTreeNode.getId(), fromTreeNodeKey,
                            null, fromTreeNode.getData(), true);
                } else {
                    treeStateManager.addAfterChild(toTreeNode.getParent(), fromTreeNodeKey,
                            toTreeNodeKey, fromTreeNode.getData(), true);
                }
                for (InMemoryTreeNode<String> child : childList) {
                    treeStateManager.addAfterChild(child.getParent(), child.getId(),
                            null, child.getData(), false);
                }
            }
        }

        private boolean noMoveItem(int from, int to, InMemoryTreeNode<String> fromTreeNode,
                                   InMemoryTreeNode<String> toTreeNode) {
            return fromTreeNode.getId().equals(toTreeNode.getParent())
                    || (fromTreeNode.hasChildren()
                    && ((from < to && toTreeNode.hasChildren() && toTreeNode.hasChildrenExpand())
                    || !toTreeNode.getParent().equals(String.valueOf(SETTING_ID))));
        }
    };

    private ArrangeAdapter.ItemEvent itemEvent = new ArrangeAdapter.ItemEvent() {
        @Override
        public void setVisibility(String id, boolean visibility) {
            ((ItemEntity)treeStateManager.getInmemoryTreeNode(id).getData())
                    .setManualVisible(visibility);
        }

        @Override
        public void addParentNode(String id) {
            InMemoryTreeNode<String> treeNode = treeStateManager.getInmemoryTreeNode(id);
            if (isMaxLevel(5, treeNode)) {
                return;
            }
            longClickEntity = (ItemEntity) treeNode.getData();
            if (longClickEntity.getGroupName() != null) {
                showCommonDialog(longClickEntity.getGroupName(), true);
            } else {
                showCommonDialog(getString(longClickEntity.getTitleID()), false);
            }
        }

        private boolean isMaxLevel(int max, InMemoryTreeNode<String> treeNode) {
            boolean check = false;
            if (treeNode.getLevel() >= max) {
                check = true;
            } else {
                for (InMemoryTreeNode<String> child : treeNode.getChildren()) {
                    if (isMaxLevel(max, child)) {
                        check = true;
                        break;
                    }
                }
            }
            return check;
        }
    };

    private CommonListener menuCommonListener = new CommonListener() {
        @Override
        public void showAddParent(String dialogTitle) {
            new AddParentDialog(
                    TreeDragSortListActivity.this, dialogTitle,
                    longClickEntity.getGroupName(), this).show();
        }

        @Override
        public boolean checkParent(String addName) {
            return (!treeStateManager.isInTree(addName)
                    && !checkShowName.contains(addName.toLowerCase(Locale.ENGLISH)));
        }

        @Override
        public void deleteParent() {
            if (longClickEntity.getItemId() != 0) {
                treeStateManager.removeNodeRecursively(String.valueOf(longClickEntity.getItemId()));
            } else {
                treeStateManager.removeNodeRecursively(longClickEntity.getGroupName());
            }
        }

        @Override
        public void addParent(String addName) {
            ItemEntity parentNode = new ItemEntity();
            parentNode.setGroupName(addName);
            String childKey;

            if (longClickEntity.getItemId() != 0) {
                childKey = String.valueOf(longClickEntity.getItemId());
            } else {
                childKey = longClickEntity.getGroupName();
            }
            InMemoryTreeNode<String> treeNode = mAdapter.getNodeMap().get(childKey);
            int position;
            InMemoryTreeNode<String> nextTreeNode;

            List<InMemoryTreeNode<String>> sameLevelList = mAdapter.getNodeMap().get(treeNode.getParent()).getChildren();
            position = sameLevelList.indexOf(treeNode);
            if (sameLevelList.size() <= 1) {
                nextTreeNode = null;
            } else if (position == 0) {
                nextTreeNode = sameLevelList.get(1);
            } else {
                nextTreeNode = sameLevelList.get(position - 1);
            }
            treeStateManager.removeNodeRecursively(childKey);
            if (nextTreeNode == null) {
                treeStateManager.addBeforeChild(treeNode.getParent(), addName,
                        null, parentNode, true);
            } else if(position == 0) {
                treeStateManager.addBeforeChild(treeNode.getParent(), addName,
                        nextTreeNode.getId(), parentNode, true);
            } else {
                treeStateManager.addAfterChild(treeNode.getParent(), addName,
                        nextTreeNode.getId(), parentNode, true);
            }
            treeStateManager.addAfterChild(addName, childKey,
                    null, longClickEntity, true);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_menu);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        buildView();
        initDragSortController();
    }

    protected void init() {
        treeStateManager = new InMemoryTreeStateManager<>();
        currentList = new ArrayList<>(getCurrentList(20, 1, true));
        checkShowName = new ArrayList<>();

        ItemEntity firstNode = getDefaultNode();
        treeStateManager.addAfterChild(null, String.valueOf(firstNode.getItemId()), null, firstNode, true);
        recursiveAddTreeNode(String.valueOf(firstNode.getItemId()), currentList, true);
        mAdapter = new ArrangeAdapter(this, treeStateManager, 20000, itemEvent);
    }

    private List<ItemEntity> getCurrentList(int count, int defaultKey, boolean hasChild) {
        List<ItemEntity> list = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            int key = (defaultKey * 1000) + index;
            ItemEntity item = new ItemEntity();
            item.setItemId(key);
            item.setOrder(index);
            item.setGroupName("tree_drag_item_" + key);
            if (hasChild && (index % 3 == 1)) {
                item.setChild(getCurrentList(3, key, false));
            }
            list.add(item);
        }
        return list;
    }

    protected void recursiveAddTreeNode(String parentNodeKey, List<ItemEntity> list, boolean isShow) {
        for (ItemEntity child : list) {
            String newChild, checkName;
            if (child.getItemId() != 0) {
                newChild = String.valueOf(child.getItemId());
            } else {
                newChild = child.getGroupName();
            }
            checkName = child.getGroupName().toLowerCase(Locale.ENGLISH);
            if (!treeStateManager.isInTree(newChild)) {
                checkShowName.add(checkName);
                treeStateManager.addAfterChild(
                        parentNodeKey, newChild, null, child, isShow);
                if (child.getChild().size() > 0) {
                    recursiveAddTreeNode(newChild, child.getChild(), false);
                }
            }
        }
    }

    protected ItemEntity getDefaultNode() {
        ItemEntity firstNode = new ItemEntity();
        firstNode.setItemId(SETTING_ID);
        firstNode.setGroupName("first_tree_node");
        return firstNode;
    }

    protected void initDragSortController() {
        TreeDragSortController controller = new TreeDragSortController(mListView);
        controller.setDragHandleId(R.id.arrange_move);
        controller.setDragInitMode(DragSortController.ON_DOWN);
        controller.setRemoveMode(DragSortController.CLICK_REMOVE);
        controller.setSortEnabled(true);

        mListView.setFloatViewManager(controller);
        mListView.setOnTouchListener(controller);
        mListView.setDragEnabled(true);
        mListView.setDragSortListener(mOnDragSortListener);
    }

    protected void buildView() {
        mListView = (TreeDragSortListView) findViewById(R.id.listview_files);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (!isSave) {
            showSaveOfDialog();
        } else {
            canceled();
        }
    }

    protected void canceled() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    protected void showCommonDialog(String title, boolean hasDelete) {
        new CommonDialog(this, title, menuCommonListener, hasDelete).show();
    }

    protected void showSaveOfDialog() {
        BaseDialog baseDialog = new BaseDialog(this)
                .setDialogView(
                        defaultDialog("Exit", "Is exit?"))
                .setPositiveButton("Store",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                done();
                            }
                        });
        baseDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        canceled();
                    }
                });
        baseDialog.show();
    }

    protected View defaultDialog(String title, String msg) {
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.dialog_common_view, null);
        ((TextView) contentView.findViewById(R.id.dialogCommon_titleTextView))
                .setText(title);
        ((TextView) contentView.findViewById(R.id.dialogCommon_messageTextView))
                .setText(msg);
        return contentView;
    }

    protected List<ItemEntity> recursiveGetFlowItem(
            Collection<InMemoryTreeNode<String>> treeNodeList,
            List<ItemEntity> itemEntityList, List<String> checkID) {
        int index = 1;
        for (InMemoryTreeNode<String> treeNode : treeNodeList) {
            if (!checkID.contains(treeNode.getId())) {
                ItemEntity entity = (ItemEntity) treeNode.getData();
                entity.setOrder(index);
                entity.setChild(new ArrayList<ItemEntity>());
                if (treeNode.hasChildren()) {
                    recursiveGetFlowItem(treeNode.getChildren(), entity.getChild(), checkID);
                }
                if (entity.getGroupName() != null && entity.getChild().size() == 0) {
                    continue;
                }
                itemEntityList.add(entity);
                checkID.add(treeNode.getId());
                index++;
            }
        }
        return itemEntityList;
    }

    protected void done() {
        List<ItemEntity> itemEntityList =
                recursiveGetFlowItem(mAdapter.getNodeMap().get(mAdapter.getVisibleList().get(0)).getChildren(),
                        new ArrayList<ItemEntity>(), new ArrayList<String>());

        finish();
    }
}
