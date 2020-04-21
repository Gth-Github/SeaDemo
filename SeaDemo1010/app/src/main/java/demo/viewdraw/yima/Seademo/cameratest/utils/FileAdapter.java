package demo.viewdraw.yima.Seademo.cameratest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import demo.viewdraw.yima.Seademo.R;


public class FileAdapter extends BaseAdapter {
//  private Context mContext;
    private ArrayList<HashMap<String, Object>> mArrList;
    private LayoutInflater mInflater;


    public FileAdapter(Context context, ArrayList<HashMap<String, Object>> arrList) {
//      this.mContext = context;
        this.mArrList = arrList;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mArrList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        viewHolder vHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_filelist_item, null);
            vHolder = new viewHolder();
            vHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            vHolder.textView = (TextView) convertView.findViewById(R.id.picname);
            vHolder.checkBox = (CheckBox) convertView.findViewById(R.id.chbox);
            convertView.setTag(vHolder);
        } else {
            vHolder = (viewHolder) convertView.getTag();
        }

        HashMap<String,Object> map = mArrList.get(position);
        File file = (File) map.get("file");

        vHolder.imageView.setImageBitmap((Bitmap) map.get("thImg"));
        vHolder.textView.setText(file.getName());
        vHolder.checkBox.setChecked((boolean) map.get("isChk"));

        vHolder.checkBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mArrList.get(position).put("isChk", ((CheckBox) view).isChecked());
            }
        });
        return convertView;
    }

    public final class viewHolder {
        public ImageView imageView;
        public TextView textView;
        public CheckBox checkBox;
    }


    public ArrayList<HashMap<String, Object>> getData() {
        return mArrList;
    }

    public ArrayList<HashMap<String, Object>> getListItemSelected() {
        ArrayList<HashMap<String, Object>> listItemSelected = new ArrayList<>();
        for (HashMap<String, Object> map : mArrList){
            if ((boolean) map.get("isChk")) {
                listItemSelected.add(map);
            }
        }
        return listItemSelected;
    }

    public void refresh(ArrayList<HashMap<String, Object>> arrList) {
        mArrList = arrList;
        notifyDataSetChanged();
    }

}
