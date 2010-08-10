/*
 * Tiny Tiny RSS Reader for Android
 * 
 * Copyright (C) 2009 J. Devauchelle and contributors.
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

package org.ttrssreader.model.feedheadline;

import org.ttrssreader.controllers.Controller;
import org.ttrssreader.controllers.DataController;
import org.ttrssreader.utils.Utils;
import android.os.AsyncTask;
import android.util.Log;

public class FeedHeadlineUpdateTask extends AsyncTask<String, Integer, Boolean> {
	
	protected Boolean doInBackground(String... ids) {
		
		Log.i(Utils.TAG, "doInBackground - getArticlesWithContent(feedId: " + ids[0] + ")");
		
		boolean displayOnlyUnread = Controller.getInstance().isDisplayOnlyUnread();
		
		DataController.getInstance().forceFullRefresh();
		DataController.getInstance().getArticlesWithContent(ids[0]);
		
		if (ids[1] != null && ids[2] != null) {
			if (!ids[1].equals("")) {
				Log.i(Utils.TAG, "doInBackground - getArticlesHeadlines(feedId: " + ids[1] + ")");
				DataController.getInstance().getArticlesHeadlines(ids[1], displayOnlyUnread);
			}
			
			if (!ids[2].equals("")) {
				Log.i(Utils.TAG, "doInBackground - getArticlesHeadlines(feedId: " + ids[2] + ")");
				DataController.getInstance().getArticlesHeadlines(ids[2], displayOnlyUnread);
			}
		}
		DataController.getInstance().disableForceFullRefresh();
		
		return true;
	}
	
}