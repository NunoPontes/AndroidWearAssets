package com.nunop.provaconceito3;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import java.io.ByteArrayOutputStream;

import static com.google.android.gms.wearable.DataMap.TAG;



public class MainActivity extends AppCompatActivity{

    private Button btnSend;
    private RadioButton rbtnRight;
    private RadioButton rbtnWrong;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = (Button) findViewById(R.id.btnSend);
        rbtnRight = (RadioButton) findViewById(R.id.rbRight);
        rbtnWrong = (RadioButton) findViewById(R.id.rbWrong);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get resource and send it to wear
                int resource;
                if(rbtnRight.isChecked())
                    resource = R.drawable.right;
                else
                    resource = R.drawable.wrong;

                sendDataToWearable(resource);

            }
        });

        Log.i(TAG, "mGoogleApiClient before creation -- Handheld");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("tag", "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("tag", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("tag", "onConnectionFailed: " + result);
                    }
                })
                // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        Log.i(TAG, "mGoogleApiClient after creation -- Handheld");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "mGoogleApiClient before connect -- Handheld");
        mGoogleApiClient.connect();
        Log.i(TAG, "mGoogleApiClient after connect -- Handheld");
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "mGoogleApiClient before disconnect -- Handheld");
        mGoogleApiClient.disconnect();
        Log.i(TAG, "mGoogleApiClient after disconnect -- Handheld");
        super.onStop();
    }

    private void sendDataToWearable(int resource)
    {
        try
        {
            Log.i(TAG, "Before send -- Handheld");
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
            Asset asset = createAssetFromBitmap(bitmap);
            PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
            Log.i(TAG,asset.toString());
            dataMap.getDataMap().putAsset("profileImage", asset);
            dataMap.getDataMap().putLong("timeStamp", System.currentTimeMillis());
            PutDataRequest request = dataMap.asPutDataRequest();
            //request.setUrgent();  //NEVER USE THIS, IT PASSES DATAEVENT FROM THE TYPE DELETED TO THE WEAR
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);

            Log.i(TAG, "After send -- Handheld");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.i(TAG, "Exception -- Handheld : " + ex.getStackTrace().toString());
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}
