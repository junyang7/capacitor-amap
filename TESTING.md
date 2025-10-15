# 📱 插件测试指南

## 🎯 测试目标

在正式发布前，需要验证插件的所有功能正常工作。

---

## 📋 测试前准备

### 1. 获取SHA1值

**插件使用的包名**：`fun.ziji.capacitor.amap`

```bash
# 进入项目目录
cd /Users/junyang7/AndroidStudioProjects/map/capacitor-amap

# 获取Debug SHA1（首次测试）
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey \
    -storepass android -keypass android | grep SHA1
```

**复制SHA1值**，例如：
```
SHA1: D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C
```

### 2. 在高德控制台申请测试Key

访问：https://console.amap.com/dev/key/app

创建 **Android平台** Key：
```
平台: Android
PackageName: fun.ziji.capacitor.amap
SHA1: [上一步获取的SHA1值]
```

**保存Key**，例如：`abc123...`

---

## 🧪 创建测试应用

### 方式1：使用现有的LocationDemo测试

回到原测试项目：
```bash
cd /Users/junyang7/AndroidStudioProjects/map/map
```

修改包名为 `fun.ziji.capacitor.amap`：

1. 修改 `app/build.gradle.kts`:
```kotlin
android {
    namespace = "fun.ziji.capacitor.amap"
    defaultConfig {
        applicationId = "fun.ziji.capacitor.amap"
    }
}
```

2. 使用新插件测试

### 方式2：创建新的Ionic测试应用

```bash
# 创建测试应用
ionic start test-amap blank --type=vue --capacitor
cd test-amap

# 安装插件
yarn add file:/Users/junyang7/AndroidStudioProjects/map/capacitor-amap

# 同步
npx cap sync android
```

---

## 📝 测试代码

在测试应用中创建测试页面：

```vue
<template>
  <div>
    <h1>高德定位测试</h1>
    
    <button @click="testLocation">测试定位</button>
    
    <div v-if="result">
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { AmapLocation } from 'capacitor-amap';

const result = ref('');

async function testLocation() {
  try {
    // 使用你申请的测试Key
    const TEST_KEY = 'abc123...';  // 替换为你的Key
    
    // 1. 检查权限
    const permStatus = await AmapLocation.checkPermissions();
    result.value += `权限状态: ${JSON.stringify(permStatus)}\n`;
    
    if (permStatus.location !== 'granted') {
      // 2. 请求权限
      const permResult = await AmapLocation.requestPermissions();
      result.value += `权限请求: ${JSON.stringify(permResult)}\n`;
    }
    
    // 3. 获取位置
    const position = await AmapLocation.getCurrentPosition({
      key: TEST_KEY,
      needAddress: true,
      enableHighAccuracy: true
    });
    
    result.value += `\n定位成功:\n${JSON.stringify(position, null, 2)}`;
    
  } catch (error) {
    result.value += `\n错误: ${error.message}`;
  }
}
</script>
```

---

## ✅ 测试清单

### 功能测试

- [ ] **checkPermissions()** - 检查权限状态
- [ ] **requestPermissions()** - 请求权限
- [ ] **getCurrentPosition()** - 单次定位
  - [ ] 返回坐标
  - [ ] 返回地址
  - [ ] 精度正常（< 50米）
- [ ] **watchPosition()** - 持续定位
  - [ ] 能够启动
  - [ ] 持续更新
  - [ ] 事件正常触发
- [ ] **clearWatch()** - 停止监听
  - [ ] 能够停止
  - [ ] 不再更新

### 错误处理测试

- [ ] API Key为空 - 应该报错
- [ ] API Key错误 - 应该报错
- [ ] 无权限时调用 - 应该报错
- [ ] 定位超时 - 应该报错

### 兼容性测试

- [ ] 华为手机
- [ ] 小米手机
- [ ] OPPO手机
- [ ] vivo手机

---

## 📋 预期结果

### 成功标志

**Logcat日志**：
```
AmapLocationPlugin: checkPermissions called
AmapLocationPlugin: getCurrentPosition called
AmapLocationPlugin: API Key set
AmapLocationPlugin: Location client started
AmapLocationPlugin: onLocationChanged, errorCode: 0
AmapLocationPlugin: Position built - lat: xx.xx, lon: xx.xx
```

**返回数据**：
```json
{
  "latitude": 40.041603,
  "longitude": 116.276618,
  "accuracy": 30.0,
  "address": "北京市海淀区...",
  "timestamp": 1697385600000
}
```

---

## 🔍 问题排查

### 定位失败

1. **检查日志**：查看 `errorCode` 和 `errorInfo`
2. **检查Key**：确保传入了正确的Key
3. **检查配置**：包名和SHA1是否匹配
4. **检查权限**：是否授予了定位权限
5. **检查网络**：是否有网络连接

### 常见错误码

- `7`：API Key配置错误（包名或SHA1不匹配）
- `12`：缺少定位权限
- `13`：GPS不可用

---

## ✅ 测试通过标准

所有以下测试通过：

1. ✅ 可以成功请求权限
2. ✅ 可以获取GPS坐标
3. ✅ 可以获取地址信息
4. ✅ 精度在合理范围（< 50米）
5. ✅ 持续定位正常更新
6. ✅ 可以正常停止监听
7. ✅ 无内存泄漏
8. ✅ 无崩溃

---

## 📤 测试完成后

测试通过后，就可以：

1. **推送到GitHub**
   ```bash
   export https_proxy=http://127.0.0.1:7897
   export http_proxy=http://127.0.0.1:7897
   export all_proxy=socks5://127.0.0.1:7897
   
   git push origin master
   ```

2. **在其他项目中使用**
   ```bash
   yarn add capacitor-amap
   npx cap sync android
   ```

3. **发布到npm**（可选）
   ```bash
   npm publish
   ```

---

**祝测试顺利！** 🚀

