package com.zyh.mybatisframework.plugin.impl.page;


/**
 * @description:
 * @author：zhanyh
 * @date: 2023/6/4
 */
public class PageSeger {

    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();


    protected static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    public static Page getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void clearPage() {
        LOCAL_PAGE.remove();
    }

    public static Page startPage(int pageNum, int pageSize) {
        Page page = new Page(pageNum, pageSize);
        setLocalPage(page);
        return page;
    }

}
