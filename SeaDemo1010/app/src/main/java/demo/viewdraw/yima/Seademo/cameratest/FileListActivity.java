package demo.viewdraw.yima.Seademo.cameratest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import demo.viewdraw.yima.Seademo.cameratest.utils.ApiTools;
import demo.viewdraw.yima.Seademo.cameratest.utils.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import demo.viewdraw.yima.Seademo.R;


//照片浏览
public class FileListActivity extends Activity implements OnItemClickListener {
	private ListView mListView;
	private FileAdapter mAdapter;
	private ArrayList<HashMap<String, Object>> mArrList = new ArrayList<>();



	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filelist);

		mListView = (ListView) findViewById(R.id.list);
		if (ApiTools.checkSdCard(this)){
			setTitle(ApiTools.imgDirPath);

			mAdapter = new FileAdapter(this, mArrList);
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(FileListActivity.this);

			new Thread(){
				@Override
				public void run() {
					setArrList();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mAdapter.refresh(mArrList);
						}
					});
				}
			}.start();
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		/**
		 * 每一个File都是一个文件对象
		 * 1.根据textView创建相应的File
		 * 2.创建Intent，设置它的Action
		 * 当点击条目的时候，即打开图片
		 */
		File f = new File(ApiTools.imgDirPath  + ((FileAdapter.viewHolder) view.getTag()).textView.getText());
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);	//设置intent的Action属性,搜索应用时会找到
		intent.setDataAndType(Uri.fromFile(f), "image/*");		//设置Intent的数据来源和数据类型，表示默认打开图片
		startActivity(intent);
	}

	@Override	//menu菜单用于删除
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mAdapter == null || mAdapter.getListItemSelected().size() <= 0) {
			ApiTools.showMsg(this, "请选择照片", true);
			return true;
		}

		//创建对话框，提示删除选择的图片
		new AlertDialog.Builder(this).setMessage("删除选择的图片吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ApiTools.showMsg(FileListActivity.this, delPics() ? "删除成功" : "删除失败", true);
			}
		}).setNegativeButton("取消", null).create().show();
		return true;
	}


	//装载图片
	private void setArrList(){
		File[] files = new File(ApiTools.imgDirPath).listFiles();
		if(files == null){
			return;
		}

		List<File> fileList = new ArrayList<>();
		for(int i=0; i<files.length; i++){
			if (files[i].isFile()) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".jpg") || fileName.endsWith("jpeg") || fileName.endsWith(".png")) {
					fileList.add(files[i]);
				}
			}
		}
		Collections.sort(fileList, new ApiTools.descendSortByTime()); //按索最后修改时间降序排序

		List<File> tmpList = new ArrayList<>();	//用于存放分页file数据
		int total = fileList.size();			//共计total张
		int nums  = 10;							//每次加载nums张
		int time  = total / nums + 1;			//需要加载time次

		// 加载time次，一次加载nums张
		// 第一个for加载time次
		// 第二个for：time=0，加载0-4张，time=1，加载5-9次；依次类推
		for(int j=0; j<time; j++){
			for (int i = 0; (i < nums) && (j * nums + i < total); i++){
				tmpList.add(fileList.get(j * nums + i));
			}

			if (tmpList.size() > 0) {
				setArrList(tmpList);
				tmpList.clear();
			}
		}
	}
	private void setArrList(List<File> fileList) {
		for (int i = 0; i < fileList.size(); i++) {
			final File file = fileList.get(i);
			mArrList.add(new HashMap<String, Object>(){{
				put("file",  file);
				put("thImg", ApiTools.getThumbnailImg(file, 64 * 3, 48 * 3));
				put("isChk", false);
			}});
		}
	}


	//删除照片操作
	private boolean delPics() {
		ArrayList<HashMap<String, Object>> arrList = mAdapter.getData();

		ArrayList<HashMap<String, Object>> listItemSelected = mAdapter.getListItemSelected();
		for (HashMap<String, Object>map : listItemSelected) {
			File file = (File) map.get("file");

			if (!file.delete()){
				Log.e(">>", "file 删除失败！"+file);
				return false;
			}

			if (!arrList.remove(map)){
				Log.e(">>", "arrList 删除失败！"+map);
				return false;
			}
		}
		mAdapter.refresh(arrList);
		return true;
	}
}
