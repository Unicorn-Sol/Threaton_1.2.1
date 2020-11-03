package com.theatron2.uiforthreaton.ui.feed;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.PriorityTaskManager;
import com.google.android.exoplayer2.util.Util;

import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_AUDIO_BUFFER_SIZE;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_METADATA_BUFFER_SIZE;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_MUXED_BUFFER_SIZE;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_TEXT_BUFFER_SIZE;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_VIDEO_BUFFER_SIZE;

@SuppressWarnings("ALL")
class CustomLoadControl implements LoadControl {

    public static final int DEFAULT_MIN_BUFFER_MS = 2500;

    public static final int DEFAULT_MAX_BUFFER_MS = 5000;
    public static final int DEFAULT_BUFFER_FOR_PLAYBACK_MS = 2500;
    public static final int DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2500;

    public static final int DEFAULT_TARGET_BUFFER_BYTES = C.LENGTH_UNSET;

    public static final boolean DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS = true;
    private static final int ABOVE_HIGH_WATERMARK = 0;
    private static final int BETWEEN_WATERMARKS = 1;
    private static final int BELOW_LOW_WATERMARK = 2;
    public static final class Builder {

    private DefaultAllocator allocator;
    private int minBufferMs;
    private int maxBufferMs;
    private int bufferForPlaybackMs;
    private int bufferForPlaybackAfterRebufferMs;
    private int targetBufferBytes;
    private boolean prioritizeTimeOverSizeThresholds;
    private PriorityTaskManager priorityTaskManager;


    public Builder() {
        allocator = null;
        minBufferMs = DEFAULT_MIN_BUFFER_MS;
        maxBufferMs = DEFAULT_MAX_BUFFER_MS;
        bufferForPlaybackMs = DEFAULT_BUFFER_FOR_PLAYBACK_MS;
        bufferForPlaybackAfterRebufferMs = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
        targetBufferBytes = DEFAULT_TARGET_BUFFER_BYTES;
        prioritizeTimeOverSizeThresholds = DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS;
        priorityTaskManager = null;
    }


    public Builder setAllocator(DefaultAllocator allocator) {
        this.allocator = allocator;
        return this;
    }

    public Builder setBufferDurationsMs(
            int minBufferMs,
            int maxBufferMs,
            int bufferForPlaybackMs,
            int bufferForPlaybackAfterRebufferMs) {
        this.minBufferMs = minBufferMs;
        this.maxBufferMs = maxBufferMs;
        this.bufferForPlaybackMs = bufferForPlaybackMs;
        this.bufferForPlaybackAfterRebufferMs = bufferForPlaybackAfterRebufferMs;
        return this;
    }

    public Builder setTargetBufferBytes(int targetBufferBytes) {
        this.targetBufferBytes = targetBufferBytes;
        return this;
    }
    public Builder setPrioritizeTimeOverSizeThresholds(boolean prioritizeTimeOverSizeThresholds) {
        this.prioritizeTimeOverSizeThresholds = prioritizeTimeOverSizeThresholds;
        return this;
    }


    public Builder setPriorityTaskManager(PriorityTaskManager priorityTaskManager) {
        this.priorityTaskManager = priorityTaskManager;
        return this;
    }

    public CustomLoadControl createDefaultLoadControl() {
        if (allocator == null) {
            allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        }
        return new CustomLoadControl(
                allocator,
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForPlaybackAfterRebufferMs,
                targetBufferBytes,
                prioritizeTimeOverSizeThresholds,
                priorityTaskManager);
        }
    }

    private final DefaultAllocator allocator;

    private final long minBufferUs;
    private final long maxBufferUs;
    private final long bufferForPlaybackUs;
    private final long bufferForPlaybackAfterRebufferUs;
    private final int targetBufferBytesOverwrite;
    private final boolean prioritizeTimeOverSizeThresholds;
    private final PriorityTaskManager priorityTaskManager;

    private int targetBufferSize;
    private boolean isBuffering;

    public CustomLoadControl() {
        this(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE));
    }


    @Deprecated
    public CustomLoadControl(DefaultAllocator allocator) {
        this(
                allocator,
                DEFAULT_MIN_BUFFER_MS,
                DEFAULT_MAX_BUFFER_MS,
                DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                DEFAULT_TARGET_BUFFER_BYTES,
                DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS);
    }


    @Deprecated
    public CustomLoadControl(
            DefaultAllocator allocator,
            int minBufferMs,
            int maxBufferMs,
            int bufferForPlaybackMs,
            int bufferForPlaybackAfterRebufferMs,
            int targetBufferBytes,
            boolean prioritizeTimeOverSizeThresholds) {
        this(
                allocator,
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForPlaybackAfterRebufferMs,
                targetBufferBytes,
                prioritizeTimeOverSizeThresholds,
                null);
    }

    @Deprecated
    public CustomLoadControl(
        DefaultAllocator allocator,
        int minBufferMs,
        int maxBufferMs,
        int bufferForPlaybackMs,
        int bufferForPlaybackAfterRebufferMs,
        int targetBufferBytes,
        boolean prioritizeTimeOverSizeThresholds,
        PriorityTaskManager priorityTaskManager) {
        assertGreaterOrEqual(bufferForPlaybackMs, 0, "bufferForPlaybackMs", "0");
        assertGreaterOrEqual(
                bufferForPlaybackAfterRebufferMs, 0, "bufferForPlaybackAfterRebufferMs", "0");
        assertGreaterOrEqual(minBufferMs, bufferForPlaybackMs, "minBufferMs", "bufferForPlaybackMs");
        assertGreaterOrEqual(
                minBufferMs,
                bufferForPlaybackAfterRebufferMs,
                "minBufferMs",
                "bufferForPlaybackAfterRebufferMs");
        assertGreaterOrEqual(maxBufferMs, minBufferMs, "maxBufferMs", "minBufferMs");

        this.allocator = allocator;
        minBufferUs = minBufferMs * 1000L;
        maxBufferUs = maxBufferMs * 1000L;
        bufferForPlaybackUs = bufferForPlaybackMs * 1000L;
        bufferForPlaybackAfterRebufferUs = bufferForPlaybackAfterRebufferMs * 1000L;
        targetBufferBytesOverwrite = targetBufferBytes;
        this.prioritizeTimeOverSizeThresholds = prioritizeTimeOverSizeThresholds;
        this.priorityTaskManager = priorityTaskManager;
    }

    @Override
    public void onPrepared() {
        reset(false);
    }

    @Override
    public void onTracksSelected(Renderer[] renderers, TrackGroupArray trackGroups,
                                 TrackSelectionArray trackSelections) {
        targetBufferSize =
                targetBufferBytesOverwrite == C.LENGTH_UNSET
                        ? calculateTargetBufferSize(renderers, trackSelections)
                        : targetBufferBytesOverwrite;
        allocator.setTargetBufferSize(targetBufferSize);
    }

    @Override
    public void onStopped() {
        reset(true);
    }

    @Override
    public void onReleased() {
        reset(true);
    }

    @Override
    public Allocator getAllocator() {
        return allocator;
    }

    @Override
    public long getBackBufferDurationUs() {
        return 0;
    }

    @Override
    public boolean retainBackBufferFromKeyframe() {
        return false;
    }

    @Override
    public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
//        boolean targetBufferSizeReached = allocator.getTotalBytesAllocated() >= targetBufferSize;
//        boolean wasBuffering = isBuffering;
//        long minBufferUs = this.minBufferUs;
//        if (playbackSpeed > 1) {
//            long mediaDurationMinBufferUs =
//                    Util.getMediaDurationForPlayoutDuration(minBufferUs, playbackSpeed);
//            minBufferUs = Math.min(mediaDurationMinBufferUs, maxBufferUs);
//        }
//        if (bufferedDurationUs < minBufferUs) {
//            isBuffering = prioritizeTimeOverSizeThresholds || !targetBufferSizeReached;
//        } else if (bufferedDurationUs > maxBufferUs || targetBufferSizeReached) {
//            isBuffering = false;
//        }
//        if (priorityTaskManager != null && isBuffering != wasBuffering) {
//            if (isBuffering) {
//                priorityTaskManager.add(C.PRIORITY_PLAYBACK);
//            } else {
//                priorityTaskManager.remove(C.PRIORITY_PLAYBACK);
//            }
//        }
        boolean wasBuffering = isBuffering;
        computeIsBuffering(bufferedDurationUs);
        if(priorityTaskManager!=null && isBuffering!=wasBuffering){
            if(isBuffering){
                priorityTaskManager.add(C.PRIORITY_PLAYBACK);
            }
            else{
                priorityTaskManager.remove(C.PRIORITY_PLAYBACK);
            }
        }
        return isBuffering;
    }

    private void computeIsBuffering(long bufferedDurationUs){
        int bufferTimeState = getBufferTimeState(bufferedDurationUs);
        boolean targetBufferSizeReached = allocator.getTotalBytesAllocated()>=targetBufferSize;
        if (bufferTimeState == BELOW_LOW_WATERMARK){
            isBuffering = true;
        }
        else if (bufferTimeState  == BETWEEN_WATERMARKS){
            isBuffering = !targetBufferSizeReached;
        }
        else{
            isBuffering = false;
        }

    }
    private int getBufferTimeState(long bufferedDurationUs) {
        return bufferedDurationUs > maxBufferUs ? ABOVE_HIGH_WATERMARK
                : (bufferedDurationUs < minBufferUs ? BELOW_LOW_WATERMARK : BETWEEN_WATERMARKS);
    }

    @Override
    public boolean shouldStartPlayback(
            long bufferedDurationUs, float playbackSpeed, boolean rebuffering) {
        bufferedDurationUs = Util.getPlayoutDurationForMediaDuration(bufferedDurationUs, playbackSpeed);
        long minBufferDurationUs = rebuffering ? bufferForPlaybackAfterRebufferUs : bufferForPlaybackUs;
        return minBufferDurationUs <= 0
                || bufferedDurationUs >= minBufferDurationUs
                || (!prioritizeTimeOverSizeThresholds
                && allocator.getTotalBytesAllocated() >= targetBufferSize);
    }

    protected int calculateTargetBufferSize(
            Renderer[] renderers, TrackSelectionArray trackSelectionArray) {
        int targetBufferSize = 0;
        for (int i = 0; i < renderers.length; i++) {
            if (trackSelectionArray.get(i) != null) {
                targetBufferSize += getDefaultBufferSize(renderers[i].getTrackType());
            }
        }
        return targetBufferSize;
    }

    private void reset(boolean resetAllocator) {
        targetBufferSize = 0;
        if (priorityTaskManager != null && isBuffering) {
            priorityTaskManager.remove(C.PRIORITY_PLAYBACK);
        }
        isBuffering = false;
        if (resetAllocator) {
            allocator.reset();
        }
    }

    private static void assertGreaterOrEqual(int value1, int value2, String name1, String name2) {
        Assertions.checkArgument(value1 >= value2, name1 + " cannot be less than " + name2);
    }



    public static int getDefaultBufferSize(int trackType) {
        switch (trackType) {
            case C.TRACK_TYPE_DEFAULT:
                return DEFAULT_MUXED_BUFFER_SIZE;
            case C.TRACK_TYPE_AUDIO:
                return DEFAULT_AUDIO_BUFFER_SIZE;
            case C.TRACK_TYPE_VIDEO:
                return DEFAULT_VIDEO_BUFFER_SIZE;
            case C.TRACK_TYPE_TEXT:
                return DEFAULT_TEXT_BUFFER_SIZE;
            case C.TRACK_TYPE_METADATA:
                return DEFAULT_METADATA_BUFFER_SIZE;
            default:
                throw new IllegalStateException();
        }
    }
}
