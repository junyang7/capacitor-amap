import type { PluginListenerHandle } from '@capacitor/core';

/**
 * 高德定位插件接口
 */
export interface AmapLocationPlugin {
  /**
   * 检查定位权限状态
   */
  checkPermissions(): Promise<PermissionStatus>;

  /**
   * 请求定位权限
   */
  requestPermissions(): Promise<PermissionStatus>;

  /**
   * 获取当前位置（单次定位）
   * @param options 定位选项（必须包含key）
   */
  getCurrentPosition(options: LocationOptions): Promise<Position>;

  /**
   * 开始监听位置变化（持续定位）
   * @param options 定位选项（必须包含key）
   */
  watchPosition(options: LocationOptions): Promise<CallbackID>;

  /**
   * 停止监听位置变化
   * @param options 包含watchId的对象
   */
  clearWatch(options: { id: string }): Promise<void>;

  /**
   * 监听位置更新事件
   */
  addListener(
    eventName: 'locationUpdate',
    listenerFunc: (position: Position) => void
  ): Promise<PluginListenerHandle>;

  /**
   * 监听定位错误事件
   */
  addListener(
    eventName: 'locationError',
    listenerFunc: (error: LocationError) => void
  ): Promise<PluginListenerHandle>;

  /**
   * 移除所有监听器
   */
  removeAllListeners(): Promise<void>;
}

/**
 * 定位选项
 */
export interface LocationOptions {
  /**
   * 高德 API Key（必填）
   * 请在高德开放平台申请：https://console.amap.com/dev/key/app
   */
  key: string;

  /**
   * 是否需要返回地址信息（逆地理编码）
   * 默认：true
   */
  needAddress?: boolean;

  /**
   * 是否高精度定位
   * 默认：true
   */
  enableHighAccuracy?: boolean;

  /**
   * 超时时间（毫秒）
   * 默认：30000
   */
  timeout?: number;

  /**
   * 定位间隔（毫秒，仅持续定位有效）
   * 默认：2000
   */
  interval?: number;
}

/**
 * 位置信息
 */
export interface Position {
  /**
   * 纬度
   */
  latitude: number;

  /**
   * 经度
   */
  longitude: number;

  /**
   * 精度（米）
   */
  accuracy: number;

  /**
   * 时间戳
   */
  timestamp: number;

  /**
   * 海拔高度（米，可选）
   */
  altitude?: number;

  /**
   * 速度（米/秒，可选）
   */
  speed?: number;

  /**
   * 方向角（可选）
   */
  heading?: number;

  /**
   * 完整地址（可选）
   */
  address?: string;

  /**
   * 省份（可选）
   */
  province?: string;

  /**
   * 城市（可选）
   */
  city?: string;

  /**
   * 区县（可选）
   */
  district?: string;

  /**
   * 定位来源（gps/network/lbs）
   */
  provider?: string;

  /**
   * 定位类型
   * 1=GPS, 5=WIFI, 6=基站, 12=混合
   */
  locationType?: number;

  /**
   * 卫星数量（可选）
   */
  satellites?: number;
}

/**
 * 权限状态
 */
export interface PermissionStatus {
  /**
   * 精确定位权限状态
   */
  location: PermissionState;

  /**
   * 粗略定位权限状态
   */
  coarseLocation: PermissionState;
}

/**
 * 权限状态枚举
 */
export type PermissionState = 'prompt' | 'prompt-with-rationale' | 'granted' | 'denied';

/**
 * 回调ID
 */
export interface CallbackID {
  /**
   * 监听器ID，用于清除监听
   */
  id: string;
}

/**
 * 定位错误
 */
export interface LocationError {
  /**
   * 错误码
   */
  code: number;

  /**
   * 错误信息
   */
  message: string;

  /**
   * 错误详情
   */
  details?: string;
}

