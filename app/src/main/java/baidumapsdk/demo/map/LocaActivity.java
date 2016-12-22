package baidumapsdk.demo.map;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

public class LocaActivity extends FragmentActivity {


    private BaiduMap baiduMap;
    private MapView mapView;
    private ListView listView;
    // MapView中央对于的屏幕坐标
    Point mCenterPoint = null;
    LatLng mLoactionLatLng;
    // 地理编码
    GeoCoder mGeoCoder = null;
    private boolean isFirstLoc = true;
    LocationClient mLocationClient = null;
    private ImageView maker;
    MyAdapter adapter;
    BitmapDescriptor mSelectIco;
    private ImageView btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loca);
        mapView = (MapView) findViewById(R.id.mapview);
        baiduMap = mapView.getMap();
        listView = (ListView) findViewById(R.id.list);
        maker = (ImageView) findViewById(R.id.maker);
        btn = (ImageView) findViewById(R.id.localbtn);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        mSelectIco = BitmapDescriptorFactory.fromResource(R.mipmap.ic_geo);

        mapView.showZoomControls(false);
        mapView.showScaleControl(false);
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.zoomTo(17f);
        baiduMap.setMapStatus(statusUpdate);

        mLoactionLatLng = baiduMap.getMapStatus().target;
        baiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onTouch(MotionEvent motionEvent) {
                float y = mapView.getHeight() / 2.0f;
                float mh = maker.getHeight();
//                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
//                    ObjectAnimator.ofFloat(maker,"Y",y,y-40f).setDuration(200).start();
//                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ObjectAnimator.ofFloat(maker, "Y", y - mh, y * 7 / 10.0f, y - mh).setDuration(800).start();
                    LatLng latLng = baiduMap.getProjection().fromScreenLocation(mCenterPoint);
                    //30.594617711680527   104.06116551095249
                    mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption()).location(latLng));
                }
            }
        });

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        mLocationClient = new LocationClient(this);
        baiduMap.setMyLocationEnabled(true);
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null || baiduMap == null)
                    return;
                MyLocationData data = new MyLocationData.Builder()//
                        // .direction(mCurrentX)//
                        .accuracy(location.getRadius())//
                        .latitude(location.getLatitude())//
                        .longitude(location.getLongitude())//
                        .build();
                baiduMap.setMyLocationData(data);
                // 设置自定义图标
                MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
                baiduMap.setMyLocationConfigeration(config);
                //30.589751   104.067706
                //30.594617711680527   104.06116551095249
                mLoactionLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                mCenterPoint = baiduMap.getMapStatus().targetScreen;
                // 是否第一次定位
                if (isFirstLoc) {
                    isFirstLoc = false;
                    // 实现动画跳转
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLoactionLatLng);
                    baiduMap.animateMapStatus(u);
                    mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption()).location(mLoactionLatLng));
                }
                mLocationClient.stop();
            }
        });
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                List<PoiInfo> list = reverseGeoCodeResult.getPoiList();
                PoiInfo info = new PoiInfo();
                info.city =reverseGeoCodeResult.getAddressDetail().city;
                info.name = reverseGeoCodeResult.getAddress();
                info.location = reverseGeoCodeResult.getLocation();
                list.add(0,info);

                Log.d("", "onGetReverseGeoCodeResult:  Address:" + reverseGeoCodeResult.getAddress() + "  Circle:"
                        + reverseGeoCodeResult.getBusinessCircle()+"  city:"
                        + reverseGeoCodeResult.getAddressDetail().city + "  district:"
                        + reverseGeoCodeResult.getAddressDetail().district + "  province:"
                        + reverseGeoCodeResult.getAddressDetail().province + "  street:"
                        + reverseGeoCodeResult.getAddressDetail().street + "  streetNumber:"
                        + reverseGeoCodeResult.getAddressDetail().streetNumber + "  describeContents:"
                        + reverseGeoCodeResult.getAddressDetail().describeContents() + "  "
                );
//                for (int i = 0; i < list.size(); i++) {
//                    PoiInfo info = list.get(i);
//                    Log.d("", "onGetReverseGeoCodeResult: " + info.address);
//                }
                adapter.setList(list);
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (baiduMap != null && mLocationClient != null) {
                    isFirstLoc = true;
                    mLocationClient.start();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mGeoCoder.destroy();

    }

    class MyAdapter extends BaseAdapter {
        List<PoiInfo> list = new ArrayList<>();
        private int checkPosition = -1;

        public void setList(List<PoiInfo> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        public void setCheckPosition(int checkPosition) {
            this.checkPosition = checkPosition;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(LocaActivity.this, R.layout.item_address_layout, null);
                holder = new ViewHolder();
                holder.address = (TextView) convertView.findViewById(R.id.address);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.check = (ImageView) convertView.findViewById(R.id.check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final PoiInfo info = list.get(position);
//            String s = "address:"+info.address +"\ncity:"+info.city+"\nname:"+info.name;
            holder.address.setText(info.address);
            holder.name.setText(info.name);

            if (checkPosition != -1 && position == checkPosition) {
                holder.check.setVisibility(View.VISIBLE);
            } else {
                holder.check.setVisibility(View.GONE);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng latlng = info.location;
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latlng);
                    baiduMap.animateMapStatus(u);
                    baiduMap.clear();
                    OverlayOptions ooA = new MarkerOptions().position(latlng).icon(mSelectIco).anchor(0.5f, 0.5f).animateType(MarkerOptions.MarkerAnimateType.grow);
                    baiduMap.addOverlay(ooA);
                    setCheckPosition(position);
                }
            });
            return convertView;
        }
    }

    class ViewHolder {
        TextView address;
        TextView name;
        ImageView check;
    }

}
