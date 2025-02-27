/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.service.database;

import static java.lang.String.format;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSpecificSQLGenerator {

    private final DatabaseTypeResolver databaseTypeResolver;
    public static final String SELECT_CLAUSE = "SELECT %s";

    @Autowired
    public DatabaseSpecificSQLGenerator(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public String escape(String arg) {
        DatabaseType dialect = databaseTypeResolver.databaseType();
        if (dialect.isMySql()) {
            return format("`%s`", arg);
        } else if (dialect.isPostgres()) {
            return format("\"%s\"", arg);
        }
        return arg;
    }

    public String formatValue(JdbcJavaType columnType, String value) {
        return (columnType.isStringType() || columnType.isAnyDateType()) ? format("'%s'", value) : value;
    }

    public String groupConcat(String arg) {
        if (databaseTypeResolver.isMySQL()) {
            return format("GROUP_CONCAT(%s)", arg);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            // STRING_AGG only works with strings
            return format("STRING_AGG(%s::varchar, ',')", arg);
        } else {
            throw new IllegalStateException("Database type is not supported for group concat " + databaseTypeResolver.databaseType());
        }
    }

    public String limit(int count) {
        return limit(count, 0);
    }

    public String limit(int count, int offset) {
        if (databaseTypeResolver.isMySQL()) {
            return format("LIMIT %s,%s", offset, count);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("LIMIT %s OFFSET %s", count, offset);
        } else {
            throw new IllegalStateException("Database type is not supported for limit " + databaseTypeResolver.databaseType());
        }
    }

    public String calcFoundRows() {
        if (databaseTypeResolver.isMySQL()) {
            return "SQL_CALC_FOUND_ROWS";
        } else {
            return "";
        }
    }

    public String countLastExecutedQueryResult(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return "SELECT FOUND_ROWS()";
        } else {
            return countQueryResult(sql);
        }
    }

    public String countQueryResult(String sql) {
        return format("SELECT COUNT(*) FROM (%s) AS temp", sql);
    }

    public String currentBusinessDate() {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATE('%s')", DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("DATE '%s'", DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else {
            throw new IllegalStateException("Database type is not supported for current date " + databaseTypeResolver.databaseType());
        }
    }

    public String currentTenantDate() {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATE('%s')", DateUtils.getLocalDateOfTenant().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("DATE '%s'", DateUtils.getLocalDateOfTenant().format(DateUtils.DEFAULT_DATE_FORMATTER));
        } else {
            throw new IllegalStateException("Database type is not supported for current date " + databaseTypeResolver.databaseType());
        }
    }

    public String currentTenantDateTime() {
        if (databaseTypeResolver.isMySQL()) {
            return format("TIMESTAMP('%s')", DateUtils.getLocalDateTimeOfSystem().format(DateUtils.DEFAULT_DATETIME_FORMATTER));
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("TIMESTAMP '%s'", DateUtils.getLocalDateTimeOfSystem().format(DateUtils.DEFAULT_DATETIME_FORMATTER));
        } else {
            throw new IllegalStateException("Database type is not supported for current date time" + databaseTypeResolver.databaseType());
        }
    }

    public String subDate(String date, String multiplier, String unit) {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATE_SUB(%s, INTERVAL %s %s)", date, multiplier, unit);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("(%s::TIMESTAMP - %s * INTERVAL '1 %s')", date, multiplier, unit);
        } else {
            throw new IllegalStateException("Database type is not supported for subtracting date " + databaseTypeResolver.databaseType());
        }
    }

    public String dateDiff(String date1, String date2) {
        if (databaseTypeResolver.isMySQL()) {
            return format("DATEDIFF(%s, %s)", date1, date2);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("EXTRACT(DAY FROM (%s::TIMESTAMP - %s::TIMESTAMP))", date1, date2);
        } else {
            throw new IllegalStateException("Database type is not supported for date diff " + databaseTypeResolver.databaseType());
        }
    }

    public String lastInsertId() {
        if (databaseTypeResolver.isMySQL()) {
            return "LAST_INSERT_ID()";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "LASTVAL()";
        } else {
            throw new IllegalStateException("Database type is not supported for last insert id " + databaseTypeResolver.databaseType());
        }
    }

    public String castChar(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("CAST(%s AS CHAR)", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s::CHAR", sql);
        } else {
            throw new IllegalStateException(
                    "Database type is not supported for casting to character " + databaseTypeResolver.databaseType());
        }
    }

    public String castInteger(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("CAST(%s AS SIGNED INTEGER)", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s::INTEGER", sql);
        } else {
            throw new IllegalStateException("Database type is not supported for casting to bigint " + databaseTypeResolver.databaseType());
        }
    }

    public String currentSchema() {
        if (databaseTypeResolver.isMySQL()) {
            return "SCHEMA()";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "CURRENT_SCHEMA()";
        } else {
            throw new IllegalStateException("Database type is not supported for current schema " + databaseTypeResolver.databaseType());
        }
    }

    public String castJson(String sql) {
        if (databaseTypeResolver.isMySQL()) {
            return format("%s", sql);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return format("%s ::json", sql);
        } else {
            throw new IllegalStateException("Database type is not supported for casting to json " + databaseTypeResolver.databaseType());
        }
    }

    public String alias(@NotNull String field, String alias) {
        return Strings.isEmpty(alias) ? field : (alias + '.') + field;
    }

    public String buildSelect(Collection<String> fields, String alias, boolean embedded) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        String select = "";
        if (!embedded) {
            select = "SELECT ";
        }
        return select + fields.stream().map(e -> alias(escape(e), alias)).collect(Collectors.joining(", "));
    }

    public String buildFrom(String definition, String alias, boolean embedded) {
        if (definition == null) {
            return "";
        }
        String from = "";
        if (!embedded) {
            from = "FROM ";
        }
        return from + escape(definition) + (Strings.isEmpty(alias) ? "" : (" " + alias));
    }

    public String buildJoin(@NotNull String definition, String alias, @NotNull String fkCol, String refAlias, @NotNull String refCol,
            String joinType) {
        String join = Strings.isEmpty(joinType) ? "JOIN" : (joinType + " JOIN");
        alias = Strings.isEmpty(alias) ? "" : (" " + alias);
        return format("%s %s%s ON %s = %s", join, escape(definition), alias, alias(escape(fkCol), alias), alias(escape(refCol), refAlias));
    }

    public String buildOrderBy(List<Sort.Order> orders, String alias, boolean embedded) {
        if (orders == null || orders.isEmpty()) {
            return "";
        }
        String orderBy = "";
        if (!embedded) {
            orderBy = "ORDER BY ";
        }
        return orderBy + orders.stream().map(e -> String.join(" ", alias(escape(e.getProperty()), alias), e.getDirection().name()))
                .collect(Collectors.joining(", "));
    }
}
