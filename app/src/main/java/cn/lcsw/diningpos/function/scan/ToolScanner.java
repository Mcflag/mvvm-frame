package cn.lcsw.diningpos.function.scan;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import java.util.regex.Pattern;

public class ToolScanner {

    public static final String TAG = ToolScanner.class.getSimpleName();

    private final static long ENTER_MESSAGE_DELAY = 0;             //无延迟，判断扫码是否完成。
    private final static long MESSAGE_DELAY = 500;             //延迟500毫秒，如无输入判断扫码完成。
    private StringBuffer mStringBufferResult;                  //扫码内容
    private String nowKeyName = "";
    private boolean mCaps;             //大小写区分
    private boolean isNoEnterEnable = false;          //默认关闭
    private boolean isMoneyInput = false;
    private boolean isNumberInput = false;
    private boolean isAiboshi = false;
    private String log = "";
    private final Handler mHandler;
    private final Runnable mScanningFinishRunnable;
    private final Runnable mScanningStartRunnable;
    private final Runnable mTypingRunnable;
    private final Runnable mTypingTestRunnable;
    private final Runnable mF1Runnable;
    private final Runnable mF2Runnable;
    private final Runnable mF3Runnable;
    private final Runnable mTabRunnable;
    private final Runnable mDelRunnable;
    private final Runnable mMenuRunnable;
    private final Runnable mRightRunnable;
    private final Runnable mMultiRunnable;
    private final Runnable mBackRunnable;
    private final Runnable mUpRunnable;
    private final Runnable mDownRunnable;
    private OnScanSuccessListener mOnScanSuccessListener;

    public ToolScanner() {
        mOnScanSuccessListener = new DefaultScanSuccessListener();
        mStringBufferResult = new StringBuffer();
        mHandler = new Handler();
        mScanningFinishRunnable = this::performScanSuccess;
        mScanningStartRunnable = this::performScanStart;
        mTypingRunnable = this::performTyping;
        mTypingTestRunnable = this::performTypingTest;
        mF1Runnable = this::performF1Press;
        mF2Runnable = this::performF2Press;
        mF3Runnable = this::performF3Press;
        mTabRunnable = this::performTabPress;
        mDelRunnable = this::performDelPress;
        mMenuRunnable = this::performMenuPress;
        mBackRunnable = this::performBackPress;
        mRightRunnable = this::performRightPress;
        mMultiRunnable = this::performMultiPress;
        mUpRunnable = this::performUpPress;
        mDownRunnable = this::performDownPress;
    }

    public void setMoneyInput(boolean flag) {
        isMoneyInput = flag;
    }

    public void setNumberInput(boolean flag) {
        isNumberInput = flag;
    }

    public void clear(){
        mStringBufferResult.setLength(0);
    }

    public void delete(int n) {
        if (n <= 0) return;
        for (int i = 0; i < n; i++) {
            if (mStringBufferResult.length() > 0) {
                mStringBufferResult.deleteCharAt(mStringBufferResult.length() - 1);
            }
        }
    }

    public void setOnScanSuccessListener(OnScanSuccessListener onScanSuccessListener) {
        mOnScanSuccessListener = onScanSuccessListener;
    }

    /**
     * 返回扫码成功后的结果
     */
    private void performScanSuccess() {
        String barcode = mStringBufferResult.toString();
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onScanSuccess(barcode);
        mStringBufferResult.setLength(0);
    }

    /**
     * 返回扫码开始
     */
    private void performScanStart() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onScanStart();
    }

    private void performTyping() {
        String barcode = mStringBufferResult.toString();
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onTyping(barcode, nowKeyName);
    }

    /**
     * 返回输入中
     */
    private void performTypingTest() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onTypingTest(log);
    }

    /**
     * 返回按下F1按键
     */
    private void performF1Press() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onF1Press();
    }

    /**
     * 返回按下F2按键
     */
    private void performF2Press() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onF2Press();
    }

    /**
     * 返回按下F3按键
     */
    private void performF3Press() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onF3Press();
    }

    /**
     * 返回按下Tab按键
     */
    private void performTabPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onTabPress();
    }

    /**
     * 返回按下Del按键
     */
    private void performDelPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onDelPress();
    }

    /**
     * 按下Menu按键
     */
    private void performMenuPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onMenuPress();
    }

    private void performBackPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onBackPress();
    }

    private void performRightPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onRightPress();
    }

    private void performMultiPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onMultiPress();
    }

    private void performUpPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onUpPress();
    }

    private void performDownPress() {
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onDownPress();
    }

    /**
     * 扫码枪事件解析
     *
     * @param event
     */
    public void analysisKeyEvent(KeyEvent event) {
        mHandler.removeCallbacks(mScanningStartRunnable);
        mHandler.post(mScanningStartRunnable);
        int keyCode = event.getKeyCode();


        //字母大小写判断
        checkLetterStatus(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            char aChar = getInputCode(event);

            Log.d("cccc", "analysisKeyEvent=" + aChar + "-" + event.toString());
            log = "analysisKeyEvent=" + aChar + "-" + event.toString();
            mHandler.removeCallbacks(mTypingTestRunnable);
            mHandler.postDelayed(mTypingTestRunnable, ENTER_MESSAGE_DELAY);

            if (aChar != 0) {
                nowKeyName = aChar + "";
                if (isMoneyInput) {
                    String s = mStringBufferResult.toString() + aChar;
                    String pattern = "^[0-9]{0,6}(\\.[0-9]{0,2})?$";

                    if (Pattern.matches(pattern, s)) {
                        mStringBufferResult.append(aChar);
                    }
                } else if (isNumberInput) {
                    String s = mStringBufferResult.toString() + aChar;
                    String pattern = "^[0-9]*$";

                    if (Pattern.matches(pattern, s)) {
                        mStringBufferResult.append(aChar);
                    }
                } else {
                    mStringBufferResult.append(aChar);
                }
            } else {
                nowKeyName = "";
            }

            if (isAiboshi) {
                if (keyCode == KeyEvent.KEYCODE_DEL && mStringBufferResult.length() > 0) {
                    mStringBufferResult.deleteCharAt(mStringBufferResult.length() - 1);
                }
                if (keyCode == KeyEvent.KEYCODE_F2 && mStringBufferResult.length() > 0) {
                    mStringBufferResult.setLength(0);
                }
            }else{
                if (keyCode == KeyEvent.KEYCODE_DEL && mStringBufferResult.length() > 0) {
                    mStringBufferResult.deleteCharAt(mStringBufferResult.length() - 1);
                }
                if (keyCode == KeyEvent.KEYCODE_BACK && mStringBufferResult.length() > 0) {
                    mStringBufferResult.setLength(0);
                }
            }


            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
//                mStringBufferResult.append("&");
                mHandler.removeCallbacks(mScanningFinishRunnable);
                mHandler.postDelayed(mScanningFinishRunnable, ENTER_MESSAGE_DELAY);
            } else {
                if (isNoEnterEnable) {
                    //延迟post，若500ms内，有其他事件
                    mHandler.removeCallbacks(mScanningFinishRunnable);
                    mHandler.postDelayed(mScanningFinishRunnable, MESSAGE_DELAY);
                } else {
                    mHandler.removeCallbacks(mTypingRunnable);
                    mHandler.postDelayed(mTypingRunnable, ENTER_MESSAGE_DELAY);
                }

                if (keyCode == KeyEvent.KEYCODE_TAB) {
                    mHandler.removeCallbacks(mTabRunnable);
                    mHandler.postDelayed(mTabRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                    mHandler.removeCallbacks(mDelRunnable);
                    mHandler.postDelayed(mDelRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                    mHandler.removeCallbacks(mMenuRunnable);
                    mHandler.postDelayed(mMenuRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mHandler.removeCallbacks(mBackRunnable);
                    mHandler.postDelayed(mBackRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    mHandler.removeCallbacks(mRightRunnable);
                    mHandler.postDelayed(mRightRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    mHandler.removeCallbacks(mUpRunnable);
                    mHandler.postDelayed(mUpRunnable, ENTER_MESSAGE_DELAY);
                }  else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    mHandler.removeCallbacks(mDownRunnable);
                    mHandler.postDelayed(mDownRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    mHandler.removeCallbacks(mMultiRunnable);
                    mHandler.postDelayed(mMultiRunnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_F1) {
                    mHandler.removeCallbacks(mF1Runnable);
                    mHandler.postDelayed(mF1Runnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_F2) {
                    mHandler.removeCallbacks(mF2Runnable);
                    mHandler.postDelayed(mF2Runnable, ENTER_MESSAGE_DELAY);
                } else if (keyCode == KeyEvent.KEYCODE_F3) {
                    mHandler.removeCallbacks(mF3Runnable);
                    mHandler.postDelayed(mF3Runnable, ENTER_MESSAGE_DELAY);
                }
            }

        }
    }

    /**
     * shift键检查
     *
     * @param event
     */
    private void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            //按着shift键，表示大写；松开shift键，表示小写
            mCaps = (event.getAction() == KeyEvent.ACTION_DOWN);
        }
    }

    //获取扫描内容
    private char getInputCode(KeyEvent event) {

        int keyCode = event.getKeyCode();

        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
//            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
            aChar = 0;
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            if (mCaps) {
                switch (keyCode) {
//                    case KeyEvent.KEYCODE_1:
//                        aChar = '!';
//                        break;
//                    case KeyEvent.KEYCODE_2:
//                        aChar = '@';
//                        break;
//                    case KeyEvent.KEYCODE_3:
//                        aChar = '#';
//                        break;
//                    case KeyEvent.KEYCODE_4:
//                        aChar = '$';
//                        break;
//                    case KeyEvent.KEYCODE_5:
//                        aChar = '%';
//                        break;
//                    case KeyEvent.KEYCODE_6:
//                        aChar = '^';
//                        break;
//                    case KeyEvent.KEYCODE_7:
//                        aChar = '&';
//                        break;
//                    case KeyEvent.KEYCODE_8:
//                        aChar = '*';
//                        break;
//                    case KeyEvent.KEYCODE_9:
//                        aChar = '(';
//                        break;
//                    case KeyEvent.KEYCODE_0:
//                        aChar = ')';
//                        break;
                    default:
                        aChar = 0;
                        break;
                }
            } else {
                aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
            }
        } else {
            //其他符号
            switch (keyCode) {
//                case KeyEvent.KEYCODE_COMMA:
//                    aChar = mCaps ? '<' : ',';
//                    break;
                case KeyEvent.KEYCODE_PERIOD:
//                    aChar = mCaps ? '>' : '.';
                    aChar = mCaps ? 0 : '.';
                    break;
//                case KeyEvent.KEYCODE_MINUS:
//                    aChar = mCaps ? '_' : '-';
//                    break;
//                case KeyEvent.KEYCODE_SLASH:
//                    aChar = mCaps ? '?' : '/';
//                    break;
//                case KeyEvent.KEYCODE_EQUALS:
//                    aChar = mCaps ? '+' : '=';
//                    break;
//                case KeyEvent.KEYCODE_BACKSLASH:
//                    aChar = mCaps ? '|' : '\\';
//                    break;
//                case KeyEvent.KEYCODE_SEMICOLON:
//                    aChar = mCaps ? ':' : ';';
//                    break;
//                case KeyEvent.KEYCODE_APOSTROPHE:
//                    aChar = mCaps ? '\"' : '\'';
//                    break;
//                case KeyEvent.KEYCODE_LEFT_BRACKET:
//                    aChar = mCaps ? '{' : '[';
//                    break;
//                case KeyEvent.KEYCODE_RIGHT_BRACKET:
//                    aChar = mCaps ? '}' : ']';
//                    break;
//                case KeyEvent.KEYCODE_GRAVE:
//                    aChar = mCaps ? '~' : '`';
//                    break;
                case KeyEvent.KEYCODE_NUMPAD_0:
                    aChar = '0';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_1:
                    aChar = '1';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_2:
                    aChar = '2';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_3:
                    aChar = '3';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_4:
                    aChar = '4';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_5:
                    aChar = '5';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_6:
                    aChar = '6';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_7:
                    aChar = '7';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_8:
                    aChar = '8';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_9:
                    aChar = '9';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_DOT:
                    aChar = '.';
                    break;
                case KeyEvent.KEYCODE_NUMPAD_ADD:
                    aChar = 0;
                    break;
                case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
                    aChar = 0;
                    break;
                case KeyEvent.KEYCODE_NUMPAD_DIVIDE:
                    aChar = 0;
                    break;
                case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:
                    aChar = 0;
                    break;
                case KeyEvent.KEYCODE_NUM_LOCK:
                    aChar = 0;
                    break;
                default:
                    aChar = 0;
                    break;
            }
        }

        return aChar;
    }

    public void onDestroy() {
        mHandler.removeCallbacks(mScanningFinishRunnable);
        mHandler.removeCallbacks(mScanningStartRunnable);
        mHandler.removeCallbacks(mF1Runnable);
        mHandler.removeCallbacks(mF2Runnable);
        mHandler.removeCallbacks(mF3Runnable);
        mHandler.removeCallbacks(mTabRunnable);
        mHandler.removeCallbacks(mDelRunnable);
        mHandler.removeCallbacks(mMenuRunnable);
        mHandler.removeCallbacks(mTypingRunnable);
        mHandler.removeCallbacks(mTypingTestRunnable);
        mHandler.removeCallbacks(mBackRunnable);
        mHandler.removeCallbacks(mMultiRunnable);
        mHandler.removeCallbacks(mRightRunnable);
        mHandler.removeCallbacks(mUpRunnable);
        mHandler.removeCallbacks(mDownRunnable);
        mOnScanSuccessListener = null;
    }

    public void enableNoEnter() {
        isNoEnterEnable = true;
    }

    public void disEnableNoEnter() {
        isNoEnterEnable = false;
    }

    public void setIsAiboshi(boolean flag) {
        isAiboshi = flag;
    }
}
