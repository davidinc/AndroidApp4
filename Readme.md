# SuperPodcast – AndroidApp4

SuperPodcast is an Android application written in Kotlin that allows users to search for Apple Podcasts, apply advanced search criteria, subscribe to podcasts, view available episodes, and play podcast audio.

This project was created for Android App Assignment 4. Its purpose is to demonstrate the core Android programming concepts covered in the original iTunes Podcast example while introducing additional functionality and variations.

## Repository

GitHub repository:

https://github.com/davidinc/AndroidApp4

## Main Features

### Podcast Search

Users can search for podcasts by entering a keyword. The application sends the search request to the Apple iTunes Search API and displays matching podcasts in a RecyclerView.

Each result displays information such as:

- Podcast title
- Artist or creator
- Podcast artwork
- Genre
- Number of episodes
- Number of words in the podcast title

### Advanced Search Filters

SuperPodcast provides several unusual search options beyond a standard keyword search:

1. **Standard Search**
   - Displays the podcast results returned by the Apple iTunes Search API.

2. **Regular Expression Title Search**
   - Filters podcast titles using a regular expression.
   - This allows users to find titles that match more advanced text patterns.

3. **Minimum Title Words**
   - Displays only podcasts whose titles contain at least the number of words entered by the user.

4. **Fewest Episodes**
   - Sorts podcasts according to their episode count, starting with the podcast containing the fewest episodes.

5. **Newest First**
   - Sorts podcasts according to their release date, with newer podcasts displayed first.

The Apple iTunes Search API does not provide podcast review ratings in its search response. Therefore, the **Fewest Episodes** filter was implemented as an alternative unusual search criterion.

### Podcast Details

Selecting a podcast opens a separate detail screen that displays:

- Larger podcast artwork
- Podcast title
- Artist or creator
- Subscription button
- List of available episodes
- Audio player

### RSS Episode Loading

The application downloads the selected podcast’s RSS feed and parses its XML data.

The RSS parser extracts episode information such as:

- Episode title
- Description
- Publication date
- Duration
- Audio file URL

### Podcast Playback

Episodes can be played from the podcast detail screen using Android Media3 ExoPlayer.

The player supports the basic playback controls provided by `PlayerView`, including:

- Play
- Pause
- Seek
- Playback progress

### Local Subscriptions

Users can subscribe to or unsubscribe from a podcast.

Subscriptions are stored locally using:

- `SharedPreferences`
- Gson JSON serialization

The **My Subscriptions** button on the main screen displays the podcasts saved by the user. The subscriptions remain available after the application is closed and reopened.

## Technologies Used

- Kotlin
- Android XML layouts
- Android View Binding
- MVVM architecture
- Kotlin Coroutines
- StateFlow
- Retrofit
- Gson
- RecyclerView
- Glide
- Android Media3 ExoPlayer
- SharedPreferences
- XmlPullParser
- Apple iTunes Search API
- Podcast RSS feeds
- Git and GitHub

## Application Architecture

The application follows a simplified MVVM structure:

### Model

The model classes represent application data.

Examples:

- `Podcast`
- `PodcastResponse`
- `Episode`
- `SearchMode`

### View

The views display information and collect user input.

Main view components include:

- `MainActivity`
- `PodcastDetailActivity`
- `activity_main.xml`
- `activity_podcast_detail.xml`
- `item_podcast.xml`
- `item_episode.xml`

### ViewModel

ViewModels keep UI logic separate from Activities and preserve UI state during configuration changes.

The application uses:

- `PodcastViewModel`
- `PodcastDetailViewModel`
- `PodcastViewModelFactory`
- `PodcastDetailViewModelFactory`

### Repository

`PodcastRepository` provides one place for Activities and ViewModels to access podcast data.

It communicates with:

- The Apple iTunes Search API
- Podcast RSS feeds
- The local subscription store

## Project Package Structure

```text
com.dawit.androidapp4
├── data
│   ├── local
│   │   └── SubscriptionStore.kt
│   ├── model
│   │   ├── Episode.kt
│   │   └── Podcast.kt
│   ├── remote
│   │   ├── ITunesApi.kt
│   │   ├── RetrofitClient.kt
│   │   └── RssEpisodeParser.kt
│   └── repository
│       └── PodcastRepository.kt
├── domain
│   ├── SearchFilterEngine.kt
│   └── SearchMode.kt
├── ui
│   ├── detail
│   │   ├── EpisodeAdapter.kt
│   │   ├── PodcastDetailActivity.kt
│   │   ├── PodcastDetailViewModel.kt
│   │   └── PodcastDetailViewModelFactory.kt
│   └── main
│       ├── PodcastAdapter.kt
│       ├── PodcastViewModel.kt
│       └── PodcastViewModelFactory.kt
└── MainActivity.kt
