package com.lqjai.common.utils;

import lombok.Data;

import java.util.List;

/**
 * 用于承载分页的数据结果
 */
@Data
public class PageResult<T> {

    private Long total;//总记录数
    private List<T> rows;//记录

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult() {
    }
}
