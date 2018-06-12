更新module
=

### 简述

将eros热更新部分中请求更新，下载合并移入；
将axhome大版本更新移入

依赖于`wxframework`中的`okhttp`工具类和`bspatch.so`库

### 使用

1. 在`app/build.gradle`中添加依赖`compile project(':version')`

2. `wxframework`中的`okhttp`工具类，需要初始化之后才能使用，目前初始化的动作在`app`中注册的`application`中完成的，所以不需要单独初始化。

	在`activity`中初始化请求大版本更新实例，大版本更新结果为最新的时候，会再去请求热更新
```
NativeVersionChecker nativeVersionChecker = new NativeVersionChecker(activity);
nativeVersionChecker.readyNativeUpdate();
```
目前更新的提示框是依赖于调用者`activity`的，所以初始化的时候需要一个`activity`实例。

3. 在`onDestroy`清理资源，避免异常。
```
@Override
    protected void onDestroy() {
        super.onDestroy();
        nativeVersionChecker.releaseAll();
    }
```


