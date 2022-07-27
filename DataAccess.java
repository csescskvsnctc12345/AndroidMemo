package local.hal.st32.android.ohs30132.todo;

import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 *
 * データベース上のデータを処理するクラス。
 *
 * @author ohs30132
 */
public class DataAccess {


	/**
	 * 主キーによるメモ内容検索メソッド。
	 * @param context コンテキスト
	 * @param id 主キー値
	 * @return 主キーに対応するcontentカラムの値。
	 */
	public static String findContentByPK(Context context, int id, String colum){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = null;
		String result = "";
		String sql = "SELECT * FROM tasks WHERE _id = " + id;

		try{
			cursor = db.rawQuery(sql, null);
			if(cursor != null && cursor.moveToFirst()){
				int idxContent = cursor.getColumnIndex(colum);
				result = cursor.getString(idxContent);
			}
		}
		catch (Exception ex) {
			Log.e("ERROR", ex.toString());
		}
		finally{
			db.close();
		}
		return result;
	}

	/**
	 * 主キーによるレコード存在チェックメソッド。
	 * @param context コンテキスト。
	 * @param id 主キー値。
	 * @return 主キーに対応するcontentカラムの値。
	 */
	public static boolean findRowByPK(Context context, int id){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = null;
		boolean result = false;
		String sql = "SELECT COUNT(*) AS count FROM tasks WHERE _id = " + id;

		try{
			cursor = db.rawQuery(sql, null);
			if(cursor != null && cursor.moveToFirst()){
				int idxCount = cursor.getColumnIndex("count");
				int count = cursor.getInt(idxCount);
				if(count >= 1){
					result = true;
				}
			}
		}
		catch (Exception ex) {
			Log.e("ERROR", ex.toString());
		}
		finally{
			db.close();
		}
		return result;
	}


	/**
	 * 全データ検索メソッド。
	 *ListActivityの一覧
	 * @param context コンテキスト
	 * @param sql文
	 * @return 検索結果のCursorオブジェクト
	 */
	public static Cursor findAll(Context context,String sql){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}


	/**
	 * 主キーによる検索
	 *
	 * @param context コンテキスト
	 * @param id 主キー値
	 * @return
	 */

	public static Memo findByPK(Context context, int id){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = null;
		Memo result = null;
		String sql = "SELECT _id, name, deadline, done, note FROM tasks WHERE _id = " + id;

		try{
			cursor = db.rawQuery(sql, null);
			if(cursor != null && cursor.moveToFirst()){
				int idxName = cursor.getColumnIndex("name");
				int idxDate = cursor.getColumnIndex("deadline");
				int idxDone = cursor.getColumnIndex("done");
				int idxNote = cursor.getColumnIndex("note");

				String name = cursor.getString(idxName);
				String deadline = cursor.getString(idxDate);
				int done = cursor.getInt(idxDone);
				String note = cursor.getString(idxNote);


				result = new Memo();
				result.setId(id);
				result.setName(name);
				result.setDate(deadline);
				result.setDone(done);
				result.setNote(note);
			}
		}
		catch (Exception ex) {
			Log.e("ERROR", ex.toString());
		}
		finally{
			db.close();
		}
		return result;
	}

	/**
	 * メモ情報を更新するメソッド。
	 * @param context コンテキスト。
	 * @param id 主キー値。
	 * @param name タスク名。
	 * @param deadline 期限。
	 * @param done 未完か完了か。
	 * @param note 詳細。
	 */
	public static void update(Context context, int id, String name, String deadline, int done, String note){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		String sql = "UPDATE tasks SET name = ?, deadline = ?, done = ?, note = ? WHERE _id = ?";
		SQLiteStatement stmt = db.compileStatement(sql);
		stmt.bindString(1, name);
		stmt.bindString(2, deadline);
		stmt.bindLong(3, done);
		stmt.bindString(4, note);
		stmt.bindLong(5, id);

		db.beginTransaction();
		try{
			stmt.executeInsert();
			db.setTransactionSuccessful();
		}
		catch (Exception ex) {
			Log.e("ERROR", ex.toString());
		}
		finally{
			db.endTransaction();
			db.close();
		}
	}


	/**
	 * メモ情報を新規登録するメソッド。
	 * @param context コンテキスト。
	 * @param name タスク名。
	 * @param deadline 期限。
	 * @param done 未完か完了か。
	 * @param note 詳細。
	 */
	public static void insert(Context context, String name, String deadline, int done, String note){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		String sql = "INSERT INTO tasks (name, deadline, done, note) VALUES(?,?,?,?)";
		SQLiteStatement stmt = db.compileStatement(sql);
		stmt.bindString(1, name);
		stmt.bindString(2, deadline);
		stmt.bindLong(3, done);
		stmt.bindString(4, note);

		db.beginTransaction();
		try{
			stmt.executeInsert();
			db.setTransactionSuccessful();
		}
		catch (Exception ex) {
			Log.e("ERROR", ex.toString());
		}
		finally{
			db.endTransaction();
			db.close();
		}
	}


	/**
	 * メモ情報を削除するメソッド。
	 * @param context コンテキスト。
	 * @param id 主キー値。
	 */
	public static void delete(Context context, int id){
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		String sql = "DELETE FROM tasks WHERE _id = ?;";
		SQLiteStatement stmt = db.compileStatement(sql);
		stmt.bindLong(1, id);


		db.beginTransaction();
		try{
			stmt.executeInsert();
			db.setTransactionSuccessful();
		}
		catch (Exception ex) {
			Log.e("ERROR", ex.toString());
		}
		finally{
			db.endTransaction();
			db.close();
		}
	}

}