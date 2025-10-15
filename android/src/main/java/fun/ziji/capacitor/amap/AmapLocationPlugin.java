package fun.ziji.capacitor.amap;

import android.Manifest;
import android.util.Log;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import java.util.HashMap;
import java.util.Map;

/**
 * 高德定位 Capacitor 插件
 * 提供定位服务的JavaScript接口
 */
@CapacitorPlugin(
    name = "AmapLocation",
    permissions = {
        @Permission(
            strings = { Manifest.permission.ACCESS_FINE_LOCATION },
            alias = "location"
        ),
        @Permission(
            strings = { Manifest.permission.ACCESS_COARSE_LOCATION },
            alias = "coarseLocation"
        )
    }
)
public class AmapLocationPlugin extends Plugin {

    private static final String TAG = "AmapLocationPlugin";
    private Map<String, AMapLocationClient> watchMap = new HashMap<>();

    @Override
    public void load() {
        super.load();
        Log.d(TAG, "AmapLocation plugin loaded");
        
        // 设置隐私合规
        AMapLocationClient.updatePrivacyShow(getContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(getContext(), true);
    }

    /**
     * 检查定位权限状态
     */
    @PluginMethod
    public void checkPermissions(PluginCall call) {
        Log.d(TAG, "checkPermissions called");
        JSObject permissionsResult = new JSObject();
        
        PermissionState locationState = getPermissionState("location");
        PermissionState coarseLocationState = getPermissionState("coarseLocation");
        
        permissionsResult.put("location", locationState.toString());
        permissionsResult.put("coarseLocation", coarseLocationState.toString());
        
        call.resolve(permissionsResult);
    }

    /**
     * 请求定位权限
     */
    @PluginMethod
    public void requestPermissions(PluginCall call) {
        Log.d(TAG, "requestPermissions called");
        requestPermissionForAliases(
            new String[]{"location", "coarseLocation"},
            call,
            "permissionsCallback"
        );
    }

    @PermissionCallback
    private void permissionsCallback(PluginCall call) {
        Log.d(TAG, "permissionsCallback called");
        JSObject permissionsResult = new JSObject();
        
        PermissionState locationState = getPermissionState("location");
        PermissionState coarseLocationState = getPermissionState("coarseLocation");
        
        permissionsResult.put("location", locationState.toString());
        permissionsResult.put("coarseLocation", coarseLocationState.toString());
        
        call.resolve(permissionsResult);
    }

    /**
     * 获取当前位置（单次定位）
     */
    @PluginMethod
    public void getCurrentPosition(PluginCall call) {
        Log.d(TAG, "getCurrentPosition called");
        
        if (!hasLocationPermissions()) {
            call.reject("Location permissions not granted");
            return;
        }

        try {
            // 获取API Key（必填）
            String apiKey = call.getString("key");
            if (apiKey == null || apiKey.isEmpty()) {
                call.reject("API Key is required. Please provide 'key' parameter.");
                return;
            }

            // 获取选项
            boolean needAddress = call.getBoolean("needAddress", true);
            boolean enableHighAccuracy = call.getBoolean("enableHighAccuracy", true);
            int timeout = call.getInt("timeout", 30000);
            
            Log.d(TAG, "Options - needAddress: " + needAddress + ", highAccuracy: " + enableHighAccuracy);

            // 创建定位客户端
            AMapLocationClient client = new AMapLocationClient(getContext());
            
            // 设置API Key
            AMapLocationClient.setApiKey(apiKey);
            Log.d(TAG, "API Key set");

            // 配置定位参数
            AMapLocationClientOption option = new AMapLocationClientOption();
            
            if (enableHighAccuracy) {
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            } else {
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            }
            
            option.setNeedAddress(needAddress);
            option.setOnceLocation(true);
            option.setOnceLocationLatest(true);
            option.setHttpTimeOut(timeout);
            option.setLocationCacheEnable(false);
            option.setGpsFirst(false);
            option.setWifiScan(true);
            AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);

            // 设置定位监听
            client.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation location) {
                    Log.d(TAG, "onLocationChanged called, errorCode: " + location.getErrorCode());
                    
                    if (location.getErrorCode() == 0) {
                        // 定位成功
                        JSObject result = buildPositionResult(location);
                        call.resolve(result);
                    } else {
                        // 定位失败
                        String errorMsg = "Location error: code=" + location.getErrorCode() + 
                                        ", msg=" + location.getErrorInfo();
                        Log.e(TAG, errorMsg);
                        call.reject(errorMsg, String.valueOf(location.getErrorCode()));
                    }
                    
                    // 销毁客户端
                    client.onDestroy();
                }
            });

            // 设置定位参数并启动
            client.setLocationOption(option);
            client.startLocation();
            Log.d(TAG, "Location client started");

        } catch (Exception e) {
            Log.e(TAG, "getCurrentPosition error: " + e.getMessage(), e);
            call.reject("Failed to start location: " + e.getMessage());
        }
    }

    /**
     * 开始监听位置变化（持续定位）
     */
    @PluginMethod
    public void watchPosition(PluginCall call) {
        Log.d(TAG, "watchPosition called");
        
        if (!hasLocationPermissions()) {
            call.reject("Location permissions not granted");
            return;
        }

        try {
            // 获取API Key（必填）
            String apiKey = call.getString("key");
            if (apiKey == null || apiKey.isEmpty()) {
                call.reject("API Key is required. Please provide 'key' parameter.");
                return;
            }

            // 获取选项
            boolean needAddress = call.getBoolean("needAddress", true);
            boolean enableHighAccuracy = call.getBoolean("enableHighAccuracy", true);
            int interval = call.getInt("interval", 2000);
            
            Log.d(TAG, "Watch options - interval: " + interval + "ms");

            // 生成唯一ID
            String watchId = "watch_" + System.currentTimeMillis();

            // 创建定位客户端
            AMapLocationClient client = new AMapLocationClient(getContext());
            
            // 设置API Key
            AMapLocationClient.setApiKey(apiKey);
            Log.d(TAG, "API Key set for watch");

            // 配置定位参数（持续定位）
            AMapLocationClientOption option = new AMapLocationClientOption();
            
            if (enableHighAccuracy) {
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            } else {
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            }
            
            option.setNeedAddress(needAddress);
            option.setOnceLocation(false);  // 持续定位
            option.setInterval(interval);
            option.setLocationCacheEnable(false);
            option.setGpsFirst(false);
            option.setWifiScan(true);
            AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);

            // 设置定位监听
            client.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation location) {
                    Log.d(TAG, "Watch - onLocationChanged, errorCode: " + location.getErrorCode());
                    
                    if (location.getErrorCode() == 0) {
                        // 定位成功，通过事件发送
                        JSObject result = buildPositionResult(location);
                        notifyListeners("locationUpdate", result);
                    } else {
                        // 定位失败，发送错误事件
                        JSObject error = new JSObject();
                        error.put("code", location.getErrorCode());
                        error.put("message", location.getErrorInfo());
                        error.put("details", location.getLocationDetail());
                        notifyListeners("locationError", error);
                    }
                }
            });

            // 保存客户端
            watchMap.put(watchId, client);

            // 设置定位参数并启动
            client.setLocationOption(option);
            client.startLocation();
            
            Log.d(TAG, "Watch started with id: " + watchId);

            // 返回watchId
            JSObject result = new JSObject();
            result.put("id", watchId);
            call.resolve(result);

        } catch (Exception e) {
            Log.e(TAG, "watchPosition error: " + e.getMessage(), e);
            call.reject("Failed to start watch: " + e.getMessage());
        }
    }

    /**
     * 停止监听位置变化
     */
    @PluginMethod
    public void clearWatch(PluginCall call) {
        Log.d(TAG, "clearWatch called");
        
        String watchId = call.getString("id");
        if (watchId == null) {
            call.reject("Watch ID is required");
            return;
        }

        AMapLocationClient client = watchMap.get(watchId);
        if (client != null) {
            client.stopLocation();
            client.onDestroy();
            watchMap.remove(watchId);
            Log.d(TAG, "Watch cleared: " + watchId);
            call.resolve();
        } else {
            call.reject("Watch ID not found: " + watchId);
        }
    }

    @Override
    protected void handleOnDestroy() {
        Log.d(TAG, "Plugin destroying, cleaning up all location clients");
        
        // 清理所有watch
        for (Map.Entry<String, AMapLocationClient> entry : watchMap.entrySet()) {
            AMapLocationClient client = entry.getValue();
            if (client != null) {
                client.stopLocation();
                client.onDestroy();
            }
        }
        watchMap.clear();
        
        super.handleOnDestroy();
    }

    /**
     * 检查是否有定位权限
     */
    private boolean hasLocationPermissions() {
        return getPermissionState("location") == PermissionState.GRANTED ||
               getPermissionState("coarseLocation") == PermissionState.GRANTED;
    }

    /**
     * 构建位置结果对象
     */
    private JSObject buildPositionResult(AMapLocation location) {
        JSObject result = new JSObject();
        
        // 基础坐标信息
        result.put("latitude", location.getLatitude());
        result.put("longitude", location.getLongitude());
        result.put("accuracy", location.getAccuracy());
        result.put("timestamp", location.getTime());
        
        // 可选信息
        if (location.getAltitude() != 0) {
            result.put("altitude", location.getAltitude());
        }
        if (location.getSpeed() != 0) {
            result.put("speed", location.getSpeed());
        }
        if (location.getBearing() != 0) {
            result.put("heading", location.getBearing());
        }
        
        // 地址信息
        if (location.getAddress() != null && !location.getAddress().isEmpty()) {
            result.put("address", location.getAddress());
        }
        if (location.getProvince() != null && !location.getProvince().isEmpty()) {
            result.put("province", location.getProvince());
        }
        if (location.getCity() != null && !location.getCity().isEmpty()) {
            result.put("city", location.getCity());
        }
        if (location.getDistrict() != null && !location.getDistrict().isEmpty()) {
            result.put("district", location.getDistrict());
        }
        
        // 定位元数据
        result.put("provider", location.getProvider());
        result.put("locationType", location.getLocationType());
        if (location.getSatellites() > 0) {
            result.put("satellites", location.getSatellites());
        }
        
        Log.d(TAG, "Position built - lat: " + location.getLatitude() + 
                   ", lon: " + location.getLongitude() + 
                   ", accuracy: " + location.getAccuracy());
        
        return result;
    }
}

