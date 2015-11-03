package mbp.alexpon.com.nu_learning;

/**
 * Created by apple on 15/6/4.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class IE_Main extends Activity{

    static TextView txt_name;
    static String str1="0";
    static String str2="0";
    static String str3="0";
    private SharedPreferences loginPreferences;
    public static final String TAG = "NfcDemo";
    public static final String MIME_TEXT_PLAIN = "text/plain";

    private Button btn_backmain;
    private TextView Ans;
    private TextView show02;
    private NfcAdapter mNfcAdapter;
    static Socket socket;
    static InputStream in;
    static OutputStream out;
    private Handler handler = new Handler();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interactive_examination);
        initViews();
        setListener();

        Thread t = new thread();
        t.start();
        handleIntent(getIntent());

    }

    public void initViews(){
        Ans = (TextView) findViewById(R.id.Ans);
        btn_backmain = (Button) findViewById(R.id.btn_backmain);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        show02 =(TextView)findViewById(R.id.show02);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        str1 = loginPreferences.getString("Name", "");
        TextView ans = (TextView) findViewById(R.id.Ans);
        TextView tv = (TextView) findViewById(R.id.txt_username);
        tv.setText(str1);
        str2 =  Ans.getText().toString();
    }

    public void setListener(){
        btn_backmain.setOnClickListener(myListener);
    }

    private Button.OnClickListener myListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent data = new Intent();
            data.setClass(IE_Main.this, MainActivity.class);
            startActivity(data);
            IE_Main.this.finish();
        }
    };

    private Runnable receive_run = new Runnable(){

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while(socket.isConnected())
            {
                byte[] rebyte = new byte[256];
                try
                {
                    in.read(rebyte);
                    str3 = new String(rebyte);
                    handler.post(new Runnable(){
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            show02.setText(str3);
                            Ans.setText("");
                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }};

    class thread extends Thread{
        public void run() {
            try{
                //int servPort=5000;

                socket = MainActivity.socket;
                in=socket.getInputStream();
                out=socket.getOutputStream();
//				String Ans_who = str2+ "-"+ str1;
//				byte[] sendstr = new byte[256];
//				System.arraycopy(Ans_who.getBytes(), 0, sendstr, 0, Ans_who.length());
//				out.write(sendstr);
//				out.flush();
//				out.close();
                Thread receive = new Thread(receive_run);
                receive.start();
                Log.d("send","OutputStream");
            }catch(Exception e){
                Log.d("send",e.getMessage());
            }
        }
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
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

    }

    @Override
    protected void onResume() {
        super.onResume();
//         It's important, that the activity is in the foreground (resumed).
//         Otherwise an IllegalStateException is thrown.
        setupForegroundDispatch(this, mNfcAdapter);
    }

    private void setupForegroundDispatch(IE_Main ie_Main,
                                         NfcAdapter Adapter) {
        final Intent intent = new Intent(ie_Main.getApplicationContext(), ie_Main.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(ie_Main.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        Adapter.enableForegroundDispatch(ie_Main, pendingIntent, filters, techList);
    }
    //    Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    private void stopForegroundDispatch(final IE_Main ie_Main, NfcAdapter Adapter) {
        Adapter.disableForegroundDispatch(ie_Main);
    }

    //    this method gets called, when the user attaches a Tag to the device.
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
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
                Ans.setText(result);
                str2 = result;
                try{
                    String Ans_who = str2+ "-"+ str1;
                    byte[] sendstr = new byte[256];
                    System.arraycopy(Ans_who.getBytes(), 0, sendstr, 0, Ans_who.length());
                    out.write(sendstr);
                    out.flush();
                    Log.d("send","OutputStream");
                }catch(Exception e){
                    Log.d("send",e.getMessage());
                }
            }
        }

    }
}
