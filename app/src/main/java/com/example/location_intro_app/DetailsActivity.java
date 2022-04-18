package com.example.location_intro_app;

import static android.provider.MediaStore.Video.Thumbnails.VIDEO_ID;

import android.content.res.Configuration;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.location_intro_app.databinding.ActivityDetailsBinding;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class DetailsActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    private ActivityDetailsBinding binding;

    private ArrayList<Integer> images;

    private String title;

    private String details;

    private String videoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;

        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        images = getIntent().getIntegerArrayListExtra("images");
        title = getIntent().getStringExtra("title");
        details = getIntent().getStringExtra("details");
        toolBarLayout.setTitle(title);
        TextView detailsView = findViewById(R.id.details);
        if (detailsView != null)
            detailsView.setText(details);

        ViewPager viewPager = findViewById(R.id.view_pager);
        if (viewPager != null) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(this, images);
            viewPager.setAdapter(adapter);
        }

        videoID = getIntent().getStringExtra("videoID");
        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager()
                .findFragmentById(R.id.youtubePlayerFragment);

//        Properties properties = new Properties();
//        InputStream inputStream =
//                Objects.requireNonNull(this.getClass().getClassLoader()).getResourceAsStream("local.properties");
//        try {
//            properties.load(inputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        youTubePlayerFragment.initialize("AIzaSyC1AtaTAZ5B3imcXECMjuk8iDlPMlxIKsU", this);
    }

    /**
     *
     * @param provider The provider which was used to initialize the YouTubePlayer
     * @param youTubePlayer A YouTubePlayer which can be used to control video playback in the provider.
     * @param wasRestored Whether the player was restored from a previously saved state, as part of the YouTubePlayerView
     *                    or YouTubePlayerFragment restoring its state. true usually means playback is resuming from where
     *                    the user expects it would, and that a new video should not be loaded
     */
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {

        youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION |
                YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);


        if(!wasRestored) {
            youTubePlayer.cueVideo(videoID);
        }
        youTubePlayer.setFullscreenControlFlags(0);
    }

    /**
     *
     * @param provider The provider which failed to initialize a YouTubePlayer.
     * @param error The reason for this failure, along with potential resolutions to this failure.
     */
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {

        final int REQUEST_CODE = 1;

        if(error.isUserRecoverableError()) {
            error.getErrorDialog(this,REQUEST_CODE).show();
        } else {
            String errorMessage = String.format("There was an error initializing the YoutubePlayer (%1$s)", error.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
}