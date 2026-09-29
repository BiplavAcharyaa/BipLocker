# 🔐 BipLocker

**BipLocker** is a lightweight Android application-locking solution designed to protect selected applications using a user-defined **PIN and device biometrics**.

Users can select applications such as **YouTube, Facebook, Instagram, WhatsApp, banking applications, galleries, or any other supported installed application** and require authentication before those applications can be accessed.

BipLocker itself is also protected using the same authentication mechanism, preventing unauthorised users from changing locking configuration or security settings.

> **Platform:** Android
> **Application Type:** Android App Locker
> **Authentication:** PIN + Device Biometrics
> **Biometric Support:** Fingerprint / Face Authentication where supported by the device
> **Backend:** None required
> **Cloud Dependency:** None required
> **Primary Function:** Application-level access control

---

# 📌 Features

### 🔐 PIN Protection

Users can configure a personal PIN that is used to unlock protected applications.

The PIN can be changed from within BipLocker after successful authentication.

```text
Current PIN
    │
    ▼
Authentication
    │
    ▼
Change PIN
    │
    ├── Enter New PIN
    │
    ├── Confirm New PIN
    │
    └── Save
```

---

### 👆 Fingerprint Authentication

BipLocker can use the fingerprint authentication capabilities available on the Android device.

The application delegates biometric verification to the Android operating system rather than processing fingerprint data itself.

---

### 🙂 Face Authentication

On compatible Android devices, BipLocker can use the device's supported biometric authentication mechanism, including face authentication where the Android device exposes it through the supported biometric framework.

Availability depends on:

* Device hardware
* Android version
* Manufacturer implementation
* Configured biometric credentials
* APIs exposed by the device

---

# 📱 Application Locking

The primary purpose of BipLocker is to allow users to select applications that should require authentication before access.

For example:

```text
Installed Applications

☑ YouTube
☑ Instagram
☐ Chrome
☑ Facebook
☐ Calculator
☑ WhatsApp
☐ Gmail
```

When a protected application is opened, BipLocker initiates the configured authentication mechanism.

```text
User
 │
 ▼
Opens Protected App
 │
 ▼
BipLocker Lock Enforcement
 │
 ▼
Authentication Screen
 │
 ├── PIN
 │
 └── Device Biometrics
 │
 ▼
Authentication Successful
 │
 ▼
Application Access Granted
```

If authentication fails or is cancelled, access to the protected application remains blocked.

---

# 🛡️ BipLocker Self-Protection

BipLocker does not only protect other applications.

**BipLocker itself is protected.**

This is an important part of the security model because an app locker would be ineffective if anyone could simply open BipLocker and disable the configured application locks.

The BipLocker application therefore requires authentication when accessing protected security/configuration functionality.

Conceptually:

```text
                 ┌──────────────────┐
                 │     BipLocker    │
                 └────────┬─────────┘
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
       Security Settings        Protected Apps
              │                       │
              ▼                       ▼
       PIN / Biometrics        PIN / Biometrics
              │                       │
              └───────────┬───────────┘
                          │
                          ▼
                   Access Granted
```

This prevents an unauthorised user from simply entering BipLocker and removing applications from the protected list.

---

# 🏗️ Architecture

BipLocker follows a **local-first Android architecture**.

No backend server is required for the core locking and authentication workflow.

```text
┌──────────────────────────────────────────────┐
│                  BipLocker                   │
│              Android Application             │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│               Presentation Layer             │
│                                              │
│  • Home / Dashboard                          │
│  • App Selection                             │
│  • PIN Authentication                        │
│  • Biometric Authentication                  │
│  • Security Settings                         │
│  • Change PIN                                │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│              Application Layer               │
│                                              │
│  • Authentication Management                 │
│  • App Lock Management                       │
│  • Protected App Configuration               │
│  • PIN Management                             │
│  • Lock State Management                     │
│  • Authentication Flow                       │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│                Android Layer                 │
│                                              │
│  • Android Biometric APIs                    │
│  • Application / Package Management          │
│  • Android Services                          │
│  • Local Storage                              │
│  • Application Lifecycle                     │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│             Android Device Security          │
│                                              │
│       Fingerprint / Face / Device PIN        │
└──────────────────────────────────────────────┘
```

---

# 🔐 Authentication Architecture

BipLocker supports two authentication mechanisms:

```text
                 BipLocker Authentication
                           │
                 ┌─────────┴─────────┐
                 │                   │
                 ▼                   ▼
              PIN Auth          Biometrics
                                     │
                             ┌───────┴───────┐
                             │               │
                             ▼               ▼
                        Fingerprint       Face
```

The user can use the available authentication method supported by the application and device.

Biometric authentication is handled through Android's security framework.

BipLocker does **not** need access to raw fingerprint or facial biometric data.

---

# 🔒 Biometric Security Model

BipLocker does not implement its own fingerprint or facial recognition algorithms.

Instead, it requests authentication through the Android biometric framework.

```text
User
 │
 ▼
BipLocker
 │
 │ Biometric Authentication Request
 ▼
Android Biometric Framework
 │
 ├── Fingerprint
 │
 └── Face Authentication
 │
 ▼
Android Security Subsystem
 │
 ▼
Authentication Result
 │
 ├── SUCCESS
 │
 └── FAILURE
 │
 ▼
BipLocker
```

The application receives the authentication result rather than the underlying biometric information.

BipLocker does not need to store:

* Fingerprint images
* Face images
* Raw biometric sensor data
* Biometric templates

The Android device remains responsible for biometric processing.

---

# 📲 Protected Application Workflow

When a user selects an application for protection, BipLocker maintains the protected application configuration locally.

Example:

```text
BipLocker
│
├── Protected Applications
│   ├── YouTube
│   ├── Instagram
│   ├── Facebook
│   └── WhatsApp
│
├── Authentication
│   ├── PIN
│   └── Biometrics
│
└── Security Settings
    └── Change PIN
```

When the user launches a protected application:

```text
Launch Application
        │
        ▼
Is Application Protected?
        │
    ┌───┴───┐
    │       │
   YES      NO
    │       │
    ▼       ▼
Lock Screen  Open Normally
    │
    ▼
Authenticate
    │
 ┌──┴───────────────┐
 │                  │
 ▼                  ▼
PIN             Biometrics
 │                  │
 └────────┬─────────┘
          ▼
   Authentication
       Result
          │
     ┌────┴────┐
     │         │
  SUCCESS    FAILURE
     │         │
     ▼         ▼
  Allow      Remain
  Access     Locked
```

---

# 📁 Project Structure

The main project directory is:

```text
BipLocker/
```

A typical project structure is:

```text
BipLocker/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── ...
│   │
│   └── build.gradle
│
├── gradle/
│
├── gradlew
├── gradlew.bat
├── build.gradle
├── settings.gradle
│
├── APK/
│   └── BipLocker.apk
│
└── README.md
```

The exact source structure can vary depending on the current Android Studio and Gradle configuration.

---

# 📦 APK

A pre-built APK is included with the project.

The main project folder is:

```text
BipLocker/
```

The expected APK location is:

```text
BipLocker/APK/BipLocker.apk
```

Therefore, from the repository root:

```text
BipLocker/
└── APK/
    └── BipLocker.apk
```

If the APK is generated through the Android Gradle build system, the standard output location is:

```text
BipLocker/app/build/outputs/apk/
```

For a debug build:

```text
BipLocker/app/build/outputs/apk/debug/app-debug.apk
```

For a release build:

```text
BipLocker/app/build/outputs/apk/release/app-release.apk
```

---

# 🚀 Installation

## Option 1 — Install the Included APK

1. Clone or download the repository.
2. Open the `BipLocker` folder.
3. Navigate to:

```text
BipLocker/APK/
```

4. Transfer `BipLocker.apk` to an Android device.
5. Open the APK.
6. Allow APK installation if Android requests permission.
7. Install the application.
8. Launch BipLocker.
9. Configure the PIN.
10. Enable/configure biometric authentication if supported.
11. Select the applications that should be protected.

---

# 🛠️ Build From Source

## Requirements

* Android Studio
* Android SDK
* Android SDK Platform Tools
* Java Development Kit compatible with the project
* Gradle Wrapper included with the repository
* Android device or Android Emulator

### Clone the Repository

```bash
git clone <repository-url>
cd BipLocker
```

### Build Debug APK

#### Windows

```powershell
.\gradlew.bat assembleDebug
```

#### Linux / macOS

```bash
./gradlew assembleDebug
```

The generated APK will normally be available at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# 🔄 Change PIN

BipLocker allows the user to change the configured PIN.

The expected flow is:

```text
BipLocker
   │
   ▼
Authenticate
   │
   ▼
Security Settings
   │
   ▼
Change PIN
   │
   ├── Current Authentication
   │
   ├── New PIN
   │
   ├── Confirm PIN
   │
   └── Save
```

The PIN change operation should only be accessible after successful authentication.

---

# 📱 Android Compatibility

BipLocker is specifically designed for Android.

Biometric functionality depends on the capabilities exposed by the Android device.

Supported authentication capabilities may vary depending on:

* Android version
* Device manufacturer
* Device hardware
* Configured device security
* Available biometric sensors
* Android biometric API support

Fingerprint authentication requires a compatible fingerprint sensor and configured fingerprint credentials.

Face authentication requires compatible device hardware/software and appropriate Android biometric support.

---

# 🔒 Security Considerations

BipLocker uses a **local authentication model** for its core functionality.

There is no requirement for:

* Remote authentication servers
* Cloud biometric databases
* External authentication providers
* Internet connectivity for biometric verification

The application relies on Android's native security mechanisms wherever applicable.

### Important

BipLocker cannot override Android's own security model.

The effectiveness of biometric authentication depends on the security capabilities and configuration of the underlying Android device.

---

# 🧪 Testing

Recommended testing should include:

### PIN

* Create PIN
* Correct PIN
* Incorrect PIN
* PIN change
* Incorrect PIN during protected-app access
* Cancelled authentication

### Biometrics

* Fingerprint authentication
* Face authentication on compatible devices
* Failed biometric authentication
* Cancelled biometric authentication
* Device without biometric hardware
* Device with biometric hardware but no enrolled credentials

### Application Locking

* Add an application to protected apps
* Remove an application from protected apps
* Launch protected application
* Authenticate successfully
* Fail authentication
* Lock multiple applications
* Verify BipLocker itself remains protected

### Security Configuration

* Attempt to access settings without authentication
* Attempt to change PIN without authentication
* Attempt to modify protected applications without authentication
* Restart device
* Verify protected application behaviour after reboot

---

# ⚙️ Technical Characteristics

| Component              | Implementation                                    |
| ---------------------- | ------------------------------------------------- |
| Platform               | Android                                           |
| Application Type       | Application Locker                                |
| Authentication         | PIN + Device Biometrics                           |
| Fingerprint            | Android Biometric Framework                       |
| Face Authentication    | Device/Android-supported biometric authentication |
| App Selection          | Installed application selection                   |
| PIN Management         | User-configurable                                 |
| BipLocker Protection   | Yes                                               |
| Protected Applications | User-selected                                     |
| Backend                | Not required                                      |
| Cloud Dependency       | Not required                                      |
| Biometric Data Storage | Not handled by BipLocker                          |
| Deployment             | APK                                               |
| Build System           | Gradle                                            |
| IDE                    | Android Studio                                    |

---

# 🧠 Design Philosophy

BipLocker is intentionally designed around three principles:

### 1. Local

The core application-locking workflow runs locally on the Android device.

### 2. Secure

Authentication is based on a user-defined PIN and Android's native biometric security mechanisms.

### 3. Simple

The user should be able to:

```text
Install BipLocker
      ↓
Set PIN
      ↓
Enable Biometrics
      ↓
Select Apps
      ↓
Protect Apps
```

No unnecessary backend infrastructure is required.

---

# 🗂️ Example Use Case

A user wants to protect personal applications on their Android device.

They install BipLocker and configure:

```text
PIN
└── 4829

Biometric
└── Enabled

Protected Applications
├── Instagram
├── Facebook
├── YouTube
├── WhatsApp
└── Gallery
```

When the user opens Instagram:

```text
Instagram
    │
    ▼
BipLocker Authentication
    │
    ├── Fingerprint
    │
    ├── Face
    │
    └── PIN
    │
    ▼
Authentication Successful
    │
    ▼
Instagram Access Granted
```

The same authentication mechanism is used to protect BipLocker's security/configuration functionality.

---

# 📄 License

Add the project's actual license here.

Example:

```text
MIT License
```

If BipLocker is proprietary software, replace this section with the applicable proprietary license or usage terms.

---

# 👨‍💻 Author

**Biplav Acharya**

**Project:** BipLocker
**Platform:** Android
**Category:** Application Security / App Locking

---

# 🚧 Project Status

**Status:** Active Development

BipLocker is an Android-focused application locker providing:

* PIN-based protection
* Device biometric authentication
* User-selectable protected applications
* PIN management
* BipLocker self-protection
* Local-first architecture
* Android-native security integration
