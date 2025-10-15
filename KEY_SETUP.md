# 高德API Key配置指南

⚠️ **说明**：本文档适用于**使用本插件的APP开发者**，不是插件开发者本人。

插件开发者只需要用Debug Keystore测试插件功能即可。

---

## 📦 包名

使用插件的APP需要配置自己的包名，例如：
```
com.yourcompany.yourapp  ← 您的应用包名
```

插件测试环境（仅供参考）：
```
fun.ziji.location  ← 插件测试项目包名
```

## 🔑 SHA1值管理

### Debug版本（开发测试）
```bash
# 查看Debug SHA1
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey \
    -storepass android -keypass android | grep SHA1

# 当前SHA1
SHA1: D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C
```

### Release版本（正式发布）

#### 1. 创建Release Keystore（首次）
```bash
keytool -genkey -v -keystore capacitor-amap-release.keystore \
    -alias capacitor_amap \
    -keyalg RSA -keysize 2048 -validity 10000

# ⚠️ 重要：记住密码！并妥善保管此文件！
```

#### 2. 查看Release SHA1
```bash
keytool -list -v -keystore /path/to/capacitor-amap-release.keystore \
    -alias capacitor_amap | grep SHA1
```

#### 3. 在build.gradle中配置
```gradle
android {
    signingConfigs {
        release {
            storeFile file("../capacitor-amap-release.keystore")
            storePassword "your_store_password"
            keyAlias "capacitor_amap"
            keyPassword "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 🌐 高德控制台配置

访问：https://console.amap.com/dev/key/app

### 方案1：分别配置（推荐）
```
Key 1 (开发测试):
  包名: fun.ziji.capacitor.amap
  SHA1: D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C
  
Key 2 (正式发布):
  包名: fun.ziji.capacitor.amap
  SHA1: [Release Keystore的SHA1]
```

### 方案2：同一Key多SHA1（更灵活）
高德支持一个Key配置多个SHA1，用分号分隔：
```
SHA1: D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C;XX:XX:XX:...
```

## ⚠️ 注意事项

### SHA1会变化的情况
1. ❌ Debug → Release切换（最常见）
2. ❌ 不同开发者电脑
3. ❌ 重装系统/Android Studio
4. ❌ 更换签名密钥
5. ❌ CI/CD环境

### SHA1不会变化
1. ✅ 修改代码
2. ✅ 重新编译
3. ✅ 升级工具版本
4. ✅ 卸载重装APP
5. ✅ 使用相同的Keystore文件

## 🔒 Keystore安全备份

```bash
# 1. 备份到安全位置
cp capacitor-amap-release.keystore ~/Documents/Backups/
cp capacitor-amap-release.keystore ~/Library/Mobile\ Documents/com~apple~CloudDocs/

# 2. 创建密码备忘录
# Store Password: ****
# Key Alias: capacitor_amap
# Key Password: ****

# 3. 定期检查备份
ls -lh ~/Documents/Backups/capacitor-amap-release.keystore
```

## 🚨 丢失Keystore的后果

如果丢失Release Keystore：
- ❌ 无法更新已发布的APP（新版本无法覆盖安装）
- ❌ 需要以新包名重新发布
- ❌ 用户需要卸载旧版重新安装
- ❌ 所有用户数据丢失

**务必妥善保管！**

