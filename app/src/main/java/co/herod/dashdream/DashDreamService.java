package co.herod.dashdream;

import android.service.dreams.DreamService;

/**
 * Created by matthewherod on 27/05/15.
 */
public class DashDreamService extends DreamService {

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Exit dream upon user touch
        setInteractive(false);
        // Hide system UI
        setFullscreen(true);
        // Set the dream layout
        setContentView(R.layout.dream);

    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
    }
}
