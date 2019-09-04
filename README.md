# Scandinavia

jniLibs 太大了，我没放进来。

把 `OpenCV-android-sdk/sdk/native/libs` 放到 `app/src/main` 下，并改名为 `jniLibs` 即可。

## Updata

`build.gradle`中添加

```java
ndk {
    abiFilters "armeabi", "armeabi-v7a"
    }
```
故`jinLibs`中仅保留`armeabi`与`armeabi-v7a`文件夹即可
