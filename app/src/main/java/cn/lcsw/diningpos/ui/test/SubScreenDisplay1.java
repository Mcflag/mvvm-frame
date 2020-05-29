package cn.lcsw.diningpos.ui.test;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import cn.lcsw.diningpos.R;

public class SubScreenDisplay1 extends Presentation {

    public SubScreenDisplay1(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_screen1);
    }
}
