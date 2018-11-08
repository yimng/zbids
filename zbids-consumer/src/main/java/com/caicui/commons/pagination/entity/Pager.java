package com.caicui.commons.pagination.entity;

import com.caicui.commons.base.Base;

import java.util.List;

/**
 * 分页
 */
public class Pager<T> extends Base {
    private static final Integer MAX_PAGE_SIZE = 50000;// 每页最大记录数限制
    private static final long serialVersionUID = 5860839963757725141L;
    private String keyword;                 // 查找关键字
    private Order order;                    // 排序方式
    private String orderBy;                 // 排序字段
    private Integer pageNumber = 1;         // 当前页码
    private Integer pageSize = 20;          // 每页记录数
    private PaginationBar paginationBar;    // 分页工具栏
    private List<T> result;                 // 返回结果
    private String searchBy;                // 查找字段
    private Integer totalCount;             // 总记录数
    private Integer from;                   // 总记录数

    public Integer getFrom() {
        if (this.getPageNumber() != null && this.getPageSize() != null) {
            this.from = (this.getPageNumber() - 1) * this.getPageSize();
        }
        return from;
    }

    public String getKeyword() {
        return keyword;
    }

    public Order getOrder() {
        return order;
    }

    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 获取总页数
     */
    public Integer getPageCount() {
        Integer pageCount = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            pageCount++;
        }
        return pageCount;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public PaginationBar getPaginationBar() {
        return paginationBar;
    }


    public List<T> getResult() {
        return result;
    }

    public String getSearchBy() {
        return searchBy;
    }

    public Integer getTotalCount() {
        return totalCount;
    }


    public void initPaginationBar() {
        this.paginationBar = new PaginationBar(pageNumber, getPageCount());
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setPageNumber(Integer pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        this.pageNumber = pageNumber;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize < 1) {
            pageSize = 1;
        } else if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        this.pageSize = pageSize;
    }

    public void setPaginationBar(PaginationBar paginationBar) {
        this.paginationBar = paginationBar;
    }


    public void setResult(List<T> result) {
        this.result = result;
    }

    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    // 排序方式（递增、递减）
    public enum Order {
        asc, desc
    }

}
