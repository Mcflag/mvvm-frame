package cn.lcsw.diningpos.function.keyboard;

public interface IResponse {
    void onResult(byte[] var1, int var2);

    void onError(short var1, int var2);
}
