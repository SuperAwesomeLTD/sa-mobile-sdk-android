The simplest way of installing the AwesomeAds SDK in Android Studio is to download the AAR library through Gradle.

Just include the following in your module's `build.gradle` file (usually the file under `MyApplication/app/`):

```
repositories {
    maven {
        url  "http://dl.bintray.com/sharkofmirkwood/maven"
    }
}

dependencies {
    compile 'tv.superawesome.sdk:sa-sdk:3.1.1@aar'
    compile 'com.google.code.gson:gson:2.4'
}
```

![](img/android_gradle_setup.png "Setting up SA in build.gradle")