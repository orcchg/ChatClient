package com.orcchg.chatclient.resources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

public class ViewUtils {

    public static int getAttributeColor(final Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return context.getResources().getColor(typedValue.resourceId);
    }

    @Nullable
    public static Drawable getAttributeDrawable(final Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return context.getResources().getDrawable(typedValue.resourceId);
    }

    public static float getAttributeDimension(final Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return context.getResources().getDimension(typedValue.resourceId);
    }

    public static Bitmap getBitmapByUrl(Context context, String url, boolean isCircle) {
        Bitmap originalBitmap = null;
        int width = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int height = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        try {
            if (isCircle) {
                originalBitmap = Glide.with(context).load(url).asBitmap().transform(ImageTransform.create(context, ImageTransform.CIRCLE_CROP)).into(width, height).get();
            } else {
                originalBitmap = Glide.with(context).load(url).asBitmap().into(width, height).get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return originalBitmap;
    }
}
