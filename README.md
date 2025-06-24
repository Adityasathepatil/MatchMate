A simple matrimonial app that shows potential matches in card format, similar to popular dating/matrimonial apps. Built for Android using modern development practices.
What This App Does

Shows a list of potential matches with their photos and basic info
Lets you accept or decline matches with simple buttons
Calculates how good each match is based on age and location
Works even when you're offline by saving data locally
Handles network problems gracefully

Getting Started
What You Need

Android Studio (latest version)
Android device or emulator (API 24 or higher)
Internet connection for first launch

How to Run

Clone this repository to your computer
Open the project in Android Studio
Wait for Gradle to sync everything
Run the app on your device or emulator

That's it! The app will automatically fetch some sample profiles and you can start swiping.
Libraries I Used and Why
Retrofit + OkHttp - For talking to the random user API. Retrofit makes API calls really simple and OkHttp helps with logging and debugging network issues.
Room Database - Google's recommended way to store data locally. Much easier than raw SQLite and works great with Kotlin.
Hilt - For dependency injection. Makes testing easier and keeps code organized by handling object creation automatically.
Jetpack Compose - Modern UI toolkit. I chose this over traditional XML layouts because it's more flexible and less verbose.
Coil - For loading profile images. Lighter than Glide/Picasso and works perfectly with Compose.
Coroutines + StateFlow - For handling background tasks and reactive UI updates. StateFlow is the modern replacement for LiveData.
App Architecture
I went with MVVM (Model-View-ViewModel) pattern because:

Separation of concerns: UI logic stays in Composables, business logic in ViewModel, data handling in Repository
Testability: Each layer can be tested independently
Lifecycle awareness: ViewModel survives configuration changes
Clean data flow: StateFlow provides clear data streams from Repository → ViewModel → UI

The flow looks like this:
API/Database → Repository → ViewModel → UI (Compose)
Extra Fields I Added
I added Education and Profession to each profile because:

Education - Important for many families when looking for matches. Shows compatibility in terms of intellectual background.
Profession - Helps understand lifestyle, income potential, and career ambitions. Very relevant for matrimonial decisions.

These fields get randomly assigned from predefined lists since the random user API doesn't provide them. In a real app, users would fill these during registration.
Match Score Logic
The match score (0-100) is calculated using two factors:
Age Proximity (50 points max):

Same age ±2 years: 50 points
±3-5 years: 40 points
±6-10 years: 30 points
±11-15 years: 20 points
More than 15 years: 10 points

City Match (50 points max):

Same city: 50 points
Different city: 0 points

This gives a simple but reasonable compatibility score. In a real app, you'd consider many more factors like religion, caste, interests, etc.
Handling Network Issues and Offline Mode
Flaky Network Simulation:

30% of API calls randomly fail to simulate real-world conditions
App shows appropriate error messages when this happens

Offline Strategy:

All fetched profiles are saved to local Room database
If network fails, app automatically loads cached data
User can still accept/decline profiles offline
Clear indicators show when using offline data

Error Handling:

Network errors show user-friendly messages
Retry button lets users try again
Database errors are logged and handled gracefully
App never crashes due to network issues
