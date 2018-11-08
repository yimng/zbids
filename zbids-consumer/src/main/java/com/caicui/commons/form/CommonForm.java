package com.caicui.commons.form;

import com.caicui.commons.base.Base;

import java.util.List;

/**
 * com.caicui.commons.form
 * Created by yukewi on 2016/1/15 9:53.
 */
public class CommonForm extends Base {
    private static final long serialVersionUID = -7402200423306649517L;
    private String action;
    private List<String> names;

    public CommonForm() {
    }

    public CommonForm(String action, List<String> names) {
        this.action = action;
        this.names = names;
    }

    public String getAction() {
        return action;
    }

    public List<String> getNames() {
        return names;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}
