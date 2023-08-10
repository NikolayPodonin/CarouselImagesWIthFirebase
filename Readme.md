# Images Carousel App with Firebase Integration

This repository demonstrates how to integrate various Firebase features into an Android app, including Firebase Authentication, App Check, Firebase Storage, and Firestore. The app uses Jetpack Compose for building the UI, ViewModel for managing app data, and Flow for handling asynchronous operations.

## Firebase Authentication

- Follow the steps outlined in [this guide](https://firebase.google.com/docs/auth/android/google-signin) to integrate Firebase Authentication with Google Sign-In and Apple Sign-In.
- Ensure you add SHA1 and SHA256 fingerprints to your Firebase project by running `./gradlew signingReport`.
- Download the `google-services.json` file from Firebase and add it to your Android project.
- You can find your OAuth client ID in the `google-services.json` file, and also in Google Cloud under "APIs & Services" > "Credentials.". You need to use one for Web client, not Android client!

## App Check

- Enable App Check on the Firebase Console.
- Add the App Check dependency to your app and configure it as specified in [this guide](https://firebase.google.com/docs/app-check/android/play-integrity-provider).
- Consider using `DebugAppCheckProvider` for debug builds to simplify testing and development.

## Firebase Storage

- Integrate Firebase Storage into your app as detailed [here](https://firebase.google.com/docs/storage/android/start#add-sdk).
- Use the provided links to learn how to upload images to Firebase Storage:
    - [Upload Files to Firebase Storage](https://firebase.google.com/docs/storage/android/upload-files#upload_files).

## Firestore

- Utilize Firestore to store collections of links to images.
- Learn how to create Firestore documents and collections:
    - [Add Data to Firestore](https://firebase.google.com/docs/firestore/manage-data/add-data#kotlin+ktx_1).

## Jetpack Compose, ViewModel, and Flow

- Implement your app's UI using Jetpack Compose.
- Use ViewModel to manage app data and UI state.
- Utilize Flow to handle asynchronous operations and data streams.

## Getting Started

To get started, follow the steps outlined in the linked guides to integrate each Firebase feature into your app. The provided sample code demonstrates the integration process.

## Contributing

Contributions are welcome! If you have suggestions or improvements, feel free to open an issue or a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

This README template provides a comprehensive overview of how to integrate Firebase features into your Android app and encourages contributors to engage with your project. Feel free to tailor it to your specific project's structure and needs.

Remember to replace the placeholders (like links, names, and licenses) with the actual content for your project. Good luck with your app development journey!