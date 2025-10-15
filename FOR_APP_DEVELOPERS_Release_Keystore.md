# 创建Release Keystore完整指南

## 🎯 为什么需要Release Keystore？

| 类型 | Debug Keystore | Release Keystore |
|------|---------------|------------------|
| 创建方式 | ✅ 自动生成 | ❌ 必须手动创建 |
| 密码 | `android`（固定） | 🔒 您自己设置 |
| 位置 | `~/.android/debug.keystore` | 📁 您自己指定 |
| 用途 | 开发测试 | **正式发布到应用商店** |
| 安全性 | 低（密码公开） | 高（您独有） |
| 可否丢失 | 可以（会自动重建） | ⚠️ **绝对不能丢失！** |

## 📝 创建步骤

### 方法1: 命令行创建（推荐）

```bash
# 进入项目目录
cd /Users/junyang7/AndroidStudioProjects/map/capacitor-amap

# 创建Release Keystore
keytool -genkey -v \
  -keystore capacitor-amap-release.keystore \
  -alias capacitor_amap \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# 执行后会提示输入：
```

**交互式问答示例**：
```
输入密钥库口令: [输入密码，例如：MySecure@2025]
再次输入新口令: [再次输入]

您的名字与姓氏是什么?
  [Unknown]:  Jun Yang        # 或您的公司名

您的组织单位名称是什么?
  [Unknown]:  Development     # 部门

您的组织名称是什么?
  [Unknown]:  Ziji Fun        # 公司名

您所在的城市或区域名称是什么?
  [Unknown]:  Beijing         # 城市

您所在的省/市/自治区名称是什么?
  [Unknown]:  Beijing         # 省份

该单位的双字母国家/地区代码是什么?
  [Unknown]:  CN              # 国家代码

CN=Jun Yang, OU=Development, O=Ziji Fun, L=Beijing, ST=Beijing, C=CN是否正确?
  [否]:  y                    # 确认

正在为以下对象生成 2,048 位RSA密钥对和自签名证书 (SHA256withRSA) (有效期为 10,000 天):
	 CN=Jun Yang, OU=Development, O=Ziji Fun, L=Beijing, ST=Beijing, C=CN

输入 <capacitor_amap> 的密钥口令
	(如果和密钥库口令相同, 按回车):  [回车或输入不同密码]

[正在存储capacitor-amap-release.keystore]
```

### 方法2: Android Studio创建

```
1. Build → Generate Signed Bundle / APK
2. 选择 APK
3. 点击 "Create new..."
4. 填写信息：
   - Key store path: 选择保存位置
   - Password: 设置密码
   - Key alias: capacitor_amap
   - Key password: 设置密码
   - Validity (years): 25
   - Certificate: 填写您的信息
5. 点击 OK
```

## 🔍 创建后验证

```bash
# 查看文件
ls -lh capacitor-amap-release.keystore

# 查看详细信息
keytool -list -v \
  -keystore capacitor-amap-release.keystore \
  -alias capacitor_amap

# 输入您设置的密码后，会显示：
# - SHA1 指纹（用于高德API Key配置）
# - SHA256 指纹
# - 有效期
# - 所有者信息
```

## 🔒 安全备份（重要！）

### 1. 立即备份
```bash
# 备份到多个位置
cp capacitor-amap-release.keystore ~/Documents/Backups/
cp capacitor-amap-release.keystore ~/Dropbox/
cp capacitor-amap-release.keystore /path/to/usb/drive/

# 加密压缩备份
zip -e capacitor-amap-release.zip capacitor-amap-release.keystore
```

### 2. 创建密码记录
创建 `keystore-info.txt`（加密保存！）：
```
=== Capacitor AMap Release Keystore ===
文件名: capacitor-amap-release.keystore
创建日期: 2025-10-15

Store Password: [您的密码]
Key Alias: capacitor_amap
Key Password: [您的密码]

SHA1: [执行keytool命令获取]
SHA256: [执行keytool命令获取]

备份位置:
1. ~/Documents/Backups/
2. iCloud Drive
3. U盘
```

### 3. 定期检查备份
```bash
# 每月检查一次备份是否完好
ls -lh ~/Documents/Backups/capacitor-amap-release.keystore

# 验证文件可用
keytool -list -keystore ~/Documents/Backups/capacitor-amap-release.keystore
```

## ⚙️ 在项目中配置

### 1. 创建 `keystore.properties`（不提交到Git！）

```bash
# 在项目根目录创建
cd /Users/junyang7/AndroidStudioProjects/map/capacitor-amap
nano android/keystore.properties
```

内容：
```properties
storeFile=../capacitor-amap-release.keystore
storePassword=您的Store密码
keyAlias=capacitor_amap
keyPassword=您的Key密码
```

### 2. 添加到 `.gitignore`

```bash
echo "android/keystore.properties" >> .gitignore
echo "*.keystore" >> .gitignore
```

### 3. 修改 `android/build.gradle`

```gradle
// 在 android 块之前加载 keystore 配置
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
}

android {
    ...
    
    signingConfigs {
        release {
            if (keystorePropertiesFile.exists()) {
                storeFile file(keystoreProperties['storeFile'])
                storePassword keystoreProperties['storePassword']
                keyAlias keystoreProperties['keyAlias']
                keyPassword keystoreProperties['keyPassword']
            }
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

## 🚀 使用Release签名构建

```bash
# 构建Release APK
./gradlew assembleRelease

# 输出位置
# android/app/build/outputs/apk/release/app-release.apk

# 验证签名
jarsigner -verify -verbose -certs \
  android/app/build/outputs/apk/release/app-release.apk
```

## 🌐 配置高德API Key

```bash
# 1. 获取Release SHA1
keytool -list -v \
  -keystore capacitor-amap-release.keystore \
  -alias capacitor_amap | grep SHA1

# 2. 访问高德控制台
# https://console.amap.com/dev/key/app

# 3. 创建或更新Key
包名: fun.ziji.capacitor.amap
SHA1: [Release Keystore的SHA1]

# 或者在现有Key添加Release SHA1（用分号分隔）
SHA1: D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C;[Release SHA1]
```

## ⚠️ 警告

### 🚨 如果丢失Release Keystore：
1. ❌ **无法更新已发布的应用**
2. ❌ 只能以新包名重新发布
3. ❌ 用户无法覆盖安装，必须卸载重装
4. ❌ 用户数据全部丢失
5. ❌ 应用评分、下载量清零

### 🛡️ 防止丢失的措施：
- ✅ 创建后立即备份多份
- ✅ 使用云存储（加密）
- ✅ 物理备份（U盘、移动硬盘）
- ✅ 团队共享（安全的方式）
- ✅ 密码使用密码管理器保存（1Password、LastPass等）

## 📊 对比总结

```
开发阶段（现在）
├─ 使用: Debug Keystore (自动生成)
├─ SHA1: D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C
├─ API Key: af6fa6e184a0454641a7cce317ff728b
└─ ✅ 可以随时重建，无需担心丢失

发布阶段（将来）
├─ 使用: Release Keystore (手动创建)
├─ SHA1: [创建后获取]
├─ API Key: [用Release SHA1申请]
└─ ⚠️ 绝对不能丢失！务必多处备份！
```

## ✅ 检查清单

创建Release Keystore后，请确认：
- [ ] Keystore文件已创建
- [ ] 能够使用密码打开
- [ ] 已记录SHA1和SHA256
- [ ] 已备份到至少3个位置
- [ ] 密码已安全保存
- [ ] 已添加到`.gitignore`
- [ ] 在高德控制台配置了Release SHA1
- [ ] 测试构建Release APK成功

