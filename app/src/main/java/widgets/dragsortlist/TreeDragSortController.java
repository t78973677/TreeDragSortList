package widgets.dragsortlist;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jain.treedragsortlist.R;


public class TreeDragSortController extends DragSortController {
    public TreeDragSortController(DragSortListView dslv) {
        super(dslv);
    }

    @Override
    public View onCreateFloatView(int position) {
        // Guaranteed that this will not be null? I think so. Nope, got
        // a NullPointerException once...
        View v = mListView.getChildAt(position + mListView.getHeaderViewsCount() - mListView.getFirstVisiblePosition());

        if (v == null) {
            return null;
        }

        int leftLineVisibility = v.findViewById(R.id.treeview_list_indent_view).getVisibility();
        int bottomLineVisibility = v.findViewById(R.id.datatree_connect_line).getVisibility();
        v.findViewById(R.id.treeview_list_indent_view).setVisibility(View.GONE);
        v.findViewById(R.id.datatree_image_layout).setVisibility(View.GONE);
        v.findViewById(R.id.datatree_connect_line).setVisibility(View.GONE);
        v.setPressed(false);

        // Create a copy of the drawing cache so that it does not get
        // recycled by the framework when the list tries to clean up memory
        v.setDrawingCacheEnabled(true);
        mFloatBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        v.findViewById(R.id.treeview_list_indent_view).setVisibility(leftLineVisibility);
        v.findViewById(R.id.datatree_image_layout).setVisibility(View.VISIBLE);
        v.findViewById(R.id.datatree_connect_line).setVisibility(bottomLineVisibility);
        if (mImageView == null) {
            mImageView = new ImageView(mListView.getContext());
        }
//        mImageView.setBackgroundColor(0);
//        mImageView.getBackground().setAlpha(0);
        mImageView.setAlpha(200);
        mImageView.setPadding(0, 0, 0, 0);
        mImageView.setImageBitmap(mFloatBitmap);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(v.getWidth(), v.getHeight()));

        return mImageView;
    }

}
