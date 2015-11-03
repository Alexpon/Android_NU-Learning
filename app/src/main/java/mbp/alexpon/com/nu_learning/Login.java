package mbp.alexpon.com.nu_learning;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Login extends ActionBarActivity {

    private EditText username;
    private EditText password;
    private TextView registerLink;
    private Button submit;
    private UserLocalStore userLocalStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setListener();
    }

    public void initViews(){
        username = (EditText) findViewById(R.id.edUsername);
        password = (EditText) findViewById(R.id.edPassword);
        registerLink = (TextView) findViewById(R.id.registerLink);
        submit = (Button) findViewById(R.id.submit);
        userLocalStore = new UserLocalStore(this);
    }

    public void setListener(){
        submit.setOnClickListener(myListener);
        registerLink.setOnClickListener(myListener);
    }

    private View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    String un = username.getText().toString();
                    String pw = password.getText().toString();

                    User user = new User(un, pw);

                    authenticate(user);

                    userLocalStore.storeUserData(user);
                    userLocalStore.setUserLoggedIn(true);

                    break;
                case R.id.registerLink:
                    Intent intent = new Intent(Login.this, Register.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void authenticate(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchUserDataInBackground(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null){
                    showErrorMessage();
                    userLocalStore.setUserLoggedIn(false);
                }
                else {
                    logUserIn(returnedUser);
                }
            }
        });

    }

    private void showErrorMessage(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Login.this);
        dialogBuilder.setMessage("Incorrect user detail");
        dialogBuilder.setPositiveButton("ok", null);
        dialogBuilder.show();
    }

    private void logUserIn(User returnedUser){
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);

        startActivity(new Intent(this, MainActivity.class));
    }
}
