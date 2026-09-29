# 🔐 BipLocker

**BipLocker** is a lightweight, security-focused Android application that provides a simple mathematical lock mechanism combined with the device's native biometric authentication.

The application is designed specifically for **Android devices** and uses the Android platform's built-in security and biometric capabilities rather than implementing a custom biometric authentication system.

> **Platform:** Android
> **Application Type:** Native Android Security Utility
> **Authentication:** Mathematical Lock + Device Biometrics
> **Biometric Support:** Fingerprint & Face Unlock
> **Backend:** None
> **Cloud Dependency:** None

---

## 📌 Overview

BipLocker provides an additional application-level locking mechanism while leveraging the security infrastructure already available on the user's Android device.

The application supports:

* 🔢 Mathematical/PIN-based authentication
* 👆 Fingerprint authentication
* 🙂 Face authentication / Face Unlock
* 🔐 Android system biometric security
* 📱 Android-native user experience
* ⚡ Lightweight local execution
* 🚫 No backend server required
* 🚫 No external database required
* 🚫 No cloud authentication required
* 🔒 Authentication handled locally on the device

The biometric functionality is intentionally designed to work with the **biometric capabilities registered on the Android device**. BipLocker does not store or process raw fingerprint or facial biometric data.

---

# 🏗️ Architecture

BipLocker follows a **local-first Android architecture**, where authentication and application logic execute directly on the user's Android device.

```text
┌──────────────────────────────────────────────┐
│                  BipLocker                   │
│                Android App                  │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│              Presentation Layer              │
│                                              │
│  • Lock Screen                               │
│  • Mathematical Authentication UI            │
│  • Biometric Authentication UI               │
│  • User Interaction                         │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│              Application Logic               │
│                                              │
│  • Authentication Flow                       │
│  • Lock Validation                            │
│  • Biometric Authentication Handling          │
│  • Authentication State Management            │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│             Android Security Layer           │
│                                              │
│        Android Biometric Framework            │
│                    │                         │
│          ┌─────────┴─────────┐               │
│          ▼                   ▼               │
│     Fingerprint          Face Unlock         │
└──────────────────────────────────────────────┘
```

### Architecture Principles

BipLocker is designed around the following principles:

1. **Local Authentication**

   * Authentication occurs directly on the Android device.
   * No authentication request is sent to a remote server.

2. **Platform Security**

   * Biometric authentication relies on Android's native biometric framework.
   * The application does not attempt to access raw biometric information.

3. **Minimal Attack Surface**

   * No unnecessary backend services.
   * No external authentication provider.
   * No remote database required.

4. **Separation of Authentication Methods**

   * Mathematical authentication remains available as an application-level authentication mechanism.
   * Biometric authentication acts as an additional device-supported authentication method.

---

# 🔐 Biometric Authentication

BipLocker supports biometric authentication provided by the Android device.

Depending on the hardware and Android configuration of the device, the available authentication methods may include:

### 👆 Fingerprint

If the device has a registered fingerprint sensor and fingerprints configured, BipLocker can invoke the Android biometric authentication flow.

The application does **not** receive the fingerprint itself.

The Android operating system performs the biometric verification and returns only the authentication result to the application.

### 🙂 Face Unlock

On compatible Android devices, BipLocker can also use the device's supported face authentication mechanism.

Availability depends on:

* Device hardware
* Android version
* Manufacturer implementation
* Whether face authentication is configured
* Whether the device exposes the authentication method through the supported Android biometric APIs

### 🔒 Privacy Model

BipLocker does not store:

* Fingerprint images
* Face images
* Biometric templates
* Raw biometric sensor data

The Android operating system remains responsible for biometric processing.

Conceptually:

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
  ├── Fingerprint Sensor
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
  └── FAILURE
  │
  ▼
BipLocker
```

---

# 📱 Android-Only Design

BipLocker is specifically designed for **Android**.

The application relies on Android platform capabilities for:

* Application lifecycle
* Authentication
* Biometric authentication
* Device security
* Local application storage
* Android UI
* Permission management

It is not intended to be a cross-platform application.

The architecture therefore prioritises native Android security and platform integration over cross-platform abstraction.

---

# 🧩 Project Structure

The repository is organised around the main Android project directory:

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
└── README.md
```

> The exact package structure may vary depending on the current Android Studio/Gradle configuration.

---

# 📦 APK

A pre-built APK is included in the project directory for direct installation on an Android device.

The expected project layout is:

```text
BipLocker/
│
├── app/
│
├── gradle/
│
├── ...
│
└── APK/
    └── BipLocker.apk
```

Therefore, the APK can be located using the following path from the main project directory:

```text
BipLocker/APK/BipLocker.apk
```

If the APK is stored directly under another build/output directory in the repository, use the corresponding generated APK path from the project.

For a standard Android Gradle build, the generated APK is typically located under:

```text
BipLocker/app/build/outputs/apk/
```

For example:

```text
BipLocker/app/build/outputs/apk/debug/app-debug.apk
```

or:

```text
BipLocker/app/build/outputs/apk/release/app-release.apk
```

---

# 🚀 Installation

## Method 1 — Install the Included APK

1. Download or clone the repository.
2. Open the `BipLocker` directory.
3. Navigate to the APK location.
4. Transfer the APK to your Android device.
5. Open the APK.
6. Allow installation from the relevant source if Android requests permission.
7. Install BipLocker.
8. Launch the application.
9. Configure the required authentication method.

Example:

```text
BipLocker/
└── APK/
    └── BipLocker.apk
```

---

# 🛠️ Building From Source

## Requirements

Before building BipLocker from source, ensure the development environment contains:

* Android Studio
* Android SDK
* Android SDK Platform Tools
* Java Development Kit compatible with the project's Gradle configuration
* Gradle Wrapper included with the project
* Android device or Android Emulator

### Clone Repository

```bash
git clone <repository-url>
cd BipLocker
```

### Build Debug APK

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On Linux/macOS:

```bash
./gradlew assembleDebug
```

The generated APK will normally be available under:

```text
app/build/outputs/apk/debug/
```

For example:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# 🔏 Security Model

BipLocker follows a **device-trusted authentication model**.

For biometric authentication, the application delegates biometric verification to the Android operating system rather than implementing biometric recognition itself.

This provides several advantages:

* No biometric database
* No biometric image storage
* No biometric data transmission
* No custom biometric recognition algorithm
* Reduced application-level handling of sensitive biometric information
* Native Android security integration

The application receives an authentication result rather than the user's underlying biometric information.

---

# 🧠 Authentication Flow

The general authentication flow is:

```text
                    ┌───────────────┐
                    │ Launch App    │
                    └───────┬───────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │ Authentication      │
                 │ Required            │
                 └──────────┬──────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
      ┌───────────────┐          ┌────────────────┐
      │ Mathematical  │          │ Biometric      │
      │ Authentication│          │ Authentication │
      └───────┬───────┘          └───────┬────────┘
              │                           │
              ▼                           ▼
       Validate Input            Android Biometric
                                      Framework
              │                           │
              └─────────────┬─────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Authentication│
                    │ Result        │
                    └───────┬───────┘
                            │
                    ┌───────┴───────┐
                    │               │
                  SUCCESS         FAILURE
                    │               │
                    ▼               ▼
             Unlock / Access      Remain Locked
```

---

# 🧪 Testing

BipLocker should be tested on physical Android devices where possible, particularly for biometric functionality.

Recommended test cases include:

### Authentication

* Correct mathematical authentication
* Incorrect mathematical authentication
* Empty/invalid input
* Authentication cancellation
* Multiple authentication attempts

### Fingerprint

* Registered fingerprint
* Unregistered fingerprint
* Incorrect fingerprint
* Fingerprint authentication cancellation
* Device without fingerprint hardware

### Face Authentication

* Registered face
* Failed face authentication
* Authentication cancellation
* Device without supported face authentication

### Device Compatibility

* Different Android versions
* Different manufacturers
* Devices with fingerprint only
* Devices with face authentication
* Devices with multiple biometric methods
* Devices without biometric hardware

---

# 🔒 Privacy

BipLocker is designed to keep authentication local to the Android device.

The biometric authentication process is delegated to Android's native security framework.

The application does not require a remote authentication server to perform biometric verification.

No cloud-based biometric database is required.

---

# ⚙️ Technical Characteristics

| Component              | Implementation                             |
| ---------------------- | ------------------------------------------ |
| Platform               | Android                                    |
| Application Type       | Native Android Application                 |
| Authentication         | Mathematical + Biometric                   |
| Fingerprint            | Android device biometric authentication    |
| Face Unlock            | Android-supported biometric authentication |
| Backend                | None required                              |
| Database               | None required for biometric authentication |
| Cloud                  | Not required                               |
| Biometric Storage      | None                                       |
| Network Authentication | Not required                               |
| Deployment             | APK                                        |
| Build System           | Gradle                                     |
| IDE                    | Android Studio                             |

---

# 📋 Project Goals

BipLocker was designed with a simple technical objective:

> **Provide a lightweight Android locking mechanism while taking advantage of the security infrastructure already provided by the Android operating system.**

The project intentionally avoids unnecessary backend infrastructure and keeps the authentication workflow local to the device.

---

# ⚠️ Compatibility Notes

Biometric capabilities are ultimately determined by the Android device and operating system.

Therefore:

* Not every Android device supports fingerprint authentication.
* Not every Android device supports face authentication through the same biometric APIs.
* The available biometric methods depend on the device manufacturer and Android version.
* The user must have biometric authentication configured on the device.
* Android may require a secure device credential such as a PIN, pattern, or password before biometric authentication can be used.

BipLocker cannot enable biometric hardware that the device itself does not provide.

---

# 👨‍💻 Development

BipLocker is intended to remain lightweight and maintainable.

The project architecture avoids unnecessary external infrastructure and focuses on:

```text
Android Application
        │
        ├── Authentication
        │
        ├── Local Application Logic
        │
        ├── Android Security APIs
        │
        └── Device Biometric Framework
```

This keeps the application architecture simple while allowing it to take advantage of native Android security capabilities.

---

# 📄 License

Add the appropriate license for the project here.

For example:

```text
MIT License
```

or replace this section with the project's actual proprietary/open-source licensing terms.

---

# 👤 Author

**Biplav Acharya**

BipLocker — Android Security Utility

---

## ⭐ Project Status

**Status:** Active Development

BipLocker is an Android-focused project with biometric authentication support and a local authentication architecture.

---
