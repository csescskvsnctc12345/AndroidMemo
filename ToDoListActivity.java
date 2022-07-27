package local.hal.st32.android.ohs30132.todo;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipData.Item;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ToDoListActivity extends ListActivity {

	private static final String PREFS_NAME = "PSPrefsFile";

	//ContextMenu用
	private List<Map<String, String>> _list;

	/**
	 * 新規登録モードを表す定数フィールド
	 */
	static final int MODE_INSERT = 1;
	/**
	 * 更新モードを表す定数フィールド
	 */
	static final int MODE_EDIT = 2;

	private int _itemid = 0;

	private static final int DEFAULT = 0;

	private String sql = "";

	MenuItem menutitle;

	static Calendar cl = Calendar.getInstance();
	static int nowYear = cl.get(Calendar.YEAR);
	static int nowMonth = cl.get(Calendar.MONTH);
	static int nowDayofMonth = cl.get(Calendar.DAY_OF_MONTH);

	static final String nowdate = String.valueOf(nowYear)+"年"
			+String.format("%1$02d", nowMonth+1)+"月"+String.format("%1$02d", nowDayofMonth)+"日";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_to_do_list);

		ListView listView = getListView();
		registerForContextMenu(listView);
	}

	/**
	 * 初期のオプションメニュー
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		MenuInflater inflater = getMenuInflater();//MenuInflaterによるXMLの取得
		inflater.inflate(R.menu.to_do_list, menu);
		MenuItem newFileitem = menu.findItem(R.id.newFile);
		newFileitem.setIcon(android.R.drawable.ic_menu_add);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		_itemid = settings.getInt("done", DEFAULT);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menutitle = menu.findItem(R.id.titleMenu);
		if(_itemid == 0){
			menutitle.setTitle("全タスク");
		}else if(_itemid == 1){
			menutitle.setTitle("未完のタスクのみ");
		}else if(_itemid == 2){
			menutitle.setTitle("完了のタスクのみ");
		}
		return true;
	}

	/**
	 * オプションメニューの処理
	 * @param item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		DataAccess dba = new DataAccess();

		switch (itemId) {
		case R.id.menuMikan:
			sql = "SELECT _id, name, deadline, done, note FROM tasks WHERE done = 0 ORDER BY deadline ASC";
			this._itemid = 1;
			editor.putInt("done", this._itemid);
			break;
		case R.id.menuKanryou:
			sql = "SELECT _id, name, deadline, done, note FROM tasks WHERE done = 1 ORDER BY deadline DESC";
			this._itemid = 2;
			editor.putInt("done", this._itemid);
			break;
		case R.id.menuAll:
			sql = "SELECT _id, name, deadline, done, note FROM tasks  ORDER BY deadline DESC";
			this._itemid = 0;
			editor.putInt("done", this._itemid);
			break;
		case R.id.newFile:
			Intent intent = new Intent(ToDoListActivity.this, ToDoEditActivity.class);
			intent.putExtra("mode", MODE_INSERT);
			startActivity(intent);
			break;
		default:
			break;
		}
		editor.commit();

		if(_itemid == 0){
			menutitle.setTitle("全タスク");
		}else if(_itemid == 1){
			menutitle.setTitle("未完のタスクのみ");
		}else if(_itemid == 2){
			menutitle.setTitle("完了のタスクのみ");
		}
		Cursor cursor = DataAccess.findAll(ToDoListActivity.this ,sql);
		String[] from = { "name","deadline","done" };
		int[] to = { R.id.tvTitle, R.id.tvDate, R.id.tvDone };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(ToDoListActivity.this, R.layout.row, cursor, from, to, 0);
		adapter.setViewBinder(new CustomViewBinder());
		setListAdapter(adapter);

		return super.onOptionsItemSelected(item);//defaultの値を返す場合false? それ以外ture?
	}


	/**
	 *ContextMenu用
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, view, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.to_do_contextmenu, menu);
		menu.setHeaderTitle(R.string.menu_context_headertitle);
		menu.setHeaderIcon(android.R.drawable.ic_menu_edit);
	}
	/**
	 *ContextMenu用
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int listId = (int) info.id;
		Memo memoData = DataAccess.findByPK(ToDoListActivity.this, listId);
		DataAccess dba = new DataAccess();

		String name = memoData.getName();
		String deadline = memoData.getDate();
		int done;
		String note = memoData.getNote();

		Cursor cursor = DataAccess.findAll(ToDoListActivity.this ,sql);
		String[] from = { "name","deadline","done" };
		int[] to = { R.id.tvTitle, R.id.tvDate, R.id.tvDone };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(ToDoListActivity.this, R.layout.row, cursor, from, to, 0);
		adapter.setViewBinder(new CustomViewBinder());

		int itemId = item.getItemId();
		switch(itemId){
			case R.id.menuContextKanryou:
				done = 1;
				dba.update(ToDoListActivity.this, listId, name, deadline, done, note);
				setListAdapter(adapter);
				break;
			case R.id.menuContextMikan:
				done = 0;
				dba.update(ToDoListActivity.this, listId, name, deadline, done, note);
				setListAdapter(adapter);
				break;
			case R.id.menuContextEtc: //ページ移動
				Intent intent = new Intent(ToDoListActivity.this, ToDoEditActivity.class);
				intent.putExtra("mode", MODE_EDIT);
				intent.putExtra("idNo", listId);
				startActivity(intent);
		}

		return super.onContextItemSelected(item);
	}


	/**
	 * 一番最初に出てくるListの一覧
	 */
	@Override
	protected void onResume(){
		super.onResume();
		//アプリ終了しても設定保存
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		_itemid = settings.getInt("done", DEFAULT);

		if(_itemid == 0){
			sql = "SELECT _id, name, deadline, done, note FROM tasks  ORDER BY deadline DESC";
		}else if(_itemid == 1){
			sql = "SELECT _id, name, deadline, done, note FROM tasks WHERE done = 0 ORDER BY deadline ASC";
		}else if(_itemid == 2){
			sql = "SELECT _id, name, deadline, done, note FROM tasks WHERE done = 1 ORDER BY deadline DESC";
		}

		Cursor cursor = DataAccess.findAll(ToDoListActivity.this , sql);
		String[] from = { "name","deadline","done" };
		int[] to = { R.id.tvTitle, R.id.tvDate, R.id.tvDone };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(ToDoListActivity.this, R.layout.row, cursor, from, to, 0);
		adapter.setViewBinder(new CustomViewBinder());
		setListAdapter(adapter);
	}

	/**
	 * Listを選んで飛ぶ処理
	 * @param listView
	 * @param view
	 * @param position
	 * @param id
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id){
		super.onListItemClick(listView, view, position, id);
		Cursor item = (Cursor) listView.getItemAtPosition(position);
		int idxId = item.getColumnIndex("_id");
		int idNo = item.getInt(idxId);

		Intent intent = new Intent(ToDoListActivity.this, ToDoEditActivity.class);
		intent.putExtra("mode", MODE_EDIT);
		intent.putExtra("idNo", idNo);
		startActivity(intent);

	}

	/**
	 * 新規ボタンが押されたときのイベント処理用メソッド。
	 * @param view
	 */

	public void onNewButtonClick(){
		Intent intent = new Intent(ToDoListActivity.this, ToDoEditActivity.class);
		intent.putExtra("mode", MODE_INSERT);
		startActivity(intent);
	}
	/**
	 * 一行に対しての処理をするメソッド
	 * @author ohs30132
	 *
	 */
	private class CustomViewBinder implements SimpleCursorAdapter.ViewBinder{
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			int ViewId = view.getId();
			switch (ViewId) {
			case R.id.tvDate:
				String date = "";
				TextView tvDate = (TextView)view;
				String strdate = cursor.getString(columnIndex);

				String[] strsp =  strdate.split("/");
				String dateYear = strsp[0];
				String dateMonth = strsp[1];
				String dateDayofMonth = strsp[2];
				date = dateYear+"年"+dateMonth+"月"+dateDayofMonth+"日";


				if(date.equals(nowdate)){
					strdate = "期限：今日";
					tvDate.setTextColor(Color.RED);
				}else{
					strdate = "期限："+date;
				}

				tvDate.setText(strdate);
				return true;
			case R.id.tvDone:
				int strdone = cursor.getInt(columnIndex);
				LinearLayout row = (LinearLayout)view.getParent();

				if(strdone == 1){
					row.setBackgroundResource(R.color.list_checked);
				}else if(strdone == 0){
					row.setBackgroundResource(R.color.list_default);
				}
				return true;
			}
			return false;
		}
	}


}
