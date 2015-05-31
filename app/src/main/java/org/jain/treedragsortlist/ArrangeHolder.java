package org.jain.treedragsortlist;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ArrangeHolder {
    public ImageView itemImage;
    public TextView itemText;
    public ImageView arrange_move;
    public ImageView connectionLine;
    public RelativeLayout datatree_item_layout;
    private View convertView;
    private int itemId;

    public ArrangeHolder(View convertView) {
        buildViewHolder(convertView);
    }

    public View getConvertView() {
        return convertView;
    }

    protected void buildViewHolder(View convertView) {
        itemImage = (ImageView)convertView.findViewById(
                R.id.menu_main_left_item_image);
        itemText = (TextView)convertView.findViewById(
                R.id.menu_main_left_item_textView);
        arrange_move = (ImageView)convertView.findViewById(
                R.id.arrange_move);
        connectionLine = (ImageView)convertView.findViewById(
                R.id.datatree_connect_line);
        datatree_item_layout = (RelativeLayout)convertView.findViewById(R.id.datatree_item_layout);
    }

    public void setViewItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getViewItemId() {
        return itemId;
    }
}
