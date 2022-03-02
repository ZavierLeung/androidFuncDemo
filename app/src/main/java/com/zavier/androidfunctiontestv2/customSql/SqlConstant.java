package com.zavier.androidfunctiontestv2.customSql;

import java.util.Arrays;
import java.util.List;

public class SqlConstant {

    public static final String SQL_ID = "rowId";
    public static final String SQL_FUNC_NAME = "funcName";
    public static final String SQL_FUNC_STATUS = "funcStatus";

    public static final String DB_NAME = "CustomSqlInfo.db";    // 数据库名称
    public static final int DB_VERSION = 1;     // 数据库的版本号
    public static final String DB_TABLE_NAME = "custom_sql_info";
    public static final String DB_TABLE_SQL = "create table if not exists " + DB_TABLE_NAME +
            " (" + SQL_ID +" integer primary key autoincrement, " + SQL_FUNC_NAME  +
            " varchar not null,"+ SQL_FUNC_STATUS+ " integer not null)";

}
