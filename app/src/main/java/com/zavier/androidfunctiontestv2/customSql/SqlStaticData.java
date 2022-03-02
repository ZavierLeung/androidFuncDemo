package com.zavier.androidfunctiontestv2.customSql;

import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

public class SqlStaticData {

    public long rowId;
    public String funcName;
    public int funcStatus;

    public SqlStaticData(){
        this.rowId = 0L;
        this.funcName = "";
        this.funcStatus = MessageUtils.FLAG_TEST_READY;
    }

    @Override
    public String toString() {
        return "SqlStaticData [_id=" + rowId + ", name=" + funcName + ", status=" + funcStatus + "]";
    }

}
