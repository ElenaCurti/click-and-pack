package com.example.clickandpack;

import android.content.Context;
import android.widget.CheckBox;

public class MyCheckBox extends CheckBox {
    public MyCheckBox(Context context) {
        super(context);
        this.setTextSize(20);   // TODO da fare senza codice
        this.setPadding(0, 15, 0, 15);

    }
}
