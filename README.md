# Photo Exercise

Thanks for taking the time to do this. It's a continuation of the design conversation from your interview: you'll build a small photo app with Kotlin Multiplatform and Compose Multiplatform.

**Please spend about 4 hours.** We'd much rather see a smaller app that feels great than everything half-finished. Deciding what to cut is part of the exercise, so tell us what you left out and why.

Questions are welcome at any point. Email carl@photalabs.com and we'll answer quickly.

## What to build

### 1. Photo feed and detail

- A grid of photos from `PhotoApi.feed()`, paginated as the user scrolls, with sensible loading, empty, and error states.
- Tapping a photo opens a detail view.

### 2. Uploads

- The user can pick one or more photos from the device's photo library and upload them, with per-item progress and a way to retry failures.
- **The app remembers what it has uploaded across launches.** After a relaunch, the user can see their upload history, and picking a photo that was already uploaded shouldn't upload it again. How you store this and how you identify "the same photo" are up to you.

### 3. The photo experience

This is where we'll spend the most time reviewing, so it's worth your attention.

- Edge-to-edge on both platforms, with an immersive detail view.
- The app should feel at home on each platform. Tell us what you adapted for iOS and what you deliberately left shared.
- At least two photo-centric interactions, done well. Examples: pinch to zoom, a swipeable pager in the detail view, a shared-element transition from grid to detail, swipe to dismiss, progressive loading from the thumbnail. Quality over count.

### Stretch (optional)

- Uploads that were in progress when the app was killed resume on the next launch.
- Already-loaded images are viewable offline.

## What's provided

The project builds and runs on Android and iOS. It contains only:

- `PhotoApi`, the service contract, along with its models (`shared/.../api/`).
- `FakePhotoApi`, an in-memory implementation with realistic latency and failures.
- An empty `App()` composable to start from.

Everything else is your call: architecture, state management, navigation, image loading, storage, and whether to use DI. Add any libraries you like. For the photo picker, writing your own `expect`/`actual` and using a library are both fine; tell us why you chose what you did.

### About the fake API

- Requests take 0.3–1.5 s and fail about 15% of the time by default. `ApiException.retryable` tells you whether retrying can help.
- Uploads can fail partway through, not just at the start.
- Uploads are idempotent by `clientUploadId`. See the KDoc on `PhotoApi.upload`.
- Each `Photo` has a `thumbnailUrl` (a tiny, fast-loading image) and `imageUrl(targetWidth)`, which the server resizes on request.
- You can tune `latency`, `failureRate`, and `uploadBytesPerSecond`, or pass a `seed` to make a run repeatable. Please leave the defaults in place when you submit.
- **The fake forgets everything when the process dies.** Uploaded photos vanish from its feed and its upload dedup resets. Photos it returned stay valid, though: their image URLs keep working. So remembering uploads across launches is your app's job, not the server's.
- A completed upload comes back as a placeholder image rather than the photo you sent. Assume the real server serves the actual image.
- Images are loaded from picsum.photos, so the app needs a network connection.

## Running

- **Android:** open the project in Android Studio and run `androidApp`.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and run. Building for iOS requires a Mac.

Verified with Kotlin 2.4.10, Compose Multiplatform 1.11.1, AGP 9.1.1, and Gradle 9.6.1 on current Android Studio and Xcode.

If you only have access to one platform, that's OK. Keep the shared code platform-neutral, build for the platform you have, and mention it in your notes.

## AI tools

AI coding tools are welcome. In the follow-up session, we'll ask you to walk through your code and explain how it works and why it's built that way, so make sure you understand everything you submit. Add a sentence to your notes on how you used them.

## Submitting

Send us a Git repository (a link, or a zip including the `.git` folder), with commits as you naturally made them.

Include:

- **A short screen recording from each platform you built for,** showing the feed, uploads (including a failure and retry), your photo interactions, and a relaunch that shows upload history.
- **A `NOTES.md`** covering the main decisions you made and why, what you adapted per platform, what you cut and what you'd do next, anything you'd do differently in a production app, and roughly how long you spent. A page is plenty.

Afterward, we'll schedule about 30 minutes to walk through your code together and make a small change to it as a pair.
