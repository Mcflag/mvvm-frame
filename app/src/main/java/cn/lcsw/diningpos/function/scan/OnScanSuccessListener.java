package cn.lcsw.diningpos.function.scan;

public interface OnScanSuccessListener {

    void onScanSuccess(String barcode);

    void onTyping(String barcode, String nowKeyName);

    void onScanStart();

    void onF1Press();

    void onF2Press();

    void onF3Press();

    void onDelPress();

    void onBackPress();

    void onTabPress();

    void onMenuPress();

    void onRightPress();

    void onMultiPress();

    void onUpPress();

    void onDownPress();

    void onTypingTest(String log);
}
