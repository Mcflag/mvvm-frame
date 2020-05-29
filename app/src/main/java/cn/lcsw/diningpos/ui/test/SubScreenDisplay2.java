package cn.lcsw.diningpos.ui.test;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

import cn.lcsw.diningpos.R;

public class SubScreenDisplay2 extends Presentation {

    private int amount;
    private TextView amountText;

    public SubScreenDisplay2(Context outerContext, Display display, int amount) {
        super(outerContext, display);
        this.amount = amount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_screen2);
        amountText = findViewById(R.id.show_amount);
        amountText.setText("请扫码支付\n￥" + amount);
    }

    public void setAmount(int amount) {
        this.amount = amount;
        if (amountText != null) {
            amountText.setText("请扫码支付\n￥" + amount);
        }
    }
}
