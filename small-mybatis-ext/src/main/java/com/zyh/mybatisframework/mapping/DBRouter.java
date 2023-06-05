package com.zyh.mybatisframework.mapping;

/**
 * @description: <TableSelect>标签
 * @author：zhanyh
 * @date: 2023/6/5
 */
public class DBRouter {
    private int size;
    private String format;
    private String join;

    public DBRouter(int size, String format, String join) {
        this.size = size;
        this.format = format;
        this.join = join;
    }

    public int getSize() {
        return size;
    }

    public String getFormat() {
        return format;
    }

    public String getJoin() {
        return join;
    }
}
