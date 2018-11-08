package com.caicui.commons.common.tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * com.caicui.commons.common.tree
 * Created by yukewi on 2015/8/18 8:53.
 */
public class TreeNodeHelper {
    private static final TreeNodeHelper HELPER = new TreeNodeHelper();

    /**
     * 组织节点Map对象
     *
     * @param treeNodeAdapterBean
     * @return
     */
    public Map<String, Object> getTreeNode(TreeNodeAdapterBean treeNodeAdapterBean) {
        return getTreeNode(treeNodeAdapterBean.getId(), treeNodeAdapterBean.getpId(), treeNodeAdapterBean.getName(), treeNodeAdapterBean.getType(), treeNodeAdapterBean.getOpen());
    }

    /**
     * 组织节点Map对象
     *
     * @param id
     * @param pId
     * @param name
     * @param type
     * @param open
     * @return
     */
    public Map<String, Object> getTreeNode(String id, String pId, String name, String type, Boolean open) {
        Map<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("id", id);
        node.put("pId", pId);
        node.put("name", name);
        node.put("type", type);
        node.put("open", open);
        return node;
    }


    /**
     * 批量转换成为Map对象
     *
     * @param treeNodeAdapterBeanList
     * @return
     */
    public List<Map<String, Object>> getTreeNodeList(List<TreeNodeAdapterBean> treeNodeAdapterBeanList) {
        List<Map<String, Object>> treeNodeMapList = new ArrayList<>();
        for (TreeNodeAdapterBean treeNodeAdapterBean : treeNodeAdapterBeanList) {
            final Map<String, Object> treeNode = getTreeNode(treeNodeAdapterBean);
            treeNodeMapList.add(treeNode);
        }
        return treeNodeMapList;
    }

    private TreeNodeHelper() {
    }

    public static TreeNodeHelper getHelper() {
        return HELPER;
    }
}
