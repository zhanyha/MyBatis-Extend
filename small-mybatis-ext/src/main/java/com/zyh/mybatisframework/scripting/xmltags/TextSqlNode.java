package com.zyh.mybatisframework.scripting.xmltags;

import com.zyh.mybatisframework.parsing.GenericTokenParser;
import com.zyh.mybatisframework.parsing.TokenHandler;
import com.zyh.mybatisframework.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * @description: 文本类型的sql节点
 * @author：zhanyh
 * @date: 2023/5/31
 */
public class TextSqlNode implements SqlNode {

    private String text;
    private Pattern injectionFilter;

    public TextSqlNode(String text) {
        this(text, null);
    }

    public TextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    /**
     * 把${} 替换成 null ?
     * 并且判断是否是动态sql
     */
    public boolean isDynamic() {
        DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
        GenericTokenParser parser = createParser(checker);
        parser.parse(text);
        return checker.isDynamic();
    }
    private GenericTokenParser createParser(TokenHandler handler) {
        return new GenericTokenParser("${", "}", handler);
    }


    @Override
    public boolean apply(DynamicContext context) {
        GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
        return false;
    }

    // 绑定记号解析器
    private static class BindingTokenParser implements TokenHandler {

        private DynamicContext context;
        private Pattern injectionFilter;

        public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null) {
                context.getBindings().put("value", null);
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                context.getBindings().put("value", parameter);
            }
            // 从缓存里取得值
            Object value = OgnlCache.getValue(content, context.getBindings());
            String srtValue = (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
            checkInjection(srtValue);
            return srtValue;
        }

        // 检查是否匹配正则表达式
        private void checkInjection(String value) {
            if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
                throw new RuntimeException("Invalid input. Please conform to regex" + injectionFilter.pattern());
            }
        }
    }


    /**
     * 动态SQL检查器
     */
    private static class DynamicCheckerTokenParser implements TokenHandler {

        private boolean isDynamic;

        public DynamicCheckerTokenParser() {
            // Prevent Synthetic Access
        }

        public boolean isDynamic() {
            return isDynamic;
        }

        @Override
        public String handleToken(String content) {
            // 设置 isDynamic 为 true，即调用了这个类就必定是动态 SQL
            this.isDynamic = true;
            return null;
        }
    }
}
