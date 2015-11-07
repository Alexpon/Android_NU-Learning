package mbp.alexpon.com.nu_learning;

/**
 * Created by apple on 15/6/4.
 */

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
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class IE_Main extends Activity{

    private SharedPreferences loginPreferences;
    public static final String TAG = "NfcDemo";
    public static final String MIME_TEXT_PLAIN = "text/plain";

    private String answer;
    private int total_question;
    private int question_no;
    private TextView user;
    private TextView title;
    private TextView ans;
    private Button btn_backmain;
    private Button btn_submit;
    private NfcAdapter mNfcAdapter;
    private UserLocalStore userLocalStore;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interactive_examination);
        initViews();
        setListener();
    }

    public void initViews(){
        Bundle bundle =this.getIntent().getExtras();
        total_question = bundle.getInt("question_num");
        Log.i("TTTT", total_question+"");
        question_no = 1;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        user = (TextView) findViewById(R.id.txt_username);
        title = (TextView) findViewById(R.id.title);
        ans = (TextView) findViewById(R.id.answer);
        btn_backmain = (Button) findViewById(R.id.btn_backmain);
        btn_submit = (Button) findViewById(R.id.ie_submit);
        userLocalStore = new UserLocalStore(this);
        user.setText(loginPreferences.getString("Name", ""));
        title.setText("題目 "+question_no);
    }

    public void setListener(){
        btn_backmain.setOnClickListener(myListener);
        btn_submit.setOnClickListener(myListener);
    }

    private Button.OnClickListener myListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()) {
                case R.id.btn_backmain:
                    Intent data = new Intent();
                    data.setClass(IE_Main.this, MainActivity.class);
                    startActivity(data);
                    IE_Main.this.finish();
                    break;
                case R.id.ie_submit:
                    uploadToServer();
                    break;
            }
        }
    };

    private void uploadToServer(){
        User user = userLocalStore.getLoggedInUser();
        Time t = new Time(Time.getCurrentTimezone());
        t.setToNow();
        int year = t.year;
        int month = t.month+1;
        int date = t.monthDay;
        DateTime dateTime = new DateTime(year, month, date);
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.uploadAnswerInBackground(user, dateTime, question_no, answer, new GetAnswerCallBack() {
            @Override
            public void done(String com) {
                Toast.makeText(getApplication(), com, Toast.LENGTH_SHORT).show();
                question_no++;
                if(total_question < question_no){
                    title.setText("你已完成所有測驗");
                    ans.setText("");
                }
                else{
                    title.setText("題目 "+question_no);
                    ans.setText("你的答案：請感應Tag");
                }
            }
        });
    }


    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            }
            else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        }
        else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

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

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    private void stopForegroundDispatch(final IE_Main ie_Main, NfcAdapter Adapter) {
        Adapter.disableForegroundDispatch(ie_Main);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        public static final String TAG = "NfcDemo";

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
                ans.setText("你的答案：" + result);
                answer = result;
                Toast.makeText(getApplication(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }



}
