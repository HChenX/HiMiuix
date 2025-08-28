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
package com.hchen.himiuix;

import static android.view.Gravity.CENTER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.hchen.himiuix.callback.OnItemSelectedListener;
import com.hchen.himiuix.helper.HapticFeedbackHelper;
import com.hchen.himiuix.helper.WindowInsetsHelper;
import com.hchen.himiuix.utils.MiuiSuperBlur;
import com.hchen.himiuix.utils.MiuixUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Miuix 底部导航栏视图
 *
 * @author 焕晨HChen
 */
public class MiuixBottomNavigatorView extends LinearLayout implements OnItemSelectedListener {
    private static final String TAG = "Miuix:BottomNavigatorView";
    private static final String XML_MENU = "menu"; // Menu tag name in XML.
    private static final String XML_GROUP = "group"; // Group tag name in XML.
    private static final String XML_ITEM = "item"; // Item tag name in XML.
    private final MenuInfo finalMenuInfo = new MenuInfo(); // 通常来说底栏 Menu 是单例
    private final List<MenuInfo> subMenuInfos = new ArrayList<>();
    private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnItemSelectedListener listener;
    private boolean isHapticFeedbackEnabled;
    private int targetHeight;
    @MenuRes
    private int menuId;
    private int checkedId;

    public MiuixBottomNavigatorView(Context context) {
        this(context, null);
    }

    public MiuixBottomNavigatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixBottomNavigatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixBottomNavigatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixBottomNavigatorView, defStyleAttr, defStyleRes);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.MiuixBottomNavigatorView_android_hapticFeedbackEnabled, true);
        menuId = typedArray.getResourceId(R.styleable.MiuixBottomNavigatorView_menu, -1);
        typedArray.recycle();

        setOrientation(HORIZONTAL);
        if (!MiuiSuperBlur.isSupportBlur())
            setBackgroundColor(getContext().getColor(R.color.miuix_default_surface_color));
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.miuix_bottom_menu_min_height));
        targetHeight = getResources().getDimensionPixelSize(R.dimen.miuix_bottom_menu_target_height);

        dividerPaint.setColor(getResources().getColor(R.color.miuix_bottom_divider_color));
        dividerPaint.setStrokeWidth(getResources().getDisplayMetrics().density);

        if (menuId != -1) inflateMenu(menuId);
        applyWindowInsets();
    }

    public void inflateMenu(@MenuRes int id) {
        inflateMenuInner(id);
    }

    public void setTitleSize(float size) {
        for (MenuInfo info : subMenuInfos) {
            info.setTitleSize(size);
        }
    }

    public void setIconSize(int size) {
        for (MenuInfo info : subMenuInfos) {
            info.setIconSize(size);
        }
    }

    public void check(int id) {
        for (MenuInfo info : subMenuInfos) {
            if (info.id == id) {
                info.setChecked(true);
            }
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (MiuiSuperBlur.isSupportBlur()) applyBlur();
    }

    private void applyBlur() {
        int colorBottomSurface = getContext().getColor(R.color.miuix_default_surface_color);
        MiuiSuperBlur.setMiViewBlurMode(this, 1);
        MiuiSuperBlur.setMiBackgroundBlurMode(this, 1);
        MiuiSuperBlur.setMiBackgroundBlurRadius(this, (int) (getContext().getResources().getDisplayMetrics().density * 66 + 0.5f));
        int[] colors = MiuiSuperBlur.getBlendColor(getContext(), colorBottomSurface, !MiuixUtils.isDarkMode(getResources()) ?
            new int[]{-1889443744, -1543503873} :
            new int[]{1970500467, -1979711488, 184549375});
        int[] colorMode = !MiuixUtils.isDarkMode(getResources()) ? new int[]{18, 3} : new int[]{19, 3, 3};
        for (int i = 0; i < colors.length; i++) {
            MiuiSuperBlur.addMiBackgroundBlendColor(this, colors[i], colorMode[i]);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MiuiSuperBlur.clearAllBlur(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(targetHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawLine(0, 0, getWidth(), 0, dividerPaint);
        super.dispatchDraw(canvas);
    }

    @SuppressLint("ResourceType")
    private void inflateMenuInner(@MenuRes int id) {
        XmlResourceParser parser = null;

        try {
            parser = getContext().getResources().getLayout(id);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            parseMenu(parser, attrs);
        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException("Error inflating menu XML", e);
        }
    }

    private void parseMenu(XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String tagName;
        boolean lookingForEndOfUnknownTag = false;
        String unknownTagName = null;

        // This loop will skip to the menu start tag
        do {
            if (eventType == XmlResourceParser.START_TAG) {
                tagName = parser.getName();
                if (Objects.equals(tagName, XML_MENU)) {
                    eventType = parser.next(); // Go to next tag
                    break;
                }

                throw new RuntimeException("Expecting menu, got: " + tagName);
            }
            eventType = parser.next();
        } while (eventType != XmlResourceParser.END_DOCUMENT);

        boolean reachedEndOfMenu = false;
        while (!reachedEndOfMenu) {
            switch (eventType) {
                case XmlResourceParser.START_TAG -> {
                    if (lookingForEndOfUnknownTag) break;

                    tagName = parser.getName();

                    if (Objects.equals(tagName, XML_GROUP)) finalMenuInfo.buildGroup(attrs);
                    else if (Objects.equals(tagName, XML_ITEM)) finalMenuInfo.buildItem(attrs);
                    else if (!Objects.equals(tagName, XML_MENU)) {
                        lookingForEndOfUnknownTag = true;
                        unknownTagName = tagName;
                    }
                }

                case XmlPullParser.END_TAG -> {
                    tagName = parser.getName();

                    if (Objects.equals(tagName, XML_MENU)) reachedEndOfMenu = true;
                    else if (lookingForEndOfUnknownTag && Objects.equals(tagName, unknownTagName)) {
                        lookingForEndOfUnknownTag = false;
                        unknownTagName = null;
                    }
                }

                case XmlPullParser.END_DOCUMENT ->
                    throw new RuntimeException("Unexpected end of document");
            }

            eventType = parser.next();
        }

        inflateMenuLayout();
    }

    private void inflateMenuLayout() {
        if (subMenuInfos.isEmpty()) return;
        for (MenuInfo state : subMenuInfos) {
            MenuSingleView singleView = new MenuSingleView(getContext());
            state.setSingleView(singleView);

            singleView.setInfo(state);
            singleView.setId(state.id);
            singleView.setTitle(state.title);
            singleView.setIcon(state.icon);
            singleView.setClickable(state.isCheckable);
            singleView.setChecked(state.isChecked);
            singleView.setEnabled(state.isEnabled);
            singleView.setHapticFeedbackEnabled(isHapticFeedbackEnabled);
            singleView.setVisibility(state.isVisible ? VISIBLE : GONE);

            addView(singleView);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuInfo item, boolean fromUser) {
        if (listener == null || listener.onNavigationItemSelected(item, fromUser)) {
            for (MenuInfo info : subMenuInfos) {
                if (Objects.equals(info, item)) continue;
                info.setChecked(false);
            }
            return true;
        }
        return false;
    }

    private void applyWindowInsets() {
        WindowInsetsHelper.setOnApplyWindowInsetsListener(new androidx.core.view.OnApplyWindowInsetsListener() {
            @NonNull @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            }
        });
    }

    public class MenuInfo {
        private MenuSingleView singleView;
        @IdRes
        private int id;
        private CharSequence title;
        private Drawable icon;
        private boolean isVisible;
        private boolean isEnabled;
        private boolean isChecked;
        private boolean isCheckable;

        private MenuInfo() {
        }

        public void setTitle(CharSequence title) {
            this.title = title;
            singleView.setTitle(title);
        }

        public void setTitleSize(float size) {
            singleView.setTitleSize(size);
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
            singleView.setIcon(icon);
        }

        public void setIconSize(int size) {
            singleView.setIconSize(size);
        }

        public void setVisible(boolean visible) {
            isVisible = visible;
            singleView.setVisibility(visible ? VISIBLE : GONE);
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
            singleView.setChecked(checked);
        }

        public void setCheckable(boolean checkable) {
            isCheckable = checkable;
            singleView.setClickable(checkable);
        }

        public int getId() {
            return id;
        }

        public CharSequence getTitle() {
            return title;
        }

        public Drawable getIcon() {
            return icon;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public boolean isCheckable() {
            return isCheckable;
        }

        private void setSingleView(MenuSingleView singleView) {
            this.singleView = singleView;
        }

        @SuppressLint("PrivateResource")
        private void buildGroup(AttributeSet attrs) {
            if (!subMenuInfos.isEmpty()) throw new RuntimeException("Repetitive Menu Group!");

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuGroup);

            id = a.getResourceId(R.styleable.MenuGroup_android_id, NO_ID);
            isVisible = a.getBoolean(R.styleable.MenuGroup_android_visible, true);
            isEnabled = a.getBoolean(R.styleable.MenuGroup_android_enabled, true);

            a.recycle();
        }

        @SuppressLint("PrivateResource")
        private void buildItem(AttributeSet attrs) {
            MenuInfo subMenuInfo = new MenuInfo();

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuItem);

            subMenuInfo.id = a.getResourceId(R.styleable.MenuItem_android_id, NO_ID);
            subMenuInfo.title = a.getText(R.styleable.MenuItem_android_title);
            subMenuInfo.icon = a.getDrawable(R.styleable.MenuItem_android_icon);

            subMenuInfo.isCheckable = a.getBoolean(R.styleable.MenuItem_android_checkable, true);
            subMenuInfo.isChecked = a.getBoolean(R.styleable.MenuItem_android_checked, false);
            subMenuInfo.isVisible = a.getBoolean(R.styleable.MenuItem_android_visible, true);
            subMenuInfo.isEnabled = a.getBoolean(R.styleable.MenuItem_android_enabled, true);

            a.recycle();

            if (subMenuInfo.id == NO_ID)
                throw new RuntimeException("Menu must have an ID set!");

            subMenuInfos.add(subMenuInfo);
        }

        @NonNull
        @Override
        public String toString() {
            return "MenuInfo{" +
                "singleView=" + singleView +
                ", id=" + id +
                ", title=" + title +
                ", icon=" + icon +
                ", isVisible=" + isVisible +
                ", isEnabled=" + isEnabled +
                ", isChecked=" + isChecked +
                ", isCheckable=" + isCheckable +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MenuInfo menuInfo)) return false;
            return id == menuInfo.id &&
                isVisible == menuInfo.isVisible &&
                isEnabled == menuInfo.isEnabled &&
                isChecked == menuInfo.isChecked &&
                isCheckable == menuInfo.isCheckable &&
                Objects.equals(singleView, menuInfo.singleView) &&
                Objects.equals(title, menuInfo.title) &&
                Objects.equals(icon, menuInfo.icon);
        }

        @Override
        public int hashCode() {
            return Objects.hash(singleView, id, title, icon, isVisible, isEnabled, isChecked, isCheckable);
        }
    }

    private class MenuSingleView extends LinearLayout {
        private MenuInfo info;
        private ImageView icon;
        private TextView title;
        private boolean isChecked;

        public MenuSingleView(Context context) {
            this(context, null);
        }

        public MenuSingleView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MenuSingleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public MenuSingleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            init(context, attrs, defStyleAttr, defStyleRes);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
            params.weight = 1;
            params.gravity = CENTER;
            setLayoutParams(params);
            setPadding(0, getResources().getDimensionPixelSize(R.dimen.miuix_bottom_menu_padding_top),
                0, getResources().getDimensionPixelSize(R.dimen.miuix_bottom_menu_padding_bottom));

            setGravity(CENTER);
            setOrientation(VERTICAL);

            icon = new ImageView(getContext());
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            title = new TextView(getContext());
            title.setGravity(CENTER);
            title.setSingleLine();
            title.setTextColor(getResources().getColor(R.color.miuix_title_color));

            addView(icon);
            addView(title);

            LayoutParams iconParams = (LayoutParams) icon.getLayoutParams();
            iconParams.height = getResources().getDimensionPixelSize(R.dimen.miuix_bottom_menu_icon_size);
            icon.setLayoutParams(iconParams);
            title.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // icon.setLayoutParams(params);
            // title.setLayoutParams(params);
        }

        public void setIcon(Drawable icon) {
            this.icon.setImageDrawable(icon);
        }

        public void setIconSize(int size) {
            LinearLayout.LayoutParams params = (LayoutParams) icon.getLayoutParams();
            params.height = size;
            params.weight = size;
            icon.setLayoutParams(params);
        }

        public void setTitle(CharSequence title) {
            this.title.setText(title);
        }

        public void setTitleSize(float size) {
            title.setTextSize(size);
        }

        public void setChecked(boolean checked) {
            if (checkedId != getId()) {
                isChecked = checked;
                updateSelectedState();
                if (isChecked) {
                    checkedId = getId();
                    repel();
                }
            }
        }

        private void setInfo(MenuInfo info) {
            this.info = info;
        }

        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean performClick() {
            HapticFeedbackHelper.performHapticFeedback(this, HapticFeedbackHelper.MIUI_BUTTON_MIDDLE);
            if (checkedId == getId()) return super.performClick();
            if (onNavigationItemSelected(info, true)) {
                setChecked(!isChecked());
            }
            return super.performClick();
        }

        private void updateSelectedState() {
            icon.setSelected(isChecked);
        }

        private void repel() {
            for (MenuInfo info : subMenuInfos) {
                if (info.id == checkedId) continue;
                if (info.singleView != null) info.setChecked(false);
            }
        }
    }
}
