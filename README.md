# capacitor-amap

高德定位插件 for Capacitor - 专为国内Android应用优化

[![npm version](https://badge.fury.io/js/capacitor-amap.svg)](https://www.npmjs.com/package/capacitor-amap)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 为什么需要这个插件？

Capacitor 官方的 `@capacitor/geolocation` 插件依赖 Google Play Services，在国内大部分Android设备上：
- ❌ 定位失败或超时
- ❌ 精度极差（误差500米+）
- ❌ 无法使用GPS

**本插件使用高德定位SDK，完美解决国内定位问题！**

## ✨ 特性

- ✅ 高精度定位（GPS + WIFI + 基站混合）
- ✅ 支持单次定位和持续定位
- ✅ 自动逆地理编码（坐标转地址）
- ✅ 完整的权限管理
- ✅ 国内所有主流设备兼容（华为、小米、OPPO、vivo等）
- ✅ 不依赖 Google Play Services

## 📦 安装

```bash
npm install capacitor-amap
# 或
yarn add capacitor-amap

# 同步到Android
npx cap sync android
```

## 🔑 配置

### 1. 申请高德API Key

访问 [高德开放平台控制台](https://console.amap.com/dev/key/app)

创建 **Android平台** Key时，请使用以下配置信息：

```
平台类型: Android
PackageName（包名）: fun.ziji.capacitor.amap
SHA1安全码: 待插件首次运行后获取（见下文）
```

> **重要提示**：每个使用本插件的应用都需要使用**自己的包名**申请独立的Key！
> 上述包名仅用于插件开发测试。

### 2. 获取你的应用的SHA1值

#### Debug版本（开发阶段）

```bash
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey \
    -storepass android -keypass android | grep SHA1
```

#### Release版本（发布阶段）

```bash
keytool -list -v -keystore /path/to/your/release.keystore \
    -alias your_alias | grep SHA1
```

### 3. 配置AndroidManifest.xml

在你的 Capacitor 应用中，编辑 `android/app/src/main/AndroidManifest.xml`：

```xml
<manifest>
    <!-- 添加权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <application>
        <!-- 声明高德定位服务（必需） -->
        <service android:name="com.amap.api.location.APSService" />
    </application>
</manifest>
```

> **注意**：不需要在AndroidManifest中配置API Key，Key通过代码参数传递。

## 🚀 快速开始

### 基础用法

```typescript
import { AmapLocation } from 'capacitor-amap';

async function getLocation() {
  try {
    // 1. 检查权限
    const permStatus = await AmapLocation.checkPermissions();
    
    if (permStatus.location !== 'granted') {
      // 2. 请求权限
      const result = await AmapLocation.requestPermissions();
      if (result.location !== 'granted') {
        throw new Error('用户拒绝定位权限');
      }
    }
    
    // 3. 获取位置
    const position = await AmapLocation.getCurrentPosition({
      key: '你的高德API_Key',  // 必填
      needAddress: true,
      enableHighAccuracy: true,
      timeout: 30000
    });
    
    console.log('纬度:', position.latitude);
    console.log('经度:', position.longitude);
    console.log('精度:', position.accuracy, '米');
    console.log('地址:', position.address);
    
    return position;
    
  } catch (error) {
    console.error('定位失败:', error);
    throw error;
  }
}
```

### 持续定位

```typescript
let watchId: string | null = null;

// 开始监听
async function startWatch() {
  const result = await AmapLocation.watchPosition({
    key: '你的高德API_Key',
    needAddress: true,
    enableHighAccuracy: true,
    interval: 5000  // 5秒更新一次
  });
  
  watchId = result.id;
  
  // 监听位置更新
  await AmapLocation.addListener('locationUpdate', (position) => {
    console.log('位置更新:', position);
  });
  
  // 监听错误
  await AmapLocation.addListener('locationError', (error) => {
    console.error('定位错误:', error);
  });
}

// 停止监听
async function stopWatch() {
  if (watchId) {
    await AmapLocation.clearWatch({ id: watchId });
    await AmapLocation.removeAllListeners();
    watchId = null;
  }
}
```

## 📖 API 文档

### `checkPermissions()`

检查定位权限状态。

**返回**：
```typescript
Promise<{
  location: 'prompt' | 'granted' | 'denied';
  coarseLocation: 'prompt' | 'granted' | 'denied';
}>
```

---

### `requestPermissions()`

请求定位权限。

**返回**：
```typescript
Promise<{
  location: 'granted' | 'denied';
  coarseLocation: 'granted' | 'denied';
}>
```

---

### `getCurrentPosition(options)`

获取当前位置（单次定位）。

**参数**：
```typescript
{
  key: string;                   // 高德API Key（必填）
  needAddress?: boolean;         // 是否返回地址，默认 true
  enableHighAccuracy?: boolean;  // 高精度模式，默认 true  
  timeout?: number;              // 超时时间（毫秒），默认 30000
}
```

**返回**：
```typescript
Promise<{
  latitude: number;      // 纬度
  longitude: number;     // 经度
  accuracy: number;      // 精度（米）
  timestamp: number;     // 时间戳
  altitude?: number;     // 海拔
  speed?: number;        // 速度（米/秒）
  heading?: number;      // 方向角
  address?: string;      // 完整地址
  province?: string;     // 省份
  city?: string;         // 城市
  district?: string;     // 区县
  provider?: string;     // 定位来源
  locationType?: number; // 定位类型
  satellites?: number;   // 卫星数量
}>
```

---

### `watchPosition(options)`

开始监听位置变化（持续定位）。

**参数**：
```typescript
{
  key: string;                   // 高德API Key（必填）
  needAddress?: boolean;
  enableHighAccuracy?: boolean;
  interval?: number;             // 更新间隔（毫秒），默认 2000
}
```

**返回**：
```typescript
Promise<{
  id: string;  // 监听器ID，用于停止监听
}>
```

---

### `clearWatch(options)`

停止监听位置变化。

**参数**：
```typescript
{
  id: string;  // watchPosition 返回的 ID
}
```

**返回**：`Promise<void>`

---

### 事件监听

#### locationUpdate

位置更新事件（watchPosition时触发）。

```typescript
await AmapLocation.addListener('locationUpdate', (position) => {
  console.log('新位置:', position);
});
```

#### locationError

定位错误事件。

```typescript
await AmapLocation.addListener('locationError', (error) => {
  console.error('错误:', error.message, error.code);
});
```

## 💡 配置信息（开发者参考）

### 插件包信息

```
包名: fun.ziji.capacitor.amap
GitHub: https://github.com/junyang7/capacitor-amap
```

### 申请API Key时需要的信息

当你在高德控制台申请API Key时，需要配置：

```
平台: Android
PackageName: [你的应用包名]
SHA1: [你的应用签名SHA1]
```

**获取你的应用SHA1**：

```bash
# Debug签名
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey \
    -storepass android -keypass android
```

**在你的应用中使用插件时的包名**：

查看 `android/app/build.gradle`：
```gradle
android {
    defaultConfig {
        applicationId "com.example.yourapp"  // ← 这是你的包名
    }
}
```

使用**你的包名**和**你的SHA1**在高德控制台申请Key！

## ⚙️ 定位参数说明

### enableHighAccuracy

- `true`：高精度模式（GPS + 网络 + WIFI），精度5-15米，耗时2-5秒
- `false`：低功耗模式（仅网络 + WIFI），精度30-100米，耗时2-3秒

### needAddress

- `true`：返回地址信息（需要网络请求）
- `false`：只返回GPS坐标（更快）

### interval

持续定位的更新间隔，单位：毫秒。
- `1000-2000ms`：实时跟踪
- `5000ms`：平衡（推荐）
- `10000ms+`：省电

## 🌍 适用场景

### ✅ 推荐使用

- 面向国内市场的Capacitor应用
- 需要精确定位
- 需要获取详细地址
- 在华为、小米、OPPO、vivo等设备运行

### ⚠️ 不推荐

- 面向海外市场（建议用 `@capacitor/geolocation`）
- 仅Web应用（本插件仅支持Android）

## 🐛 常见问题

### Q: 定位失败，提示API Key错误

**答**：请检查：
1. 是否传入了正确的 `key` 参数
2. Key是否在高德控制台正确配置
3. 包名和SHA1是否与控制台配置一致

### Q: 没有地址信息

**答**：确保：
1. 设置了 `needAddress: true`
2. API Key配置正确
3. 设备网络连接正常

### Q: 精度不高

**答**：尝试：
- 设置 `enableHighAccuracy: true`
- 在户外测试（GPS信号更好）
- 确保WIFI已开启

## 📊 性能指标

| 指标 | 值 |
|------|---|
| 首次定位时间 | 2-5秒 |
| 定位精度（高精度模式） | 5-15米 |
| 定位精度（低功耗模式） | 30-100米 |
| 内存占用 | ~30MB |

## 🔗 相关链接

- [高德开放平台](https://lbs.amap.com/)
- [高德定位SDK文档](https://lbs.amap.com/api/android-location-sdk/summary)
- [错误码对照表](https://lbs.amap.com/api/android-location-sdk/guide/utilities/errorcode/)
- [Capacitor官方文档](https://capacitorjs.com/)

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 支持

- GitHub Issues: https://github.com/junyang7/capacitor-amap/issues
- 高德开放平台: https://lbs.amap.com/

---

**Built with ❤️ for Capacitor developers in China**

