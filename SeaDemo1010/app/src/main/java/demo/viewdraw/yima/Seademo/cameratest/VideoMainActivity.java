package demo.viewdraw.yima.Seademo.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ftr.tangram.modules.DataInterity;

import demo.viewdraw.yima.Seademo.R;


public class VideoMainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avtowifi);

        DataInterity dataInterity = new DataInterity();
        //byte[] data = new byte[]{'A', 'A', 'A', 'A', 0x1, 0, 0, 0, 'G', 'G', 'G', 'G', 0x18, 'E', 'E', 'E', 'E'};
        byte[] data = {65,65,65,65,14,0,0,0,71,71,71,71,8,7,42,10,10,4,97,100,99,0,16,48,24,0,69,69,69,69,0,0,0,0,0,0, 'A', 'A', 'A', 'A', 0x1, 0, 0, 0, 'G', 'G', 'G', 'G', 0x18, 'E', 'E', 'E', 'E'};

        dataInterity.recv(data, data.length);
    }

    public void click(View v){
        startActivity(new Intent(this, VideoActivity.class));
    }
}
