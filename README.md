# Universal Music Player — Android starter

## Build an APK using GitHub Actions (phone-friendly)

1. Sign in to GitHub in your phone browser.
2. Create a new **public or private** repository, for example `UniversalMusicPlayer`.
3. Upload the contents of this project folder to the repository root. Make sure `.github/workflows/android.yml` is included; if GitHub's web uploader hides `.github`, create the workflow file in GitHub using **Add file → Create new file** and name it `.github/workflows/android.yml`.
4. Commit the files to the `main` branch.
5. Open the repository's **Actions** tab. Select **Build Android APK** and, if prompted, tap **Run workflow**. A push to `main` also starts a build automatically.
6. Wait for the workflow to finish with a green check mark.
7. Open the completed workflow run, scroll to **Artifacts**, and download `UniversalMusicPlayer-debug-apk`.
8. Extract the downloaded artifact ZIP and tap `app-debug.apk` to install it. Android may ask you to allow installs from your browser or file manager.

## Requirements for this cloud build

- GitHub account
- Phone browser
- Internet connection

The workflow uses JDK 17, Gradle 8.9 and Android Gradle Plugin 8.7.3.

## Current app functionality

- Reads local audio files through Android MediaStore.
- Requests audio permission at runtime.
- Plays selected audio using AndroidX Media3 ExoPlayer.
- Previous, play/pause and next controls.
- Original lyrics and translation text fields.
- Basic LRC timestamp parser.

## Known limitations of this first build

- Lyrics are manually pasted; automatic lyrics lookup and machine translation are not implemented yet.
- Lyrics are not yet saved per song.
- Background playback and lock-screen controls are not implemented yet.
- Lyrics availability depends on the source/provider and its licensing.

Package name: `com.example.universalmusicplayer`
