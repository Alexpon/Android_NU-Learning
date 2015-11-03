package mbp.alexpon.com.nu_learning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class Register extends Activity {

    private EditText name;
    private EditText username;
    private EditText password;
    private EditText department;
    private EditText email;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        setListener();
    }

    public void initViews(){
        name = (EditText) findViewById(R.id.edRN);
        username = (EditText) findViewById(R.id.edRUN);
        password = (EditText) findViewById(R.id.edRPD);
        department = (EditText) findViewById(R.id.edRD);
        email = (EditText) findViewById(R.id.edRE);
        submit = (Button) findViewById(R.id.register);
    }

    public void setListener(){
        submit.setOnClickListener(myListener);
    }

    private View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register:
                    String n = name.getText().toString();
                    String un = username.getText().toString();
                    String pw = password.getText().toString();
                    String dep = department.getText().toString();
                    String e = email.getText().toString();
                    User user = new User(n, un, pw, dep, e);
                    registerUser(user);
                    break;
            }
        }
    };

    private void registerUser(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeUserDataInBackground(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }



}
