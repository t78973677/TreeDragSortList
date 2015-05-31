package org.jain.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.jain.treedragsortlist.CommonListener;
import org.jain.treedragsortlist.R;

public class CommonDialog extends BaseDialog implements View.OnClickListener {

    private CommonListener listener;

    public CommonDialog(Context context, String title, CommonListener listener
            , boolean hasDelete) {
        super(context);
        setView(LayoutInflater.from(context).inflate(R.layout.dialog_common_list, null));
        setTitle(title);
        TextView addSecondCommon = getView(R.id.add_second_common_text);
        TextView deleteCommon = getView(R.id.delete_second_common_text);
        addSecondCommon.setOnClickListener(this);
        deleteCommon.setOnClickListener(this);

        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_second_common_text:
                listener.showAddParent(((TextView) view).getText().toString());
                break;
            case R.id.delete_second_common_text:
                listener.deleteParent();
                break;
        }
        cancel();
    }
}
