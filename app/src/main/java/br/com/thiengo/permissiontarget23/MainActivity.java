package br.com.thiengo.permissiontarget23;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import me.drakeet.materialdialog.MaterialDialog;


public class MainActivity extends AppCompatActivity implements LocationListener {
    public static final String TAG = "LOG";
    public static final int REQUEST_PERMISSIONS_CODE = 128;

    private MaterialDialog mMaterialDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "test");
        switch( requestCode ){
            case REQUEST_PERMISSIONS_CODE:
                for( int i = 0; i < permissions.length; i++ ){

                    if( permissions[i].equalsIgnoreCase( Manifest.permission.ACCESS_FINE_LOCATION )
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED ){

                        readMyCurrentCoordinates();
                    }
                    else if( permissions[i].equalsIgnoreCase( Manifest.permission.WRITE_EXTERNAL_STORAGE )
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){

                        createDeleteFolder();
                    }
                    else if( permissions[i].equalsIgnoreCase( Manifest.permission.READ_EXTERNAL_STORAGE )
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){

                        readFile(Environment.getExternalStorageDirectory().toString() + "/myFolder");
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // LISTENERS
    public void callLoadImage(View view) {
        Log.i(TAG, "callLoadImage()");

        ImageView iv = (ImageView) findViewById(R.id.iv_logo);
        Picasso.with(this).load("http://www.thiengo.com.br/img/system/logo/logo-thiengo-calopsita-70x70.png").into(iv);
    }

    public void callWriteOnSDCard(View view) {
        Log.i(TAG, "callWriteOnSDCard()");

        if( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) ){
                callDialog( "É preciso a permission WRITE_EXTERNAL_STORAGE para apresentação do content.", new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} );
            }
            else{
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE );
            }
        }
        else{
            createDeleteFolder();
        }

    }

    public void callReadFromSDCard(View view) {
        Log.i(TAG, "callReadFromSDCard()");

        if( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.READ_EXTERNAL_STORAGE ) ){
                callDialog( "É preciso a permission READ_EXTERNAL_STORAGE para apresentação do content.", new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} );
            }
            else{
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE );
            }
        }
        else{
            readFile(Environment.getExternalStorageDirectory().toString() + "/myFolder");
        }
    }

    public void callAccessLocation(View view) {
        Log.i(TAG, "callAccessLocation()");

        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.ACCESS_FINE_LOCATION ) ){
                callDialog( "É preciso a permission ACCESS_FINE_LOCATION para apresentação dos eventos locais.", new String[]{Manifest.permission.ACCESS_FINE_LOCATION} );
            }
            else{
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE );
            }
        }
        else{
            readMyCurrentCoordinates();
        }
    }




    // FILE
        private void createDeleteFolder() {
            String path = Environment.getExternalStorageDirectory().toString() + "/myFolder";
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/myFolder");

            if (file.exists()) {
                new File(Environment.getExternalStorageDirectory().toString() + "/myFolder", "myFile.txt").delete();
                if (file.delete()) {
                    Log.i(TAG, "Folder DELETED!");
                } else {
                    Log.i(TAG, "Folder delete action was FAIL, take some permissions!");
                }
            } else {
                if (file.mkdir()) {
                    createFile(path);
                    Log.i(TAG, "Folder CREATED!");
                } else {
                    Log.i(TAG, "Folder create action was FAIL, take some permissions!");
                }
            }
        }

        private void createFile(String path) {
            File file = new File(path + "/myFile.txt");
            OutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(new String("Just a test").getBytes());

                Log.i(TAG, "File CREATED!");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        private void readFile(String path) {
        File file = new File(path, "myFile.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, text.toString());
    }


    // GEOLOCATION
        private void readMyCurrentCoordinates() {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Location location = null;
            double latitude = 0;
            double longitude = 0;

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.i(TAG, "No geo resource able to be used.");
            }
            else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
                    Log.d(TAG, "Network");
                    location = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 2000, 0, this );
                        Log.d(TAG, "GPS Enabled");
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
            Log.i( TAG, "Lat: "+latitude+" | Long: "+longitude );
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i( TAG, "Lat: "+location.getLatitude()+" | Long: "+location.getLongitude() );
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {

        }




    // UTIL
        private void callDialog( String message, final String[] permissions ){
            mMaterialDialog = new MaterialDialog(this)
                .setTitle("Permission")
                .setMessage( message )
                .setPositiveButton("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_CODE);
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
            mMaterialDialog.show();
        }
}
