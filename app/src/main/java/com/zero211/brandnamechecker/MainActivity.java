package com.zero211.brandnamechecker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
{
    public static final String USER_NAME_PLACEHOLDER = "USERNAME";
    private static final String FAILED_DOWNLOAD = "failed";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void checkIt(View view)
    {
        EditText edtTxtBrandName = (EditText) findViewById(R.id.edTxtBrandName);
        String brandName = edtTxtBrandName.getText().toString();

        Log.i("Main:checkIt", "Checking brandname: " + brandName);

        for (TLD tld: TLD.values())
        {
            tld.resetView(this);
        }

        for (Site site: Site.values())
        {
            site.resetView(this);
        }

        for (TLD tld: TLD.values())
        {
            tld.checkBrandName(this, brandName);
        }


        for (Site site: Site.values())
        {
            site.checkBrandName(this, brandName);
        }
    }
}
