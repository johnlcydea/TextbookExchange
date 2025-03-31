# Textbook Exchange App

![Android](https://img.shields.io/badge/Platform-Android-green) ![Jetpack Compose](https://img.shields.io/badge/Built%20With-Jetpack%20Compose-blue) ![Firebase](https://img.shields.io/badge/Backend-Firebase-orange)

The **Textbook Exchange App** is a mobile application built for Android using **Jetpack Compose**, **Room**, and **Firebase**. It allows users to exchange textbooks by adding, editing, viewing, and deleting book listings. Users can log in, manage their own listings, and browse books for sale by other users. The app persists data locally using **Room** and syncs with **Firebase Firestore** for remote storage.

---

## Features

- **User Authentication**: Log in using Firebase Authentication (currently hardcoded to "User1" for testing).
- **Add a Book**: Add a new book with details (title, author, category, price, image URL).
- **Edit a Book**: Update existing book details.
- **Delete a Book**: Remove a book from the user’s listings with a confirmation dialog.
- **View Listings**: Display "My Listings" (user’s books) and "Books for Sale" (other users’ books) on the dashboard.
- **Data Persistence**: Store books locally using Room and sync with Firebase Firestore.
- **Swipe to Refresh**: Refresh the book list from Firestore using a swipe gesture.

---

## Setup Instructions

### Prerequisites

- **Android Studio**: Version 2023.1.1 or later.
- **JDK**: Version 17.
- **Firebase Project**: Set up a Firebase project and add the `google-services.json` file to the `app/` directory.
- **Emulator/Device**: An Android emulator (API 21 or higher) or a physical device with USB debugging enabled.

---

## Steps to Run

### 1. Open in Android Studio
- Open **Android Studio**.
- Select **Open** and navigate to the cloned project directory (`TextbookExchange`).

### 2. Sync Project
- Click **Sync Project with Gradle Files** to download dependencies.

### 3. Set Up Firebase
- Ensure the `google-services.json` file is in the `app/` directory.
- Verify that **Firebase Authentication** and **Firestore** are enabled in your Firebase project.

### 4. Run the App
- Select an emulator or connect a physical device.
- Click the **Run** button in Android Studio.
- The app will launch, showing the **dashboard screen**.

## Dependencies

The project relies on the following libraries and tools:

| Library/Tool         | Version   | Purpose                                |
|----------------------|-----------|----------------------------------------|
| **Jetpack Compose**  | `1.7.0`   | UI toolkit for building the app interface. |
| **Room**             | `2.6.1`   | Local database for storing book listings. |
| **Firebase**         | `33.4.0`  | Firestore and Authentication for cloud storage and user login. |
| **Coroutines**       | `1.8.1`   | Asynchronous operations for smooth performance. |
| **Coil**             | `2.7.0`   | Image loading library for displaying book images. |
| **Accompanist**      | `0.27.0`  | Swipe-to-refresh functionality for updating data. |

## Project Structure

The project is organized as follows:

- **`app/src/main/java/com/example/textbookexchange/`**:
  - **`MainActivity.kt`**: Entry point, sets up navigation and dependencies.
  - **`Book.kt`**: Data model for a book.
  - **`data/local/`**: Room database setup (`AppDatabase.kt`, `BookDao.kt`).
  - **`data/repository/`**: Repository for data operations (`BookRepository.kt`).
  - **`ui/booklist/`**: ViewModel for managing book data (`BookViewModel.kt`).
  - **`screens/`**: Jetpack Compose screens (`DashboardScreen.kt`, `AddBookScreen.kt`, `EditBookScreen.kt`).

- **`app/build.gradle.kts`**: Gradle configuration with dependencies for Compose, Room, Firebase,
