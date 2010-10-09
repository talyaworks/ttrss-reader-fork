/*
 * Tiny Tiny RSS Reader for Android
 * 
 * Copyright (C) 2010 N. Braden and contributors.
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

package org.ttrssreader.model.article;

import java.util.Date;
import java.util.Set;
import org.ttrssreader.utils.Utils;
import android.util.Log;

public class ArticleItem {
    
    private int mId;
    private String mTitle;
    private int mFeedId;
    private boolean mIsUnread;
    private String mContent;
    private String mArticleUrl;
    private String mArticleCommentUrl;
    private Date mUpdateDate;
    private Set<String> attachments;
    
    public ArticleItem() {
    }
    
    public ArticleItem(int feedId, int id, String title, boolean isUnread, Date updateDate, String content,
            String articleUrl, String articleCommentUrl, Set<String> attachments) {
        setId(id);
        setTitle(title);
        setFeedId(feedId);
        setUnread(isUnread);
        setUpdateDate(updateDate);
        setArticleUrl(articleUrl);
        setArticleCommentUrl(articleCommentUrl);
        setContent(content);
        setAttachments(attachments);
    }
    
    /*
     * Article-ID and Feed-ID given as String, will be parsed in setId(String mId) or set to 0 if value is invalid.
     */
    public ArticleItem(String feedId, String id, String title, boolean isUnread, Date updateDate, String content,
            String articleUrl, String articleCommentUrl, Set<String> attachments) {
        setId(id);
        setTitle(title);
        setFeedId(feedId);
        setUnread(isUnread);
        setUpdateDate(updateDate);
        setArticleUrl(articleUrl);
        setArticleCommentUrl(articleCommentUrl);
        setContent(content);
        setAttachments(attachments);
    }
    
    public int getId() {
        return mId;
    }
    
    public void setId(int id) {
        this.mId = id;
    }
    
    public void setId(String id) {
        // Check if mId is a number, else set to 0
        try {
            if (id == null) {
                this.mId = 0;
                id = "null"; // Set to (String) "null" for log-output..
            } else if (!id.matches("[0-9]+")) {
                this.mId = 0;
            } else {
                this.mId = Integer.parseInt(id);
            }
        } catch (NumberFormatException e) {
            Log.d(Utils.TAG, "Article-ID has to be an integer-value but was " + id);
        }
    }
    
    public String getTitle() {
        return mTitle;
    }
    
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }
    
    public int getFeedId() {
        return mFeedId;
    }
    
    public void setFeedId(int feedId) {
        this.mFeedId = feedId;
    }
    
    public void setFeedId(String feedId) {
        // Check if mId is a number, else set to 0
        try {
            if (feedId == null) {
                this.mFeedId = 0;
                feedId = "null"; // Set to (String) "null" for log-output..
            } else if (!feedId.matches("-*[0-9]+")) {
                this.mFeedId = 0;
            } else {
                this.mFeedId = Integer.parseInt(feedId);
            }
        } catch (NumberFormatException e) {
            Log.d(Utils.TAG, "Feed-ID has to be an integer-value but was " + feedId);
        }
    }
    
    public boolean isUnread() {
        return mIsUnread;
    }
    
    public void setUnread(boolean mIsUnread) {
        this.mIsUnread = mIsUnread;
    }
    
    public String getContent() {
        return mContent;
    }
    
    public void setContent(String mContent) {
        if (mContent == null || mContent.equals("") || mContent.equals("null")) {
            this.mContent = null;
        } else {
            this.mContent = mContent;
        }
    }
    
    public String getArticleUrl() {
        return mArticleUrl;
    }
    
    public void setArticleUrl(String mArticleUrl) {
        this.mArticleUrl = mArticleUrl;
    }
    
    public String getArticleCommentUrl() {
        return mArticleCommentUrl;
    }
    
    public void setArticleCommentUrl(String mArticleCommentUrl) {
        this.mArticleCommentUrl = mArticleCommentUrl;
    }
    
    public Date getUpdateDate() {
        return mUpdateDate;
    }
    
    public void setUpdateDate(Date mUpdateDate) {
        this.mUpdateDate = mUpdateDate;
    }

    public Set<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<String> attachments) {
        this.attachments = attachments;
    }
}
