package com.caicui.commons.common.tree;

import java.io.Serializable;

/**
 * com.caicui.commons.common.tree
 * Created by yukewi on 2015/8/18 10:30.
 */
public class TreeNodeAdapterBean implements Serializable {
    private static final long serialVersionUID = -7499013346708171204L;
    private Boolean open;
    private String id;
    private String pId;
    private String name;
    private String type;

    public TreeNodeAdapterBean() {
    }

    public TreeNodeAdapterBean(String id, String pId, String name, String type, Boolean open) {
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.type = type;
        this.open = open;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getOpen() {
        return open;
    }

    public String getType() {
        return type;
    }

    public String getpId() {
        return pId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }
}

