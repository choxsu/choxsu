/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.choxsu.common.base.dialect;

import com.choxsu.common.base.db.Column;
import com.jfinal.plugin.activerecord.dialect.AnsiSqlDialect;

import java.util.List;


public class BaseAnsiSqlDialect extends AnsiSqlDialect implements IBaseModelDialect {


    @Override
    public String forFindByColumns(String table, String loadColumns, List<Column> columns, String orderBy, Object limit) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        sqlBuilder.append(loadColumns)
                .append(" FROM ")
                .append(table).append(" ");

        appIfNotEmpty(columns, sqlBuilder);


        if (orderBy != null) {
            sqlBuilder.append(" ORDER BY ").append(orderBy);
        }

        if (limit != null) {
            throw new RuntimeException("limit param not finished BaseAnsiSqlDialect.");
        }

        return sqlBuilder.toString();
    }


    @Override
    public String forPaginateSelect(String loadColumns) {
        return "SELECT " + loadColumns;
    }


    @Override
    public String forPaginateFrom(String table, List<Column> columns, String orderBy) {
        StringBuilder sqlBuilder = new StringBuilder(" FROM ").append(table);

        appIfNotEmpty(columns, sqlBuilder);

        if (orderBy != null) {
            sqlBuilder.append(" ORDER BY ").append(orderBy);
        }

        return sqlBuilder.toString();
    }


    private void appIfNotEmpty(List<Column> columns, StringBuilder sqlBuilder) {
        if (columns != null && !columns.isEmpty()) {
            sqlBuilder.append(" WHERE ");

            int index = 0;
            for (Column column : columns) {
                sqlBuilder.append(String.format(" %s %s ? ", column.getName(), column.getLogic()));
                if (index != columns.size() - 1) {
                    sqlBuilder.append(" AND ");
                }
                index++;
            }
        }
    }


}