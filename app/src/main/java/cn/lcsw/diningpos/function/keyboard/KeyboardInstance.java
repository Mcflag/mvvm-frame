package cn.lcsw.diningpos.function.keyboard;

import android.content.Context;
import android.util.Log;
import cn.lcsw.diningpos.base.BaseApplication;

public class KeyboardInstance {
    private static volatile KeyboardInstance instance = null;

    private Context mContext;
    private PayKeyboard keyboard;
    private USBDetector detector;
    private static ICallback callback = null;
    private static IEnterCallback enterCallback = null;
    private static ISettingCallback settingCallback = null;
    private static IBackCallback backCallback = null;

    private KeyboardInstance() {
        mContext = BaseApplication.Companion.getINSTANCE();
        detector = PayKeyboard.getDetector(mContext);
        detector.setListener(new ICheckListener() {
            @Override
            public void onAttach() {
                openKeyboard();
            }
        });
    }

    public static KeyboardInstance getInstance() {
        if (instance == null) {
            synchronized (KeyboardInstance.class) {
                if (instance == null) {
                    instance = new KeyboardInstance();
                }
            }
        }
        return instance;
    }

    public void keyboardRelease() {
        if (keyboard != null) {
            keyboard.release();
            keyboard = null;

        }
        if (detector != null) {
            detector.release();
            detector = null;
        }
    }

    public void keyboardReset() {
        if (keyboard != null) {
            keyboard.reset();
        }
    }

    public void keyboardListenerReset() {
        callback = null;
        enterCallback = null;
        backCallback = null;
        settingCallback = null;
    }

    public void keyboardSetResult(boolean isOk) {
        if (keyboard != null) {
            keyboard.setResult(isOk);
        }
    }

    public void setFaceLock(boolean isLock) {
        if (keyboard != null) {
            keyboard.setFaceLock(isLock);
        }
    }

    public void openKeyboard() {
        if (keyboard == null || keyboard.isReleased()) {
            keyboard = PayKeyboard.get(mContext);
            if (keyboard != null) {
                keyboard.setLayout(1);
                keyboard.setBaudRate(9600);
                keyboard.setListener(new DefaultKeyboardListener() {
                    @Override
                    public void onKeyDown(final int keyCode, final String keyName) {
                        super.onKeyDown(keyCode, keyName);
                        Log.d("KeyboardUI", String.format("key down event code : %s, name: %s \n ", keyCode, keyName));
                        if (keyCode == 23 && "PAY".equals(keyName)) {
                            if (enterCallback != null) {
                                enterCallback.enterPress();
                            }
                        } else if (keyCode == 29 && "OPT".equals(keyName)) {
                            if (settingCallback != null) {
                                settingCallback.settingPress();
                            }
                        } else if ((keyCode == 20 && "Backspace".equals(keyName)) || (keyCode == 30 && "ESC".equals(keyName))) {
                            if (backCallback != null) {
                                backCallback.backPress();
                            }
                        }
                    }

                    @Override
                    public void onKeyUp(int keyCode, String keyName) {
                        super.onKeyUp(keyCode, keyName);
                    }

                    @Override
                    public void onPay(IPayRequest iPayRequest) {
                        super.onPay(iPayRequest);
                        Log.d("KeyboardUI", String.format("%.2f", iPayRequest.getMoney()));
                        if (callback != null) {
                            callback.getMoney(iPayRequest.getMoney());
                        }
                    }

                    @Override
                    public void onAvailable() {
                        super.onAvailable();
                        if (keyboard != null && !keyboard.isReleased()) {
                            keyboard.updateSign(4, 0);
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        Log.d("KeyboardUI", "usb exception!!!!");
                        keyboard = null;
                        super.onException(e);
                    }

                    @Override
                    public void onRelease() {
                        super.onRelease();
                        keyboard = null;
                        Log.d("KeyboardUI", "Keyboard release!!!!!!");
                    }
                });
                keyboard.open();
            }
        } else {
            Log.d("KeyboardUI", "keyboard exists!!!");
        }
    }

    public void setListener(ICallback listener) {
        this.callback = listener;
    }

    public interface ICallback {
        void getMoney(Double money);
    }

    public void setEnterListener(IEnterCallback listener) {
        this.enterCallback = listener;
    }

    public interface IEnterCallback {
        void enterPress();
    }

    public void setSettingListener(ISettingCallback listener) {
        this.settingCallback = listener;
    }

    public interface ISettingCallback {
        void settingPress();
    }

    public void setBackListener(IBackCallback listener) {
        this.backCallback = listener;
    }

    public interface IBackCallback {
        void backPress();
    }
}
