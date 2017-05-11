package com.lisn.baidumapplugin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 2017/5/11.
 */

abstract class SearchAdapter extends BaseAdapter{

    private List<SearchBean> datas;
    private Context mContext;

    public SearchAdapter(Context mContext) {
        this.mContext=mContext;
    }
    public void setData(List<SearchBean> datas){
        this.datas=datas;
    }
    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView=View.inflate(mContext, R.layout.item_search, null);
        }
        TextView tv_name = (TextView)convertView.findViewById(R.id.tv_name);
        TextView tv_address = (TextView)convertView.findViewById(R.id.tv_address);
        LinearLayout ll_content = (LinearLayout)convertView.findViewById(R.id.ll_content);
        final SearchBean data = datas.get(position);
        ll_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener( data);
            }
        });
        tv_name.setText(data.name);
        tv_address.setText(data.address);
        return convertView;
    }

    public abstract void ItemClickListener(SearchBean data);

}
