package com.zavier.androidfunctiontestv2.customSql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zavier.androidfunctiontestv2.customUtils.LogUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class SqlHelper extends SQLiteOpenHelper {
    private static SqlHelper mHelper = null;
    private SQLiteDatabase mDatebase = null;
    private final String TAG = "SqlHelper";

    public SqlHelper(@Nullable Context context) {
        super(context, SqlConstant.DB_NAME, null, SqlConstant.DB_VERSION);
    }

    public SqlHelper(@Nullable Context context, int version) {
        super(context, SqlConstant.DB_NAME, null, version);
    }

    public static SqlHelper getInstance(Context context, int version){
        if(version > 0 && mHelper == null){
            mHelper = new SqlHelper(context,version);
        } else if(mHelper == null){
            mHelper = new SqlHelper(context);
        }

        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SqlConstant.DB_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //打开数据库的读连接
    public SQLiteDatabase openReadLink(){
        if(mDatebase == null || !mDatebase.isOpen()){
            mDatebase = mHelper.getReadableDatabase();
        }
        return mDatebase;
    }

    //打开数据库的写连接
    public SQLiteDatabase openWriteLink(){
        if(mDatebase == null || !mDatebase.isOpen()){
            mDatebase = mHelper.getWritableDatabase();
        }
        return mDatebase;
    }

    //关闭数据库连接
    public void closeDBLink(){
        if(mDatebase != null && mDatebase.isOpen()){
            mDatebase.close();
            mDatebase = null;
        }
    }

    //根据指定条件删除表记录
    public int deleteDB(String tableName,String condition){
        return mDatebase.delete(tableName,condition,null);
    }

    //删除全部表记录
    public int deleteAllDB(String tableName){
        return mDatebase.delete(tableName,null,null);
    }

    public int update(SqlStaticData info) {
        // 执行更新记录动作，该语句返回记录更新的数目
        return update(info, SqlConstant.SQL_ID + "=" + info.rowId);
    }

    //根据条件更新指定的表记录
    public int update(SqlStaticData info, String condition){
        ContentValues cv = new ContentValues();
        cv.put(SqlConstant.SQL_FUNC_NAME, info.funcName);
        cv.put(SqlConstant.SQL_FUNC_STATUS, info.funcStatus);
        return mDatebase.update(SqlConstant.DB_TABLE_NAME,cv,condition,null);
    }

    //往表里添加单条记录
    public long insertOne(SqlStaticData info){
        long result = -1;
        ArrayList<SqlStaticData> tempArray = new ArrayList<SqlStaticData>();
        // 判断是否有相同的名字，则返回-2（注意：条件语句的等号后面要用单引号括起来）
        if(info.funcName != null && info.funcName.length() > 0){
            String condition = String.format(SqlConstant.SQL_FUNC_NAME + "='%s'", info.funcName);
            tempArray = query(condition);
            if(tempArray.size() > 0){
                return -2;
            }
        }

        ContentValues cv = new ContentValues();
        cv.put(SqlConstant.SQL_FUNC_NAME, info.funcName);
        cv.put(SqlConstant.SQL_FUNC_STATUS, info.funcStatus);
        result = mDatebase.insert(SqlConstant.DB_TABLE_NAME,"",cv);

        return result;
    }

    //往表里添加多条记录
    public long insertMore(ArrayList<SqlStaticData> infoArray){
        long result = -1;
        for(int i = 0; i < infoArray.size(); i++){
            SqlStaticData info = infoArray.get(i);
            ArrayList<SqlStaticData> tempArray = new ArrayList<SqlStaticData>();
            // 判断是否有相同的名字，则返回-2（注意：条件语句的等号后面要用单引号括起来）
            if(info.funcName != null && info.funcName.length() > 0){
                String condition = String.format(SqlConstant.SQL_FUNC_NAME + "='%s'", info.funcName);
                tempArray = query(condition);
                if(tempArray.size() > 0){
                    update(info, condition);
                    result = tempArray.get(0).rowId;
                    continue;
                }
            }

            ContentValues cv = new ContentValues();
            cv.put(SqlConstant.SQL_FUNC_NAME, info.funcName);
            cv.put(SqlConstant.SQL_FUNC_STATUS, info.funcStatus);
            result = mDatebase.insert(SqlConstant.DB_TABLE_NAME,"",cv);

            if(result == -1)
                return result;
        }
        return result;
    }

    //根据指定条件查询记录，并返回结果数据队列
    public ArrayList<SqlStaticData> query(String condition){
        String sql = String.format("select * from %s where %s;", SqlConstant.DB_TABLE_NAME,condition);
        ArrayList<SqlStaticData> info = new ArrayList<SqlStaticData>();
        //执行记录查询动作，返回结果集的游标
        Cursor cursor = mDatebase.rawQuery(sql,null);

        while (cursor.moveToNext()){
            SqlStaticData info1 = new SqlStaticData();
            info1.rowId = cursor.getLong(cursor.getColumnIndex(SqlConstant.SQL_ID));
            info1.funcName = cursor.getString(cursor.getColumnIndex(SqlConstant.SQL_FUNC_NAME));
            info1.funcStatus = cursor.getInt(cursor.getColumnIndex(SqlConstant.SQL_FUNC_STATUS));
            info.add(info1);
        }
        cursor.close();
        return info;
    }

    //根据指定条件查询记录，并返回结果数据队列
    public ArrayList<SqlStaticData> queryAll(){
        String sql = String.format("select * from %s;", SqlConstant.DB_TABLE_NAME);
        ArrayList<SqlStaticData> info = new ArrayList<SqlStaticData>();
        //执行记录查询动作，返回结果集的游标
        Cursor cursor = mDatebase.rawQuery(sql,null);

        while (cursor.moveToNext()){
            SqlStaticData info1 = new SqlStaticData();
            info1.rowId = cursor.getLong(cursor.getColumnIndex(SqlConstant.SQL_ID));
            info1.funcName = cursor.getString(cursor.getColumnIndex(SqlConstant.SQL_FUNC_NAME));
            info1.funcStatus = cursor.getInt(cursor.getColumnIndex(SqlConstant.SQL_FUNC_STATUS));
            info.add(info1);
        }
        cursor.close();
        return info;
    }

    public boolean queryByNameIsExist(String name){
        ArrayList<SqlStaticData> infoArray = query(String.format(SqlConstant.SQL_FUNC_NAME + "='%s'", name));
        if(infoArray.size() > 0){
            LogUtils.LogD(TAG, name + " is exist");
            return true;
        }
        LogUtils.LogD(TAG, name + " is not exist");
        return false;
    }

    public int getTableRow(){
        String sql = String.format("select * from %s;", SqlConstant.SQL_FUNC_NAME);
        //执行记录查询动作，返回结果集的游标
        Cursor cursor = mDatebase.rawQuery(sql,null);
        if(cursor.getCount() == 0){
            return 0;
        } else {
            return cursor.getCount();
        }
    }
}
