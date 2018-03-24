package com.netspace.library.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import com.netspace.library.application.MyiBaseApplication;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Downloader.Response;
import com.squareup.picasso.Downloader.ResponseException;
import com.squareup.picasso.NetworkPolicy;
import java.io.File;
import java.io.IOException;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call.Factory;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.http.HttpStatus;

public final class OkHttp3Downloader implements Downloader {
    private static final int MAX_DISK_CACHE_SIZE = 52428800;
    private static final int MIN_DISK_CACHE_SIZE = 5242880;
    private static final String PICASSO_CACHE = "picasso-cache";
    private final Cache cache;
    private final Factory client;

    private static File defaultCacheDir(Context context) {
        File cache = new File(context.getApplicationContext().getCacheDir(), PICASSO_CACHE);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    private static long calculateDiskCacheSize(File dir) {
        long size = 5242880;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            size = (((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize())) / 50;
        } catch (IllegalArgumentException e) {
        }
        return Math.max(Math.min(size, 52428800), 5242880);
    }

    public static Cache createDefaultCache(Context context) {
        File dir = defaultCacheDir(context);
        return new Cache(dir, calculateDiskCacheSize(dir));
    }

    private static OkHttpClient createOkHttpClient(File cacheDir, long maxSize) {
        if (MyiBaseApplication.isUseSSL()) {
            return new Builder().cache(new Cache(cacheDir, maxSize)).sslSocketFactory(SSLConnection.getSSLSocketFactory(), SSLConnection.getTrustManager()).build();
        }
        return new Builder().cache(new Cache(cacheDir, maxSize)).build();
    }

    public OkHttp3Downloader(Context context) {
        this(defaultCacheDir(context));
    }

    public OkHttp3Downloader(File cacheDir) {
        this(cacheDir, calculateDiskCacheSize(cacheDir));
    }

    public OkHttp3Downloader(Context context, long maxSize) {
        this(defaultCacheDir(context), maxSize);
    }

    public OkHttp3Downloader(File cacheDir, long maxSize) {
        this(createOkHttpClient(cacheDir, maxSize));
    }

    public OkHttp3Downloader(OkHttpClient client) {
        this.client = client;
        this.cache = client.cache();
    }

    public OkHttp3Downloader(Factory client) {
        this.client = client;
        this.cache = null;
    }

    public Response load(Uri uri, int networkPolicy) throws IOException {
        Request.Builder builder;
        CacheControl cacheControl = null;
        if (networkPolicy != 0) {
            if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
                cacheControl = CacheControl.FORCE_CACHE;
            } else {
                builder = new CacheControl.Builder();
                if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
                    builder.noCache();
                }
                if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
                    builder.noStore();
                }
                cacheControl = builder.build();
            }
        }
        builder = new Request.Builder().url(uri.toString());
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        okhttp3.Response response = this.client.newCall(builder.build()).execute();
        int responseCode = response.code();
        if (responseCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
            response.body().close();
            throw new ResponseException(new StringBuilder(String.valueOf(responseCode)).append(" ").append(response.message()).toString(), networkPolicy, responseCode);
        }
        boolean fromCache = response.cacheResponse() != null;
        ResponseBody responseBody = response.body();
        return new Response(responseBody.byteStream(), fromCache, responseBody.contentLength());
    }

    public void shutdown() {
        if (this.cache != null) {
            try {
                this.cache.close();
            } catch (IOException e) {
            }
        }
    }
}
