# 插件开发指南

## 🎯 作为插件开发者，您需要关心的事情

### ✅ 需要做的

1. **开发插件代码**
   - TypeScript接口定义 (`src/definitions.ts`)
   - Android原生实现 (`android/src/main/java/...`)
   - Web占位实现 (`src/web.ts`)

2. **测试插件功能**
   - 创建测试APP（已完成：`/map`）
   - 使用Debug Keystore测试
   - 验证JS调用正常

3. **编写文档**
   - README.md（使用说明）
   - API文档
   - 配置指南

4. **发布到npm**
   ```bash
   npm run build
   npm version patch
   npm publish
   ```

### ❌ 不需要做的

1. **❌ 创建Release Keystore**
   - 插件本身不需要签名
   - 只有完整的APP才需要

2. **❌ 发布到应用商店**
   - 插件发布到npm，不是应用商店
   - Google Play、华为商店等与您无关

3. **❌ 申请生产环境的高德Key**
   - 每个使用者用自己的包名和SHA1申请
   - 您只需要测试用的Key

---

## 📋 当前测试环境

### 测试项目：`/Users/junyang7/AndroidStudioProjects/map/map`

```
作用：模拟一个使用您插件的APP
目的：测试插件功能是否正常

配置：
- 包名：fun.ziji.location
- SHA1：D8:60:69:37:4D:10:21:6C:C9:88:60:F7:64:BE:AE:C1:51:82:E5:8C (Debug)
- 高德Key：af6fa6e184a0454641a7cce317ff728b

状态：✅ 仅用于测试，不会发布
```

---

## 🚀 发布流程

### 1. 完成开发和测试
```bash
# 测试所有功能
cd /Users/junyang7/AndroidStudioProjects/map/map
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# 验证：
✅ 权限管理
✅ 单次定位
✅ 持续定位
✅ API Key动态传参
```

### 2. 构建插件
```bash
cd /Users/junyang7/AndroidStudioProjects/map/capacitor-amap

# TypeScript编译
npm run build

# 验证输出
ls -la dist/
```

### 3. 更新版本
```bash
# 根据变更类型选择
npm version patch  # 修复bug：1.0.0 → 1.0.1
npm version minor  # 新功能：1.0.0 → 1.1.0
npm version major  # 破坏性更新：1.0.0 → 2.0.0
```

### 4. 发布到npm
```bash
# 首次发布需要登录
npm login

# 发布
npm publish

# 查看
npm view capacitor-amap
```

### 5. 用户使用
```bash
# 其他开发者安装
npm install capacitor-amap

# 他们需要：
# 1. 用自己的包名和SHA1申请高德Key
# 2. 在代码中使用
import { Amap } from 'capacitor-amap';
await Amap.getCurrentPosition({ 
  key: '他们的Key',
  needAddress: true 
});
```

---

## 📝 文档检查清单

确保README.md包含：

- [ ] 安装方法
- [ ] 基本用法示例
- [ ] API接口说明
- [ ] **重点**：告诉用户需要用自己的包名和SHA1申请Key
- [ ] 权限配置说明
- [ ] 常见问题
- [ ] 完整示例代码

---

## 🔍 与使用者的职责划分

| 任务 | 插件开发者（您） | 使用者（其他开发者） |
|------|----------------|-------------------|
| 开发插件功能 | ✅ | - |
| 测试插件（Debug） | ✅ | - |
| 发布到npm | ✅ | - |
| 编写文档 | ✅ | - |
| | | |
| 安装插件 | - | ✅ |
| 申请高德Key | ❌ 仅测试用 | ✅ 用自己的信息 |
| 集成到APP | - | ✅ |
| 创建Release Keystore | ❌ 不需要 | ✅ 发布APP时需要 |
| 发布到应用商店 | ❌ 不需要 | ✅ |

---

## ⚠️ 常见误解

### ❌ 错误想法
> "我需要创建Release Keystore来发布插件"

### ✅ 正确理解
> "插件发布到npm，不需要Keystore"

---

### ❌ 错误想法
> "我需要为插件申请一个通用的高德Key"

### ✅ 正确理解
> "每个使用插件的APP都需要用自己的包名和SHA1申请独立的Key"

---

### ❌ 错误想法
> "我需要把插件发布到应用商店"

### ✅ 正确理解
> "插件发布到npm，使用者的APP才发布到应用商店"

---

## 🎓 类比说明

```
您的插件 = 汽车零件（轮胎）
├─ 发布到：汽车配件商店（npm）
├─ 购买者：汽车制造商（APP开发者）
└─ 不需要：车牌和驾照（Keystore和应用商店）

使用者的APP = 完整汽车
├─ 使用：您的轮胎（插件）
├─ 需要：车牌和驾照（Keystore）
└─ 上路：车管所审批（应用商店审核）
```

---

## 📊 项目结构

```
capacitor-amap/              ← 您的插件（npm包）
├── android/                 ← Android原生代码
│   └── src/main/java/...
├── src/                     ← TypeScript代码
│   ├── definitions.ts
│   ├── index.ts
│   └── web.ts
├── package.json             ← npm配置
├── README.md                ← 使用文档
└── PLUGIN_DEVELOPMENT.md    ← 本文档

map/                         ← 测试项目（模拟使用者）
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   └── public/
│   │   │   │       └── index.html  ← 测试页面
│   │   │   └── java/
│   │   │       └── fun/ziji/capacitor/amap/
│   │   │           └── AmapPlugin.java  ← 插件副本（用于测试）
│   │   └── ...
│   └── build.gradle.kts
└── ...

发布流程：
1. capacitor-amap → npm publish → npmjs.com ✅
2. map → (不发布，仅用于测试) ❌
```

---

## ✅ 总结

**您是插件开发者，不是APP开发者**

您需要：
1. ✅ 开发插件
2. ✅ 用Debug Keystore测试
3. ✅ 发布到npm
4. ✅ 编写清晰文档

您不需要：
1. ❌ Release Keystore
2. ❌ 发布到应用商店
3. ❌ 申请生产环境高德Key

让使用者自己处理他们APP的签名和发布！

