package com.beanpattern.model;

import java.util.List;

/**
 * 分页返回体。
 */
public class PageResult<T> {

    private List<T> list;
    private int page;
    private int pageSize;
    private long total;
    private boolean hasMore;

    public static <T> PageResult<T> of(List<T> list, int page, int pageSize, long total) {
        PageResult<T> result = new PageResult<>();
        result.list = list;
        result.page = page;
        result.pageSize = pageSize;
        result.total = total;
        result.hasMore = (long) page * pageSize < total;
        return result;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
