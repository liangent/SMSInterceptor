package com.liangent.smsinterceptor;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SMSInterceptor implements IXposedHookLoadPackage {

	public static final String PACKAGE_NAME = SMSInterceptor.class.getPackage()
			.getName();

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		SharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME);
		Set<String> packages = prefs.getStringSet(
				"packages",
				new HashSet<String>(Arrays.asList(new String[] {
						"com.carrot.carrotfantasy",
						"com.happyelements.AndroidAnimal" })));
		final String packageName = lpparam.packageName;
		if (!packages.contains(packageName)) {
			return;
		}
		XposedBridge.log("SMS: Known app " + packageName + " loaded.");

		findAndHookMethod(SmsManager.class, "sendTextMessage", String.class,
				String.class, String.class, PendingIntent.class,
				PendingIntent.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						String destinationAddress = (String) param.args[0];
						String scAddress = (String) param.args[1];
						String text = (String) param.args[2];
						PendingIntent sentIntent = (PendingIntent) param.args[3];
						PendingIntent deliveryIntent = (PendingIntent) param.args[4];
						param.setResult(null);

						XposedBridge.log("SMS: " + packageName + " => "
								+ destinationAddress + " @ " + scAddress + ": "
								+ text);

						if (sentIntent != null) {
							sentIntent.send(Activity.RESULT_OK);
						}
						if (deliveryIntent != null) {
							deliveryIntent.send();
						}
					}
				});
	}
}
