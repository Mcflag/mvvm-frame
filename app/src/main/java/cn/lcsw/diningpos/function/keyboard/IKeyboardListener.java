package cn.lcsw.diningpos.function.keyboard;

public interface IKeyboardListener {
    void onKeyDown(int var1, String var2);

    void onKeyUp(int var1, String var2);

    void onPay(IPayRequest var1);

    void onAvailable();

    void onException(Exception var1);

    void onRelease();
}
