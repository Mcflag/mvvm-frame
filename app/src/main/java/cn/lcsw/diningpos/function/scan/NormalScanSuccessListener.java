package cn.lcsw.diningpos.function.scan;

public abstract class NormalScanSuccessListener  implements OnScanSuccessListener{

    @Override
    public void onScanStart() {

    }

    @Override
    public void onTyping(String barcode, String nowKeyName){

    }

    @Override
    public void onTypingTest(String log){

    }

    @Override
    public void onF1Press(){

    }

    @Override
    public void onF2Press(){

    }

    @Override
    public void onF3Press(){

    }

    @Override
    public void onDelPress(){

    }

    @Override
    public void onTabPress(){

    }

    @Override
    public void onMenuPress(){

    }

    @Override
    public void onBackPress() {

    }

    @Override
    public void onRightPress() {

    }

    @Override
    public void onMultiPress() {

    }

    @Override
    public void onUpPress() {

    }

    @Override
    public void onDownPress() {

    }

    public abstract void onScanSuccess(String barcode);
}
