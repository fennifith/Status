package com.james.status.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public class ViewRenderTask extends AsyncTask<Integer, Void, Bitmap> {

    private final WeakReference<Renderable> reference;

    public ViewRenderTask(Renderable renderable) {
        reference = new WeakReference<>(renderable);
    }

    @Override
    protected Bitmap doInBackground(Integer... integers) {
        if (integers[0] == null || integers[0] < 1 || integers[1] == null || integers[1] < 1)
            return null;

        Bitmap bitmap = Bitmap.createBitmap(integers[0], integers[1], Bitmap.Config.ARGB_8888);
        Renderable renderable = reference.get();
        if (renderable != null)
            renderable.render(new Canvas(bitmap));

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Renderable renderable = reference.get();
        if (renderable != null)
            renderable.onRendered(bitmap);
    }

    public interface Renderable {
        void render(Canvas canvas);

        void onRendered(@Nullable Bitmap bitmap);
    }

}
