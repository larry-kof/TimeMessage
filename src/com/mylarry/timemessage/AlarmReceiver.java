package com.mylarry.timemessage;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		 if ("android.alarm.demo.action".equals(intent.getAction())) {
			 System.out.println("dddddddddddddddddddddddd");
			 
			 String phone_number =null; 
             String sms_content = null;
             
             SharedPreferences sendMessage = arg0.getSharedPreferences("SendMessage", 0);

             phone_number = sendMessage.getString("sendToPhoneNumber", "");
             sms_content = sendMessage.getString("sendMessage", "");
             
             System.out.println("phone_number="+phone_number);
             System.out.println("sms_content="+sms_content);
             
             if(phone_number.equals("")) {
             } else {
                 SmsManager smsManager = SmsManager.getDefault();
                 if(sms_content.length() > 70) {
                     List<String> contents = smsManager.divideMessage(sms_content);
                     for(String sms : contents) {
                         smsManager.sendTextMessage(phone_number, null, sms, null, null);
                     }
                 } else {
                  smsManager.sendTextMessage(phone_number, null, sms_content, null, null);
                 }
             }
             
             Toast.makeText(arg0, "定时信息已被发送", Toast.LENGTH_LONG).show();
             SharedPreferences.Editor localEditor = sendMessage.edit();
             
             localEditor.putString("sendToPhoneNumber", "");
             localEditor.putString("sendMessage", "");
             localEditor.commit();
		 }else if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
             long time;
             
             SharedPreferences sendMessage = arg0.getSharedPreferences("SendMessage", 0);

             time =sendMessage.getLong("time", 0);
             Calendar c=Calendar.getInstance();
             
             if(time==0||time<c.getTimeInMillis())
             {
            	 
             }else{
            	AlarmManager am = (AlarmManager)arg0.getSystemService(Context.ALARM_SERVICE);
         		Intent intent2 = new Intent("android.alarm.demo.action");
                PendingIntent sender = PendingIntent.getBroadcast(
                		 arg0, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
                 
                am.set(AlarmManager.RTC_WAKEUP, time, sender);
             }
             
		 }
	}

}
