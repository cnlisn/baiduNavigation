package com.lisn.baidumapplugin;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by admin on 2017/5/11.
 */

class SearchBean {
    public  LatLng location;
    public String name;
    public String address;

    public SearchBean(String name, String address, LatLng location) {
        this.name = name;
        this.address = address;
        this.location = location;
    }
}
