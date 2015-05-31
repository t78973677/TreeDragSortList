package org.jain.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jain.treedragsortlist.CommonListener;
import org.jain.treedragsortlist.R;
import org.jain.utils.StringUtil;
import org.jain.utils.SystemUtil;

public class AddParentDialog extends BaseDialog {

    private CommonListener listener;

    public AddParentDialog(Context context, String title, String optionName,
                           CommonListener listener) {
        super(context);
        this.listener = listener;
        setTitle(title);
        setView(LayoutInflater.from(context).inflate(R.layout.dialog_add_parent, null));
        TextView option_menu_item_textView = getView(R.id.option_menu_item_textView);
        option_menu_item_textView.setText(optionName);

        setPositiveButton("Yes", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel();
            }
        });
        setShowListener();
    }

    private void setShowListener() {
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button mPositiveButton = ((BaseDialog) dialog)
                        .getPositiveButton();
                mPositiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText option_menu_parent_edit = getView(R.id.option_menu_parent_edit);
                        // check currentName
                        String name = option_menu_parent_edit.getText()
                                .toString().trim();

                        String errorMessage = "";

                        if (StringUtil.isEmpty(name)) {
                            errorMessage = "not empty";
                        } else if (!AddParentDialog.this.listener
                                .checkParent(name)) {
                            errorMessage = "have this name";
                        }
                        if (!errorMessage.equals("")) {
                            option_menu_parent_edit.requestFocus();
                            option_menu_parent_edit.setError(errorMessage);
                            SystemUtil.removeEditTextError(
                                    option_menu_parent_edit, null);
                            return;
                        }
                        AddParentDialog.this.listener.addParent(name);
                        option_menu_parent_edit.setText("");
                        cancel();
                    }
                });
            }
        });
    }
}
