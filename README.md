# TreeDragSortList #

----------
# 1.move item #
![](http://std.hku.edu.tw/~b911100304/android/2015-05-31_155837.png)

# 2.add parent #
![](http://std.hku.edu.tw/~b911100304/android/2015-05-31_160102.png)

----------
## TreeDragSortListActivity ##

    protected void init() {
        treeStateManager = new InMemoryTreeStateManager<>();
        currentList = new ArrayList<>(getCurrentList(20, 1, true));
        checkShowName = new ArrayList<>();

        ItemEntity firstNode = getDefaultNode();
        treeStateManager.addAfterChild(null, String.valueOf(firstNode.getItemId()), null, firstNode, true);
        recursiveAddTreeNode(String.valueOf(firstNode.getItemId()), currentList, true);
        mAdapter = new ArrangeAdapter(this, treeStateManager, 20000, itemEvent);
    }