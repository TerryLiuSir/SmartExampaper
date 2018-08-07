HwRealizeManager：识别类

1.添加jar包：Hanvon_SDK_Android_1.0_20161031_pro.jar

2.添加权限
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

	<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
	
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.READ_PHONE_STATE"></uses-permission>
	<uses-permission android:name="android.permission.BLUETOOTH"></uses-permission>


用法简介：

1.定义对象：
	private HwRealizeManager mHwRealizeManager;
	
2.实例化对象：
	mHwRealizeManager = new HwRealizeManager(getApplicationContext(), new HwRealizeManager.HwRealizeListener() {
            @Override
            public void onRealizeFormula(String data) {
                //公式识别结果返回字符串，使用LaTeX格式表述
            }

            @Override
            public void onRealizeSingle(String[] data) {
				//单字符识别结果返回字符串数组，多个识别结果
            }

            @Override
            public void onRealizeLine(String[] data) {
				//单字符识别结果返回字符串数组，多个识别结果
            }
        });
		
3.识别方法调用：
	i-识别公式：
	mHwRealizeManager.realizeFormula(stringData);
	
	ii-识别单字符：
	mHwRealizeManager.realizeSingle(HwRealizeManager.LANG_CHNS, stringData);
	
	iii-识别文本行：
	mHwRealizeManager.realizeLine(HwRealizeManager.LANG_CHNS, stringData);
	
4.销毁释放资源：
	mHwRealizeManager.destroy();
	
	
**********************************************************************************************************************************************************

1.手写行轨迹串，格式是: x1,y1,x2,y2,-1,0, x3,y3,x4,y4,-1,0,-1,-1 每一笔写完以: -1,0结尾。全部写完以：-1,-1结尾。实测下来最后不加-1,-1也可以。

2.目前现阶段无论选择题还是填空题，都可以使用realizeLine方法做识别，参数固定使用LANG_CHNS，这样既可识别单字符中英文，也可识别多字符中英文。

**********************************************************************************************************************************************************