# Emobilis School Management System

Android app built with Jetpack Compose + Kotlin + Firebase.

## Setup Instructions

### 1. Firebase Setup (REQUIRED)
1. Go to https://console.firebase.google.com
2. Create a new project
3. Add Android app with package name: `com.emobilis.app`
4. Enable **Email/Password** authentication
5. Create a **Firestore** database
6. Download `google-services.json` and replace `app/google-services.json`

### 2. Update GPS Coordinates
Edit `app/src/main/java/com/emobilis/app/AppConstants.kt`:
```kotlin
const val SCHOOL_LATITUDE  = -1.286389   // Replace with actual school lat
const val SCHOOL_LONGITUDE = 36.817223   // Replace with actual school lng
const val SCHOOL_RADIUS_METERS = 200.0
```

### 3. Create Staff Accounts
Staff (lecturers/technicians) must be created manually:
1. Create user in Firebase Auth console
2. Add document to `staff` Firestore collection:
```json
{
  "uid": "<Firebase Auth UID>",
  "fullName": "Staff Name",
  "email": "staff@emobilis.ac.ke",
  "role": "lecturer",
  "department": "Software Development"
}
```
Role options: `lecturer`, `lab_technician`, `admin`

### 4. Firestore Security Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 5. Open in Android Studio
- Open Android Studio
- File > Open > select the `emobilis` folder
- Wait for Gradle sync
- Run on device or emulator (Android 8.0+, API 26+)

## Project Structure
```
app/src/main/java/com/emobilis/app/
├── MainActivity.kt
├── AppConstants.kt
├── EmobilisMessagingService.kt
├── data/
│   ├── model/         # Student, Staff, Attendance, ComputerAlert, Message
│   └── repository/    # AuthRepository, AttendanceRepository, MessageRepository
├── ui/
│   ├── theme/         # EmobilisTheme, colors
│   ├── auth/          # LoginScreen, RegisterScreen
│   ├── student/       # StudentPortalScreen (Home, Attendance, Messages, Alerts, Profile tabs)
│   ├── lecturer/      # LecturerPortalScreen (Dashboard, Students, Messages tabs)
│   └── technician/    # TechnicianPortalScreen (Alert list + resolve)
├── util/
│   └── LocationHelper.kt
└── viewmodel/
    ├── AuthViewModel.kt
    ├── AttendanceViewModel.kt
    └── MessageViewModel.kt
```

## User Roles
| Role | Access |
|------|--------|
| Student | Self-register, sign attendance, report computer issues, send messages |
| Lecturer | View students, send class notices, fees reminders |
| Lab Technician | View and resolve computer alerts |

## Requirements
- Android Studio Hedgehog or newer
- Android 8.0+ (API 26+) on device
- Internet connection for Firebase
- Location permission for GPS attendance
