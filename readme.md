# Coder Android Client


## GRADLE 8.9
- [Download](https://gradle.org/next-steps/?version=8.9&format=bin)

## ENV
- JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
- ANDROID_HOME: /home/Android/sdk
- ADD PATH:
    platform-tools
    java-17-openjdk-amd64/bin
    gradle-8.9/bin


## build
```sh
# keystore
keytool -genkeypair -v -storeType JKS -keystore coder.jks -alias coder -keyalg RSA -validity 10000

# publish
./gradlew clean assembleRelease
./gradle assembleRelease
```

