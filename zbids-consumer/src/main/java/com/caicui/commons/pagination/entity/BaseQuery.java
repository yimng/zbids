package com.caicui.commons.pagination.entity;

import com.caicui.commons.base.Base;

/**
 * com.caicui.commons.pagination.entity
 * Created by yukewi on 2015/7/14 17:39.
 */
public class BaseQuery extends Base {
    protected Integer from = 0;   // 每页记录数
    protected Integer size;       // 记录数

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
