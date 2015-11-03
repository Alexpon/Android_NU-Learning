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


    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;
    private Button btn_GL;
    private Button btn_setup;
    private Button btn_IE;
    private TextView info;

    private static String str1="0",str2="0";
    public static Socket socket;
    static InputStream in;
    static OutputStream out;

    private UserLocalStore userLocalStore;

    public static final String MIME_TEXT_PLAIN = "text/plain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkNfc();
        setListener();

        handleIntent(getIntent());
        //Thread t = new thread();
        //t.start();
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
        int month = t.month+1; //?
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        DateTime dateTime = new DateTime(year, month, date, hour, minute, second);

        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeRosterDataInBackground(user, dateTime, new GetUserCallBack() {
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
            public void done(int switcher) {
                if(switcher==1){
                    Toast.makeText(getApplicationContext(), "Success!!", Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(MainActivity.this, IE_Main.class);
                    //startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "It is not a quiz time!", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(), "Switcher = " + switcher, Toast.LENGTH_SHORT).show();
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
        //resolveIntent(intent);
        Toast.makeText(this, ByteArrayToHexString(intent.getByteArrayExtra(mNfcAdapter.EXTRA_ID)), Toast.LENGTH_LONG).show();
        showAlertDialog();
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

    private void resolveIntent(Intent intent){
        Log.i("NFC", "In resolve intent");
        String action = intent.getAction();

        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Log.i("NFC", "ACTION NDEF DISCOVERED");
            String type = intent.getType();
            if(MIME_TEXT_PLAIN.equals(type)){
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            }
            else{
                Log.i("NFC", "Wrong mime type: "+type);
            }

        }
        else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Log.i("NFC", "ACTION TECH DISCOVERED");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
        else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Log.i("NFC", "ACTION TAG DISCOVERED");
        }
        else{
            Log.i("NFC", "UNKNOW INTENT");
            return;
        }
    }


    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
	        /*
	         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
	         *
	         * http://www.nfc-forum.org/specs/
	         *
	         * bit_7 defines encoding
	         * bit_6 reserved for future use, must be 0
	         * bit_5..0 length of IANA language code
	         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                str2 = result;
            }
        }
    }



    private void handleIntent(Intent intent) {
        // TODO Auto-generated method stub

    }

    class thread extends Thread{
        public void run() {
            try{
                int servPort=5000;
                sleep(5000);
                socket = new Socket("192.168.2.115",servPort);
                in=socket.getInputStream();
                out=socket.getOutputStream();
                str1 = "@-" + str1;
                Log.d("Name", str1);

                byte[] sendstr = new byte[256];
                System.arraycopy(str1.getBytes(), 0, sendstr, 0, str1.length());
                out.write(sendstr);
                out.flush();
                Log.d("sendd","OutputStream");
            }
            catch(Exception e){
                Log.d("send",e.getMessage());
            }
        }
    }

}
