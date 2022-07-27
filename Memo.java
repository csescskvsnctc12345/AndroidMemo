package local.hal.st32.android.ohs30132.todo;

import java.util.Date;


/**
 * メモ情報を格納するエンティティクラス
 * @author
 *
 */

public class Memo {
	/**
	 * 主キーのID値
	 */
	private int _id;
	/**
	 * タスク名
	 */
	private String _name;
	/**
	 * DATE
	 */
	private String _date;
	/**
	 * 状態
	 */
	private int _done;
	/**
	 * メモ
	 */
	private String _note;


	//以下アクセサメソッド。

	public int getId(){
		return _id;
	}
	public void setId(int id){
		_id = id;
	}
	public String getName(){
		return _name;
	}
	public void setName(String name){
		_name = name;
	}
	public String getDate(){
		return _date;
	}
	public void setDate(String date){
		_date = date;
	}
	public int getDone(){
		return _done;
	}
	public void setDone(int done){
		_done = done;
	}
	public String getNote(){
		return _note;
	}
	public void setNote(String note){
		_note = note;
	}

}
