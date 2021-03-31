package com.reactnativediscovercasts

import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.google.android.gms.cast.Cast
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
import java.lang.Error


public class DiscoverCastsModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext), LifecycleEventListener  {

  override fun getName(): String {
    return "DiscoverCasts"
  }

  private var mRouter: MediaRouter? = null
  private var mCallback: MediaRouter.Callback? = null
  private var mSelector: MediaRouteSelector? = null

  private var reactContext: ReactApplicationContext

  init {
    this.reactContext = reactContext
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
        val deviceMap = mModule.mapRouteDevice(route)
        if (deviceMap != null)
          sendEvent("onRouteAdded", deviceMap)
      }
    }

    override fun onRouteChanged(router: MediaRouter?, route: RouteInfo?) {
      super.onRouteChanged(router, route)
      mModule.reactContext.runOnUiQueueThread {
        val deviceMap = mModule.mapRouteDevice(route)
        if (deviceMap != null)
          sendEvent("onRouteChanged", deviceMap)
      }
    }

    override fun onRouteRemoved(router: MediaRouter?, route: RouteInfo?) {
      super.onRouteRemoved(router, route)
      mModule.reactContext.runOnUiQueueThread {
        val deviceMap = mModule.mapRouteDevice(route)
        if (deviceMap != null)
          sendEvent("onRouteRemoved", deviceMap)
      }
    }
  }

  fun mapRouteDevice(routeInfo: RouteInfo?): WritableMap? {
    if(routeInfo == null)
      return null
    val device = CastDevice.getFromBundle(routeInfo.extras) ?: return null
    val deviceMap = Arguments.createMap()
    deviceMap.putString("friendlyName", device.friendlyName)
    deviceMap.putString("deviceId", device.deviceId)
    deviceMap.putString("deviceVersion", device.deviceVersion)
    deviceMap.putBoolean("isOnLocalNetwork", device.isOnLocalNetwork)
    deviceMap.putString("modelName", device.modelName)
    deviceMap.putString("ipAddress", device.inetAddress.toString())
    return deviceMap
  }

  @ReactMethod
  fun getAvailableDevices(promise: Promise) {
    reactContext.runOnUiQueueThread{
      val payload = Arguments.createArray()
      val routes: List<RouteInfo> = mRouter!!.getRoutes()

      for (routeInfo in routes) {
        val deviceMap = mapRouteDevice(routeInfo)
        if (deviceMap != null)
          payload.pushMap(deviceMap)
      }

      promise.resolve(payload)
    }
  }

  @ReactMethod
  fun connectToDevice(deviceId: String, promise: Promise) {
    reactContext.runOnUiQueueThread {
      val route: RouteInfo? = mRouter!!.getRoutes().reduce { final, route ->
        val device = CastDevice.getFromBundle(route.extras)
        if (device.deviceId == deviceId)
          route
        else final
      }

      if (route == null)
        promise.reject(Error("Could not connect to device"))

      mRouter!!.selectRoute(route!!)
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

  override fun onHostPause() {}

}

