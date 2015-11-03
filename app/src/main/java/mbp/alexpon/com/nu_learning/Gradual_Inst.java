package mbp.alexpon.com.nu_learning;

/**
 * Created by apple on 15/7/1.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class Gradual_Inst extends Activity {
    private static TextView tasknameTextView;
    private static TextView taskinstTextView;
    private Button taskstart;
    private static List<task> books;
    private TechCrunchTask downloadTask;

    String string="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_graduinst);
        initViews();
        setListener();
        DomParse();
    }

    public void initViews(){
        tasknameTextView=(TextView) findViewById(R.id.taskname);
        taskinstTextView=(TextView) findViewById(R.id.taskinstruction);
        taskstart=(Button) findViewById(R.id.button1);
    }

    public void setListener(){
        taskstart.setOnClickListener(myListener);
    }

    private Button.OnClickListener myListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            /*Intent data = new Intent();
            data.setClass(Gradual_Inst.this, Grandual_Ing.class);
            startActivity(data);*/
            Gradual_Inst.this.finish();
        }
    };

    public static class TechCrunchTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String downloadURL = "http://alexpon.host56.com/example.xml";
            try {
                URL url = new URL(downloadURL);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                DomParseInst dom = new DomParseInst();
                books = dom.ReadbookXML(inputStream);

            } catch (Exception e) {
                Log.i("ERROR", e+"");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tasknameTextView.setText(books.get(0).title.toString());
            taskinstTextView.setText("說明：\n"+books.get(0).content.toString());
        }


    }

    private void DomParse(){
            if(downloadTask != null) {
                downloadTask.cancel(true);
            }
            else{
                downloadTask = new TechCrunchTask();
                downloadTask.execute();
            }
    }

}
