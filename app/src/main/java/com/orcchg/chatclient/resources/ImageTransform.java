package com.orcchg.chatclient.resources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.IntDef;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ImageTransform {
    public static final int CIRCLE_CROP = 0;
    public static final int TOP_CROP = 1;
    public static final int CENTER_CROP = 2;
    public static final int BOTTOM_CROP = 3;
    @IntDef({CIRCLE_CROP, TOP_CROP, CENTER_CROP, BOTTOM_CROP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CropType {}

    public static Transformation<Bitmap> create(Context context, @CropType int cropType) {
        switch (cropType) {
            case CIRCLE_CROP:
                return new CropCircleTransformation(context);
            default:
                return new CropTransform(cropType, context);
        }
    }

    /* Transformations */
    // --------------------------------------------------------------------------------------------
    public static class CropCircleTransformation implements Transformation<Bitmap> {

        private BitmapPool mBitmapPool;

        public CropCircleTransformation(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public CropCircleTransformation(BitmapPool pool) {
            this.mBitmapPool = pool;
        }

        @Override
        public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
            Bitmap source = resource.get();
            int size = Math.min(source.getWidth(), source.getHeight());

            int width = (source.getWidth() - size) / 2;
            int height = (source.getHeight() - size) / 2;

            Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            if (width != 0 || height != 0) {
                // source isn't square, move viewport to center
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            return BitmapResource.obtain(bitmap, mBitmapPool);
        }

        @Override public String getId() {
            return "com.orcchg.chatclient.resources.ImageTransform: CropCircleTransformation";
        }
    }

    public static class CropTransform extends BitmapTransformation {
        private final @CropType int mCropType;

        public CropTransform(@CropType int cropType, Context context) {
            super(context);
            mCropType = cropType;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            final Bitmap toReuse = pool.get(outWidth, outHeight, toTransform.getConfig() != null ? toTransform.getConfig() : Bitmap.Config.ARGB_8888);
            Bitmap transformed = crop(toReuse, toTransform, outWidth, outHeight);
            if (toReuse != null && toReuse != transformed && !pool.put(toReuse)) {
                toReuse.recycle();
            }
            return transformed;
        }

        private Bitmap crop(Bitmap recycled, Bitmap toTransform, int outWidth, int outHeight) {
            if (toTransform == null) {
                return null;
            } else if (toTransform.getWidth() == outWidth && toTransform.getHeight() == outHeight) {
                return toTransform;
            }

            final float scale;
            float dx = 0, dy = 0, offsetFactor = 0.0f;
            Matrix m = new Matrix();
            if (toTransform.getWidth() * outHeight > outWidth * toTransform.getHeight()) {
                scale = (float) outHeight / (float) toTransform.getHeight();
                dx = (outWidth - toTransform.getWidth() * scale) * 0.5f;
            } else {
                scale = (float) outWidth / (float) toTransform.getWidth();
                switch (mCropType) {
                    case TOP_CROP:
                        dy = 0;
                        break;
                    default:
                    case CENTER_CROP:
                        dy = (outHeight - toTransform.getHeight() * scale) * 0.5f;
                        offsetFactor = 0.5f;
                        break;
                    case BOTTOM_CROP:
                        dy = (outHeight - toTransform.getHeight() * scale);
                        break;
                }
            }

            m.setScale(scale, scale);
            m.postTranslate((int) (dx + 0.5f), (int) (dy + offsetFactor));
            final Bitmap result;
            if (recycled != null) {
                result = recycled;
            } else {
                result = Bitmap.createBitmap(outWidth, outHeight, getSafeConfig(toTransform));
            }

            // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
            TransformationUtils.setAlpha(toTransform, result);

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(toTransform, m, paint);
            return result;
        }

        @Override public String getId() {
            return "com.orcchg.chatclient.resources.ImageTransform: CropTransform_" + mCropType;
        }
    }

    /* Internals */
    // --------------------------------------------------------------------------------------------
    private static Bitmap.Config getSafeConfig(Bitmap bitmap) {
        return bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
    }
}
