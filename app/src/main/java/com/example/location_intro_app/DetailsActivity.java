package com.example.location_intro_app;

import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.location_intro_app.databinding.ActivityDetailsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    private ActivityDetailsBinding binding;

    private ArrayList<Integer> images;

    private String details;

    private String videoID;

    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;

        toolbar.inflateMenu(R.menu.main_menu);
        images = getIntent().getIntegerArrayListExtra("images");
        title = getIntent().getStringExtra("title");
        details = getIntent().getStringExtra("details");
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
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