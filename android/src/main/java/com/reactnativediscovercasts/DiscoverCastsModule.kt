package com.reactnativediscovercasts

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
import java.lang.Error
import java.lang.reflect.Method

public class DiscoverCastsModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext), LifecycleEventListener  {

  override fun getName(): String {
    return "DiscoverCasts"
  }

  private var mRouter: MediaRouter? = null
  private var mCallback: MediaRouter.Callback? = null
  private var mSelector: MediaRouteSelector? = null
  private var bReceiver: BroadcastReceiver? = null

  private var reactContext: ReactApplicationContext = reactContext

  init {
    this.reactContext.addLifecycleEventListener(this)
  }

  private class RouteCallback(module: DiscoverCastsModule): MediaRouter.Callback()  {
    private var mModule: DiscoverCastsModule

    init {
      mModule = module
    }

    private fun sendEvent(eventName: String, params: Any?) {
      mModule.reactContext
        .getJSModule(RCTDeviceEventEmitter::class.java)
        .emit(eventName, params)
    }

    override fun onRouteAdded(router: MediaRouter, route: RouteInfo?) {
      super.onRouteAdded(router, route)
      mModule.reactContext.runOnUiQueueThread {
        if(route != null)
          sendEvent("onRouteAdded", mModule.getRouteInfos(route))
      }
    }

    override fun onRouteChanged(router: MediaRouter?, route: RouteInfo?) {
      super.onRouteChanged(router, route)
      mModule.reactContext.runOnUiQueueThread {
        if(route != null)
          sendEvent("onRouteChanged", mModule.getRouteInfos(route))
      }
    }

    override fun onRouteRemoved(router: MediaRouter?, route: RouteInfo?) {
      super.onRouteRemoved(router, route)
      mModule.reactContext.runOnUiQueueThread {
        if(route != null)
          sendEvent("onRouteRemoved", mModule.getRouteInfos(route))
      }
    }

    /*override fun onRoutePresentationDisplayChanged(router: MediaRouter?, route: RouteInfo?) {
      super.onRoutePresentationDisplayChanged(router, route)
      mModule.reactContext.runOnUiQueueThread {
        if(route != null)
          sendEvent("onRouteAdded", mModule.getRouteInfos(route))
      }
    }*/

    override fun onProviderAdded(router: MediaRouter?, provider: MediaRouter.ProviderInfo?) {
      super.onProviderAdded(router, provider)
      mModule.reactContext.runOnUiQueueThread {
        /*var a = router?.bluetoothRoute

        var b = false

        if(b)
          sendEvent("onRouteAdded", null)
        val deviceMap = mModule.mapRouteDevice(router.)
        if (deviceMap != null)
          sendEvent("onRouteAdded", deviceMap)*/
      }
    }

    override fun onProviderChanged(router: MediaRouter?, provider: MediaRouter.ProviderInfo?) {
      super.onProviderChanged(router, provider)
      mModule.reactContext.runOnUiQueueThread {
        /*var a = router?.bluetoothRoute

        var b = false

        if(b)
          sendEvent("onRouteAdded", null)
        val deviceMap = mModule.mapRouteDevice(router.)
        if (deviceMap != null)
          sendEvent("onRouteAdded", deviceMap)*/
      }
    }

    override fun onProviderRemoved(router: MediaRouter?, provider: MediaRouter.ProviderInfo?) {
      super.onProviderRemoved(router, provider)
      mModule.reactContext.runOnUiQueueThread {
        /*var a = router?.bluetoothRoute

        var b = false

        if(b)
          sendEvent("onRouteAdded", null)*/
      }
    }
  }

  @ReactMethod
  fun startScan() {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val intentFilter = IntentFilter()
    intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
    intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
    intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED)
    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)
    intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    bReceiver = object : BroadcastReceiver() {
      @RequiresApi(Build.VERSION_CODES.M)
      override fun onReceive(context: Context, intent: Intent) {
        val payloadDevices = Arguments.createArray()
        val pairedDevices: Set<BluetoothDevice>? = adapter?.bondedDevices
        pairedDevices?.forEach { device ->
          payloadDevices.pushMap(getBluetoothInfos(device))
        }
        val payload = Arguments.createMap()
        payload.putArray("devices", payloadDevices)
        reactApplicationContext
          .getJSModule(RCTDeviceEventEmitter::class.java)
          .emit("onConnectedDevices", payload)
      }
    }
    reactApplicationContext.registerReceiver(bReceiver, intentFilter)
    reactApplicationContext.addLifecycleEventListener(this)
    adapter.startDiscovery()
  }

  @ReactMethod
  fun getAvailableDevices(promise: Promise) {
    reactContext.runOnUiQueueThread {
      val payload = Arguments.createArray()
      val routes: List<RouteInfo> = mRouter!!.getRoutes()

      for (routeInfo in routes) {
        payload.pushMap(getRouteInfos(routeInfo))
      }

      promise.resolve(payload)
    }
  }

  @ReactMethod
  fun connectToDevice(id: String, promise: Promise) {
    reactContext.runOnUiQueueThread {
      val route: RouteInfo? = mRouter!!.getRoutes().reduce { final, route ->
        if (route.id == id)
          route
        else final
      }

      if (route == null) {
        promise.reject(Error("Could not connect to device"))
        return@runOnUiQueueThread
      }

      mRouter!!.selectRoute(route)
      promise.resolve(true)
    }
  }

  @ReactMethod
  fun disconnectFromDevice(id: String, promise: Promise) {
    reactContext.runOnUiQueueThread {
      val route: RouteInfo? = mRouter!!.getRoutes().reduce { final, route ->
        if (route.id == id)
          route
        else final
      }

      if (route == null) {
        promise.reject(Error("Could not disconnect from device"))
        return@runOnUiQueueThread
      }

      mRouter!!.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)
      promise.resolve(true)
    }
  }

  @ReactMethod
  fun getAdditionalDeviceInfos(id: String, promise: Promise) {
    reactContext.runOnUiQueueThread {
      val route: RouteInfo? = mRouter!!.getRoutes().reduce { final, route ->
        if (route.id == id)
          route
        else final
      }

      if (route == null) {
        promise.reject(Error("Could not connect to device"))
        return@runOnUiQueueThread
      }

      promise.resolve(getRouteInfos(route))
    }
  }

  override fun onHostResume() {
    reactContext.runOnUiQueueThread {
      mRouter = MediaRouter.getInstance(reactApplicationContext)
      mCallback = RouteCallback(this)
      mSelector = MediaRouteSelector.Builder().addControlCategory(
        CastMediaControlIntent.categoryForCast(DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build()
      mRouter!!.addCallback(mSelector!!, mCallback!!,  /* flags= */0)
    }
  }

  override fun onHostDestroy() {
  }

  override fun onHostPause() {
  }

  fun getRouteInfos(route: RouteInfo): WritableMap? {
    val payload = Arguments.createMap()

    payload.putString("id", route.id)
    payload.putInt("deviceType", route.deviceType)
    payload.putBoolean("isConnected", route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTED)

    val castDevice = CastDevice.getFromBundle(route.extras)
    if(castDevice != null) {
      payload.putString("name", castDevice.friendlyName)
      payload.putString("deviceId", castDevice.deviceId)
      payload.putString("deviceVersion", castDevice.deviceVersion)
      payload.putBoolean("isOnLocalNetwork", castDevice.isOnLocalNetwork)
      payload.putString("modelName", castDevice.modelName)
      payload.putString("ipAddress", castDevice.inetAddress.toString())
    }
    else {
      payload.putString("name", route.name)
    }

    return payload
  }

  fun getBluetoothInfos(device: BluetoothDevice): WritableMap? {
    val payload = Arguments.createMap()

    var isConnected = try {
      val m: Method = device.javaClass.getMethod("isConnected")
      m.invoke(device) as Boolean
    } catch (e: java.lang.Exception) {
      false
      //throw IllegalStateException(e)
    }

    payload.putString("id", device.address)
    payload.putInt("deviceType", device.bluetoothClass.deviceClass)
    payload.putBoolean("isConnected", isConnected)
    payload.putString("name", device.name)

    return payload
  }
}
