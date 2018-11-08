package com.caicui.commons.pagination.entity;


import com.caicui.commons.base.Base;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * com.caicui.commons.pagination.entity
 * Created by yukewi on 2015/7/15 12:02.
 */
public class PaginationBar extends Base {
    private static final long serialVersionUID = -2802978347072192781L;

    private PageItem firstPageItem;
    private PageItem prePageItem;
    private Boolean hasPrev;
    private Boolean hasNext;
    private PageItem activePageItem;
    private PageItem lastPageItem;
    private PageItem nextPageItem;
    private List<PageItem> items;
    private List<PageItem> condenseItems;

    public List<PageItem> getCondenseItems() {
        condenseItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items) && CollectionUtils.size(items) <= 5) {
            condenseItems.addAll(items);
        }

        if (CollectionUtils.isNotEmpty(items) && CollectionUtils.size(items) > 5) {


            final PageItem firstIndex01 = items.get(0);
            final PageItem firstIndex02 = items.get(1);
            final PageItem lastIndex03 = items.get(items.size() - 3);
            final PageItem lastIndex02 = items.get(items.size() - 2);
            final PageItem lastIndex01 = items.get(items.size() - 1);

            if (activePageItem.getIndex().equals(firstIndex01.getIndex())           //
                    || activePageItem.getIndex().equals(firstIndex02.getIndex())    //
                    || activePageItem.getIndex().equals(lastIndex03.getIndex())     //
                    || activePageItem.getIndex().equals(lastIndex02.getIndex())     //
                    || activePageItem.getIndex().equals(lastIndex01.getIndex())     //
                    ) {
                condenseItems.add(firstIndex01);
                condenseItems.add(firstIndex02);
                condenseItems.add(new PageItem());
                condenseItems.add(lastIndex03);
                condenseItems.add(lastIndex02);
                condenseItems.add(lastIndex01);
            } else {
                condenseItems.add(firstIndex01);
                condenseItems.add(firstIndex02);
                condenseItems.add(new PageItem());
                condenseItems.add(activePageItem);
                condenseItems.add(new PageItem());
                condenseItems.add(lastIndex02);
                condenseItems.add(lastIndex01);
            }
        }
        return condenseItems;
    }


    private PageItem getActivePage(List<PageItem> items, Integer pageNumber) {
        if (CollectionUtils.isNotEmpty(items) && CollectionUtils.size(items) > pageNumber && pageNumber > 0) {
            return items.get(pageNumber - 1);
        }
        return null;
    }

    public void setCondenseItems(List<PageItem> condenseItems) {
        this.condenseItems = condenseItems;
    }

    public Boolean getHasPrev() {
        return hasPrev;
    }

    public void setHasPrev(Boolean hasPrev) {
        this.hasPrev = hasPrev;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public PaginationBar(Integer pageNumber, Integer pageCount) {
        List<PageItem> itemList = new ArrayList<>();
        if (pageCount != null) {
            for (int i = 0; i < pageCount; i++) {
                PageItem pageItem = new PageItem(i);
                itemList.add(pageItem);
            }
        }
        this.items = itemList;
        if (CollectionUtils.isNotEmpty(itemList)) {
            this.firstPageItem = itemList.get(0);
            this.lastPageItem = itemList.get(itemList.size() - 1);
            for (int i = 0; i < itemList.size(); i++) {
                if (pageNumber.equals(i + 1)) {
                    this.activePageItem = itemList.get(i);
                    if ((i - 1) >= 0) {
                        this.prePageItem = itemList.get(i - 1);
                        this.hasPrev = true;
                    } else {
                        this.hasPrev = false;
                    }
                    if ((i + 1) <= itemList.size() - 1) {
                        this.nextPageItem = itemList.get(i + 1);
                        this.hasNext = true;
                    } else {
                        this.hasNext = false;
                    }
                }
            }
        }
    }

    public PageItem getFirstPageItem() {
        return firstPageItem;
    }

    public void setFirstPageItem(PageItem firstPageItem) {
        this.firstPageItem = firstPageItem;
    }

    public PageItem getPrePageItem() {
        return prePageItem;
    }

    public void setPrePageItem(PageItem prePageItem) {
        this.prePageItem = prePageItem;
    }

    public PageItem getActivePageItem() {
        return activePageItem;
    }

    public void setActivePageItem(PageItem activePageItem) {
        this.activePageItem = activePageItem;
    }

    public PageItem getLastPageItem() {
        return lastPageItem;
    }

    public void setLastPageItem(PageItem lastPageItem) {
        this.lastPageItem = lastPageItem;
    }

    public PageItem getNextPageItem() {
        return nextPageItem;
    }

    public void setNextPageItem(PageItem nextPageItem) {
        this.nextPageItem = nextPageItem;
    }

    public List<PageItem> getItems() {
        return items;
    }

    public void setItems(List<PageItem> items) {
        this.items = items;
    }
}
