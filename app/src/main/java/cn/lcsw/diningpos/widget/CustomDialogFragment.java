package cn.lcsw.diningpos.widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import cn.lcsw.diningpos.R;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.TimeUnit;

public class CustomDialogFragment extends BaseDialogFragment {

    public static final String TITLE = "title";
    public static final String SUBTITLE = "subTitle";
    public static final String CONTENT_TEXT = "ContentText";
    public static final String PROGRESSBAR = "progressbar";
    public static final String EDIT_TEXT = "EDIT_TEXT";
    public static final String IMG = "img";
    public static final String CANCEL_ACTION = "cancel_action";
    public static final String CANCEL_TIME = "cancel_count_down_time";
    public static final String OK_ACTION = "ok_action";
    public static final String NETUAL_ACTION = "netual_action";
    public static final String NETUAL_TIME = "netual_count_down_time";
    public static final int NETUAL_TYPE_POSITIVE = 1;
    public static final int NETUAL_TYPE_NEGATIVE = 0;

    final static int COUNTS = 5;//点击次数
    final static long DURATION = 2 * 1000;//规定有效时间
    long[] mHits = new long[COUNTS];

    public TextView dialog_title;
    public LinearLayout dialog_content;
    public TextView dialog_sub_title;
    public TextView dialog_content_text;
    public ImageView dialog_img;
    public ProgressBar dialog_progressbar;
    public EditText dialog_edit_text;
    public TextView dialog_cancel_action;
    public TextView dialog_ok_action;
    public TextView dialog_netual_action;
    public View dialog_line1;
    public View dialog_line2;
    public LinearLayout dialog_action;
    public View go_to_setting;

    private Bundle mBundle = new Bundle();
    public NoticeDialogListener mListener;
    private Disposable mDisposable;
    private int netualType = NETUAL_TYPE_POSITIVE;

    public CustomDialogFragment create() {
        CustomDialogFragment dialogFragment = new CustomDialogFragment();
        dialogFragment.setArguments(mBundle);
        return dialogFragment;
    }

    public interface NoticeDialogListener {

        void onDialogPositiveClick(CustomDialogFragment dialog);

        void onDialogNegativeClick(CustomDialogFragment dialog);

        void onDialogNetualClick(CustomDialogFragment dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.LoadingTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.getDecorView().setPadding(200, 0, 200, 0);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = 17;
            window.setAttributes(params);
        }
    }

    @SuppressLint("AutoDispose")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = inflater.inflate(R.layout.dialog_layout, container, false);
        dialog_title = v.findViewById(R.id.dialog_title);
        dialog_sub_title = v.findViewById(R.id.dialog_sub_title);
        dialog_content_text = v.findViewById(R.id.dialog_content_text);
        dialog_progressbar = v.findViewById(R.id.dialog_progressbar);
        dialog_edit_text = v.findViewById(R.id.dialog_edit_text);
        dialog_cancel_action = v.findViewById(R.id.dialog_cancel_action);
        dialog_ok_action = v.findViewById(R.id.dialog_ok_action);
        dialog_netual_action = v.findViewById(R.id.dialog_netual_action);
        dialog_line2 = v.findViewById(R.id.dialog_line2);
        dialog_line1 = v.findViewById(R.id.dialog_line1);
        dialog_img = v.findViewById(R.id.dialog_img);
        dialog_action = v.findViewById(R.id.dialog_action);
        go_to_setting = v.findViewById(R.id.go_to_setting);

        go_to_setting.setOnClickListener(v1 -> {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });

        if (mBundle.getString(TITLE) != null) {
            setDialogTitle(mBundle.getString(TITLE));
        }

        if (mBundle.getString(SUBTITLE) != null) {
            setDialogSubTitle(mBundle.getString(SUBTITLE));
        }

        if (mBundle.getString(CONTENT_TEXT) != null) {
            setDialogContentText(mBundle.getString(CONTENT_TEXT));
        }

        if (mBundle.getInt(IMG) != 0) {
            setDialogImgSource(mBundle.getInt(IMG));
        }

        if (mBundle.getBoolean(PROGRESSBAR)) {
            setDialogProgressbar(mBundle.getBoolean(PROGRESSBAR));
        }

        if (mBundle.getString(EDIT_TEXT) != null) {
            setEditText(mBundle.getString(EDIT_TEXT));
        }

        if (mBundle.getString(OK_ACTION) != null) {
            setDialogPositiveAction(mBundle.getString(OK_ACTION));
        }

        final String cancelText = mBundle.getString(CANCEL_ACTION);
        if (cancelText != null) {
            setDialogNegativeAction(mBundle.getString(CANCEL_ACTION));
        }

        final int negCountDownTime = mBundle.getInt(CANCEL_TIME, 0);
        if (negCountDownTime > 0) {
            Observable.interval(1, TimeUnit.SECONDS).take(negCountDownTime)
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mDisposable = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            setDialogNegativeAction(cancelText + "(" + (negCountDownTime - aLong) + "s)");
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (mListener != null) {
                                mListener.onDialogNegativeClick(CustomDialogFragment.this);
                            }
                        }
                    });
        }

        switch (netualType) {
            case NETUAL_TYPE_NEGATIVE:
                dialog_netual_action.setBackgroundResource(R.drawable.selector_negative_btn);
                dialog_netual_action.setTextColor(getResources().getColor(R.color.color_33));
                break;
            case NETUAL_TYPE_POSITIVE:
                dialog_netual_action.setBackgroundResource(R.drawable.shape_round_square_blue);
                dialog_netual_action.setTextColor(getResources().getColor(R.color.white));
                break;
            default:
                break;
        }

        final String netualText = mBundle.getString(NETUAL_ACTION);
        if (netualText != null) {
            setDialogNetualAction(mBundle.getString(NETUAL_ACTION));
        }

        final int netualCountDownTime = mBundle.getInt(NETUAL_TIME, 0);
        if (netualCountDownTime > 0) {
            Observable.interval(1, TimeUnit.SECONDS).take(netualCountDownTime)
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mDisposable = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            setDialogNetualAction(netualText + "(" + (netualCountDownTime - aLong) + "s)");
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (mListener != null) {
                                mListener.onDialogNetualClick(CustomDialogFragment.this);
                            }
                        }
                    });
        }

        return v;
    }

//    public CustomDialogFragment setScannerListener(DefaultScanSuccessListener listener) {
//        scanListener = listener;
//        return this;
//    }

    public CustomDialogFragment setNoticeDialogListener(NoticeDialogListener listener) {
        this.mListener = listener;
        return this;
    }

    public CustomDialogFragment putTitle(String title) {
        mBundle.putString(TITLE, title);
        return this;
    }

    public CustomDialogFragment putSubTitle(String subTitle) {
        mBundle.putString(SUBTITLE, subTitle);
        return this;
    }


    public CustomDialogFragment putContentText(String ContentText) {
        mBundle.putString(CONTENT_TEXT, ContentText);
        return this;
    }

    public CustomDialogFragment putImgSource(@DrawableRes int resId) {
        mBundle.putInt(IMG, resId);
        return this;
    }

    public CustomDialogFragment putProgressbar(boolean show) {
        mBundle.putBoolean(PROGRESSBAR, show);
        return this;
    }

    public CustomDialogFragment putEditText(String hint) {
        mBundle.putString(EDIT_TEXT, hint);
        return this;
    }

    public CustomDialogFragment putPositiveText(String text) {
        mBundle.putString(OK_ACTION, text);
        return this;
    }

    public CustomDialogFragment putNegativeText(String text) {
        mBundle.putString(CANCEL_ACTION, text);
        return this;
    }

    public CustomDialogFragment putNegativeBtnCountdownTime(int countdownTime) {
        mBundle.putInt(CANCEL_TIME, countdownTime);
        return this;
    }

    public CustomDialogFragment putNetualText(String text) {
        mBundle.putString(NETUAL_ACTION, text);
        return this;
    }

    public CustomDialogFragment putNetualType(int type) {
        netualType = type;
        return this;
    }

    public CustomDialogFragment putNetualBtnCountdownTime(int countdownTime) {
        mBundle.putInt(NETUAL_TIME, countdownTime);
        return this;
    }


    public CustomDialogFragment changeType() {
        dialog_title.setVisibility(View.GONE);
        dialog_sub_title.setVisibility(View.GONE);
        dialog_content_text.setVisibility(View.GONE);
        dialog_img.setVisibility(View.GONE);
        dialog_progressbar.setVisibility(View.GONE);
        dialog_cancel_action.setVisibility(View.GONE);
        dialog_line2.setVisibility(View.GONE);
        dialog_line1.setVisibility(View.GONE);
        dialog_action.setVisibility(View.GONE);
        return this;
    }

    private CustomDialogFragment setDialogTitle(String title) {
        dialog_title.setText(title);
        dialog_title.setVisibility(View.VISIBLE);
        return this;
    }

    private CustomDialogFragment setDialogSubTitle(String title) {
        dialog_sub_title.setText(title);
        dialog_sub_title.setVisibility(View.VISIBLE);
        return this;
    }

    private CustomDialogFragment setDialogContentText(String content_text) {
        dialog_content_text.setVisibility(View.VISIBLE);
        dialog_content_text.setText(content_text);
        return this;
    }

    private CustomDialogFragment setDialogImgSource(@DrawableRes int resId) {
        dialog_img.setVisibility(View.VISIBLE);
        dialog_img.setImageResource(resId);
        return this;
    }

    private CustomDialogFragment setDialogProgressbar(boolean show) {
        dialog_progressbar.setVisibility(View.VISIBLE);
        dialog_progressbar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.colorAccent),
                        android.graphics.PorterDuff.Mode.MULTIPLY);
        return this;
    }

    private CustomDialogFragment setEditText(String hint) {
        dialog_edit_text.setHint(hint);
        dialog_edit_text.setVisibility(View.VISIBLE);
        return this;
    }

    private CustomDialogFragment setDialogNegativeAction(String text) {
        dialog_cancel_action.setVisibility(View.VISIBLE);
        dialog_cancel_action.setText(text);
        dialog_cancel_action.setOnClickListener(v -> mListener.onDialogNegativeClick(CustomDialogFragment.this));
        return this;
    }


    private CustomDialogFragment setDialogPositiveAction(String text) {
        dialog_action.setVisibility(View.VISIBLE);
        dialog_ok_action.setVisibility(View.VISIBLE);
        dialog_ok_action.setText(text);
        dialog_ok_action.setOnClickListener(v -> mListener.onDialogPositiveClick(CustomDialogFragment.this));
        return this;
    }

    private CustomDialogFragment setDialogNetualAction(String text) {
        dialog_action.setVisibility(View.VISIBLE);
        dialog_netual_action.setVisibility(View.VISIBLE);
        dialog_netual_action.setText(text);
        dialog_netual_action.setOnClickListener(v -> mListener.onDialogNetualClick(CustomDialogFragment.this));
        return this;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
