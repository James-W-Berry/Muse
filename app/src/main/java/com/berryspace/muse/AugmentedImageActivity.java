package com.berryspace.muse;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.berryspace.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector.ConnectionListener;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class AugmentedImageActivity extends AppCompatActivity {

    private static final String TAG = "AugmentedImageActivity";
    private String currentlyPlaying = "";
    private ArFragment arFragment;
    private ImageView fitToScanView;
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "com.berryspace.muse://callback/";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    break;
                case R.id.navigation_camera:
                    Log.d(TAG, "already on the camera screen");
                    break;
            }
            return true;
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("AugmentedImageActivity", "Connected to Spotify");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("AugmentedImageActivity", throwable.getMessage(), throwable);
                    }
                });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has
                    // been detected,  but not yet tracked.

                    // Play album from detected image
                    if(!currentlyPlaying.equals(augmentedImage.getName())) {
                        Log.i(TAG, "was playing: "+ currentlyPlaying);
                        currentlyPlaying = augmentedImage.getName();
                        Log.i(TAG, "now playing: "+ currentlyPlaying);
                        String text = "Now Playing: " + augmentedImage.getName();
                        TextView heading = findViewById(R.id.nowPlaying);
                        heading.setText(text);
                        stopMusic();
                        String uri = augmentedImage.getName().replace(".jpg", "");
                        playMusic(uri);
                    }
                    break;
                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    AugmentedImageNode node = new AugmentedImageNode(this);
                    node.setImage(augmentedImage);
                    augmentedImageMap.put(augmentedImage, node);
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    private void playMusic(String spotifyUri) {
        Log.i(TAG + " playing Spotify URI:  ", spotifyUri);
        mSpotifyAppRemote.getPlayerApi().play(spotifyUri);
    }

    private void stopMusic() {
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}
