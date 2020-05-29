package cn.lcsw.diningpos.ui.test;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import cn.lcsw.diningpos.R;

public class DifferentDisplay extends Presentation {

    public DifferentDisplay(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_screen);
    }
}
