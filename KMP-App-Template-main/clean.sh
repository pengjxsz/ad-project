# 确保在项目根目录执行
./gradlew clean

# 删除 Xcode 缓存
rm -rf ~/Library/Developer/Xcode/DerivedData

# 删除 Pods 目录和锁定文件
rm -rf iosApp/Pods
rm -rf iosApp/Podfile.lock

# 删除 KMP 编译缓存（重要）
rm -rf composeApp/build 
rm -rf composeApp/.gradle
