package com.thoughtworks.wechatmoment.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCache implements IImageCacahe {
    private LruCache<String, Bitmap> mLruCache;
    static String mCacheDir = "sdcard/cache/";

    public ImageCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    @Override
    public Bitmap getFromCache(String url) {
        Bitmap bitmap = mLruCache.get(url);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(mCacheDir + url);
        }
        return bitmap;
    }

    @Override
    public void addToCache(Bitmap bitmap, String url) throws IOException {
        addToLruCache(bitmap, url);
        addToDiskCache(bitmap, url);
    }

    private void addToLruCache(Bitmap bitmap, String url) {
        if (getLruCache(url) == null) {
            mLruCache.put(url, bitmap);
        }
    }

    private Bitmap getLruCache(String url) {
        return mLruCache.get(url);
    }

    private void addToDiskCache(Bitmap bitmap, String url) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mCacheDir + url);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
