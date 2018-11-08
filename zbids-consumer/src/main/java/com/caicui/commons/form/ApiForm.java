package com.caicui.commons.form;

import java.util.Arrays;
import java.util.List;

/**
 * com.caicui.commons.form
 * Created by yukewi on 2016/1/15 10:35.
 */
public class ApiForm extends CommonForm {
    private String no;
    private String description;

    public ApiForm(String action, List<String> names, String no, String description) {
        super(action, names);
        this.no = no;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getNo() {
        return no;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public static ApiForm makeForm(String action, String[] names, String no, String description) {
        return new ApiForm(action, Arrays.asList(names), no, description);
    }
}
