package com.thoughtworks.wechatmoment.loader;

import android.graphics.Bitmap;

import java.io.IOException;

public interface IImageCacahe {

    Bitmap getFromCache(String url);

    void addToCache(Bitmap bitmap, String url) throws IOException;
}
