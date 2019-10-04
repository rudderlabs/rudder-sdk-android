package com.rudderlabs.android.sdk.core;

import android.text.TextUtils;
/*
* Builder for event type "page"
* - `url` is a mandatory field for page events
* */
public class PagePropertyBuilder extends RudderPropertyBuilder {
    private String title;

    public PagePropertyBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    private String url;

    public PagePropertyBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    private String path;

    public PagePropertyBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    private String referrer;

    public PagePropertyBuilder setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    private String search;

    public PagePropertyBuilder setSearch(String search) {
        this.search = search;
        return this;
    }

    private String keywords;

    public PagePropertyBuilder setKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    @Override
    public RudderProperty build() throws RudderException {
        if (TextUtils.isEmpty(url)) {
            throw new RudderException("url can not be null or empty");
        }

        RudderProperty property = new RudderProperty();
        property.setProperty("title", this.title);
        property.setProperty("url", this.url);
        property.setProperty("path", this.path);
        property.setProperty("referrer", this.referrer);
        property.setProperty("search", this.search);
        property.setProperty("keywords", this.keywords);

        return property;
    }
}
