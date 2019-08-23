package com.ocwvar.xlocker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 崩溃处理器
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

	private final String savePath;
	private Context context;

	public CrashHandler( Context context ) {
		this.savePath = Environment.getExternalStorageDirectory().getPath() + "/.xcl/";
		this.context = context.getApplicationContext();
	}

	@Override
	@SuppressLint ("SimpleDateFormat")
	public void uncaughtException( Thread t, Throwable e ) {
		showTip();

		//生成文件名以及文件对象
		final String timeString = new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss" ).format( new Date( System.currentTimeMillis() ) );
		final String fileName = context.getPackageName() + "_" + timeString;
		File logFile = new File( savePath + fileName + ".log" );

		//创建文件对象
		try {
			logFile.delete();
			if ( !logFile.createNewFile() ) {
				logFile = null;
			}
		} catch ( Exception ignore ) {
			logFile = null;
		}

		//判断是否满足条件
		if ( logFile == null || !logFile.exists() || !logFile.canWrite() || !logFile.canRead() ) {
			return;
		}

		final StringBuilder stringBuilder = new StringBuilder();
		//添加应用参数
		stringBuilder.append( "应用包名:" ).append( context.getPackageName() ).append( "\n" );

		try {
			final PackageInfo packageInfo = this.context.getPackageManager().getPackageInfo( context.getPackageName(), PackageManager.GET_CONFIGURATIONS );
			stringBuilder.append( "版本号:" ).append(packageInfo.versionCode).append( "\n" );
			stringBuilder.append( "版本名称:" ).append( packageInfo.versionName ).append( "\n" );
		} catch ( Exception ignore ) {
		}

		//添加版本相关参数
		stringBuilder.append( "Android系统版本:" ).append( Build.VERSION.SDK_INT ).append( "\n" );
		stringBuilder.append( "应用总可用RAM:" ).append( getRAMSize( Runtime.getRuntime().maxMemory() ) ).append( "\n" );
		stringBuilder.append( "CPU ABI:" ).append( Build.CPU_ABI ).append( "\n" );
		stringBuilder.append( "CPU ABI2:" ).append( Build.CPU_ABI2 ).append( "\n" );

		stringBuilder.append( "===============================================" ).append( "\n" );
		stringBuilder.append( "异常类型:" ).append( e.getCause() ).append( "\n" );
		stringBuilder.append( "异常线程:" ).append( t.getName() ).append( "\n" );
		stringBuilder.append( "异常信息:" ).append( e.getMessage() ).append( "\n" );
		stringBuilder.append( "本地化信息:" ).append( e.getLocalizedMessage() ).append( "\n" );

		final StackTraceElement[] logs = e.getStackTrace();
		if ( logs != null && logs.length > 0 ) {
			stringBuilder.append( "==================Base Exception=================" ).append( "\n" );
			for ( final StackTraceElement stackTraceElement : logs ) {
				stringBuilder.append( stackTraceElement ).append( "\n" );
			}
		}

		final Throwable courseThrowable = e.getCause();
		if ( courseThrowable != null && courseThrowable.getStackTrace() != null ) {
			stringBuilder.append( "==================Cause Exception=================" ).append( "\n" );
			for ( final StackTraceElement stackTraceElement : courseThrowable.getStackTrace() ) {
				stringBuilder.append( stackTraceElement ).append( "\n" );
			}
		}

		try {
			//得到字节流
			final byte[] data = stringBuilder.toString().getBytes( Charset.forName( "UTF-8" ) );

			//写入文件
			try {
				final FileOutputStream outputStream = new FileOutputStream( logFile, false );
				outputStream.write( data );
				outputStream.flush();
				outputStream.close();
				Thread.sleep( 5000L );
			} catch ( Exception ignore ) {
			}

		} catch ( Exception ignore ) {
		}

		//重启APP
		showTip();

		//关闭所有页面
		android.os.Process.killProcess( android.os.Process.myPid() );
		System.exit( 1 );
	}

	private String getRAMSize( long value ) {
		double temp = value;
		temp = temp / 1024d / 1024d;
		return String.valueOf( temp ) + " MB";
	}

	/**
	 * 显示一个提示给用户
	 */
	private void showTip() {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText( context, "x2L0cKeR 出现异常,日志已生成", Toast.LENGTH_LONG ).show();
				Looper.loop();
			}
		}.start();
	}

}
