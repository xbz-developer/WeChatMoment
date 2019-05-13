package com.thoughtworks.wechatmoment.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.thoughtworks.wechatmoment.factory.ThreadPoolExecutorProxyFactory;
import com.thoughtworks.wechatmoment.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int SUCCESS_COMPLETE = 1;
    private static ImageLoader mImageLoader;
    private IImageCacahe mImageCache;
    static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_COMPLETE: {
                    LoaderResult result = (LoaderResult) msg.obj;
                    ImageView imageView = result.imageView;
                    String uri = (String) imageView.getTag();
                    if (uri.equals(result.url)) {
                        if (result.bitmap != null)
                            imageView.setImageBitmap(result.bitmap);
                    } else {
                        Log.e(TAG, "set image bitmap,but url has changed, ignored!");
                    }
                    break;
                }
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    };

    private ImageLoader() {
        mImageCache = new ImageCache();
    }

    public static ImageLoader getInstance() {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader();
                }
            }
        }
        return mImageLoader;
    }

    private Bitmap downloadBitmapFromUrl(String urlString) {
        Log.d(TAG, "downloadBitmapFromUrl: ");
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            FileUtils.close(in);
        }
        return bitmap;
    }

    public void displayImage(final String url, final ImageView target) {
        Log.d(TAG, "displayImage: ");
        target.setTag(url);
        ThreadPoolExecutorProxyFactory.getImageLoaderPoolExecutorProxy().execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = mImageCache.getFromCache(url);
                if (bitmap != null) {
                    handler.obtainMessage(SUCCESS_COMPLETE, new LoaderResult(target, url, bitmap)).sendToTarget();
                } else {
                    bitmap = downloadBitmapFromUrl(url);
                    if (bitmap == null) {
                        return;
                    }
                    Log.d(TAG, "Height = " + bitmap.getHeight() + ",Width = " + bitmap.getWidth());
                    handler.obtainMessage(SUCCESS_COMPLETE, new LoaderResult(target, url, bitmap)).sendToTarget();
                    try {
                        mImageCache.addToCache(bitmap, url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    static class LoaderResult {
        ImageView imageView;
        String url;
        Bitmap bitmap;

        public LoaderResult(ImageView imageView, String url, Bitmap bitmap) {
            this.imageView = imageView;
            this.url = url;
            this.bitmap = bitmap;
        }
    }
}