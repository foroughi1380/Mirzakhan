package ir.mirzabazi.mirza.Game.Permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import ir.mirzabazi.mirza.Game.GameBoard

var allPermission = arrayOf<String>(
    Manifest.permission.INTERNET ,
    Manifest.permission.READ_EXTERNAL_STORAGE ,
    Manifest.permission.WRITE_EXTERNAL_STORAGE ,
    Manifest.permission.CAMERA ,
    Manifest.permission.ACCESS_COARSE_LOCATION ,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.VIBRATE)
fun CheckAllPermission(context: Context , per_lis: GameBoard.Companion.IPermissionCheckListener){
    var ok = true
    allPermission.forEach {
        ok = ok && (ActivityCompat.checkSelfPermission(context , it) == PackageManager.PERMISSION_GRANTED)
    }

    if (ok){
        per_lis.PermissionGranted()
    }else{
        per_lis.PermissionDenied()
    }
}