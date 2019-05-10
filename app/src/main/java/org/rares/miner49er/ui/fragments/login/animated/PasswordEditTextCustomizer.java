package org.rares.miner49er.ui.fragments.login.animated;

import android.graphics.Typeface;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.widget.AppCompatEditText;
import org.rares.miner49er.util.TextUtils;

public class PasswordEditTextCustomizer {

    private AppCompatEditText passwordEditText;
    private Typeface passwordTypeface;

    private PasswordTransformationMethod passwordOn = new PasswordTransformationMethod();
    private OnTouchListener passwordPeekListener = new OnTouchListener() {
        private int selectionStart;
        private int selectionEnd;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!v.equals(passwordEditText)) {
                return false;
            }
            int DRAWABLE_RIGHT = 2;// <0 ^1 >2 v3
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getRawX() >= (
                        passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                    passwordEditText.setTypeface(passwordTypeface);
                    passwordEditText.setTransformationMethod(null);
                    selectionStart = passwordEditText.getSelectionStart();
                    selectionEnd = passwordEditText.getSelectionEnd();
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (
                        passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordEditText.setTransformationMethod(passwordOn);
                    if (selectionStart == selectionEnd && selectionStart == 0) {
                        selectionStart = passwordEditText.getEditableText().length();
                        selectionEnd = selectionStart;
                    }
                    passwordEditText.setSelection(selectionStart, selectionEnd);
                    return true;
                }
            }
            return false;
        }
    };

    public PasswordEditTextCustomizer(AppCompatEditText passwordEditText, Typeface passwordTypeface) {
        this.passwordEditText = passwordEditText;
        this.passwordTypeface = passwordTypeface;
    }

    public void customize(/*Runnable onDoneAction*/) {
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                TextUtils.hideKeyboardFrom(v);
/*                if (onDoneAction != null) {
                    onDoneAction.run();
                }*/
            }
            return false;
        });
        passwordEditText.setOnTouchListener(passwordPeekListener);
    }

}
