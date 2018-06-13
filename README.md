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
	NativeVersionReqBean nvrb = new NativeVersionReqBean();
	nvrb.setAppName("appname");
	nvrb.setBaseUrl("https://***/checkClientVersion");
	nvrb.setClientVersion("***.***.***");

	// 热更新请求参数 
	JsVersionReqBean jvrb = new JsVersionReqBean();
	jvrb.setAndroid("***.***.***");// 当前的大版本
	jvrb.setAppName("appname");
	jvrb.setBaseUrl("https://***/checkJsVersion");
	jvrb.setJsVersion("***");// 当前的小版本

	// 只去检查大版本
	// NativeVersionChecker nvc = new NativeVersionChecker(activity, nvrb);
	
	// 大版本检查结果最新 或 非强更情况下选择不更新 会继续检查热更新
	NativeVersionChecker nvc = new NativeVersionChecker(activity, nvrb, jvrb);
	nvc.checkNativeUpdate();

	// 热更新
	JsVersionChecker jvc = new JsVersionChecker(activity, jvrb);
	jvc.checkJsUpdate();

	// 释放资源
	nvc.releaseAll();
	jvc.releaseAll();
```


