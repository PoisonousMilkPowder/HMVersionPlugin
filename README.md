针对weex 的二次开发框架eros 做的一个热更新+大版本更新的插件
==

引用方式：
==
**Step 1.** 在project/build.gradle中添加

```
	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```

**Step 2.** 在app/build.gradle中添加

```
	dependencies {
		implementation 'com.github.PoisonousMilkPowder:HMVersionPlugin:0.0.3'
	}
```
使用方式：
==
**目前只能在原生中调用，后续会扩展module出去**

**更新提示框依赖于一个activity实例**

```
	// 大版本请求参数对象 大版本更新需要三个参数
	HashMap<String, String> params = new HashMap<>();
    params.put("clientType", "android");
    params.put("appName", "home-app");
    params.put("clientVersion", "1.0.40");
    NativeVersionChecker nvc = new NativeVersionChecker(this,
            "完整的请求地址",
            params);
    nvc.checkNativeUpdate();


	// 释放资源
	nvc.releaseAll();

```


