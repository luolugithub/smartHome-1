package com.demo.smarthome.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.smarthome.R;
import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.service.Cfg;

import java.util.ArrayList;


public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        ActivityControl.getInstance().addActivity(this);
        TextView title = (TextView) findViewById(R.id.backBtn);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        }) ;

        ListView contentList = (ListView) findViewById(R.id.contentListView);

        //添加5个列表项

        ArrayList<String> contentText = new ArrayList<String>();
        contentText.add(this.getResources().getString(R.string.help_content_01));
        contentText.add(this.getResources().getString(R.string.help_content_02));
        contentText.add(this.getResources().getString(R.string.help_content_03));
        contentText.add(this.getResources().getString(R.string.help_content_04));
        contentText.add(this.getResources().getString(R.string.help_content_05));
        contentText.add(this.getResources().getString(R.string.help_content_06));
        contentText.add(this.getResources().getString(R.string.help_content_07));
        //为列表设置适配器
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.help_list_content, contentText);
        contentList.setAdapter(listAdapter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 结束Activity&从栈中移除该Activity
        ActivityControl.getInstance().removeActivity(this);
    }
}
