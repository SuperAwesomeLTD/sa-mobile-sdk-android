package tv.superawesome.lib.saevents;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import tv.superawesome.lib.sautils.SAUtils;

public class SAViewTimeModule {
    private final static int DELAY = 1000;

    private Handler handler = new Handler();
    private Runnable runnable = () -> {
        View view = getView();

        if (view == null || view.getParent() == null) {
            stop();
            return;
        }

        if (SAUtils.isViewVisibleOnScreen(view)) {
            increment();
        } else {
            Log.d("SuperAwesome", "SAViewTimeModule.not-visible");
        }

        triggerDelay();
    };
    private AtomicInteger totalVisibleTimeInSeconds = new AtomicInteger();
    private WeakReference<View> viewWeakReference;
    private Listener listener;

    public void observeViewTime(View view, Listener listener) {
        this.viewWeakReference = new WeakReference<>(view);
        this.listener = listener;
        this.totalVisibleTimeInSeconds.set(0);

        triggerDelay();
    }

    synchronized public void stop() {
        Log.d("SuperAwesome", "SAViewTimeModule.stop:" + totalVisibleTimeInSeconds);
        handler.removeCallbacks(runnable);
        viewWeakReference = null;

        int total = totalVisibleTimeInSeconds.getAndSet(0);
        triggerEvent(total);
    }

    private void increment() {
        Log.d("SuperAwesome", "SAViewTimeModule.increment:" + totalVisibleTimeInSeconds);
        totalVisibleTimeInSeconds.incrementAndGet();
    }

    private void triggerEvent(int total) {
        if (listener != null && total > 0) {
            listener.onViewTimeObserved(total);
        }
    }

    private void triggerDelay() {
        handler.postDelayed(runnable, DELAY);
    }

    private View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }

    public interface Listener {
        void onViewTimeObserved(int seconds);
    }
}
