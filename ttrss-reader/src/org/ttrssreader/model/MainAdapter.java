/*
 * ttrss-reader-fork for Android
 * 
 * Copyright (C) 2010 N. Braden.
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

package org.ttrssreader.model;

import java.util.ArrayList;
import java.util.List;
import org.ttrssreader.controllers.Controller;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class MainAdapter extends BaseAdapter {
    
    protected Context context;
    protected Cursor cursor;
    
    protected boolean displayOnlyUnread;
    protected boolean invertSortFeedCats;
    protected boolean invertSortArticles;
    protected int categoryId;
    protected int feedId;
    protected long changedTime;
    
    protected boolean selectArticlesForCategory;
    
    public MainAdapter() {
        this.displayOnlyUnread = Controller.getInstance().onlyUnread();
        this.invertSortFeedCats = Controller.getInstance().invertSortFeedscats();
        this.invertSortArticles = Controller.getInstance().invertSortArticlelist();
    }
    
    public MainAdapter(Context context) {
        this();
        this.context = context;
        makeQuery();
    }
    
    public MainAdapter(Context context, int categoryId) {
        this();
        this.context = context;
        this.categoryId = categoryId;
        makeQuery();
    }
    
    public MainAdapter(Context context, int feedId, int categoryId, boolean selectArticlesForCategory) {
        this();
        this.context = context;
        this.feedId = feedId;
        this.categoryId = categoryId;
        this.selectArticlesForCategory = selectArticlesForCategory;
        makeQuery();
    }
    
    public final void closeCursor() {
        if (cursor == null)
            return;
        
        synchronized (cursor) {
            if (cursor == null)
                return;
            
            // Catch all SQLiteExceptions to make sure no "unable to close due to unfinalised statements" errors arise
            try {
                cursor.close();
            } catch (SQLiteException e) {
            }
        }
    }
    
    @Override
    public final int getCount() {
        synchronized (cursor) {
            if (cursor.isClosed())
                makeQuery();
            
            return cursor.getCount();
        }
    }
    
    @Override
    public final long getItemId(int position) {
        return position;
    }
    
    public final int getId(int position) {
        int ret = 0;
        synchronized (cursor) {
            if (cursor.isClosed())
                makeQuery();
            
            if (cursor.getCount() >= position)
                if (cursor.moveToPosition(position))
                    ret = cursor.getInt(0);
        }
        return ret;
    }
    
    public final List<Integer> getIds() {
        List<Integer> result = new ArrayList<Integer>();
        synchronized (cursor) {
            if (cursor.isClosed())
                makeQuery();
            
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    result.add(cursor.getInt(0));
                    cursor.move(1);
                }
            }
        }
        return result;
    }
    
    public final String getTitle(int position) {
        String ret = "";
        synchronized (cursor) {
            if (cursor.isClosed())
                makeQuery();
            
            if (cursor.getCount() >= position)
                if (cursor.moveToPosition(position))
                    ret = cursor.getString(1);
        }
        return ret;
    }
    
    public static final String formatTitle(String title, int unread) {
        if (unread > 0) {
            return title + " (" + unread + ")";
        } else {
            return title;
        }
    }
    
    public final synchronized void makeQuery() {
        makeQuery(false);
    }
    
    /**
     * Only refresh if forceRefresh is true (called from constructor) or one of the display-attributes changed.
     * 
     * @param forceRefresh
     *            Discards the current cursor and forces a refresh, including a newly built SQL-Query.
     */
    public final synchronized void makeQuery(boolean forceRefresh) {
        boolean refresh = false || forceRefresh;
        // Check if display-settings have changed
        if (displayOnlyUnread != Controller.getInstance().onlyUnread()) {
            refresh = true;
            displayOnlyUnread = !displayOnlyUnread;
        }
        if (invertSortFeedCats != Controller.getInstance().invertSortFeedscats()) {
            refresh = true;
            invertSortFeedCats = !invertSortFeedCats;
        }
        if (invertSortArticles != Controller.getInstance().invertSortArticlelist()) {
            refresh = true;
            invertSortArticles = !invertSortArticles;
        }
        
        // if: sort-order or display-settings changed
        // if: forced by explicit call with forceRefresh
        // if: cursor is closed or null
        if (refresh || (cursor != null && cursor.isClosed()) || cursor == null) {
            // if (cursor != null) // Not necessary, child can close it if it issues a new query.
            // closeCursor();
            
            try {
                cursor = executeQuery(false, false, refresh); // normal query
                
                if (!checkUnread(cursor))
                    cursor = executeQuery(true, false, true); // Override unread if query was empty
                    
            } catch (Exception e) {
                cursor = executeQuery(false, true, refresh); // Fail-safe-query
            }
            
        } else if (cursor != null) {
            cursor.requery();
        }
        
    }
    
    /**
     * Tries to find out if the given cursor points to a dataset with unread articles in it, returns true if it does.
     * 
     * @param c
     *            the cursor.
     * @return true if there are unread articles in the dataset, else false.
     */
    private final boolean checkUnread(Cursor c) {
        if (c == null || c.isClosed())
            return true; // TODO: Check for concurrency-issues here, to avoid anything strange this should return false.
            
        boolean gotUnread = false;
        if (c.moveToFirst()) {
            int col = c.getColumnIndex("unread");
            if (col > -1) {
                while (!c.isAfterLast()) {
                    int unread = c.getInt(col);
                    if (unread > 0) {
                        gotUnread = true;
                        break;
                    }
                    c.move(1);
                }
            }
            
        }
        
        c.moveToFirst();
        return gotUnread;
    }
    
    @Override
    public abstract Object getItem(int position);
    
    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);
    
    /**
     * Builds the query for this adapter as a string and returns it to be invoked on a database object.
     * 
     * @param overrideDisplayUnread
     *            if true unread articles/feeds/anything won't be filtered as specified by the setting but will be
     *            included in the result.
     * @param buildSafeQuery
     *            indicates that the query should modified to also display unread content even though displayUnread is
     *            disabled, this is used to get a new query when the current query is empty.
     * @param forceRefresh
     *            this indicates that a refresh of the cursor should be forced.
     * @return a valid SQL-Query string for this adapter.
     */
    protected abstract Cursor executeQuery(boolean overrideDisplayUnread, boolean buildSafeQuery, boolean forceRefresh);
    
}