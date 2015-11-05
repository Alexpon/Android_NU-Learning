package mbp.alexpon.com.nu_learning;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Arrays;


public class MainActivity extends Activity {



    public static final String RosterCard1 = "EDF5A050";
    public static final String RosterCard2 = "0D7D9D50";
    public String class_name = "";

    private NfcAdapter mNfcAdapter;
    private Button btn_GL;
    private Button btn_setup;
    private Button btn_IE;
    private TextView info;

    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkNfc();
        setListener();
    }

    public void initViews(){
        info = (TextView) findViewById(R.id.info);
        btn_GL = (Button) findViewById(R.id.btn_GL);
        btn_setup = (Button) findViewById(R.id.btn_setup);
        btn_IE = (Button) findViewById(R.id.btn_IE);
        userLocalStore = new UserLocalStore(this);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    public void setListener(){
        btn_setup.setOnClickListener(myListener);
        btn_GL.setOnClickListener(myListener);
        btn_IE.setOnClickListener(myListener);
    }

    private Button.OnClickListener myListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.btn_setup:
                    if(authenticate()){
                        userLocalStore.clearUserData();
                        userLocalStore.setUserLoggedIn(false);
                        info.setText("同學還沒登入哦！\n請點選下面按鈕登入");
                        btn_setup.setText("Login");
                    }
                    else {
                        Intent setIntent = new Intent();
                        setIntent.setClass(MainActivity.this, Login.class);
                        startActivity(setIntent);
                    }
                    break;
                case R.id.btn_GL:
                    Intent glIntent = new Intent();
                    if(authenticate()){
                        glIntent.setClass(MainActivity.this, Gradual_Main.class);
                        startActivity(glIntent);
                    }
                    else{
                        glIntent.setClass(MainActivity.this, Login.class);
                        startActivity(glIntent);
                    }
                    break;

                case R.id.btn_IE:
                    Intent ieIntent = new Intent();
                    if(authenticate()) {
                        is_start_quiz();
                    }
                    else{
                        ieIntent.setClass(MainActivity.this, Login.class);
                        startActivity(ieIntent);
                    }
                    break;
            }
        }
    };


    protected void onStart(){
        super.onStart();
        if(authenticate()){
            displayUserDetails();
            btn_setup.setText("Logout");
        }
        else{
            info.setText("同學還沒登入哦！\n請點選下面按鈕登入");
            btn_setup.setText("Login");
        }
    }

    private boolean authenticate(){
        return userLocalStore.getUserLoggedIn();
    }

    private void displayUserDetails(){
        User user = userLocalStore.getLoggedInUser();
        info.setText("Welcome " + user.getName() + "\nStudent ID: " + user.getUsername()
                + "\nDepartment: " + user.getDepartment() + "\nEmail: " + user.getEmail());
    }

    private void roster(){
        User user = userLocalStore.getLoggedInUser();
        Time t = new Time(Time.getCurrentTimezone());
        t.setToNow();
        int year = t.year;
        int month = t.month+1;
        int date = t.monthDay;
        int hour = t.hour;
        int minute = t.minute;
        int second = t.second;
        DateTime dateTime = new DateTime(year, month, date, hour, minute, second);

        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeRosterDataInBackground(user, dateTime, class_name, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                Toast.makeText(getApplicationContext(), "Roster Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void is_start_quiz(){

        AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
        editDialog.setTitle("請輸入教師編號");

        final EditText editText = new EditText(MainActivity.this);
        editDialog.setView(editText);

        editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
           public void onClick(DialogInterface arg0, int arg1){
               Toast.makeText(getApplicationContext(), editText.getText().toString(), Toast.LENGTH_SHORT).show();
               check_from_database(editText.getText().toString());
           }
        });

        editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
           public void onClick(DialogInterface arg0, int arg1){
           }
        });
        editDialog.show();
    }

    private void check_from_database(String teacherId){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchTeacherQuizInBackground(teacherId, new GetSwitcherCallBack() {
            @Override
            public void done(int question_num) {
                if(question_num==0){
                    Toast.makeText(getApplicationContext(), "It is not a quiz time!", Toast.LENGTH_SHORT).show();
                }
                else {
                    NewQuizRecord(question_num);
                    Toast.makeText(getApplicationContext(), "Success!!"+question_num, Toast.LENGTH_SHORT).show();
                    /*Bundle bundle = new Bundle();
                    bundle.putInt("question_num", question_num);
                    Intent intent = new Intent(MainActivity.this, IE_Main.class);
                    intent.putExtras(bundle);
                    startActivity(intent);*/
                }
            }
        });
    }

    public void NewQuizRecord(final int question_num){
        User user = userLocalStore.getLoggedInUser();
        Time t = new Time(Time.getCurrentTimezone());
        t.setToNow();
        int year = t.year;
        int month = t.month+1;
        int date = t.monthDay;
        DateTime dateTime = new DateTime(year, month, date);
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.newQuizRecordInBackground(user, dateTime, new GetAnswerCallBack() {
            @Override
            public void done(String com) {
                Bundle bundle = new Bundle();
                bundle.putInt("question_num", question_num);
                Intent intent = new Intent(MainActivity.this, IE_Main.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    public void checkNfc(){

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled.Please lunch NFC", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "NFC is abled.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String cardID = ByteArrayToHexString(intent.getByteArrayExtra(mNfcAdapter.EXTRA_ID));
        if(cardID.equals(RosterCard1) || cardID.equals(RosterCard2)){
            class_name = "CourseA";
            showAlertDialog();
        }
        //resolveIntent(intent);
        super.onNewIntent(intent);
    }


    private String ByteArrayToHexString(byte [] array){
        int i, j, in;
        String [] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for(i = 0; i<array.length; i++){
            in = (int) array[i] & 0xff;
            j = (in >> 4) & 0x0f;
            out += hex[j];
            j = in & 0x0f;
            out += hex[j];
        }
        return out;
    }

    @Override
    protected void onResume() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);

        super.onResume();
    }

    @Override
    protected void onPause() {
        mNfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    private void showAlertDialog(){
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("點名");
        myAlertDialog.setMessage("請問要送出現在資訊嗎？");
        myAlertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        myAlertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                roster();
            }
        });
        myAlertDialog.show();
    }


}
