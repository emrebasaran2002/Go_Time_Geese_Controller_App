package uw.gotimegeese.controllerapp.game_activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DPadView extends View {

    public interface OnDirectionChangedListener {

        /**
         * @param direction 0 = down, 1 = right, 2 = up, 3 = left, -1 = no input.
         */
        void onDirectionChanged(int direction);
    }

    // Constants for the geometry of the widget's visual appearance.
    private static final float DPAD_CENTER_OFFSET = 0.045f;
    private static final float DPAD_RATIO_1 = 0.35f;
    private static final float DPAD_RATIO_2 = 0.505f;
    private static final float DPAD_RATIO_3 = (float) (DPAD_RATIO_1 * Math.sqrt(2));
    private static final float CONTOUR_RATIO = 0.07f;

    // Constants for the colors we use when drawing.
    private static final int DPAD_COLOR_NORMAL = Color.parseColor("#c7c7c7");
    private static final int DPAD_COLOR_PRESSED = Color.parseColor("#8f8f8f");
    private static final int CONTOUR_COLOR = Color.parseColor("#1c1c1c");

    // The amount in DIPs by which the location of the user's finger must change before we
    // consider it to have moved.
    private static final float TOUCH_PRECISION_DIP = 1.5f;

    private final Path path = new Path();
    private final Paint dPadPaint = new Paint(), contourPaint = new Paint();

    private float touchPrecisionPx;
    private float touchX = -1, touchY = -1;
    private float touchDir = -1;

    private OnDirectionChangedListener listener;

    public DPadView(Context context) {
        super(context);
        _init();
    }

    public DPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        _init();
    }

    public DPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _init();
    }

    private void _init() {
        // Prepare the Paint objects we use to draw.
        dPadPaint.setAntiAlias(true);
        dPadPaint.setStyle(Paint.Style.FILL);
        contourPaint.setAntiAlias(true);
        contourPaint.setStyle(Paint.Style.STROKE);
        contourPaint.setColor(CONTOUR_COLOR);

        // Compute the precision used in handling touch input from the device's display metrics.
        touchPrecisionPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOUCH_PRECISION_DIP,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(_measure(widthMeasureSpec), _measure(heightMeasureSpec));
    }

    /**
     * Intended to only be used in the implementation for {@code onMeasure()}. Determines if the
     * default size of 300px is permissible under the supplied measure spec and returns an
     * appropriate measurement.
     */
    private int _measure(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY ||
                (mode == MeasureSpec.AT_MOST && size < 300)){
            return size;
        }
        return 300;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Calculate the parameters we'll use to draw the widget.
        float cx = getWidth() * 0.5f;
        float cy = getHeight() * 0.5f;
        float radius = Math.min(cx, cy);

        // Draw the D-Pad portion of the widget.
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            path.rewind();
            path.moveTo(cx + (i % 2 == 0 ? 0 : 2 - i) * radius * DPAD_CENTER_OFFSET,
                    cy + (i % 2 == 0 ? 1 - i : 0) * radius * DPAD_CENTER_OFFSET);
            _rLineTo(path, radius * DPAD_RATIO_1, angle - (Math.PI / 4));
            _rLineTo(path, radius * DPAD_RATIO_2, angle);
            _rLineTo(path, radius * DPAD_RATIO_3, angle + (Math.PI / 2));
            _rLineTo(path, radius * DPAD_RATIO_2, angle - Math.PI);
            path.close();
            dPadPaint.setColor(touchDir == i ? DPAD_COLOR_PRESSED : DPAD_COLOR_NORMAL);
            canvas.drawPath(path, dPadPaint);
        }

        // Draw the contour around the perimeter of the widget.
        contourPaint.setStrokeWidth(radius * CONTOUR_RATIO);
        canvas.drawCircle(cx, cy, radius * (1 - 0.5f * CONTOUR_RATIO), contourPaint);
    }

    /**
     * Intended to only be used in the implementation for {@code onDraw()}. Draws a line from the
     * current location in the path at the given bearing.
     */
    private void _rLineTo(@NonNull Path path, float length, double angle) {
        path.rLineTo((float) (length * Math.sin(angle)), (float) (length * Math.cos(angle)));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            touchDir = -1;
            if (listener != null) {
                listener.onDirectionChanged(-1);
            }
            invalidate();
        } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float newX = event.getX();
            float newY = event.getY();

            // Only handle the change if the we currently don't have a direction or if the user's
            // finger has moved a significant enough distance to warrant an update.
            if (touchDir == -1 || Math.abs(touchX - newX) > touchPrecisionPx
                    || Math.abs(touchY - newY) > touchPrecisionPx) {
                // First and foremost, update the position of the user's finger.
                touchX = newX;
                touchY = newY;

                // Compute the new direction from the position of the user's finger.
                int newDir;
                float dx = newX - getWidth() * 0.5f;
                float dy = newY - getHeight() * 0.5f;
                if (Math.abs(dx) > Math.abs(dy)) {
                    newDir = dx > 0 ? 1 : 3;
                } else {
                    newDir = dy > 0 ? 0 : 2;
                }

                // If needed, update the client with the new direction and invalidate the view.
                if (touchDir != newDir) {
                    touchDir = newDir;
                    if (listener != null) {
                        listener.onDirectionChanged(newDir);
                    }
                    invalidate();
                }
            }
        }
        return true;
    }

    /**
     * Sets the {@code OnDirectionChangedListener} to be notified when the directional input
     * supplied by the user changes. You may also pass {@code null} to disconnect a listener you
     * set previously.
     */
    public void setListener(@Nullable OnDirectionChangedListener listener) {
        this.listener = listener;
    }
}
