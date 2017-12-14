package com.stedi.multitouchpaint.painters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.data.Brush;
import com.stedi.multitouchpaint.data.HistoryItem;
import com.stedi.multitouchpaint.data.Pointer;
import com.stedi.multitouchpaint.view.CanvasView;

import java.util.ArrayList;
import java.util.HashMap;

public class PathPainter extends Painter {
    private static PathPainter instance;

    private ArrayList<HistoryItem> history = new ArrayList<>();
    private HashMap<Integer, HistoryItem> pointerDownItems = new HashMap<>();
    private HashMap<Integer, Pointer> currentPointers = new HashMap<>();

    private Paint paint;
    private Bitmap cachedOldBitmap, oldBitmap, historyBitmap;
    private Canvas cachedOldCanvas, historyCanvas;

    private boolean invalidateOnUndo, cachedOldBitmapUpdated;

    private PathPainter() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public static PathPainter getInstance() {
        if (instance == null)
            instance = new PathPainter();
        return instance;
    }

    @Override
    public void onPointerDown(MotionEvent event, Brush brush, CanvasView canvasView) {
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
        canvasView.invalidate();
    }

    @Override
    public void onPointerMove(MotionEvent event, Brush brush, CanvasView canvasView) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            Pointer pointer = currentPointers.get(pointerId);
            float x = event.getX(i);
            float y = event.getY(i);

            if (Math.abs(x - pointer.getX()) >= App.Companion.getTouchMoveAccuracy() ||
                    Math.abs(y - pointer.getY()) >= App.Companion.getTouchMoveAccuracy()) {
                HistoryItem item = pointerDownItems.get(pointerId);

                // draw smooth path
                Path path = item.getPath();
                path.quadTo(pointer.getX(), pointer.getY(),
                        (x + pointer.getX()) / 2, (y + pointer.getY()) / 2);
                canvasView.invalidate();

                pointer.set(x, y);
            }
        }
    }

    @Override
    public void onPointerUp(MotionEvent event, Brush brush, CanvasView canvasView) {
        int pointerId = event.getPointerId(event.getActionIndex());

        HistoryItem item = pointerDownItems.get(pointerId);
        item.setStatus(HistoryItem.Status.ON_BITMAP_CANVAS);

        pointerDownItems.remove(pointerId);
        currentPointers.remove(pointerId);

        canvasView.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
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
            canvas.drawBitmap(oldBitmap, 0, 0, null);
        }

        // draw items which was previously current on history bitmap
        for (HistoryItem item : history) {
            if (item.getStatus() == HistoryItem.Status.ON_BITMAP_CANVAS) {
                if (historyBitmap == null) {
                    historyBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                    historyCanvas = new Canvas(historyBitmap);
                }

                drawHistoryItem(item, historyCanvas);
                item.setStatus(HistoryItem.Status.READY_TO_DELETE);
            }
        }

        // draw bitmap containing history items (and more, if no undo was done)
        if (historyBitmap != null) {
            canvas.drawBitmap(historyBitmap, 0, 0, null);
        }

        // draw only current pointers on view canvas
        for (HistoryItem item : history) {
            if (item.getStatus() == HistoryItem.Status.ON_VIEW_CANVAS) {
                drawHistoryItem(item, canvas);
            }
        }

        // remove and cache in bitmap old history item
        // only one per onDraw()
        if (history.size() > App.Companion.getMaxTouchHistory()) {
            HistoryItem item = history.get(0);
            if (item.getStatus() == HistoryItem.Status.READY_TO_DELETE) {
                if (cachedOldBitmap == null) {
                    cachedOldBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                    cachedOldCanvas = new Canvas(cachedOldBitmap);
                }

                drawHistoryItem(item, cachedOldCanvas);
                history.remove(item);

                cachedOldBitmapUpdated = true;
            }
        }
    }

    @Override
    public boolean onUndo() {
        if (history.size() > 0) {
            invalidateOnUndo = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onClear() {
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
        return true;
    }

    @Override
    public void onSetPicture(Bitmap bitmap, int canvasWidth, int canvasHeight) {
        cachedOldBitmap = Bitmap.createScaledBitmap(bitmap, canvasWidth, canvasHeight, true);
        if (!cachedOldBitmap.isMutable())
            cachedOldBitmap = cachedOldBitmap.copy(Bitmap.Config.ARGB_8888, true);
        cachedOldCanvas = new Canvas(cachedOldBitmap);
        oldBitmap = cachedOldBitmap.copy(Bitmap.Config.ARGB_8888, false);
    }

    @Override
    public boolean isDrawing() {
        return currentPointers.size() > 0;
    }

    private void drawHistoryItem(HistoryItem item, Canvas canvas) {
        paint.setStrokeWidth(item.getBrush().getThicknessPx());
        paint.setColor(item.getBrush().getColor());
        canvas.drawPath(item.getPath(), paint);
    }
}
