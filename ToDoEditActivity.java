package local.hal.st32.android.ohs30132.todo;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ToDoEditActivity extends Activity{

	/**
	 * 新規登録モードか更新モードかを表すフィールド
	 */
	private int _mode = ToDoListActivity.MODE_INSERT;
	/**
	 * 更新モードの際、現在表示しているメモ情報のデータベース上の主キー値。
	 */
	private int _idNo = 0;
	/**
	 * ダイアログのカレンダー
	 */
	private TextView _label = null;

	private int _itemid = 0;

	private String sql = "";

	private static final String PREFS_NAME = "PSPrefsFile";

	Calendar cal = Calendar.getInstance();
	int nowYear = cal.get(Calendar.YEAR);
	int nowMonth = cal.get(Calendar.MONTH)+1;
	int nowDayofMonth = cal.get(Calendar.DAY_OF_MONTH);

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_to_do_edit);
		_label = (TextView)findViewById(R.id.etInputDate);

		Intent intent = getIntent();
		_mode = intent.getIntExtra("mode", ToDoListActivity.MODE_INSERT);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if(_mode == ToDoListActivity.MODE_INSERT){ //初めてリストを選択した時
			TextView tvTitleEdit = (TextView) findViewById(R.id.tvTitleEdit);
			tvTitleEdit.setText(R.string.tv_title_insert);

			//日付に１～９に０を付ける処理
			String strNowMonth ="";
			String strNowDayofMonth ="";

			strNowMonth = Integer.toString(nowMonth);
			strNowDayofMonth = Integer.toString(nowDayofMonth);

			if(nowMonth <= 9){
				strNowMonth = "0"+strNowMonth;
			}

			if(nowDayofMonth <= 9){
				strNowDayofMonth = "0"+strNowDayofMonth;
			}

			TextView etInputDate = (TextView) findViewById(R.id.etInputDate);
			etInputDate.setText(nowYear + "/" + strNowMonth + "/" + strNowDayofMonth);
		}else{ //２回目以降
			_idNo = intent.getIntExtra("idNo", 0);
			Memo memoData = DataAccess.findByPK(ToDoEditActivity.this, _idNo);

			EditText etInputName = (EditText) findViewById(R.id.etInputName);
			etInputName.setText(memoData.getName());

			TextView etInputDate = (TextView) findViewById(R.id.etInputDate);
			etInputDate.setText(memoData.getDate());

			ToggleButton toBuDone = (ToggleButton) findViewById(R.id.btnToggle);
			int Done = memoData.getDone();
			if(Done == 0){
				toBuDone.setChecked(false);
			}else{
				toBuDone.setChecked(true);
			}

			EditText etInputNote = (EditText) findViewById(R.id.etInputNote);
			etInputNote.setText(memoData.getNote());

			String[] strsp =  memoData.getDate().split("/");
			nowYear = Integer.parseInt(strsp[0]);
			nowMonth = Integer.parseInt(strsp[1]);
			nowDayofMonth = Integer.parseInt(strsp[2]);


		}
	}

	/**
	 * 初期のオプションメニュー
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		MenuInflater inflater = getMenuInflater();//MenuInflaterによるXMLの取得
		inflater.inflate(R.menu.to_do_edit, menu);
		MenuItem updateitem = menu.findItem(R.id.fileUpdate);
		updateitem.setIcon(android.R.drawable.ic_menu_save);
		MenuItem deletitem = menu.findItem(R.id.fileDelete);
		deletitem.setIcon(android.R.drawable.ic_menu_delete);

		Intent intent = getIntent();
		_mode = intent.getIntExtra("mode", ToDoListActivity.MODE_INSERT);

		if(_mode == ToDoListActivity.MODE_INSERT){ //初めてリストを選択した時
			updateitem.setTitle("登録");
			deletitem.setVisible(false);
		}else{
			updateitem.setTitle("更新");
		}
		return true;
	}

	/**
	 * menuの処理
	 * @param item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		DataAccess dba = new DataAccess();


		switch (itemId) {
		case R.id.fileUpdate:
			onSaveButtonClick();
			break;
		case R.id.fileDelete:
			onDeleteButtonClick();
			break;
		 case android.R.id.home:
	            finish();
	            return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);//defaultの値を返す場合false? それ以外ture?
	}

	/**
	 * 登録・更新ボタンが押された時のイベント処理用メソッド
	 *
	 */
	public void onSaveButtonClick(){
		EditText etInputName = (EditText) findViewById(R.id.etInputName);
		String inputName = etInputName.getText().toString();
		if(inputName.equals("")){
			Toast.makeText(ToDoEditActivity.this, R.string.msg_input_title, Toast.LENGTH_SHORT).show();
		}
		else{
			TextView etInputDate = (TextView) findViewById(R.id.etInputDate);
			EditText etInputNote = (EditText) findViewById(R.id.etInputNote);
			ToggleButton toBuDone = (ToggleButton) findViewById(R.id.btnToggle);
			String inputDate = etInputDate.getText().toString();
			String inputNote = etInputNote.getText().toString();
			int Toggle = 0;
			if(toBuDone.isChecked()){
				Toggle = 1;
			}else{
				Toggle = 0;
			}

			if(_mode == ToDoListActivity.MODE_INSERT){
				DataAccess.insert(ToDoEditActivity.this, inputName, inputDate, Toggle, inputNote);
			}
			else{
				DataAccess.update(ToDoEditActivity.this, _idNo, inputName, inputDate, Toggle, inputNote);
			}
			finish();
		}
	}


	/**
	 * 戻る
	 */
	public void onBackButtonClick(View view){
		finish();
	}

	/**
	 * 削除
	 */
	public void onDeleteButtonClick(){
		Builder builder = new Builder(ToDoEditActivity.this);
		builder.setTitle("通常ダイアログ");
		builder.setMessage("よろしいか？");
		builder.setPositiveButton("OK", new DialogButtonClickListener());
		builder.setNegativeButton("NG", new DialogButtonClickListener());
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private class DialogButtonClickListener implements DialogInterface.OnClickListener{
		@Override
		public void onClick(DialogInterface dialog, int which){
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				DataAccess.delete(ToDoEditActivity.this, _idNo);
				finish();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	}

	/**
	 * 期限変更
	 */
	public void showDatePickerDialog(View view){
		Calendar cal = Calendar.getInstance();
		Memo memoData = DataAccess.findByPK(ToDoEditActivity.this, _idNo);
		/*
		 * DatePickerDialog(引数);
		 * 1：コンテキスト
		 * 2：リスナクラス
		 * 3：初期値、年
		 * 4：初期値、月
		 * 5：初期値、日
		 */
		DatePickerDialog dialog = new DatePickerDialog(ToDoEditActivity.this, new
				DatePickerDialogDateSetListener(), nowYear, nowMonth-1, nowDayofMonth);
		dialog.show();
	}

	private class DatePickerDialogDateSetListener implements DatePickerDialog.OnDateSetListener{
		@Override
		public void onDateSet(DatePicker view, int year, int monthofYear, int dayofMonth){
			nowYear = year;
			nowMonth = monthofYear+1;
			nowDayofMonth = dayofMonth;

			//日付に１～９に０を付ける処理
			String strNowMonth ="";
			String strNowDayofMonth ="";

			strNowMonth = Integer.toString(nowMonth);
			strNowDayofMonth = Integer.toString(nowDayofMonth);

			if(nowMonth <= 9){
				strNowMonth = "0"+strNowMonth;
			}

			if(nowDayofMonth <= 9){
				strNowDayofMonth = "0"+strNowDayofMonth;
			}

			_label.setText(year + "/" + (strNowMonth) + "/" + strNowDayofMonth);
		}
	}
}
