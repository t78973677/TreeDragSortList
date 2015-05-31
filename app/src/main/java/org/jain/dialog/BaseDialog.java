package org.jain.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseDialog extends AlertDialog {

    public static OnClickListener EmptyListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

        }
    };

    protected Button positiveButton;
    protected Button negativeButton;
    protected Button neutralButton;
    protected Context context;
    protected View contentView;

    protected int positiveButtonTextSize = 30;
    protected int negativeButtonTextSize = 30;
    protected int neutralButtonTextSize = 30;

    public BaseDialog(Context context) {
        this(context, 3);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        positiveButton = getButton(DialogInterface.BUTTON_POSITIVE);
        negativeButton = getButton(DialogInterface.BUTTON_NEGATIVE);
        neutralButton = getButton(DialogInterface.BUTTON_NEUTRAL);
        setButtonTextsizeAndBackground();
        setTitleTextSizeAndBackground();
    }

    private void setButtonTextsizeAndBackground() {
        setButtonTextSizeAndBackground(positiveButton, positiveButtonTextSize);
        setButtonTextSizeAndBackground(negativeButton, negativeButtonTextSize);
        setButtonTextSizeAndBackground(neutralButton, neutralButtonTextSize);
    }

    private void setTitleTextSizeAndBackground() {
        int resAlertTitle = context.getResources().getIdentifier("alertTitle", "id", "android");
        TextView titleTextView = (TextView) findViewById(resAlertTitle);
        if (titleTextView != null) {

            ViewGroup titleLayoutView = (ViewGroup) titleTextView.getParent();
            titleLayoutView
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            setGlobalTextTypeface(titleTextView);
        }
    }

    private void setButtonTextSizeAndBackground(Button button, int pixelTextSize) {
        if (button == null) {
            return;
        }

        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelTextSize);
        setGlobalTextTypeface(button);

        ViewGroup.LayoutParams param = button.getLayoutParams();
        param.height = ViewGroup.LayoutParams.MATCH_PARENT;
        button.setLayoutParams(param);

        ViewGroup buttonViewGroup = ((ViewGroup) button.getParent());
        buttonViewGroup.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public int getPositiveButtonTextSize() {
        return positiveButtonTextSize;
    }

    public void setPositiveButtonTextSize(int positiveButtonTextSize) {
        this.positiveButtonTextSize = positiveButtonTextSize;
    }

    public int getNegativeButtonTextSize() {
        return negativeButtonTextSize;
    }

    public void setNegativeButtonTextSize(int negativeButtonTextSize) {
        this.negativeButtonTextSize = negativeButtonTextSize;
    }

    public int getNeutralButtonTextSize() {
        return neutralButtonTextSize;
    }

    public void setNeutralButtonTextSize(int neutralButtonTextSize) {
        this.neutralButtonTextSize = neutralButtonTextSize;
    }

    public BaseDialog setPositiveButton(String textID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_POSITIVE, textID, clickListener);
        return this;
    }

    public BaseDialog setPositiveButton(int resID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(resID), clickListener);
        return this;
    }

    public BaseDialog setPositiveButton(CharSequence csID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_POSITIVE, csID.toString(), clickListener);
        return this;
    }

    public BaseDialog setNegativeButton(String textID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_NEGATIVE, textID, clickListener);
        return this;
    }

    public BaseDialog setNegativeButton(int resID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(resID), clickListener);
        return this;
    }

    public BaseDialog setNegativeButton(CharSequence csID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_NEGATIVE, csID.toString(), clickListener);
        return this;
    }

    public BaseDialog setNeutralButton(String textID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_NEUTRAL, textID, clickListener);
        return this;
    }

    public BaseDialog setNeutralButton(int resID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(resID), clickListener);
        return this;
    }

    public BaseDialog setNeutralButton(CharSequence csID, OnClickListener clickListener) {
        setButton(DialogInterface.BUTTON_NEUTRAL, csID.toString(), clickListener);
        return this;
    }

    public BaseDialog setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    public BaseDialog setDialogTitle(int titleID) {
        super.setTitle(titleID);
        return this;
    }

    public BaseDialog setDialogView(View view) {
        setGlobalTextTypeface(view);
        super.setView(view);
        return this;
    }

    private void setGlobalTextTypeface(View contentView) {
        if (contentView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) contentView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setGlobalTextTypeface(viewGroup.getChildAt(i));
            }
        }
    }

    public BaseDialog setDialogView(View view, int viewSpacingLeft
            , int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        setGlobalTextTypeface(view);
        super.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
        return this;
    }

    public BaseDialog setMessage(String message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public void setView(View view) {
        setGlobalTextTypeface(view);
        super.setView(view);
        contentView = view;
    }

    @Override
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        setGlobalTextTypeface(view);
        super.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
        contentView = view;
    }

    protected final <E extends View> E getView(int id) {
        return (E) contentView.findViewById(id);
    }

    public Button getPositiveButton() {
        return getButton(DialogInterface.BUTTON_POSITIVE);
    }

    public Button getNegativeButton() {
        return getButton(DialogInterface.BUTTON_NEGATIVE);
    }

    public Button getNeutralButton() {
        return getButton(DialogInterface.BUTTON_NEUTRAL);
    }

}
