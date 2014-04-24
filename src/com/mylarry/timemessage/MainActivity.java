package com.mylarry.timemessage;

import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TimePicker.OnTimeChangedListener;

public class MainActivity extends Activity implements OnItemClickListener,OnClickListener{
	AutoCompleteTextView act;
	String sendTimeStr;
	ProgressDialog myDialog ;
	 Dialog alertDialog;
	 
	 Calendar sendTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		act=(AutoCompleteTextView)this.findViewById(R.id.autoCompleteTextView1);
        act.setThreshold(1);  //设置输入一个字符 提示，默认为2  
        act.setOnItemClickListener(this);  
        
		TimePicker tp =  (TimePicker)this.findViewById(R.id.timePicker);
		tp.setOnTimeChangedListener(new OnTimeChangedListener(){

	          public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
	        	  hour=hourOfDay;
	        	  min=minute;
	          }
	            
	    });
        
		
        new MyThread().start();
        myDialog = ProgressDialog.show(this, "正在获取通讯录数据..", "获取中,请稍后..", true, true); 
	}
	
	class MyThread extends Thread{
		public void run(){
			getPhoneContacts();
			handler.sendEmptyMessage(0);
		}
	}
	
	public Handler handler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			PhoneAdapter mAdapter = new PhoneAdapter(contactsList, getApplicationContext());  
			act.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
			myDialog.dismiss();
		}
		
	};
	
	public void onItemClick(AdapterView<?> parent, View view, int position,  
            long id) {  
          
        PhoneContact pc = (PhoneContact)act.getAdapter().getItem(position);//contactsList.get(position);  
        act.setText(pc.ContactsNumber);  
    }  
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("onresume");
		LinearLayout message_edit=(LinearLayout)this.findViewById(R.id.message_edit);
		EditText message=(EditText)this.findViewById(R.id.message);
		
		Button sendButton=(Button)this.findViewById(R.id.send_button);
		int buttonWidth=sendButton.getWidth();
		System.out.println("buttonWidth="+buttonWidth);
		DisplayMetrics metrics = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		LinearLayout.LayoutParams message_para = new LinearLayout.LayoutParams(metrics.widthPixels*4/5, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		message_edit.removeAllViews();
		message_edit.addView(message, 0, message_para);
		message.setVisibility(View.VISIBLE);
		
		LinearLayout.LayoutParams button_para = new LinearLayout.LayoutParams(metrics.widthPixels/5, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		message_edit.addView(sendButton, 1, button_para);
		
		DatePicker dp = (DatePicker)this.findViewById(R.id.datePicker);
		dp.setCalendarViewShown(false);
		
		sendButton.setOnClickListener(this);
		
		
		Button clearButton = (Button)this.findViewById(R.id.clear_button);
		clearButton.setOnClickListener(this);
	}



	public int setNotifyTime(final Calendar sendTime){
		
        Calendar c = Calendar.getInstance();
        
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE) ;
        
        if(sendTime.before(c))
        {
        	return 0;
        }
        System.out.println(year+" "+month+" "+day+" "+hour+" "+minute);
        
        
        year = sendTime.get(Calendar.YEAR);
        month = sendTime.get(Calendar.MONTH);
        day = sendTime.get(Calendar.DAY_OF_MONTH);
        
        hour = sendTime.get(Calendar.HOUR_OF_DAY);
        minute = sendTime.get(Calendar.MINUTE) ;
        sendTimeStr=year+"/"+(month+1)+"/"+day+" "+hour+":"+minute;
        System.out.println("sendTime  "+year+" "+month+" "+day+" "+hour+" "+minute);
        
        long interval = getMills(sendTime);	
        EditText message=(EditText)this.findViewById(R.id.message);
        
        
        SharedPreferences settings = this.getSharedPreferences("SendMessage", 0);
        String phone_number =null; 
        String sms_content = null;
        phone_number = settings.getString("sendToPhoneNumber", "");
        sms_content = settings.getString("sendMessage", "");
        
        if(!phone_number.equals("")&&!sms_content.equals("")){
        	return -3;
        }
        
        if(message.getText().toString().equals(""))
        {
        	return -1;
        }
        if(act.getText().toString().equals(""))
        {
        	return -2;
        }
        
        this.sendTime=sendTime;
        
        
		 alertDialog = new AlertDialog.Builder(this).
				    setTitle("提示").setMessage("信息将于"+sendTimeStr+"发送到手机:"+act.getText().toString()).
				    setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							timeSendMessage(sendTime);
							
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					}).
				    create();
        
		alertDialog.show();
        return 1;
	}
	
	private void timeSendMessage(Calendar sendTime){
		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent("android.alarm.demo.action");
        PendingIntent sender = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
        am.set(AlarmManager.RTC_WAKEUP, sendTime.getTimeInMillis(), sender);
        
        SharedPreferences settings = this.getSharedPreferences("SendMessage", 0);
        SharedPreferences.Editor localEditor = settings.edit();
        localEditor.putString("sendToPhoneNumber", act.getText().toString());
        
        EditText message=(EditText)this.findViewById(R.id.message);
        localEditor.putString("sendMessage",message.getText().toString());
        localEditor.putLong("time", sendTime.getTimeInMillis());
        
        localEditor.commit();
        
        message.setText("");
        act.setText("");
	}
	
	private long getMills(Calendar future){
		long mills=0;
		Calendar c = Calendar.getInstance();
		mills=future.getTimeInMillis()-c.getTimeInMillis();
		return mills;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	ArrayList<PhoneContact> contactsList=new ArrayList<PhoneContact>();
	
	private void getPhoneContacts() {  
	   ContentResolver resolver = this.getContentResolver();  
		    
		   // 获取手机联系人  
	   Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);  
	    
	    
	   if (cursor != null) {  
	       while (cursor.moveToNext()) {  
	    
	       String contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));  
	       
	       String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
	         
	       Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
		       String PhoneNumber = null;
		       if(phone.moveToNext()){
		    	   PhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		    	   phone.close();
		       }
		       else continue;

		       PhoneContact contact=new PhoneContact();
		       contact.contactsName=contactName;
		       contact.ContactsNumber=PhoneNumber;
		       contactsList.add(contact);
		       }  
		    
		       cursor.close();  
		   }  
	   }

	private int hour;
	private int min;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.send_button)
		{
			
			
			DatePicker dp = (DatePicker)this.findViewById(R.id.datePicker);
			Calendar future=Calendar.getInstance();
			future.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), hour, min);
			
			int ret= setNotifyTime(future);
			if(ret==0)
			{
				Dialog alertDialog = new AlertDialog.Builder(this).
					    setTitle("错误的时间").
					    setMessage("请输入晚于现在的时间").setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).
					    create();
				alertDialog.show();
			}
			else if(ret==-1)
			{
				Dialog alertDialog = new AlertDialog.Builder(this).
					    setTitle("请输入要发送的信息").
					    setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).
					    create();
				alertDialog.show();
			}else if(ret==-2)
			{
				Dialog alertDialog = new AlertDialog.Builder(this).
					    setTitle("请输入对方手机号").
					    setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).
					    create();
				alertDialog.show();
			}else if(ret==-3)
			{
				Dialog alertDialog = new AlertDialog.Builder(this).
					    setTitle("提示").setMessage("上次输入的信息并未发送，是否覆盖之前的信息？").
					    setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								
								MainActivity.this. alertDialog = new AlertDialog.Builder(MainActivity.this).
										    setTitle("提示").setMessage("信息将于"+sendTimeStr+"发送到手机:"+act.getText().toString()).
										    setPositiveButton("确定", new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													// TODO Auto-generated method stub
													dialog.dismiss();
													timeSendMessage(sendTime);
													
												}
											}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													// TODO Auto-generated method stub
													dialog.dismiss();
												}
											}).
										    create();
								MainActivity.this.alertDialog.show();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).
					    create();
				alertDialog.show();
			}
		}
		else if(v.getId()==R.id.clear_button){
			final SharedPreferences sendMessage = getSharedPreferences("SendMessage", 0);
			
			
			final String phone_number=sendMessage.getString("sendToPhoneNumber", "");
			final String message = sendMessage.getString("sendMessage", "");
			final long time = sendMessage.getLong("time", 0);
			
			Calendar sendTime = Calendar.getInstance();
			sendTime.setTimeInMillis(time);
			int year = sendTime.get(Calendar.YEAR);
	        int month = sendTime.get(Calendar.MONTH);
	        int day = sendTime.get(Calendar.DAY_OF_MONTH);
	        
	        int hour = sendTime.get(Calendar.HOUR_OF_DAY);
	        int minute = sendTime.get(Calendar.MINUTE) ;
			
			String tempsendTimeStr=year+"/"+(month+1)+"/"+day+" "+hour+":"+minute;
			if(phone_number.equals(""))
			{
				Dialog alertDialog = new AlertDialog.Builder(this).
					    setTitle("提示").setMessage("没有要发送的数据").
					    setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).
					    create();
				alertDialog.show();
			}else {
				Dialog alertDialog = new AlertDialog.Builder(this).
					    setTitle("提示").setMessage("以下信息将于"+tempsendTimeStr+"发送到手机："+phone_number+"\n["+message+"]").
					    setPositiveButton("保留", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).setNegativeButton("清除", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								SharedPreferences.Editor localEditor = sendMessage.edit();
					            
					            localEditor.putString("sendToPhoneNumber", "");
					            localEditor.putString("sendMessage", "");
					            localEditor.commit();
					            AlarmManager am = (AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
					            
					            Intent intent = new Intent("android.alarm.demo.action");
					            PendingIntent sender = PendingIntent.getBroadcast(
					            		MainActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					            am.cancel(sender);
							}
						}).
					    create();
				alertDialog.show();
			}
			

		}
	} 
}
class PhoneContact {
	public String contactsName;
	public String ContactsNumber;
}
