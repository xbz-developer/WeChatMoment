package com.thoughtworks.wechatmoment.factory;

import com.thoughtworks.wechatmoment.manager.ThreadPoolExecutorProxy;

public class ThreadPoolExecutorProxyFactory {

    static ThreadPoolExecutorProxy mImageLoaderPoolExecutorProxy;

    public static ThreadPoolExecutorProxy getImageLoaderPoolExecutorProxy() {
        if (mImageLoaderPoolExecutorProxy == null) {
            synchronized (ThreadPoolExecutorProxyFactory.class) {
                if (mImageLoaderPoolExecutorProxy == null) {
                    mImageLoaderPoolExecutorProxy = new ThreadPoolExecutorProxy(5, 5, 3000);
                }
            }
        }
        return mImageLoaderPoolExecutorProxy;
    }
}

