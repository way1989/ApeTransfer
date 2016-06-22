package com.ape.filepicker;

import android.content.Context;
import android.content.Intent;


/**
 * Created by ex3ndr on 14.10.14.
 */
public class Intents {
    public static Intent pickFile(Context context) {
        return new Intent(context, FilePickerActivity.class);
    }

}
