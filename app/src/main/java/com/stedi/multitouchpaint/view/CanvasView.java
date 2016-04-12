package com.stedi.multitouchpaint.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.Config;
import com.stedi.multitouchpaint.history.Brush;
import com.stedi.multitouchpaint.history.HistoryItem;
import com.stedi.multitouchpaint.history.Pointer;

import java.util.ArrayList;
import java.util.HashMap;

public class CanvasView extends View {
    private ArrayList<HistoryItem> history = new ArrayList<>();
    private HashMap<Integer, HistoryItem> pointerDownItems = new HashMap<>();
    private HashMap<Integer, Pointer> currentPointers = new HashMap<>();

    private Paint paint;
    private Bitmap cachedOldBitmap, oldBitmap, historyBitmap;
    private Canvas cachedOldCanvas, historyCanvas;

    private boolean invalidateOnUndo, cachedOldBitmapUpdated;

    private Pipette pipette;
    private Bitmap bitmapForPipette;

    private boolean pipetteMode;

    private Brush brush;

    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.WHITE);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            // pointers down
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (pipetteMode) {
                    onPipetteMove(event);
                } else {
                    onPathPointersDown(event);
                }
                break;
            }

            // pointers move
            case MotionEvent.ACTION_MOVE: {
                if (pipetteMode) {
                    onPipetteMove(event);
                } else {
                    onPathPointersMove(event);
                }
                break;
            }

            // pointers up
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                if (!pipetteMode) {
                    onPathPointersUp(event);
                }
                break;
            }

            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas viewCanvas) {
        if (pipetteMode) {
            onDrawPipette(viewCanvas);
        } else {
            onDrawPath(viewCanvas);
        }
    }

    public void setBrush(Brush brush) {
        this.brush = brush;
    }

    public void undo() {
        if (history.size() > 0) {
            invalidateOnUndo = true;
            invalidate();
        }
    }

    public Bitmap generatePicture() {
        Bitmap bitmap = createFullBitmap();
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public void clearPicture() {
        if (cachedOldBitmap != null) {
            cachedOldBitmap.recycle();
            cachedOldBitmap = null;
        }
        if (oldBitmap != null) {
            oldBitmap.recycle();
            oldBitmap = null;
        }
        if (historyBitmap != null) {
            historyBitmap.recycle();
            historyBitmap = null;
        }
        history.clear();
        pointerDownItems.clear();
        currentPointers.clear();
        invalidate();
    }

    public void setPicture(Bitmap bitmap) {
        clearPicture();

        cachedOldBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        if (!cachedOldBitmap.isMutable())
            cachedOldBitmap = cachedOldBitmap.copy(Bitmap.Config.ARGB_8888, true);
        cachedOldCanvas = new Canvas(cachedOldBitmap);
        oldBitmap = cachedOldBitmap.copy(Bitmap.Config.ARGB_8888, false);

        invalidate();
    }

    public void activatePipette() {
        togglePipetteMode(true);
    }

    public void disablePipette() {
        togglePipetteMode(false);
    }

    public boolean inPipetteMode() {
        return pipetteMode;
    }

    public boolean isDrawing() {
        return currentPointers.size() > 0;
    }

    /**
     * Create history item and draw dot
     */
    private void onPathPointersDown(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        float x = event.getX(actionIndex);
        float y = event.getY(actionIndex);

        // new history and pointer item
        HistoryItem item = new HistoryItem(brush);
        item.setStatus(HistoryItem.Status.ON_VIEW_CANVAS);
        history.add(item);
        pointerDownItems.put(pointerId, item);
        currentPointers.put(pointerId, new Pointer(x + 0.1f, y));

        // draw fake dot
        Path path = item.getPath();
        path.moveTo(x, y);
        path.lineTo(x + 0.1f, y);
        invalidate();
    }

    /**
     * Draw smooth path for every pointer
     */
    private void onPathPointersMove(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            Pointer pointer = currentPointers.get(pointerId);
            float x = event.getX(i);
            float y = event.getY(i);

            if (Math.abs(x - pointer.getX()) >= Config.TOUCH_MOVE_ACCURACY ||
                    Math.abs(y - pointer.getY()) >= Config.TOUCH_MOVE_ACCURACY) {
                HistoryItem item = pointerDownItems.get(pointerId);

                // draw smooth path
                Path path = item.getPath();
                path.quadTo(pointer.getX(), pointer.getY(),
                        (x + pointer.getX()) / 2, (y + pointer.getY()) / 2);
                invalidate();

                pointer.set(x, y);
            }
        }
    }

    /**
     * Mark history item to be drawn on bitmap
     */
    private void onPathPointersUp(MotionEvent event) {
        int pointerId = event.getPointerId(event.getActionIndex());

        HistoryItem item = pointerDownItems.get(pointerId);
        item.setStatus(HistoryItem.Status.ON_BITMAP_CANVAS);

        pointerDownItems.remove(pointerId);
        currentPointers.remove(pointerId);

        invalidate();
    }

    /**
     * Main draw logic (suitable for hardware acceleration on/off)
     */
    private void onDrawPath(Canvas viewCanvas) {
        // update history bitmap and old bitmap (if needed) on undo
        if (invalidateOnUndo) {
            invalidateOnUndo = false;

            if (cachedOldBitmap != null && cachedOldBitmapUpdated) {
                oldBitmap = cachedOldBitmap.copy(Bitmap.Config.ARGB_8888, false);
                cachedOldBitmapUpdated = false;
            }

            historyBitmap.eraseColor(Color.TRANSPARENT);
            history.remove(history.size() - 1);

            for (HistoryItem item : history) {
                drawHistoryItem(item, historyCanvas);
            }
        }

        // draw bitmap containing deleted items (from cached bitmap)
        if (oldBitmap != null) {
            viewCanvas.drawBitmap(oldBitmap, 0, 0, null);
        }

        // draw items which was previously current on history bitmap
        for (HistoryItem item : history) {
            if (item.getStatus() == HistoryItem.Status.ON_BITMAP_CANVAS) {
                if (historyBitmap == null) {
                    historyBitmap = createFullBitmap();
                    historyCanvas = new Canvas(historyBitmap);
                }

                drawHistoryItem(item, historyCanvas);
                item.setStatus(HistoryItem.Status.READY_TO_DELETE);
            }
        }

        // draw bitmap containing history items (and more, if no undo was done)
        if (historyBitmap != null) {
            viewCanvas.drawBitmap(historyBitmap, 0, 0, null);
        }

        // draw only current pointers on view canvas
        for (HistoryItem item : history) {
            if (item.getStatus() == HistoryItem.Status.ON_VIEW_CANVAS) {
                drawHistoryItem(item, viewCanvas);
            }
        }

        // remove and cache in bitmap old history item
        // only one per onDraw()
        if (history.size() > Config.MAX_TOUCH_HISTORY) {
            HistoryItem item = history.get(0);
            if (item.getStatus() == HistoryItem.Status.READY_TO_DELETE) {
                if (cachedOldBitmap == null) {
                    cachedOldBitmap = createFullBitmap();
                    cachedOldCanvas = new Canvas(cachedOldBitmap);
                }

                drawHistoryItem(item, cachedOldCanvas);
                history.remove(item);

                cachedOldBitmapUpdated = true;
            }
        }
    }

    /**
     * Move logic for pipette mode
     */
    private void onPipetteMove(MotionEvent event) {
        float x = event.getX(event.getActionIndex());
        float y = event.getY(event.getActionIndex());
        pipette.move(x, y);
        invalidate();
    }

    /**
     * Draw logic for pipette mode
     */
    private void onDrawPipette(Canvas viewCanvas) {
        viewCanvas.drawBitmap(bitmapForPipette, 0, 0, null);
        pipette.draw(viewCanvas);
    }

    private void togglePipetteMode(boolean toggle) {
        if (pipetteMode == toggle)
            return;

        if (toggle) {
            bitmapForPipette = generatePicture();
            pipette = new Pipette(bitmapForPipette.getWidth() / 2, bitmapForPipette.getHeight() / 2, bitmapForPipette);
            pipetteMode = true;
        } else {
            bitmapForPipette.recycle();
            bitmapForPipette = null;
            Brush newBrush = Brush.copy(brush);
            newBrush.setColor(pipette.getColor());
            App.getBus().post(newBrush);
            pipette = null;
            pipetteMode = false;
        }

        invalidate();
    }

    private void drawHistoryItem(HistoryItem item, Canvas canvas) {
        paint.setStrokeWidth(item.getBrush().getThicknessPx());
        paint.setColor(item.getBrush().getColor());
        canvas.drawPath(item.getPath(), paint);
    }

    private Bitmap createFullBitmap() {
        return Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    }
}
