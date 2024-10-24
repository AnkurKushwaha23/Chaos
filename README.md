# Chaos Music Player: *"Feel Calm in Chaos"*

**Chaos Music Player** is a offline music player for Android, designed to fetch and play your local music files seamlessly. With a wide array of features like repeat mode, shuffle, search, favorites, playlists, and a sleep timer, Chaos provides a calm and personalized music experience—perfect for enjoying your music library exactly the way you like it.

[![Get the App](https://img.shields.io/badge/Get%20Chaos%20Music%20Player-Download-4CAF50?style=flat&logo=google-drive&logoColor=white)](https://drive.google.com/file/d/1j2WnoppEDhbhTkxawwy8pLrJ7P9kZqlw/view?usp=sharing)

## Features

- **Local Song Fetching**: Automatically scans and loads all music files from the device's storage.
- **Playlists**: Create and manage playlists for personalized music experiences.
- **Favorites**: Mark songs as favorites and access them easily.
- **Repeat & Shuffle**: Control the playback with repeat (one/all) and shuffle options.
- **Search**: Quickly search for songs in your library by title, artist, or album.
- **Sleep Timer**: Automatically stops playback after a set period of time.
- **Background & Foreground Music Playback**: Supports playing music in the background while the user interacts with other apps.
- **Media Controls**: Supports system media controls (play, pause, next, previous) with **MediaSessionCompat**.
- **Notifications**: Displays controls in notifications to manage playback.

## Technologies Used

- **MVVM Architecture**: The app is structured following the Model-View-ViewModel pattern to separate concerns and maintain code modularity. The implementation is customized to suit the project’s needs.
- **Room Database**: Used to store the user's favorite songs and playlists. The many-to-many relationship model was implemented to handle songs being added to multiple playlists.
- **MediaPlayer**: Android’s native `MediaPlayer` API is used for audio playback.
- **MediaSessionCompat**: Integrated to manage media controls and handle interactions with other system media components.
- **Background, Foreground, and Bound Services**: Managed playback using Android's service lifecycle to keep the player active in the background and foreground.
- **WorkManager**: Integrated for managing scheduled tasks like the sleep timer.
- **Broadcast Receivers**: Used for handling system events such as headphone plug/unplug and managing playback accordingly.
- **PendingIntent**: Utilized for controlling actions from notifications, media buttons, and widgets.

## Learning Experience

While developing **Chaos Music Player**, I gained practical knowledge of essential Android components, including:

- **Services**: Background, foreground, and bound services to manage playback.
- **PendingIntent & Notification**: For controlling media playback outside the app (e.g., from lock screen or notifications).
- **BroadcastReceiver**: To handle external events like headphone plug/unplug and managing play/pause state.
- **MediaSessionCompat**: Integrating system-wide media controls across different Android API levels, which posed challenges due to scarce resources.
- **Room DB with Many-to-Many Relationship**: Implemented Room database to store favorite songs and playlists with a many-to-many relationship between songs and playlists.
  
Developing this app provided me with insights into how Android features work differently on various API levels and how to implement backward-compatible solutions.

## Challenges

One of the biggest challenges in this project was finding up-to-date resources for integrating **MediaSessionCompat** properly across different Android versions. While there were limited resources available on platforms like YouTube, Stack Overflow, and GitHub, ChatGPT played a major role in providing helpful code snippets and guidance for this implementation.

## Acknowledgments

- **ChatGPT**: A significant portion of the implementation guidance and problem-solving (around 45%) came from the help provided by ChatGPT, which acted as an essential resource for resolving technical challenges.
- **YouTube Tech Creators**: Contributed helpful tutorials and explanations, especially for the general concepts of Room DB, media player functionality, and Android services.
- **StackOverflow & GitHub**: Though limited, some threads and repositories were helpful in tackling smaller issues throughout development.

## Future Enhancements

- **Equalizer**: Adding an in-built equalizer for users to tweak audio settings.
- **Themes & UI Enhancements**: Incorporating more visual themes to provide users with customization options.
- **Cloud Sync**: Enabling cloud sync for playlists and favorites across devices.
- **Widget Support**: Adding a home screen widget for easy access to playback controls.

## Conclusion

I completed **Chaos Music Player** through a collaborative effort. I contributed 50% of the work, ChatGPT provided 45% of the help, and the remaining 5% came from YouTube tutorials. This project has been a rewarding learning experience, and I'm thankful to all the resources that helped me reach the finish line!

## Screenshots 


![Screenshot_20241011-084655](https://github.com/user-attachments/assets/5d9fc6f7-71e1-4f65-8942-574eb5a443a7)

![Screenshot_20241011-084735](https://github.com/user-attachments/assets/4c0f1072-d8db-4d0f-823f-283adcc5b3fb)

![Screenshot_20241011-084857](https://github.com/user-attachments/assets/3eda4f20-eb30-4a41-a6e1-559ceb190846)

![Screenshot_20241011-084925](https://github.com/user-attachments/assets/a22cbb97-efa5-48a1-bc25-9cdd263dd774)

![Screenshot_20241011-084935](https://github.com/user-attachments/assets/9fd2e2fa-806c-4375-b44f-ab9c12d800a8)

![Screenshot_20241011-085000](https://github.com/user-attachments/assets/2fce0fc5-7bd8-4122-af93-795f70d1dc09)

![Screenshot_20241011-085046](https://github.com/user-attachments/assets/55014de9-34ed-4b0d-a294-7200a42e8fc7)

![Screenshot_20241011-085054](https://github.com/user-attachments/assets/9564bd3c-d399-4621-af37-40789c899002)

![Screenshot_20241011-085126](https://github.com/user-attachments/assets/0f8e93d9-c940-46d9-8a3e-6dbe480eb3c3)

![Screenshot_20241011-085131](https://github.com/user-attachments/assets/7216ac08-0acb-4efb-9df5-d11acda32f99)

