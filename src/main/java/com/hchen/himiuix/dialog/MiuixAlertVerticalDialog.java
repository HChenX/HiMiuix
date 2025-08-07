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
package com.hchen.himiuix.dialog;

import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.hchen.himiuix.R;
import com.hchen.himiuix.utils.MiuixUtils;

/**
 * Miuix Vertical Dialog
 *
 * @author 焕晨HChen
 */
class MiuixAlertVerticalDialog extends MiuixAlertDialogBase {
    MiuixAlertVerticalDialog(@NonNull Dialog dialog) {
        super(dialog);
    }

    @Override
    void inflater() {
        mainLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.miuix_vertical_dialog, null);
    }

    @Override
    void initWindow() {
        window.setContentView(mainLayout);

        if (MiuixUtils.isPad(context)) {
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (point.x / 2.5);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        } else {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams params = window.getAttributes();
            params.verticalMargin = (float) resources.getDimensionPixelSize(R.dimen.miuix_dialog_vertical_margin) / point.y;
            params.width = (int) (point.x / 1.07);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }

        window.setWindowAnimations(R.style.MiuixDialogAnimation);
    }

    @Override
    void loadView() {
        super.loadView();

        if (buttonArray.isEmpty()) return;
        LinearLayout buttons;
        addView(buttonsLayout,
            buttons = (LinearLayout) (buttonArray.size() >= 3 ?
                LayoutInflater.from(context).inflate(R.layout.miuix_dialog_button_vertical_3, null) :
                LayoutInflater.from(context).inflate(R.layout.miuix_dialog_button_horizontal_2, null))
        );

        for (int i = 0; i < buttons.getChildCount(); i++) {
            switch (i) {
                case 0 -> {
                    button1 = new ButtonInfo(
                        (Button) buttons.getChildAt(0),
                        buttonArray.get(0).key(),
                        buttonArray.get(0).text(),
                        buttonArray.get(0).listener()
                    );
                }
                case 1 -> {
                    button2 = buttonArray.size() > 1 ?
                        new ButtonInfo(
                            (Button) buttons.getChildAt(1),
                            buttonArray.get(1).key(),
                            buttonArray.get(1).text(),
                            buttonArray.get(1).listener()
                        ) :
                        new ButtonInfo(
                            (Button) buttons.getChildAt(1),
                            BUTTON_NONE, null, null
                        );
                }
                case 2 -> {
                    button3 = buttonArray.size() > 2 ?
                        new ButtonInfo(
                            (Button) buttons.getChildAt(2),
                            buttonArray.get(2).key(),
                            buttonArray.get(2).text(),
                            buttonArray.get(2).listener()
                        ) :
                        new ButtonInfo(
                            (Button) buttons.getChildAt(2),
                            BUTTON_NONE, null, null
                        );
                }
            }
        }
    }

    @Override
    void updateContent() {
        super.updateContent();

        if (button1 != null && button1.key() != BUTTON_NONE) {
            button1.button().setText(button1.text());
            button1.button().setOnClickListener(createButtonClickListener(button1.key(), button1.listener()));
        }
        if (button2 != null && button2.key() != BUTTON_NONE) {
            button2.button().setText(button2.text());
            button2.button().setOnClickListener(createButtonClickListener(button2.key(), button2.listener()));
        }
        if (button3 != null && button3.key() != BUTTON_NONE) {
            button3.button().setText(button3.text());
            button3.button().setOnClickListener(createButtonClickListener(button3.key(), button3.listener()));
        }
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();

        if (button1 != null && button1.key() != BUTTON_NONE) {
            button1.button().setVisibility(VISIBLE);
            updateButtonStyle(button1.key(), button1.button());
        }
        if (button2 != null && button2.key() != BUTTON_NONE) {
            button2.button().setVisibility(VISIBLE);
            updateButtonStyle(button2.key(), button2.button());
        }
        if (button3 != null && button3.key() != BUTTON_NONE) {
            button3.button().setVisibility(VISIBLE);
            updateButtonStyle(button3.key(), button3.button());
        }
    }

    @Override
    void updateLocation() {
        super.updateLocation();

        if (buttonArray.size() >= 3) {
            assert button2 != null;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button2.button().getLayoutParams();
            params.topMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_button_margin);
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_button_margin);
            button2.button().setLayoutParams(params);
        } else if (buttonArray.size() == 2) {
            assert button1 != null;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button1.button().getLayoutParams();
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_button_margin);
            button1.button().setLayoutParams(params);
        }
    }

    private void updateButtonStyle(int key, Button button) {
        if (key == BUTTON_POSITIVE) {
            button.setBackgroundResource(R.drawable.miuix_dialog_button_2);
            button.setTextColor(resources.getColor(R.color.miuix_dialog_button_text_2));
        } else {
            button.setBackgroundResource(R.drawable.miuix_dialog_button_1);
            button.setTextColor(resources.getColor(R.color.miuix_dialog_button_text_1));
        }
    }
}
