package com.caicui.commons.pagination.entity;

import com.caicui.commons.base.Base;

/**
 * com.caicui.commons.pagination.entity
 * Created by yukewi on 2015/7/15 11:57.
 */
public class PageItem extends Base {
    private static final long serialVersionUID = 7630185523749713875L;

    public PageItem() {
    }

    public PageItem(Integer index) {
        this.index = index;
    }

    private Integer index;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
