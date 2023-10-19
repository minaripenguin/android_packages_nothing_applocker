package com.nothing.applocker.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.nothing.applocker.R;

public final class ActivityNtApplockerBinding implements ViewBinding {
    public final TextView fullscreenContent;
    public final LinearLayout fullscreenContentControls;
    public final ImageView pipLockIcon;
    private final FrameLayout rootView;

    private ActivityNtApplockerBinding(FrameLayout frameLayout, TextView textView, LinearLayout linearLayout, ImageView imageView) {
        this.rootView = frameLayout;
        this.fullscreenContent = textView;
        this.fullscreenContentControls = linearLayout;
        this.pipLockIcon = imageView;
    }

    @Override
    public FrameLayout getRoot() {
        return this.rootView;
    }

    public static ActivityNtApplockerBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityNtApplockerBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_nt_applocker, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityNtApplockerBinding bind(View view) {
        int i = R.id.fullscreen_content;
        TextView textView = (TextView) ViewBindings.findChildViewById(view, R.id.fullscreen_content);
        if (textView != null) {
            i = R.id.fullscreen_content_controls;
            LinearLayout linearLayout = (LinearLayout) ViewBindings.findChildViewById(view, R.id.fullscreen_content_controls);
            if (linearLayout != null) {
                i = R.id.pip_lock_icon;
                ImageView imageView = (ImageView) ViewBindings.findChildViewById(view, R.id.pip_lock_icon);
                if (imageView != null) {
                    return new ActivityNtApplockerBinding((FrameLayout) view, textView, linearLayout, imageView);
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
