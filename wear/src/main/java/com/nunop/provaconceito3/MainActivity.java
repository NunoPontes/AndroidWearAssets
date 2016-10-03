package com.nunop.provaconceito3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.wearable.DataMap.TAG;


public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public ImageView imgView;
    private TextView mTextView;
    private Bitmap bitmap;
    private Asset profileAsset;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                imgView=(ImageView) findViewById(R.id.img);
            }
        });


        Log.i(TAG, "mGoogleApiClient before creation -- Wearable");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        Log.i(TAG, "mGoogleApiClient after creation -- Wearable");

        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "mGoogleApiClient after connect (OnResume) -- Wearable");
        mGoogleApiClient.connect();
        Log.i(TAG, "mGoogleApiClient after connect (OnResume) -- Wearable");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "mGoogleApiClient before removeListener & disconnect -- Wearable");
        //Wearable.DataApi.removeListener(mGoogleApiClient, this);
        //mGoogleApiClient.disconnect();
        Log.i(TAG, "mGoogleApiClient after removeListener & disconnect -- Wearable");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "mGoogleApiClient before addListener -- Wearable");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.i(TAG, "mGoogleApiClient after addListener -- Wearable");
        // Now you can use the Data Layer API
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("tag", "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("tag", "onConnectionFailed: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Enter this funciton when receives the asset

        Log.i(TAG, "onDataChanged before boucle event -- Wearable");


        //DataEvent event = dataEvents.get(0);
        for (DataEvent event : dataEvents)
        {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/image") == 0)
                {
                    Log.i(TAG, "onDataChanged before get Asset -- Wearable");
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
                    Log.i(TAG, "PRE ASYNC");
                    new DownloadImageTask().execute(profileAsset);
                    Log.i(TAG, "POS ASYNC");
                    Log.i(TAG, "onDataChanged after get Asset -- Wearable");
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();


        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }


    private class DownloadImageTask extends AsyncTask<Asset, Void, Bitmap> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected Bitmap doInBackground(Asset... profileAsset) {
            Log.i(TAG, "ASYNC BEGIN");
            return loadBitmapFromAsset(profileAsset[0]);
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Bitmap result) {
            ImageView mImageView =(ImageView) findViewById(R.id.img);
            mImageView.setImageBitmap(result);
            Log.i(TAG, "ASYNC FINISHED");
        }
    }
}