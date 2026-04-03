package com.ainotes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * XSS 防护过滤器
 * 对请求参数中的 HTML 标签进行转义
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
    }

    private static class XssHttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return clean(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = clean(values[i]);
            }
            return cleaned;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> original = super.getParameterMap();
            Map<String, String[]> cleaned = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, String[]> entry : original.entrySet()) {
                String[] values = entry.getValue();
                String[] cleanedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    cleanedValues[i] = clean(values[i]);
                }
                cleaned.put(entry.getKey(), cleanedValues);
            }
            return cleaned;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return clean(value);
        }

        private String clean(String value) {
            if (value == null) return null;
            return StringEscapeUtils.escapeHtml4(value);
        }
    }
}
