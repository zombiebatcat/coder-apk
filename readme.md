# Coder Android Client
```sh
# keystore
keytool -genkeypair -v -storeType JKS -keystore coder.jks -alias coder -keyalg RSA -validity 10000

# publish
./gradlew clean assembleRelease
./gradle assembleRelease
```

