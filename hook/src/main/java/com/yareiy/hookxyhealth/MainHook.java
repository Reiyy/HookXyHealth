package com.yareiy.hookxyhealth;

import android.app.Activity;
import android.view.View;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

@SuppressWarnings("RedundantThrows")
public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
       if ("com.dvn.mpcare".equals(lpparam.packageName)) {
            // 获取壳下的真类
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    ClassLoader classLoader = context.getClassLoader();
                    Method[] methods = MainHook.class.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName().startsWith("mod_") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == ClassLoader.class) {
                            method.setAccessible(true);
                            method.invoke(MainHook.this, classLoader);
                        }
                    }
                }
            });
        }
    }
    
    // private void mod_skipSplash(ClassLoader classLoader){
    //     // 立即进入主页
    //     XposedHelpers.findAndHookMethod("com.dvn.mpcare.ui.activity.SplashActivity",
    //             classLoader,
    //             "onCreate",
    //             android.os.Bundle.class,
    //             new XC_MethodHook() {
    //                 @Override
    //                 protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    //                     Activity splashActivity = (Activity) param.thisObject;
    //                     // 立即启动IndexActivity
    //                     Intent intent = new Intent();
    //                     intent.setClassName(splashActivity, "com.dvn.mpcare.ui.activity.IndexActivity");
    //                     splashActivity.startActivity(intent);
    //                     // 结束SplashActivity，阻止后续逻辑
    //                     splashActivity.finish();
    //                 }
    //             });
    // }

    // 移除首页多余选项
    private void mod_HideIndexButton(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod("com.dvn.mpcare.ui.activity.IndexActivity",
                classLoader,
                "q",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Activity activity = (Activity) param.thisObject;

                        // 动态获取资源ID
                        int rbHealthId = activity.getResources().getIdentifier(
                                "rb_index_health_radiobutton", "id", "com.dvn.mpcare"
                        );
                        int rbLoveId = activity.getResources().getIdentifier(
                                "rb_index_love_radiobutton", "id", "com.dvn.mpcare"
                        );
                        int rbCloudId = activity.getResources().getIdentifier(
                                "rb_index_cloud_radiobutton", "id", "com.dvn.mpcare"
                        );
                        int rbFriendId = activity.getResources().getIdentifier(
                                "rb_index_friend_radiobutton", "id", "com.dvn.mpcare"
                        );
                        int rbAdvisoryId = activity.getResources().getIdentifier(
                                "rb_index_advisory_radiobutton", "id", "com.dvn.mpcare"
                        );
                        int layoutScanId = activity.getResources().getIdentifier(
                                "layout_scan", "id", "com.dvn.mpcare"
                        );

                        // 隐藏多余按钮
                        activity.findViewById(rbHealthId).setVisibility(View.GONE);
                        activity.findViewById(rbLoveId).setVisibility(View.GONE);
                        activity.findViewById(rbCloudId).setVisibility(View.GONE);
                        activity.findViewById(rbFriendId).setVisibility(View.GONE);
                        activity.findViewById(layoutScanId).setVisibility(View.GONE);
                        activity.findViewById(rbAdvisoryId).setVisibility(View.GONE);
                    }
                });
    }

    // 跳过进入检测选项页面时的登录检查
    private void mod_skiplogin(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod("com.dvn.mpcare.ui.activity.base.BaseActivity",
                classLoader,
                "a",
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(false); // 强制允许跳转
                    }
                });
    }

    // 移除检测中心页面多余的检测项
    private void mod_HideCheckButton(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod("com.dvn.mpcare.ui.activity.check.MainCheckActivity",
                classLoader,
                "n",
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object activity = param.thisObject;
                // 获取 LinearLayout 对象，这里成员变量 l 存放检测图标的容器
                LinearLayout layout = (LinearLayout) XposedHelpers.getObjectField(activity, "l");
                if (layout != null) {
                    // 倒序遍历所有子View
                    for (int i = layout.getChildCount() - 1; i >= 0; i--) {
                        View child = layout.getChildAt(i);
                        // 如果该View的 id 不等于 3（血压检测项），则移除
                        if (child.getId() != 3) {
                            layout.removeViewAt(i);
                        }
                    }
                }
            }
        });
    }

    // 跳过检测中心页面的权限检查
    // private void mod_SkipPermissionChecking(ClassLoader classLoader){
    //     XposedHelpers.findAndHookMethod("com.dvn.mpcare.ui.activity.check.MainCheckActivity", 
    //         classLoader, "a", Object.class, new XC_MethodHook() {
    //         @Override
    //         protected void beforeHookedMethod(MethodHookParam param) {
    //             try {
    //                 Object csVar = param.args[1];
    //                 if (csVar != null) {
    //                     XposedHelpers.callMethod(csVar, "a");
    //                 }
    //                 param.setResult(null);
    //             } catch (Throwable e) {
    //                 XposedBridge.log("Hook failed: " + e);
    //             }
    //         }
    //     });
    // }


    // 跳过进入检测中心的登录和网络请求
    private void mod_skipLogin(ClassLoader classLoader){
        // XposedHelpers.findAndHookMethod(
        //     "com.dvn.mpcare.base.DvnApplication",
        //     classLoader,
        //     "A",
        //     new XC_MethodHook() {
        //         @Override
        //         protected void beforeHookedMethod(MethodHookParam param) {
        //             param.setResult(false); // 强制返回 false
        //         }
        //     }
        // );

        // 伪造用户信息
        XposedHelpers.findAndHookMethod(
            "com.dvn.mpcare.base.DvnApplication",
            classLoader,
            "j",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        // 创建伪造UserManagerItem对象
                        Class<?> userClass = XposedHelpers.findClass(
                            "com.dvn.mpcare.bean.member.UserManagerItem",
                            classLoader
                        );
                        Object fakeUser = XposedHelpers.newInstance(userClass);

                        XposedHelpers.setObjectField(fakeUser, "id", "233");
                        XposedHelpers.setObjectField(fakeUser, "name", "=w=");
                        XposedHelpers.setObjectField(fakeUser, "age", "42");
                        XposedHelpers.setObjectField(fakeUser, "height", "177");
                        XposedHelpers.setObjectField(fakeUser, "sex", "1");
                        XposedHelpers.setObjectField(fakeUser, "weight", "77");

                        // 替换DvnApplication的静态字段g
                        XposedHelpers.setStaticObjectField(
                            XposedHelpers.findClass(
                                "com.dvn.mpcare.base.DvnApplication",
                                classLoader
                            ),
                            "g",
                            fakeUser
                        );

                        // 强制返回伪造的用户
                        param.setResult(fakeUser);
                    } catch (Throwable e) {
                        XposedBridge.log("Hook failed: " + e);
                    }
                }
            }
        );

        // 跳过弹出补全信息提示
        XposedHelpers.findAndHookMethod("com.dvn.mpcare.ui.activity.check.MainCheckActivity", 
            classLoader, "l", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null);
                }
            });

        // 阻止数据获取请求
        Class<?> listenerClass = XposedHelpers.findClass("com.android.volley.Response$Listener", classLoader);
        Class<?> errorListenerClass = XposedHelpers.findClass("com.android.volley.Response$ErrorListener", classLoader);
        XposedHelpers.findAndHookMethod(
            "dt", 
            classLoader,
            "c",
            listenerClass, // Response.Listener
            errorListenerClass, // Response.ErrorListener
            String.class, // detectType (str)
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null);

                }
            }
        );
    }

    // 跳过获取用户信息
    // private void mod_skipGetUserInfoData(ClassLoader classLoader){




    // }


    // 跳过数据上传，并加载报告页面
    private void mod_JumpUploadData(ClassLoader classLoader){
        // 动态获取 Volley 相关类
        Class<?> listenerClass = XposedHelpers.findClass("com.android.volley.Response$Listener", classLoader);
        Class<?> errorListenerClass = XposedHelpers.findClass("com.android.volley.Response$ErrorListener", classLoader);

        XposedHelpers.findAndHookMethod(
            "dt", 
            classLoader,
            "a",
            listenerClass, // Response.Listener
            errorListenerClass, // Response.ErrorListener
            String.class, // detectType (str)
            String.class, // deviceType (str2)
            String.class, // dp (str3=O)
            String.class, // sp (str4=N)
            String.class, // ml (str5=P)
            String.class, // detectAt (str6)
            String.class, // remark (str7)
            String.class, // id (str8)
            String.class, // userId (str9)
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    XposedBridge.log("当前线程: " + Thread.currentThread().getName());
                   
                    try {
                        // 过滤非目标请求（detectType="6"）
                        if(!"6".equals(param.args[2])) return;

                        // 立即终止原网络请求
                        param.setResult(null);

                        // 获取原始测量值斌转换为String类型
                        String dpValue = (String)param.args[4]; // str3对应O字段
                        String spValue = (String)param.args[5]; // str4对应N字段
                        String mlValue = (String)param.args[6]; // str5对应P字段

                        // 初始化OtherReportResult
                        Class<?> resultClass = XposedHelpers.findClass(
                            "com.dvn.mpcare.bean.healthreport.OtherReportResult",
                            classLoader
                        );
                        Object fakeResponse = XposedHelpers.newInstance(resultClass);

                        // 初始化OtherReportBean
                        Class<?> beanClass = XposedHelpers.findClass(
                            "com.dvn.mpcare.bean.healthreport.OtherReportResult$OtherReportBean",
                            classLoader
                        );

                        // 非静态内部类构造函数需要外部类实例作为第一个参数
                        Constructor<?> constructor = beanClass.getDeclaredConstructor(resultClass);
                        constructor.setAccessible(true);
                        Object fakeBean = constructor.newInstance(fakeResponse);
                        
                        // 设置Bean字段
                        XposedHelpers.setObjectField(fakeBean, "dp", dpValue); 
                        XposedHelpers.setObjectField(fakeBean, "sp", spValue);
                        XposedHelpers.setObjectField(fakeBean, "ml", mlValue);
                        XposedHelpers.setObjectField(fakeBean, "status", "血压信息");
                        
                        // 设置Result字段
                        XposedHelpers.setObjectField(fakeResponse, "data", fakeBean);
                        XposedHelpers.setIntField(fakeResponse, "code", 2000);

                        // 触发回调
                        Object listener = param.args[0]; // 原始k0对象
                        XposedHelpers.callMethod(listener, "a", fakeResponse); // 原onResponse

                    } catch (Throwable e) {
                        // 错误处理
                        XposedBridge.log("[Hook Error] " + Log.getStackTraceString(e));
                    }
                }
            }
        );
    }   

    }






