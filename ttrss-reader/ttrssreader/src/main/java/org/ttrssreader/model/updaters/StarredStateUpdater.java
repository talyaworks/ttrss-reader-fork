/*
 * Copyright (c) 2015, Nils Braden
 *
 * This file is part of ttrss-reader-fork. This program is free software; you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; If
 * not, see http://www.gnu.org/licenses/.
 */

package org.ttrssreader.model.updaters;

import org.ttrssreader.controllers.DBHelper;
import org.ttrssreader.controllers.Data;
import org.ttrssreader.model.pojos.Article;

public class StarredStateUpdater implements IUpdatable {

    @SuppressWarnings("unused")
    private static final String TAG = StarredStateUpdater.class.getSimpleName();

    private Article article;
    private int articleState;

    /**
     * Sets the articles' Starred-Status according to articleState
     */
    public StarredStateUpdater(Article article, int articleState) {
        this.article = article;
        this.articleState = articleState;
    }

    @Override
    public void update(Updater parent) {
        if (articleState >= 0) {
            article.isStarred = articleState > 0;
            DBHelper.getInstance().markArticle(article.id, "isStarred", articleState);
            Data.getInstance().calculateCounters();
            Data.getInstance().notifyListeners();
            Data.getInstance().setArticleStarred(article.id, articleState);
        }
    }

}
