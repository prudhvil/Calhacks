package worth.butler.alfred.butler;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

public class SendMessageService extends IntentService {

    private GoogleApiClient mGoogleApiClient;
    private String transcriptionNodeId = null;
    private String message;

    public static final String REQUEST_COMMAND_MESSAGE_PATH = "/request_command";
    private static final String LOG_TAG = "Wear SendMessageService";

    public SendMessageService() {
        super("SendMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        message = intent.getExtras().getString("command");
        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        initCapability();
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        // Do nothing
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        // Do nothing
                    }
                })
                .addApi(Wearable.API)
                .build();
        this.mGoogleApiClient.connect();
    }

    /* Establishes GoogleApiClient connection. */
    private void initCapability() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CapabilityApi.GetCapabilityResult capResult = Wearable.CapabilityApi.getCapability(
                        mGoogleApiClient, REQUEST_COMMAND_MESSAGE_PATH,
                        CapabilityApi.FILTER_REACHABLE).await();

                Collection<String> nodes = getNodes();
                Log.d("# nodes detected:", String.valueOf(nodes.size()));

                for (String node : nodes) {
                    transcriptionNodeId = node;
                }

                Log.d("node id detected", String.valueOf(transcriptionNodeId));

                Wearable.MessageApi.sendMessage(mGoogleApiClient, transcriptionNodeId,
                        REQUEST_COMMAND_MESSAGE_PATH, message.getBytes()).setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.e(LOG_TAG, "Message Failed");
                                }
                                else {
                                    Log.d(LOG_TAG,"message sent");
                                }
                            }
                        });
            }
        }).start();
    }

    /* Gets nodes for GoogleApiClient. */
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            if (node.isNearby()) {
                results.add(node.getId());
            }
        }
        return results;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
