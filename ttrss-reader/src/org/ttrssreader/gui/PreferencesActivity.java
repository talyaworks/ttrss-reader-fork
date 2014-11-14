/*
 * ttrss-reader-fork for Android
 * 
 * Copyright (C) 2010 Nils Braden
 * Copyright (C) 2009-2010 J. Devauchelle.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

package org.ttrssreader.gui;

import java.io.File;
import java.util.List;
import org.ttrssreader.R;
import org.ttrssreader.controllers.Controller;
import org.ttrssreader.controllers.DBHelper;
import org.ttrssreader.model.HeaderAdapter;
import org.ttrssreader.preferences.Constants;
import org.ttrssreader.preferences.FileBrowserHelper;
import org.ttrssreader.preferences.FileBrowserHelper.FileBrowserFailOverCallback;
import org.ttrssreader.preferences.WifiPreferencesActivity;
import org.ttrssreader.utils.AsyncTask;
import org.ttrssreader.utils.PostMortemReportExceptionHandler;
import org.ttrssreader.utils.Utils;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;

public class PreferencesActivity extends PreferenceActivity {
    
    @SuppressWarnings("unused")
    private static final String TAG = PreferencesActivity.class.getSimpleName();
    
    private PostMortemReportExceptionHandler mDamageReport = new PostMortemReportExceptionHandler(this);
    
    private static final String PREFS_DISPLAY = "prefs_display";
    private static final String PREFS_HEADERS = "prefs_headers";
    private static final String PREFS_HTTP = "prefs_http";
    private static final String PREFS_MAIN_TOP = "prefs_main_top";
    private static final String PREFS_SSL = "prefs_ssl";
    private static final String PREFS_SYSTEM = "prefs_system";
    private static final String PREFS_USAGE = "prefs_usage";
    private static final String PREFS_WIFI = "prefs_wifibased";
    
    static final int ACTIVITY_SHOW_PREFERENCES = 43;
    private static final int ACTIVITY_CHOOSE_ATTACHMENT_FOLDER = 1;
    private static final int ACTIVITY_CHOOSE_CACHE_FOLDER = 2;
    
    private static AsyncTask<Void, Void, Void> init;
    private static List<Header> _headers;
    
    private static Preference downloadPath;
    private static Preference cachePath;
    private static PreferenceActivity activity;
    
    private Context context;
    private boolean needResource = false;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Controller.getInstance().getTheme());
        super.onCreate(savedInstanceState); // IMPORTANT!
        mDamageReport.initialize();
        
        context = getApplicationContext();
        activity = this;
        setResult(ACTIVITY_SHOW_PREFERENCES);
        
        if (needResource) {
            addPreferencesFromResource(R.xml.prefs_main_top);
            addPreferencesFromResource(R.xml.prefs_http);
            addPreferencesFromResource(R.xml.prefs_ssl);
            addPreferencesFromResource(R.xml.prefs_wifi);
            addPreferencesFromResource(R.xml.prefs_usage);
            addPreferencesFromResource(R.xml.prefs_display);
            addPreferencesFromResource(R.xml.prefs_system);
            addPreferencesFromResource(R.xml.prefs_main_bottom);
        }
        
        initializePreferences(null);
    }
    
    @Override
    public void onBuildHeaders(List<Header> headers) {
        _headers = headers;
        if (onIsHidingHeaders()) {
            needResource = true;
        } else {
            loadHeadersFromResource(R.xml.prefs_headers, headers);
        }
    }
    
    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, _headers));
        }
    }
    
    @SuppressWarnings("deprecation")
    private static void initializePreferences(PreferenceFragment fragment) {
        
        if (fragment != null) {
            downloadPath = fragment.findPreference(Constants.SAVE_ATTACHMENT);
            cachePath = fragment.findPreference(Constants.CACHE_FOLDER);
        } else {
            downloadPath = activity.findPreference(Constants.SAVE_ATTACHMENT);
            cachePath = activity.findPreference(Constants.CACHE_FOLDER);
        }
        
        if (downloadPath != null) {
            downloadPath.setSummary(Controller.getInstance().saveAttachmentPath());
            downloadPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FileBrowserHelper.getInstance().showFileBrowserActivity(activity,
                            new File(Controller.getInstance().saveAttachmentPath()), ACTIVITY_CHOOSE_ATTACHMENT_FOLDER,
                            callbackDownloadPath);
                    return true;
                }
                
                FileBrowserFailOverCallback callbackDownloadPath = new FileBrowserFailOverCallback() {
                    @Override
                    public void onPathEntered(String path) {
                        downloadPath.setSummary(path);
                        Controller.getInstance().setSaveAttachmentPath(path);
                    }
                    
                    @Override
                    public void onCancel() {
                    }
                };
            });
        }
        
        if (cachePath != null) {
            cachePath.setSummary(Controller.getInstance().cacheFolder());
            cachePath.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FileBrowserHelper.getInstance().showFileBrowserActivity(activity,
                            new File(Controller.getInstance().cacheFolder()), ACTIVITY_CHOOSE_CACHE_FOLDER,
                            callbackCachePath);
                    return true;
                }
                
                FileBrowserFailOverCallback callbackCachePath = new FileBrowserFailOverCallback() {
                    @Override
                    public void onPathEntered(String path) {
                        cachePath.setSummary(path);
                        Controller.getInstance().setCacheFolder(path);
                    }
                    
                    @Override
                    public void onCancel() {
                    }
                };
            });
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(
                Controller.getInstance());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(
                Controller.getInstance());
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        if (init != null) {
            init.cancel(true);
            init = null;
        }
        
        if (!Utils.checkIsConfigInvalid()) {
            init = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Controller.getInstance().checkAndInitializeController(context, null);
                    return null;
                }
            };
            init.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (Controller.getInstance().isPreferencesChanged()) {
            new BackupManager(this).dataChanged();
            Controller.getInstance().setPreferencesChanged(false);
        }
    }
    
    @Override
    protected void onDestroy() {
        mDamageReport.restoreOriginalHandler();
        mDamageReport = null;
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.preferences, menu);
        return true;
    }
    
    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        ComponentName comp = new ComponentName(this.getPackageName(), getClass().getName());
        switch (item.getItemId()) {
            case R.id.Preferences_Menu_Reset:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                Constants.resetPreferences(prefs);
                this.finish();
                startActivity(new Intent().setComponent(comp));
                return true;
            case R.id.Preferences_Menu_ResetDatabase:
                Controller.getInstance().setDeleteDBScheduled(true);
                DBHelper.getInstance().checkAndInitializeDB(this);
                this.finish();
                startActivity(new Intent().setComponent(comp));
                return true;
        }
        return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path = null;
        if (resultCode == RESULT_OK && data != null) {
            // obtain the filename
            Uri fileUri = data.getData();
            if (fileUri != null)
                path = fileUri.getPath();
        }
        if (path != null) {
            switch (requestCode) {
                case ACTIVITY_CHOOSE_ATTACHMENT_FOLDER:
                    downloadPath.setSummary(path);
                    Controller.getInstance().setSaveAttachmentPath(path);
                    break;
                
                case ACTIVITY_CHOOSE_CACHE_FOLDER:
                    cachePath.setSummary(path);
                    Controller.getInstance().setCacheFolder(path);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private static class PreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            String cat = getArguments().getString("cat");
            if (PREFS_DISPLAY.equals(cat))
                addPreferencesFromResource(R.xml.prefs_display);
            if (PREFS_HEADERS.equals(cat))
                addPreferencesFromResource(R.xml.prefs_headers);
            if (PREFS_HTTP.equals(cat))
                addPreferencesFromResource(R.xml.prefs_http);
            if (PREFS_MAIN_TOP.equals(cat))
                addPreferencesFromResource(R.xml.prefs_main_top);
            if (PREFS_SSL.equals(cat))
                addPreferencesFromResource(R.xml.prefs_ssl);
            if (PREFS_SYSTEM.equals(cat)) {
                addPreferencesFromResource(R.xml.prefs_system);
                initializePreferences(this); // Manually initialize Listeners for Download- and CachePath
            }
            if (PREFS_USAGE.equals(cat))
                addPreferencesFromResource(R.xml.prefs_usage);
            if (PREFS_WIFI.equals(cat)) {
                initWifibasedPreferences();
            }
        }
        
        private void initWifibasedPreferences() {
            addPreferencesFromResource(R.xml.prefs_wifibased);
            PreferenceCategory mWifibasedCategory = (PreferenceCategory) findPreference("wifibasedCategory");
            WifiManager mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> mWifiList = mWifiManager.getConfiguredNetworks();
            
            if (mWifiList == null)
                return;
            
            for (WifiConfiguration wifi : mWifiList) {
                // Friendly SSID-Name
                String ssid = wifi.SSID.replaceAll("\"", "");
                // Add PreferenceScreen for each network
                
                PreferenceScreen pref = getPreferenceManager().createPreferenceScreen(getActivity());
                pref.setPersistent(false);
                pref.setKey("wifiNetwork" + ssid);
                pref.setTitle(ssid);
                
                Intent intent = new Intent(getActivity(), WifiPreferencesActivity.class);
                intent.putExtra(WifiPreferencesActivity.KEY_SSID, ssid); // TODO: ssid == null?
                pref.setIntent(intent);
                if (WifiConfiguration.Status.CURRENT == wifi.status)
                    pref.setSummary(getResources().getString(R.string.ConnectionWifiConnected));
                else
                    pref.setSummary(getResources().getString(R.string.ConnectionWifiNotInRange));
                mWifibasedCategory.addPreference(pref);
            }
        }
        
    }
    
    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PreferencesFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }
    
    @Override
    public void switchToHeader(Header header) {
        if (header.fragment != null) {
            super.switchToHeader(header);
        }
    }
    
}
