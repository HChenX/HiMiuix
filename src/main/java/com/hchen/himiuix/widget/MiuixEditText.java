/*
 * This file is part of HiMiuix.
 *
 * HiMiuix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * HiMiuix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with HiMiuix. If not, see <https://www.gnu.org/licenses/lgpl-2.1>.
 *
 * Copyright (C) 2023–2025 HChenX
 */
package com.hchen.himiuix.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnImeVisibilityChangedListener;
import com.hchen.himiuix.helper.ImeHelper;

/**
 * Miuix Edit Text
 *
 * @author 焕晨HChen
 */
public class MiuixEditText extends LinearLayout implements OnImeVisibilityChangedListener {
    private static final String TAG = "HiMiuix:EditText";
    private TextView tipView;
    private ImageView iconView;
    private EditText editText;
    private boolean isIntercept;
    private boolean isAutoRequestFocus;

    public MiuixEditText(Context context) {
        this(context, null);
    }

    public MiuixEditText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("AppCompatCustomView")
    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setOrientation(HORIZONTAL);
        setPadding(getResources().getDimensionPixelSize(R.dimen.miuix_edit_margin), 0, 0, 0);
        setBackgroundResource(R.drawable.miuix_edit_non_focused_border);

        tipView = new TextView(getContext(), null, 0, R.style.MiuixTitleStyle);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.miuix_edit_margin);
        tipView.setLayoutParams(params);
        tipView.setClickable(true);
        addView(tipView);

        editText = new EditText(getContext()) {
            private OnClickListener clickListener;

            @Override
            public boolean dispatchTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isIntercept) {
                        if (clickListener != null)
                            clickListener.onClick(editText);
                        return true;
                    }
                }
                return super.dispatchTouchEvent(event);
            }

            @Override
            public void setOnClickListener(@Nullable OnClickListener l) {
                clickListener = l;
                super.setOnClickListener(l);
            }

            @Override
            protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
                if (!isIntercept) updateBackground(focused);
                super.onFocusChanged(focused, direction, previouslyFocusedRect);
            }
        };
        params = new LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.miuix_edit_height));
        params.weight = 1;
        params.gravity = Gravity.CENTER;
        editText.setLayoutParams(params);
        editText.setBackground(null);
        editText.setSingleLine(true);
        editText.setIncludeFontPadding(false);
        editText.setVerticalScrollBarEnabled(false);
        editText.setHorizontalScrollBarEnabled(false);
        editText.setPadding(0, 0, 0, 0);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHintTextColor(getContext().getColor(R.color.miuix_edit_hint));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
            editText.setLocalePreferredLineHeightForMinimumUsed(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            editText.setTextCursorDrawable(ContextCompat.getDrawable(getContext(), R.drawable.miuix_edit_cursor));
        addView(editText);

        iconView = new ImageView(getContext(), null, 0, R.style.MiuixIconStyle);
        params = new LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.miuix_edit_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.miuix_edit_margin);
        iconView.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.miuix_edit_icon));
        iconView.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.miuix_edit_icon));
        iconView.setLayoutParams(params);
        iconView.setClickable(true);
        addView(iconView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ImeHelper.addListeners(this);
        showInputIfNeed();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ImeHelper.removeListeners(this);
    }

    // -------------------- Inner EditText --------------------
    public final void setHint(@StringRes int resId) {
        editText.setHint(resId);
    }

    public void setHint(@Nullable CharSequence hint) {
        editText.setHint(hint);
    }

    @Nullable
    public CharSequence getHint() {
        return editText.getHint();
    }

    public void setText(@StringRes int resId) {
        editText.setText(resId);
    }

    public void setText(@Nullable CharSequence text) {
        editText.setText(text);
    }

    @Nullable
    public CharSequence getText() {
        return editText.getText();
    }

    public void setInputType(int type) {
        editText.setInputType(type);
    }

    public void setImeOptions(int imeOptions) {
        editText.setImeOptions(imeOptions);
    }

    public void setOnClickListener(OnClickListener listener) {
        editText.setOnClickListener(listener);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        editText.removeTextChangedListener(watcher);
    }

    public void setKeyListener(KeyListener listener) {
        editText.setKeyListener(listener);
    }

    @Nullable
    public KeyListener getKeyListener() {
        return editText.getKeyListener();
    }

    public void setFilters(InputFilter[] filters) {
        editText.setFilters(filters);
    }

    @Nullable
    public InputFilter[] getFilters() {
        return editText.getFilters();
    }

    public boolean hasEditFocus() {
        return editText.hasFocus();
    }

    @Override
    public void clearFocus() {
        editText.clearFocus();
        iconView.clearFocus();
        tipView.clearFocus();
    }

    // -------------------------------------------------------

    // ------------------- Inner TipView --------------------
    public void setTipText(@StringRes int text) {
        tipView.setText(text);
    }

    public void setTipText(CharSequence text) {
        tipView.setText(text);
    }

    @Nullable
    public CharSequence getTipText() {
        return tipView.getText();
    }

    public void setTipOnClickListener(OnClickListener listener) {
        tipView.setOnClickListener(listener);
    }

    // -------------------------------------------------------

    // -------------------- Inner IconView --------------------
    public void setImageDrawable(Drawable drawable) {
        iconView.setImageDrawable(drawable);
    }

    public void setImageIcon(Icon icon) {
        iconView.setImageIcon(icon);
    }

    public void setImageBitmap(Bitmap bitmap) {
        iconView.setImageBitmap(bitmap);
    }

    public void setImageResource(@DrawableRes int resId) {
        iconView.setImageResource(resId);
    }

    public Drawable getDrawable() {
        return iconView.getDrawable();
    }

    public void setIconOnClickListener(OnClickListener listener) {
        iconView.setOnClickListener(listener);
    }

    // ---------------------------------------------------------

    // ------------------------ Inner --------------------------
    @NonNull
    public EditText getInnerEditText() {
        return editText;
    }

    @NonNull
    public TextView getInnerTipView() {
        return tipView;
    }

    @NonNull
    public ImageView getInnerIconView() {
        return iconView;
    }

    // ---------------------------------------------------------


    // 是否自动请求焦点并弹出键盘
    public void setAutoRequestFocus(boolean auto) {
        isAutoRequestFocus = auto;
    }

    // 拦截 Edit 点击动作
    // 但你依然可以收到 Edit 点击事件
    public void setIntercept(boolean intercept) {
        isIntercept = intercept;
    }

    public void updateBackground(boolean hasBorder) {
        if (hasBorder) setBackgroundResource(R.drawable.miuix_edit_focused_border);
        else setBackgroundResource(R.drawable.miuix_edit_non_focused_border);
    }

    private void showInputIfNeed() {
        if (!isAutoRequestFocus) return;
        if (editText == null) return;
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        if (!isInputVisible()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController windowInsetsController = getRootView().getWindowInsetsController();
                if (windowInsetsController != null) {
                    windowInsetsController.show(WindowInsets.Type.ime());
                }
            } else {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, 0);
            }
        }
    }

    private boolean isInputVisible() {
        Rect r = new Rect();
        getWindowVisibleDisplayFrame(r);
        int screenHeight = getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;
        return keypadHeight > screenHeight * 0.15;
    }

    @Override
    public void visibilityChanged(boolean isShown) {
        if (!isShown) clearFocus();
    }
}
