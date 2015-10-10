package worth.butler.alfred.butler;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener{

    private GoogleApiClient mGoogleApiClient;

    public static final String REQUEST_COMMAND_MESSAGE_PATH = "/request_command";
    private static final String LOG_TAG = "Mobile MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "GoogleApiClient connected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "GoogleApiClient Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(LOG_TAG,"GoogleApiClient connection failed");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(REQUEST_COMMAND_MESSAGE_PATH)) {
            String command = messageEvent.getData().toString();
            (new SendCommandTask()).execute(command);
        }
    }

    public class SendCommandTask extends AsyncTask<String,Void,Void> {
        String apiUrl = "http://guarded-reef-9305.herokuapp.com";
        String TASK_LOG_TAG = "";
        @Override
        protected Void doInBackground(String... params) {
            String command = params[0];
            String url = apiUrl + "/" + command;
            Log.v(TASK_LOG_TAG, url);

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            try {
                HttpResponse response = httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
                Log.e("error sending command",e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("error sending command",e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }
}