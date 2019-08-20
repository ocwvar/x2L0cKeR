package com.ocwvar.xlocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout linearLayout = new LinearLayout(TestActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final Button openSetting = new Button(TestActivity.this);
        openSetting.setText("打开应用设置");
        openSetting.setOnClickListener(v -> startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                .setData(Uri.fromParts("package", getPackageName(), null))
        ));


        final Button openAccess = new Button(TestActivity.this);
        openAccess.setText("打开无障碍设置");
        openAccess.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        linearLayout.addView(openAccess);
        linearLayout.addView(openSetting);

        setContentView(linearLayout);
    }
}
