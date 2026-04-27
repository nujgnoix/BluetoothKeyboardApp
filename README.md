# Bluetooth Keyboard App

一个安卓APP，能够让安卓手机模拟为蓝牙键盘，让PC或Mac连接，然后通过手机输入文字，同步到电脑上。

## 功能特点

- 蓝牙HID设备模拟
- 支持通过手机输入文本到电脑
- 简洁美观的用户界面
- 支持系统默认输入法
- 实时连接状态显示

## 项目结构

```
BluetoothKeyboardApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/bluetoothkeyboardapp/
│   │   │   │   ├── BluetoothHidService.java
│   │   │   │   ├── ConnectionManager.java
│   │   │   │   ├── InputHandler.java
│   │   │   │   └── MainActivity.java
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── input_background.xml
│   │   │   │   │   └── rounded_button.xml
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml
│   │   │   │   └── values/
│   │   │   │       ├── colors.xml
│   │   │   │       ├── strings.xml
│   │   │   │       └── themes.xml
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
├── build.gradle
├── gradle.properties
├── settings.gradle
└── gradlew
```

## 构建步骤

### 前提条件

- Android Studio (最新版本)
- JDK 8或更高版本
- Android SDK (API 33)
- 稳定的网络连接（用于下载依赖）

### 构建方法

#### 方法一：使用Android Studio构建（推荐）

1. **克隆项目**
   ```bash
   git clone <项目地址>
   cd BluetoothKeyboardApp
   ```

2. **使用Android Studio打开项目**
   - 启动Android Studio
   - 选择 "Open an existing project"
   - 导航到项目目录并选择它

3. **配置项目**
   - 等待Gradle同步完成
   - 如果出现依赖下载问题，点击 "Sync Project with Gradle Files"
   - 确保所有SDK组件都已安装（API 33、Build Tools 33.0.0等）

4. **构建项目**
   - 点击 "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"

5. **获取APK文件**
   - 构建完成后，点击 "locate" 按钮找到APK文件
   - APK文件通常位于 `app/build/outputs/apk/debug/app-debug.apk`

#### 方法二：使用命令行构建

1. **设置环境变量**
   ```bash
   export ANDROID_HOME=/path/to/android-sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/33.0.0
   ```

2. **构建项目**
   ```bash
   cd BluetoothKeyboardApp
   ./gradlew assembleDebug
   ```

3. **获取APK文件**
   - APK文件位于 `app/build/outputs/apk/debug/app-debug.apk`

### 常见问题及解决方案

1. **Gradle同步失败**
   - 检查网络连接
   - 尝试使用VPN
   - 清理Gradle缓存：`rm -rf ~/.gradle/caches`

2. **依赖下载失败**
   - 检查网络连接
   - 配置Gradle使用国内镜像

3. **构建错误**
   - 确保所有SDK组件都已安装
   - 尝试更新Android Studio到最新版本
   - 清理项目：点击 "Build" > "Clean Project"

4. **蓝牙权限问题**
   - 确保在运行时授予应用蓝牙和位置权限
   - 在Android 6.0+设备上，需要手动授予位置权限以进行蓝牙扫描

### 替代方案

如果您遇到构建困难，可以考虑使用以下方法：

1. **使用在线构建服务**
   - GitHub Actions
   - Bitrise
   - CircleCI

2. **使用预构建的APK**
   - 联系开发者获取预构建的APK文件
   - 确保从可信来源获取APK文件

## 安装指南

### 方法一：通过USB安装

1. **启用USB调试**
   - 在手机上进入 "设置" > "关于手机"
   - 连续点击 "版本号" 7次以启用开发者选项
   - 进入 "设置" > "开发者选项" > 启用 "USB调试"

2. **连接手机到电脑**
   - 使用USB数据线将手机连接到电脑
   - 在手机上允许USB调试权限

3. **安装APK**
   - 在Android Studio中，点击 "Run" > "Run 'app'"
   - 选择你的设备并点击 "OK"

### 方法二：直接安装APK

1. **将APK文件传输到手机**
   - 通过电子邮件、云存储或USB将APK文件发送到手机

2. **安装APK**
   - 在手机上找到APK文件
   - 点击文件开始安装
   - 允许 "未知来源" 安装权限

## 使用说明

1. **打开应用**
   - 在手机上找到并打开 "Bluetooth Keyboard" 应用

2. **连接到电脑**
   - 点击 "Connect" 按钮
   - 确保手机蓝牙已开启
   - 在电脑上搜索蓝牙设备
   - 找到并配对 "Bluetooth Keyboard" 设备

3. **输入文本**
   - 在应用的输入框中输入文本
   - 点击 "Send" 按钮
   - 文本将通过蓝牙发送到电脑

4. **断开连接**
   - 点击 "Disconnect" 按钮
   - 或在电脑上断开蓝牙连接

## 权限说明

应用需要以下权限：
- **BLUETOOTH**：用于蓝牙通信
- **BLUETOOTH_ADMIN**：用于蓝牙设备管理
- **BLUETOOTH_CONNECT**：用于连接蓝牙设备
- **BLUETOOTH_SCAN**：用于扫描蓝牙设备
- **ACCESS_FINE_LOCATION**：用于蓝牙设备扫描（Android 6.0+ 要求）

## 技术说明

- 使用Android的 `BluetoothHidDevice` API实现HID设备模拟
- 采用标准键盘HID报告描述符
- 实现了字符到键码的映射
- 支持基本的文本输入和特殊键

## 注意事项

- 此应用需要Android 5.0 (API 21)或更高版本
- 电脑需要支持蓝牙HID设备
- 首次使用时，可能需要在电脑上手动配对设备
- 输入延迟可能会因蓝牙连接质量而有所不同

## 故障排除

### 无法连接到电脑
- 确保手机蓝牙已开启
- 确保电脑蓝牙已开启
- 尝试重新配对设备
- 检查手机和电脑之间的蓝牙信号强度

### 输入延迟高
- 确保手机和电脑距离较近
- 避免蓝牙信号干扰
- 尝试重新连接设备

### 某些字符无法输入
- 应用支持基本的ASCII字符
- 复杂的Unicode字符可能无法正确处理

## 后续计划

- 添加鼠标模拟功能
- 支持自定义键盘布局
- 优化输入延迟
- 添加快捷键支持

## 许可证

MIT License
