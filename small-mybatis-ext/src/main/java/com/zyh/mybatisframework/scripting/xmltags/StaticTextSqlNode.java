package com.zyh.mybatisframework.scripting.xmltags;

/**
 * @description: 静态文本sql
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class StaticTextSqlNode implements SqlNode{
    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    /**
     * 解析动态sql的递归出口
     * @param context
     * @return
     */
    @Override
    public boolean apply(DynamicContext context) {
        //将文本加入context
        context.appendSql(text);
        return true;
    }
}
