package mbp.alexpon.com.nu_learning;

/**
 * Created by apple on 15/6/4.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Gradual_Main extends Activity {
    private Button btn_menu;
    private Button btn_task1;
    private int task_num=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gradual_learning);
        initViews();
        setListener();
        btn_task1.setText("任務" + task_num);

    }

    public void initViews(){
        btn_menu = (Button) findViewById(R.id.glbackmain);
        btn_task1 = (Button) findViewById(R.id.btn_task);
    }

    public void setListener(){
        btn_menu.setOnClickListener(myListener);
        btn_task1.setOnClickListener(myListener);
    }

    private Button.OnClickListener myListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.glbackmain:
                    Intent backIntent = new Intent();
                    backIntent.setClass(Gradual_Main.this, MainActivity.class);
                    startActivity(backIntent);
                    Gradual_Main.this.finish();
                    break;
                case R.id.btn_task:
                    Intent instIntent = new Intent();
                    instIntent.setClass(Gradual_Main.this, Gradual_Inst.class);
                    startActivity(instIntent);
                    Gradual_Main.this.finish();
                    break;
            }
        }
    };

}
