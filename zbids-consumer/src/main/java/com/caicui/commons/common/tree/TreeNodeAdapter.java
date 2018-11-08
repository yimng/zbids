package com.caicui.commons.common.tree;

import java.util.List;

/**
 * com.caicui.commons.common.tree
 * Created by yukewi on 2015/8/18 10:53.
 */
public interface TreeNodeAdapter<T extends Object> {

    TreeNodeAdapterBean adapt(T t);

    List<TreeNodeAdapterBean> adapt(List<T> originalList);

}
