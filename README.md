![Smart Counter - by Kosi, professional Android developer](https://github.com/user-attachments/assets/ee131e14-1ed7-4cdc-8735-0c44acede8f1)

![Smart Counter - by Kosi, professional Android developer 1](https://github.com/user-attachments/assets/2464449b-806c-48d3-8b12-9cbf09529259)


# Smart Counter
A Compose MVVM Android app that counts people in real-time using camera-based face/head detection. 
It also allows manual counting via volume buttons. 
Volume up to increment and volume down to decrement. 
Built with MVVM architecture and clean code for easy maintanance and scalability.


## Features

- ## Smart Count:
  Count using the adjustible camera view, returns the number of faces detected by the camera.

- ## Individual Count:
  - Count using the volume+ and volume- to increament and decreament count respectively.
  - Mimics the electronic tally counter.

- ## Session Count:
  - Count as a crew/team and see all participants' count and total in real-time.
  - Count with other available users who are online and 1KM around you.
  - See available counters around you.
  - Select who joins the count session.
  - See each others count in real-time.

- Save Count to offline database (RoomDB).

- Edit Profile details.  


## Built using the following tech stack:

- Kotlin for Android.

- Jetpack Compose.

- Kotlin Coroutines for asynchronous operations which gives the app a faster response and better battery efficiency.

- Google ML kit to detect faces and return the number of faces detected.

- GeoFirestore: used to get the loation of available counters and add them to a session count.
  
- Room Database to store counts offline.

- MVVM Architecture.

- Firebase firestore.

- Firebase Auth: Implemented email/password with firebase Auth.
