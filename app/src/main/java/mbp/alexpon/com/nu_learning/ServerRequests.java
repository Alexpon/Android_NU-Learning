package mbp.alexpon.com.nu_learning;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by apple on 15/8/19.
 */
public class ServerRequests {
    ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://140.116.97.92/NU-Learning/";

    public ServerRequests(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    public void storeUserDataInBackground(User user, GetUserCallBack userCallback) {
        progressDialog.show();
        new storeUserDataAsyncTask(user, userCallback).execute();
    }

    public void fetchUserDataInBackground(User user, GetUserCallBack callback) {
        progressDialog.show();
        new fetchUserDataAsyncTask(user, callback).execute();
    }

    public void storeRosterDataInBackground(User user, DateTime dateTime, String class_name, GetUserCallBack userCallback) {
        progressDialog.show();
        new storeRosterDataAsyncTask(user, dateTime, class_name, userCallback).execute();
    }

    public void fetchTeacherQuizInBackground(String teacherId, GetSwitcherCallBack callback) {
        progressDialog.show();
        new fetchTeacherQuizAsyncTask(teacherId, callback).execute();
    }

    public void newQuizRecordInBackground(User user, DateTime dateTime, GetAnswerCallBack answerCallBack) {
        progressDialog.show();
        new newQuizRecordAsyncTask(user, dateTime, answerCallBack).execute();
    }

    public void uploadAnswerInBackground(User user, DateTime dateTime, int question_no, String answer, GetAnswerCallBack answerCallBack) {
        progressDialog.show();
        new uploadAnswerAsyncTask(user, dateTime, question_no, answer, answerCallBack).execute();
    }


    public class storeUserDataAsyncTask extends AsyncTask<Void, Void, Void> {

        User user;
        GetUserCallBack userCallBack;

        public storeUserDataAsyncTask(User user, GetUserCallBack userCallback) {
            this.user = user;
            this.userCallBack = userCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("name", user.getName()));
            dataToSend.add(new BasicNameValuePair("username", user.getUsername()));
            dataToSend.add(new BasicNameValuePair("password", user.getPassword()));
            dataToSend.add(new BasicNameValuePair("department", user.getDepartment()));
            dataToSend.add(new BasicNameValuePair("email", user.getEmail()));


            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "NURegister.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            userCallBack.done(null);
            super.onPostExecute(aVoid);
        }
    }

    public class fetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {

        User user;
        GetUserCallBack userCallBack;

        public fetchUserDataAsyncTask(User user, GetUserCallBack userCallback) {
            this.user = user;
            this.userCallBack = userCallback;
        }

        @Override
        protected User doInBackground(Void... params) {

            User returnedUser = null;
            String result = "";

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", user.getUsername()));
            dataToSend.add(new BasicNameValuePair("password", user.getPassword()));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "NUFetchUser.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);

                HttpEntity entity = httpResponse.getEntity();
                result = EntityUtils.toString(entity);
                //Log.i("ERRRR", result);
                //JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = new JSONArray(result);


                if(jsonArray.length() == 0){
                    returnedUser = null;
                }
                else{
                    JSONObject stock_data = jsonArray.getJSONObject(0);
                    String name = stock_data.getString("name");
                    String department = stock_data.getString("department");
                    String email = stock_data.getString("email");
                    returnedUser = new User(name, user.getUsername(), user.getPassword(), department, email);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    public class storeRosterDataAsyncTask extends AsyncTask<Void, Void, Void> {
        User user;
        DateTime dateTime;
        String class_name;
        GetUserCallBack userCallBack;

        public storeRosterDataAsyncTask(User user, DateTime dateTime, String class_name, GetUserCallBack userCallback) {
            this.user = user;
            this.dateTime = dateTime;
            this.userCallBack = userCallback;
            this.class_name = class_name;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("student_name", user.getName()));
            dataToSend.add(new BasicNameValuePair("student_id", user.getUsername()));
            dataToSend.add(new BasicNameValuePair("department", user.getDepartment()));
            dataToSend.add(new BasicNameValuePair("class_name", class_name));
            dataToSend.add(new BasicNameValuePair("year", dateTime.year + ""));
            dataToSend.add(new BasicNameValuePair("month", dateTime.month + ""));
            dataToSend.add(new BasicNameValuePair("date", dateTime.date + ""));
            dataToSend.add(new BasicNameValuePair("hour", dateTime.hour + ""));
            dataToSend.add(new BasicNameValuePair("minute", dateTime.minute + ""));
            dataToSend.add(new BasicNameValuePair("second", dateTime.second + ""));


            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "NURoster.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            userCallBack.done(null);
            super.onPostExecute(aVoid);
        }
    }

    public class fetchTeacherQuizAsyncTask extends AsyncTask<Void, Void, Integer> {

        String teacherId;
        GetSwitcherCallBack switcherCallback;

        public fetchTeacherQuizAsyncTask(String teacherId, GetSwitcherCallBack switcherCallback) {
            this.teacherId = teacherId;
            this.switcherCallback = switcherCallback;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("teacher_id", teacherId));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "NUFetchTeacher.php");

            int question = 0;
            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);

                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                JSONArray jsonArray = new JSONArray(result);
                //JSONObject jsonObject = new JSONObject(result);

                if(jsonArray.length() == 0){
                    question = 0;
                }
                else{
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    question = jsonObject.getInt("question");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return question;
        }

        @Override
        protected void onPostExecute(Integer returnedUser) {
            progressDialog.dismiss();
            switcherCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    public class newQuizRecordAsyncTask extends AsyncTask<Void, Void, Void> {

        User user;
        DateTime dateTime;
        GetAnswerCallBack answerCallBack;

        public newQuizRecordAsyncTask(User user, DateTime dateTime, GetAnswerCallBack answerCallBack) {
            this.user = user;
            this.dateTime = dateTime;
            this.answerCallBack = answerCallBack;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("student_id", user.getUsername()));
            dataToSend.add(new BasicNameValuePair("student_name", user.getName()));
            dataToSend.add(new BasicNameValuePair("department", user.getDepartment()));
            dataToSend.add(new BasicNameValuePair("year", dateTime.year+""));
            dataToSend.add(new BasicNameValuePair("month", dateTime.month+""));
            dataToSend.add(new BasicNameValuePair("date", dateTime.date+""));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "NUNewQuizRecord.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            answerCallBack.done("Success");
            super.onPostExecute(aVoid);
        }
    }

    public class uploadAnswerAsyncTask extends AsyncTask<Void, Void, Void> {

        User user;
        DateTime dateTime;
        int question_no;
        String answer;
        GetAnswerCallBack answerCallBack;

        public uploadAnswerAsyncTask(User user, DateTime dateTime, int question_no, String answer, GetAnswerCallBack answerCallBack) {
            this.user = user;
            this.dateTime = dateTime;
            this.question_no = question_no;
            this.answer = answer;
            this.answerCallBack = answerCallBack;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("student_id", user.getUsername()));
            dataToSend.add(new BasicNameValuePair("year", dateTime.year+""));
            dataToSend.add(new BasicNameValuePair("month", dateTime.month+""));
            dataToSend.add(new BasicNameValuePair("date", dateTime.date+""));
            dataToSend.add(new BasicNameValuePair("question_no", question_no+""));
            dataToSend.add(new BasicNameValuePair("answer", answer));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "NUUploadAnswer.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            answerCallBack.done("Success");
            super.onPostExecute(aVoid);
        }
    }
}
