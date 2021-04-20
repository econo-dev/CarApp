package com.gal.carapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class CarsAdapter extends BaseAdapter {

    List<Cars> carList;
    Context context;

    public CarsAdapter(Context context, List<Cars> carList){
        this.context=context;
        this.carList=carList;
    }

    @Override
    public int getCount() {
        return carList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
