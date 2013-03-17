/*
 * Copyright (C) 2009 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ttrssreader.imageCache;

import org.ttrssreader.R;
import org.ttrssreader.controllers.Controller;
import org.ttrssreader.gui.interfaces.ICacheEndListener;
import org.ttrssreader.utils.AsyncTask;
import org.ttrssreader.utils.Utils;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ForegroundService extends Service implements ICacheEndListener {
    
    public static final String ACTION_LOAD_IMAGES = "load_images";
    public static final String ACTION_LOAD_ARTICLES = "load_articles";
    
    private ImageCacher imageCacher;
    private static ForegroundService instance = null;
    private static ICacheEndListener parent;
    
    public static boolean isInstanceCreated() {
        return instance != null;
    }
    
    private boolean imageCache = false;
    
    public static void loadImagesToo() {
        if (instance != null)
            instance.imageCache = true;
    }
    
    public static void registerCallback(ICacheEndListener parentGUI) {
        parent = parentGUI;
    }
    
    @Override
    public void onCreate() {
        instance = this;
    }
    
    @Override
    public void onDestroy() {
        finishService();
    }
    
    /**
     * Cleans up all running notifications, notifies waiting activities and clears the instance of the service.
     */
    public void finishService() {
        if (instance != null) {
            stopForeground(true);
            Controller.getInstance().notifyActivities();
            instance = null;
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }
    
    void handleCommand(Intent intent) {
        // Fail-safe
        if (intent == null || intent.getAction() == null)
            return;
        
        int icon = R.drawable.notification_icon;
        CharSequence title = "";
        CharSequence ticker = getText(R.string.Cache_service_started);
        CharSequence text = getText(R.string.Cache_service_text);
        
        if (ACTION_LOAD_IMAGES.equals(intent.getAction())) {
            title = getText(R.string.Cache_service_imagecache);
            imageCacher = new ImageCacher(this, this, false);
            imageCacher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (ACTION_LOAD_ARTICLES.equals(intent.getAction())) {
            title = getText(R.string.Cache_service_articlecache);
            imageCacher = new ImageCacher(this, this, true);
            imageCacher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        
        // Display notification
        Notification notification = Utils.buildNotification(getApplicationContext(), icon, ticker, title, text, true,
                new Intent());
        startForeground(R.string.Cache_service_started, notification);
    }
    
    @Override
    public void onCacheEnd() {
        // Start a new cacher if images have been requested
        if (imageCache) {
            imageCache = false;
            imageCacher = new ImageCacher(this, this, false);
            imageCacher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            finishService();
            this.stopSelf();
        }
    }
    
    @Override
    public void onCacheProgress(int taskCount, int progress) {
        if (parent != null)
            parent.onCacheProgress(taskCount, progress);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
}
