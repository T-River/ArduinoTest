package example.android.maps;

//TODO: 何をインポートしてるのか理解する
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.graphics.Canvas;//線かくためにimport
import android.graphics.Paint;
import android.graphics.Color;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Chiba and move the camera
        LatLng chiba = new LatLng(35.34,136.15);//緯度、経度の設定　TODO:Arduinoからの値を反映する
        mMap.addMarker(new MarkerOptions().position(chiba).title("Biwako"));//設定した緯度経度の位置のマーカー名設定
        mMap.moveCamera(CameraUpdateFactory.newLatLng(chiba));//カメラをchibaの位置に動かす

        CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(35.34,136.15),11);//アプリ開いたときの表示（緯度経度ズーム）を設定
        mMap.moveCamera(cUpdate);//カメラ動かす

        LatLng shiga = new LatLng(35.24,136.05);
        mMap.addMarker(new MarkerOptions().position(shiga).title("Biwa_Lake!!"));

    }//緯度経度配列で指定して、ボタンで+1  マーカーも変更
}
