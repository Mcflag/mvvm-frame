package cn.lcsw.diningpos.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.lcsw.diningpos.R;


/**
 * Created by zxk on 2018/7/2.
 * Description: 密码输入框
 */
public class PasswordView extends View {

    //密码长度
    private int passwordLength;
    private int passwordTextSize;
    //密码颜色
    private int passwordColor;
    //密码背景色
    private int passwordBgColor;
    //密码框圆角半径 占 密码框宽度 的比例
    private float passwordRadius;
    //相邻密码框间隔 占 密码框宽度 的比例
    private float spaceWidth;
    //整体背景色
    private int bgColor;
    //密码明文 变 星号 的延迟时间
    private int delayTime;
    //密码明文 变 星号 的延迟时间
    private boolean isEnterByKeybard;
    //存放密码
    private List<Integer> password = new ArrayList<>();
    //输入法管理
    private InputMethodManager input;
    //单个密码框的宽度(正方形);
    private int borderWidth;
    //画的字所在的rect
    private Rect mBounds;
    //是否是延迟delaytime后的刷新
    private boolean isInvalidate;
    //按删除键还是新输入的
    private boolean isDelete;
    private MyHandler handler = new MyHandler(this);

    private Paint mPaint;
    private boolean isKeyboardShow = false;

    public PasswordView(Context context) {
        this(context, null);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBounds = new Rect();
        isInvalidate = false;
        input = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        this.setOnKeyListener(new NumberKeyListener());
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasswordView);
        passwordLength = typedArray.getInt(R.styleable.PasswordView_passwordLength, 6);
        passwordTextSize = (int) typedArray.getDimension(R.styleable.PasswordView_passwordTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));
        passwordColor = typedArray.getColor(R.styleable.PasswordView_passwordColor, Color.parseColor("#1EA7FF"));
        passwordBgColor = typedArray.getColor(R.styleable.PasswordView_passwordBgColor, Color.parseColor("#DFDFDF"));
        passwordRadius = typedArray.getFloat(R.styleable.PasswordView_passwordRadius, 0.17f);
        spaceWidth = typedArray.getFloat(R.styleable.PasswordView_spaceWidth, 0.25f);
        bgColor = typedArray.getColor(R.styleable.PasswordView_bgColor, Color.parseColor("#FFFFFF"));
        delayTime = typedArray.getInteger(R.styleable.PasswordView_delayTime, 1000);
        isEnterByKeybard = typedArray.getBoolean(R.styleable.PasswordView_keyboard, true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        typedArray.recycle();

    }

    /**
     * 目前只兼容 宽度exactly 高度at_most
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.AT_MOST) {
            borderWidth = (int) ((widthSpecSize - getPaddingLeft() - getPaddingRight()) / (passwordLength + (passwordLength - 1) * spaceWidth));
            heightSpecSize = borderWidth + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (passwordTextSize == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics())) {
            passwordTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, borderWidth / 7, getResources().getDisplayMetrics());
        }
        mPaint.setTextSize(passwordTextSize);

        //画背景色
        mPaint.setColor(bgColor);
        canvas.drawRect(0, 0, getMeasuredWidth(), getPaddingTop() + borderWidth + getPaddingBottom(), mPaint);

        //画密码框
        drawBorder(canvas);

        //画密码
        drawPassword(canvas);

        if (!isKeyboardShow) {
            showKeyBoard();
        }
    }

    /**
     * 画密码框
     *
     * @param canvas
     */
    private void drawBorder(Canvas canvas) {
        mPaint.setColor(passwordBgColor);

        for (int i = 0; i < passwordLength; i++) {
            int left = (int) (i * (1 + spaceWidth) * borderWidth) + getPaddingLeft();
            int right = left + borderWidth;
            int top = getPaddingTop();
            int bottom = getPaddingTop() + borderWidth;

            RectF border = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(border, passwordRadius * borderWidth, passwordRadius * borderWidth, mPaint);
        }
    }

    /**
     * 画密码
     *
     * @param canvas
     */
    private void drawPassword(Canvas canvas) {
        if (isInvalidate) {
            drawCipherText(canvas);
            return;
        }

        drawText(canvas);
    }

    /**
     * 画密文
     *
     * @param canvas
     */
    private void drawCipherText(Canvas canvas) {
        isInvalidate = false;
        for (int i = 0; i < password.size(); i++) {
            drawStar(canvas, i);
        }
        handler.removeMessages(0x111);
    }

    /**
     * 最后一位是数字 前面的是*
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        for (int i = 0; i < password.size(); i++) {
            //新输入的 && 是最后一位 ---> 是数字
            if (!isDelete && i == password.size() - 1) {
                String num = password.get(i) + "";
                drawPlainText(canvas, num, i);
                handler.sendEmptyMessageDelayed(0x111, delayTime);
            } else {
                drawStar(canvas, i);
            }
        }
    }

    /**
     * 画明文
     *
     * @param canvas
     * @param num
     * @param i
     */
    private void drawPlainText(Canvas canvas, String num, int i) {
        mPaint.getTextBounds(num, 0, num.length(), mBounds);
        mPaint.setColor(passwordColor);
        canvas.drawText(num, i * (1 + spaceWidth) * borderWidth + getPaddingLeft() + borderWidth / 2 - mBounds.width() / 2, getPaddingTop() + borderWidth - (borderWidth / 2 - mBounds.height() / 2), mPaint);

    }

    /**
     * 画星号
     *
     * @param canvas
     * @param i
     */
    private void drawStar(Canvas canvas, int i) {
        mPaint.setColor(passwordColor);
        Bitmap ciphertext = BitmapFactory.decodeResource(getResources(), R.drawable.ciphertext);
        Rect src = new Rect(0, 0, ciphertext.getWidth(), ciphertext.getHeight());
        RectF dest = new RectF(getPaddingLeft() + i * (1 + spaceWidth) * borderWidth + borderWidth / 2 - ciphertext.getWidth() / 2, getPaddingTop() + borderWidth / 2 - ciphertext.getHeight() / 2, getPaddingLeft() + i * (1 + spaceWidth) * borderWidth + borderWidth / 2 - ciphertext.getWidth() / 2 + ciphertext.getWidth(), getPaddingTop() + borderWidth / 2 - ciphertext.getHeight() / 2 + ciphertext.getHeight());
        canvas.drawBitmap(ciphertext, src, dest, mPaint);
    }

    /**
     * delayTime后刷新密码输入框
     */
    private void refreshView() {
        isInvalidate = true;
        invalidate();
    }

    /**
     * 获取密码
     *
     * @return
     */
    public String getPassword() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i : password) {
            stringBuffer.append(i);
        }
        return stringBuffer.toString();
    }

    public void clearPassword() {
        if (password.size() > 0) {
            password.clear();
        }
        invalidate();
    }

    public void addPassWord(String s) {
        password.clear();
        for (int i = 0; i < s.length(); i++) {
            password.add(Integer.parseInt(String.valueOf(s.charAt(i))));
        }
        refreshView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            hideKeyBoard();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//点击弹出键盘
            showKeyBoard();
            return true;
        }
        return super.onTouchEvent(event);
    }

    //显示键盘
    public void showKeyBoard() {

        if (!isEnterByKeybard) {
            return;
        }

        requestFocus();
        if (input != null) {
            isKeyboardShow = true;
            input.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }
    }

    //隐藏键盘
    public void hideKeyBoard() {
        if (input != null) {
            isKeyboardShow = false;
            input.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//只允许输入数字
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new NumInputConnection(this, false);
    }

    class NumInputConnection extends BaseInputConnection {

        public NumInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            //这里是接收文本的输入法，我们只允许输入数字，则不做任何处理
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            //屏蔽返回键，发送自己的删除事件
            if (beforeLength == 1 && afterLength == 0) {
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    static class MyHandler extends Handler {
        private WeakReference<PasswordView> views;

        public MyHandler(PasswordView view) {
            views = new WeakReference<PasswordView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x111:
                    PasswordView passwordView = views.get();
                    if (passwordView != null) {
                        passwordView.refreshView();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class NumberKeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.isShiftPressed()) {//处理*#等键
                    return false;
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {//只处理数字
                    if (password.size() < passwordLength) {
                        isDelete = false;
                        password.add(keyCode - 7);
                        handler.removeMessages(0x111);
                        invalidate();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!password.isEmpty()) {//不为空时，删除最后一个数字
                        isDelete = true;
                        password.remove(password.size() - 1);
                        handler.removeMessages(0x111);
                        invalidate();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    OnTextChangeListener onTextChangeListener;

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

}
