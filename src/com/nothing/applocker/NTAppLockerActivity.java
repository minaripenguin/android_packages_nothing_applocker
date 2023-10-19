package com.nothing.applocker;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.nothing.applocker.databinding.ActivityNtApplockerBinding;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class NTAppLockerActivity extends AppCompatActivity {
    private static final String NT_APP_LOCKER_BLOCKING_UID = "LOCKED_UID";
    private static final String NT_APP_LOCKER_COMPONENT = "LOCKED_COMPONENT";
    private static final String NT_APP_LOCKER_PACKAGE = "LOCKED_PACKAGE";
    private static final int UI_ANIMATION_DELAY = 300;
    private ActivityNtApplockerBinding binding;
    private BiometricPrompt mBiometricPrompt;
    private String mComponent;
    private TextView mContentView;
    private View mControlsView;
    private Executor mExecutor;
    private ImageView mPipLockIcon;
    private String mPkg;
    private BiometricPrompt.PromptInfo mPromptInfo;
    private int mUID;
    private int mUserId;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            NTAppLockerActivity.this.mContentView.getWindowInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            NTAppLockerActivity.this.hide();
        }
    };
    private String TAG = "NTAppLockerActivity";
    private boolean mIsAuthing = false;
    private boolean mIsErrorHappened = false;
    private boolean mDelayAuthAfterFocused = false;
    private boolean mDelayAuthAfterPortrait = false;
    private long mFailedTime = 0;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d(this.TAG, "onCreate");
        final Intent intent = new Intent();
        ApplicationInfo applicationInfo = null;
        if (getIntent() != null) {
            int intExtra = getIntent().getIntExtra(NT_APP_LOCKER_BLOCKING_UID, 0);
            this.mUID = intExtra;
            this.mUserId = intExtra;
            try {
                Method declaredMethod = Class.forName("android.os.UserHandle").getDeclaredMethod("getUserId", Integer.TYPE);
                declaredMethod.setAccessible(true);
                this.mUserId = ((Integer) declaredMethod.invoke(null, Integer.valueOf(this.mUID))).intValue();
            } catch (Exception e) {
                Log.w(this.TAG, "getUserId: failed ", e);
            }
            intent.putExtra(NT_APP_LOCKER_BLOCKING_UID, this.mUID);
            String stringExtra = getIntent().getStringExtra(NT_APP_LOCKER_PACKAGE);
            this.mPkg = stringExtra;
            intent.putExtra(NT_APP_LOCKER_PACKAGE, stringExtra);
            String stringExtra2 = getIntent().getStringExtra(NT_APP_LOCKER_COMPONENT);
            this.mComponent = stringExtra2;
            intent.putExtra(NT_APP_LOCKER_COMPONENT, stringExtra2);
        }
        ActivityNtApplockerBinding inflate = ActivityNtApplockerBinding.inflate(getLayoutInflater());
        this.binding = inflate;
        setContentView(inflate.getRoot());
        this.mControlsView = this.binding.fullscreenContentControls;
        this.mContentView = this.binding.fullscreenContent;
        this.mPipLockIcon = this.binding.pipLockIcon;
        this.mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NTAppLockerActivity.this.mIsAuthing) {
                    return;
                }
                NTAppLockerActivity.this.startAuthenticate("mContentView onClick");
            }
        });
        this.mExecutor = ContextCompat.getMainExecutor(this);
        this.mBiometricPrompt = new BiometricPrompt(this, this.mExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int i, CharSequence charSequence) {
                super.onAuthenticationError(i, charSequence);
                NTAppLockerActivity.this.mIsErrorHappened = true;
                Log.d(NTAppLockerActivity.this.TAG, "onAuthenticationError " + i + " errString " + ((Object) charSequence));
                NTAppLockerActivity.this.updateAuthStatus(false, "onAuthenticationError");
                NTAppLockerActivity.this.mFailedTime = SystemClock.elapsedRealtime();
                NTAppLockerActivity.this.setResult(0, intent);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult authenticationResult) {
                super.onAuthenticationSucceeded(authenticationResult);
                Log.d(NTAppLockerActivity.this.TAG, "onAuthenticationSucceeded");
                NTAppLockerActivity.this.updateAuthStatus(false, "onAuthenticationSucceeded");
                NTAppLockerActivity.this.setResult(-1, intent);
                NTAppLockerActivity.this.finishActivity("onAuthenticationSucceeded");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(NTAppLockerActivity.this.TAG, "onAuthenticationFailed");
            }
        });
        PackageManager packageManager = getPackageManager();
        try {
            applicationInfo = (ApplicationInfo) PackageManager.class.getMethod("getApplicationInfoAsUser", String.class, Integer.TYPE, Integer.TYPE).invoke(packageManager, this.mPkg, 0, Integer.valueOf(this.mUserId));
        } catch (Exception unused) {
        }
        String str = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : this.mPkg);
        Log.d(this.TAG, "appName " + str + " mUserId " + this.mUserId + " mPkg " + this.mPkg);
        this.mPromptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.verify_dialog_title)).setSubtitle(getString(R.string.verify_dialog_summary, new Object[]{str})).setAllowedAuthenticators(32783).build();
    }

    public void startAuthenticate(String str) {
        boolean hasWindowFocus = hasWindowFocus();
        Log.d(this.TAG, "startAuthenticate hasFocus " + hasWindowFocus + " reason " + str);
        if (!hasWindowFocus) {
            this.mDelayAuthAfterFocused = true;
        } else if (this.mBiometricPrompt != null) {
            this.mIsErrorHappened = false;
            updateAuthStatus(true, "startAuthenticate");
            this.mBiometricPrompt.authenticate(this.mPromptInfo);
        }
    }

    private void stopAuthenticate() {
        if (this.mBiometricPrompt != null) {
            updateAuthStatus(false, "stopAuthenticate");
            this.mBiometricPrompt.cancelAuthentication();
        }
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        delayedHide(100);
    }

    @Override
    public void onResume() {
        super.onResume();
        setRequestedOrientation(1);
        if (isAppLocked(this.mPkg, this.mUID)) {
            startAuthenticate("onResume");
        } else {
            finishActivity("App not locked");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(this.TAG, "onPause");
        stopAuthenticate();
        this.mIsErrorHappened = false;
    }

    public void hide() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        this.mControlsView.setVisibility(8);
    }

    @Override
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        long elapsedRealtime = SystemClock.elapsedRealtime() - this.mFailedTime;
        boolean isAppInMultiWindowMode = WindowModeUtil.isAppInMultiWindowMode();
        boolean isAppInPinnedWindowWindowMode = WindowModeUtil.isAppInPinnedWindowWindowMode();
        boolean isAppInWindowformWindowMode = WindowModeUtil.isAppInWindowformWindowMode();
        Log.d(this.TAG, "onWindowFocusChanged " + z + " mIsAuthing " + this.mIsAuthing + " isNTInMultiWindowMode " + isAppInMultiWindowMode + " isInPictureInPictureMode " + isAppInPinnedWindowWindowMode + " isWindowformWindowMode " + isAppInWindowformWindowMode + " mIsErrorHappened " + this.mIsErrorHappened + " mDelayAuthAfterFocused " + this.mDelayAuthAfterFocused + " time " + elapsedRealtime);
        updateLockIcon(isAppInMultiWindowMode, isAppInPinnedWindowWindowMode, isAppInWindowformWindowMode);
        if (z && (!this.mIsAuthing)) {
            if (this.mDelayAuthAfterFocused) {
                this.mDelayAuthAfterFocused = false;
                startAuthenticate("onWindowFocusChanged-DelayAuth");
            } else if (elapsedRealtime > 280 && isAppInMultiWindowMode) {
                startAuthenticate("onWindowFocusChanged-inMultiWindowMode");
            } else if (this.mIsErrorHappened) {
                this.mIsErrorHappened = false;
                if (isAppInMultiWindowMode || isAppInPinnedWindowWindowMode || isAppInWindowformWindowMode) {
                    return;
                }
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                intent.setFlags(268435456);
                startActivity(intent);
                finishActivity("onWindowFocusChanged-ErrorHappened");
            }
        }
    }

    private void updateLockIcon(boolean z, boolean z2, boolean z3) {
        ImageView imageView = this.mPipLockIcon;
        if (imageView != null) {
            if (z2) {
                imageView.setScaleX(2.45f);
                this.mPipLockIcon.setScaleY(2.45f);
            } else if (z3) {
                imageView.setScaleX(1.36f);
                this.mPipLockIcon.setScaleY(1.36f);
            } else {
                imageView.setScaleX(1.0f);
                this.mPipLockIcon.setScaleY(1.0f);
            }
            this.mPipLockIcon.setVisibility(0);
        }
    }

    private void delayedHide(int i) {
        this.mHideHandler.removeCallbacks(this.mHideRunnable);
        this.mHideHandler.postDelayed(this.mHideRunnable, i);
    }

    public void finishActivity(String str) {
        Log.d(this.TAG, "finishActivity " + str);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(this.TAG, "onBackPressed");
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(268435456);
        startActivity(intent);
        super.onBackPressed();
    }

    public void updateAuthStatus(boolean z, String str) {
        Log.d(this.TAG, "updateAuthStatus " + z + " - " + str);
        this.mIsAuthing = z;
    }

    private boolean isAppLocked(String str, int i) {
        try {
            if (TextUtils.isEmpty(str)) {
                Log.d(this.TAG, "isAppLocked: packageName is empty");
                return false;
            }
            Class<?> cls = Class.forName("android.view.NtWindowManager");
            Method declaredMethod = cls.getDeclaredMethod("getInstance", new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null, new Object[0]);
            Method declaredMethod2 = cls.getDeclaredMethod("isAppLocked", String.class, Integer.TYPE);
            declaredMethod2.setAccessible(true);
            boolean booleanValue = ((Boolean) declaredMethod2.invoke(invoke, str, Integer.valueOf(i))).booleanValue();
            Log.d(this.TAG, "isAppLocked: " + booleanValue + " packageName " + str + " uid " + i);
            return booleanValue;
        } catch (Exception e) {
            Log.w(this.TAG, "isAppLocked: failed ", e);
            return false;
        }
    }

    private boolean isPortrait() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        return rotation == 0 || rotation == 2;
    }
}
