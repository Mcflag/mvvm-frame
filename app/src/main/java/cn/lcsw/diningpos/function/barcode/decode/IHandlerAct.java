package cn.lcsw.diningpos.function.barcode.decode;

public interface IHandlerAct {

    MainHandler getHandler();

    void checkResult(String result);

}
