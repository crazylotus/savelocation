package com.example.recordtrack


import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.recordtrack.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where

import java.util.Locale
import kotlin.concurrent.fixedRateTimer
import kotlin.properties.Delegates


class MainActivity : BaseActivity(),  LocationAdapter.OnClickListerner, OnMapReadyCallback {

    var closeApp = false

    override fun onResume() {
        super.onResume()
        closeApp = false
    }

    private lateinit var binding: ActivityMainBinding


    var latitude =0.0
    var longitude =0.0



    /**
     * FusedLocationProviderApi Save request parameters
     */
    private var mLocationRequest: LocationRequest? = null


    /**
     * Provide callbacks for location events.
     */
    private var mLocationCallback: LocationCallback? = null

    /**
     * An object representing the current location
     */
    private var mCurrentLocation: Location? = null

    //A client that handles connection / connection failures for Google locations
    // (changed from play-services 11.0.0)
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private val provider: String? = null

    private var realm: Realm? = null
    private val mMap: GoogleMap? = null

    var locationList : ArrayList<LocationDetails> = ArrayList()

    var adapter: LocationAdapter? = null

    lateinit var userId : String

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        sharedPreferences = getSharedPreferences("application", Context.MODE_PRIVATE);
        realm =Realm.getDefaultInstance()

        var imageUrl = sharedPreferences.getString("user_image_url","")
        var name = sharedPreferences.getString("user_name","")

        userId = sharedPreferences.getString("user_id","").toString()
        binding.profileName.text=name
        Glide.with(this).load(imageUrl).into(binding.profileImage)


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.signOut.setOnClickListener {
            signOut()
        }

        if(isLocationPermissionGranted()){
            locationFetch()
        }


        fixedRateTimer("timer",false,0,5*(1000*60)){
            this@MainActivity.runOnUiThread {
                locationFetch()
            }
        }


        val linearLayoutManager = LinearLayoutManager(this)
        binding.locationList.setLayoutManager(linearLayoutManager)
        binding.locationList.setLayoutManager(linearLayoutManager)
        binding.locationList.setHasFixedSize(true)
        adapter = LocationAdapter(this, locationList, this)
        binding.locationList.setAdapter(adapter)

        val tasks : RealmResults<UserLocationModel> = realm!!.where<UserLocationModel>().findAll()
        val openTasks : List<UserLocationModel> = tasks.where().equalTo("userId", userId).findAll()

        for (item in openTasks){
            var locationDetails = LocationDetails()
            locationDetails.apply {
                userId=item.userId
                location = item.location
                latitude = item.latitude
                longitude = item.longitude
                id = item.id
            }
            locationList.add(locationDetails)
        }

        adapter!!.notifyDataSetChanged()

        mapFragment = supportFragmentManager.findFragmentById(R.id.search_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.close.setOnClickListener {
            binding.map.visibility=View.GONE
            val latLong: LatLng
            latLong = LatLng(latitude, longitude)
            val cameraPosition = CameraPosition.Builder()
                .target(latLong).zoom(0f).tilt(0f).build()
            googleMap.moveCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition)
            )
            googleMap.clear()
        }

        binding.goLocation.setOnClickListener {
            binding.goLocation.visibility= View.GONE
            googleMap.uiSettings.isZoomControlsEnabled = false
            val latLong: LatLng
            latLong = LatLng(latitude, longitude)

            val cameraPosition = CameraPosition.Builder()
                .target(latLong).zoom(16.5f).tilt(0f).build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.moveCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition)
            )

            val dropmarker: MarkerOptions
            dropmarker = MarkerOptions()
            dropmarker.position(latLong)
            dropmarker.icon(
                bitmapDescriptorFromVector(
                    this,
                    R.drawable.baseline_location_on_24
                )
            )
            dropmarker.alpha(.6f)
            //dropmarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dest_dot))
            googleMap.addMarker(dropmarker)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun locationFetch(){


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        /**
         * Location Setting API to
         */
        /**
         * Location Setting API to
         */
        val mSettingsClient = LocationServices.getSettingsClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                mCurrentLocation = result.locations[0]
                if (mCurrentLocation != null) {
                    Log.e("Location(Lat)==", "" + mCurrentLocation!!.getLatitude())
                    Log.e("Location(Long)==", "" + mCurrentLocation!!.getLongitude())
                    latitude = mCurrentLocation!!.getLatitude()
                    longitude =  mCurrentLocation!!.getLongitude()

                    val geocoder: Geocoder
                    val addresses: List<Address>?
                    geocoder = Geocoder(this@MainActivity, Locale.getDefault())

                    addresses = geocoder.getFromLocation(
                        mCurrentLocation!!.getLatitude(),
                        mCurrentLocation!!.getLongitude(),
                        1
                    ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                    //  val address = addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    if(!addresses!!.isEmpty()){
                        val city = addresses!![0].locality
                        val state = addresses!![0].adminArea
                        val country = addresses!![0].countryName
                        val postalCode = addresses!![0].postalCode
                        val knownName = addresses!![0].featureName

                        Log.e("Location","$latitude $longitude $city $state $country $postalCode $knownName")
                        addDataToDatabase( mCurrentLocation!!.getLatitude(),
                            mCurrentLocation!!.getLongitude(),city+","+country)
                    }else{
                        Log.e("Location","$latitude $longitude address is empty")
                    }
                }

                /**
                 * To get location information consistently
                 * mLocationRequest.setNumUpdates(1) Commented out
                 * Uncomment the code below
                 */
                mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            }

            //Locatio nMeaning that all relevant information is available
            override fun onLocationAvailability(availability: LocationAvailability) {
                //boolean isLocation = availability.isLocationAvailable();
            }
        }


        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(5000)
        mLocationRequest!!.setFastestInterval(5000)
        //To get location information only once here
        //To get location information only once here
        mLocationRequest!!.setNumUpdates(3)
        if (provider.equals(LocationManager.GPS_PROVIDER, ignoreCase = true)) {
            //Accuracy is a top priority regardless of battery consumption
            mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        } else {
            //Acquired location information based on balance of battery and accuracy (somewhat higher accuracy)
            mLocationRequest!!.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        /**
         * Stores the type of location service the client wants to use. Also used for positioning.
         */
        /**
         * Stores the type of location service the client wants to use. Also used for positioning.
         */
        val mLocationSettingsRequest = builder.build()
        val locationResponse: Task<LocationSettingsResponse> =
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
        locationResponse.addOnSuccessListener(this, OnSuccessListener {
            Log.e("Response", "Successful acquisition of location information!!")
            //
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                return@OnSuccessListener
            }
            if (mLocationCallback != null) {
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!,
                    mLocationCallback!!,
                    Looper.myLooper()
                )
            }
        })
    }

    private fun addDataToDatabase(
        latitude : Double,longitude: Double, location : String
    ) {

        // on below line we are creating
        // a variable for our modal class.
        val modal = UserLocationModel()

        // on below line we are getting id for the course which we are storing.
        val id: Number? = realm!!.where(UserLocationModel::class.java).max("id")

        // on below line we are
        // creating a variable for our id.
        val nextId: Long

        // validating if id is null or not.
        nextId = if (id == null) {
            // if id is null
            // we are passing it as 1.
            1
        } else {
            // if id is not null then
            // we are incrementing it by 1
            (id.toInt() + 1).toLong()
        }
        // on below line we are setting the
        // data entered by user in our modal class.
        modal.id=nextId
        modal.latitude= latitude
        modal.longitude = longitude
        modal.userId= sharedPreferences.getString("user_id","")
        modal.location = location


        // on below line we are calling a method to execute a transaction.
        realm!!.executeTransaction { realm -> // inside on execute method we are calling a method
            // to insert to real m database from our modal class.
            realm.insert(modal)
        }
        val locationDetails = LocationDetails()
        locationDetails.apply {
            this.id=nextId
            this.latitude= latitude
            this.longitude = longitude
            this.userId= sharedPreferences.getString("user_id","")
            this.location = location
        }
        locationList.add(locationDetails)
        adapter!!.notifyDataSetChanged()
    }

    fun mapDialog(latitude: Double = 9.9246, longitude : Double = 78.1386 ){

        binding.map.visibility=View.VISIBLE
        binding.goLocation.visibility=View.VISIBLE
        this.longitude= longitude
        this.latitude = latitude

       /* try {
            val inflater = layoutInflater
            val dialoglayout: View = inflater.inflate(R.layout.custom_dialog_common, null)
            val builder = AlertDialog.Builder(this, R.style.customDialog)
            val play = dialoglayout.findViewById<ImageView>(R.id.go_location)
            val close = dialoglayout.findViewById<ImageView>(R.id.close)

            builder.setCancelable(false)
            builder.setView(dialoglayout)
            val dialog = builder.create()
            play.setOnClickListener {
                play.visibility= View.GONE
                googleMap.uiSettings.isZoomControlsEnabled = false
                val latLong: LatLng
                latLong = LatLng(latitude, longitude)

                val cameraPosition = CameraPosition.Builder()
                    .target(latLong).zoom(16.5f).tilt(0f).build()

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.moveCamera(
                    CameraUpdateFactory
                        .newCameraPosition(cameraPosition)
                )

                val dropmarker: MarkerOptions
                dropmarker = MarkerOptions()
                dropmarker.position(latLong)
                dropmarker.icon(
                    bitmapDescriptorFromVector(
                        this,
                        R.drawable.baseline_location_on_24
                    )
                )
                dropmarker.alpha(.6f)
                //dropmarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dest_dot))
                googleMap.addMarker(dropmarker)
            }
            close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun isDarkTheme(resources: Resources): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMapToolbarEnabled = false

        if (isDarkTheme(resources)) {
            googleMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style_dark
                )
            )
        } else {
            googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        }

    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                123
            )
            false
        } else {
            true
        }
    }


    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                val editor = sharedPreferences.edit();
                editor.putString("user_image_url","")
                editor.putString("user_name", "")
                editor.putString("user_id", "");
                editor.apply();

                intent = Intent(this,SplashScreen::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Apply activity transition
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                } else {
                    // Swap without transition
                    startActivity(intent)
                }
                finish()
            }
    }

    override fun onBackPressed() {
        if(!closeApp){
            closeApp = true
            Toast.makeText(this,"If you want to close the application,Tab again",Toast.LENGTH_SHORT).show()
        }else{
            finish()
        }

        //super.onBackPressed()
    }

    override fun onItemClickListener(position: Int) {
        mapDialog(locationList[position].latitude!!,locationList[position].longitude!!)
    }
}