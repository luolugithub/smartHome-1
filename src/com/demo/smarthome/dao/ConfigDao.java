package com.demo.smarthome.dao;

import com.demo.smarthome.db.DatabaseHelper;
import com.demo.smarthome.service.ConfigService;

import android.content.Context;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 配置
 * @author Administrator
 * 
 */
public class ConfigDao implements ConfigService {

	private String ACTIVITY_TAG = "ConfigDao";
	private DatabaseHelper dbHelper = null;

	public ConfigDao(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	/**
	 *
	 */
	@Override
	public String getCfgByKey(String key) {
		// Log.i(ACTIVITY_TAG, "public String getCfgByKey(String key) key:" +
		// key);
		String value = "";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT value from syscfg where key =? ";
			Cursor cursor = db.rawQuery(sqlStr, new String[] { key });
			if (cursor.moveToFirst()) {
				index = 0;
				value = cursor.getString(index++);
			}
		} catch (Exception e) {
			value = "";
			// Log.i(ACTIVITY_TAG,"public String getCfgByKey(String key)catch  Exception:"+
			// e.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}

		if (value == null) {
			value = "";
		}

		// Log.i(ACTIVITY_TAG, "public String getCfgByKey(String key) key" +
		// key+ "  value:" + value);
		return value;
	}

	/**
	 *
	 */
	@Override
	public boolean SaveSysCfgByKey(String key, String value) {
		// Log.i(ACTIVITY_TAG,
		// "public boolean SaveSysCfgByKey(String key, String value key"+ key +
		// "  value:" + value);
		boolean flag = false;
		if (key == null) {
			return flag;
		}

		if (key == "") {
			return flag;
		}
		if (value == null) {
			value = "";
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (findConfigByKey(db, key)) { // �м�¼
				// update
				flag = updateConfig(db, key, value);
			} else { // �޼�¼ insert
				flag = insertConfig(db, key, value);
			}
			flag = true;
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return flag;
	}

	// =====
	/**
	 *
	 * 
	 * @param
	 * @return
	 * */
	private boolean findConfigByKey(SQLiteDatabase db, String key) {

		boolean flag = false;
		if (db == null) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select  value from syscfg where key= ? ",
				new String[] { key });
		if (cursor.moveToFirst()) {
			String value = cursor.getString(0);
			if (value != null) {
				if (value.trim() != "") {
					flag = true;
				}
			}
		}
		return flag;
	}

	/**
	 * 
	 * @param db
	 *
	 * @param key
	 *
	 * @param value
	 *            ֵ
	 * @return
	 */
	private boolean updateConfig(SQLiteDatabase db, String key, String value) {
		boolean flag = false;

		// Log.i(ACTIVITY_TAG,
		// " private boolean updateConfig(SQLiteDatabase db, String key, String value)  key:"+
		// key + "   value:" + value);
		if (db == null) {
			return flag;
		}
		try {
			db.execSQL("update  syscfg set " + "value=?  where key=?",
					new Object[] { value, key });
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 
	 * @param db
	 * @param key
	 * @param value
	 * @return
	 */
	private boolean insertConfig(SQLiteDatabase db, String key, String value) {
		// Log.i(ACTIVITY_TAG,
		// "   private boolean insertConfig(SQLiteDatabase db, String key, String value)   key:"+
		// key + "   value:" + value);
		boolean flag = false;
		if (db == null) {
			return flag;
		}
		try {
			db.execSQL("insert into syscfg (" + "key,value ) values (?,?)",
					new Object[] { key, value });
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
}
