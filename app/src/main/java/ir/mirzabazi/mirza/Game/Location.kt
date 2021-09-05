package ir.mirzabazi.mirza.Game

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat

class Location : LocationListener{
    private val listener : LocationListener

    private val handler : Handler
    private val context : Context
    //default values
    private val location_min_time = 300L //mil second

    private val location_min_distance = 1f
    private var curent_provider : String? = null

    constructor(context : Context, listener: LocationListener){
        this.listener = listener
        handler = Handler()
        this.context = context
    }

    fun StartGet(per_listener : GameBoard.Companion.IPermissionCheckListener){
        /*
        * this method check permission and set a listener for location
        * */
        var manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //select best locationProvider
        var providers = listOf(LocationManager.GPS_PROVIDER , LocationManager.NETWORK_PROVIDER)
        var EnableProvider = ""
        providers.forEach {
            if (manager.isProviderEnabled(it)){
                EnableProvider = it
                return@forEach
            }
        }

        if (EnableProvider.isEmpty()){
            listener.providersNotEnable()
            return
        }
        curent_provider = EnableProvider

        //check permission
        var per_grant = ActivityCompat.checkSelfPermission(context , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (per_grant){
            manager.requestLocationUpdates(EnableProvider , location_min_time , location_min_distance , this)
            per_listener.PermissionGranted()
        }else{
            per_listener.PermissionDenied()
            return
        }

    }

    fun StopGet(){
        /*
        * remove the location update
        * */
        var manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        manager.removeUpdates(this)
        curent_provider = null
    }

    //location listener

    override fun onLocationChanged(location: Location?) {
        /*
        * this method call the listener on handler thread
        * */
        if (location == null) return
        var lat = location.latitude
        var lon = location.longitude

        handler.post{
            listener.LocationChange(lat , lon)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {
        if (provider != null && provider.equals(curent_provider)) {
            handler.post {
                listener.providerEnable()
            }
        }
    }

    override fun onProviderDisabled(provider: String?) {
        if (provider != null && provider.equals(curent_provider)) {
            handler.post {
                listener.providerDisabled()
            }
        }
    }

    companion object {
        interface LocationListener{
            fun LocationChange(lat : Double , lon : Double)
            fun providersNotEnable()
            fun providerDisabled()
            fun providerEnable()
        }
    }
}