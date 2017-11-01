package com.formatrix.techpoint.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;


// requires android.permission.WRITE_EXTERNAL_STORAGE
public final class DownloadUtility
{
	private DownloadUtility() {}


	public static void downloadFile(Context context, String url, String fileName)
	{
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

//			here is the title of the file download progress
			request.setTitle("File Download");

//			here is the description of the file progress
			request.setDescription("File is being downloaded.....");
			
			// set download directory
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

//			*********************************************************************************************************************
//			this is probably how to make a download folder in the memory card
//			File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"Fusion");
//			if (!directory.exists()){
//				directory.mkdirs();
//			}
//			request.setDestinationInExternalPublicDir(String.valueOf(directory), fileName);

//			*********************************************************************************************************************



//			// when downloading music and videos they will be listed in the player
			request.allowScanningByMediaScanner();
			
			// notify user when download is completed
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			
			// start download
			DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
		}
		else
		{
			try
			{
				if(url!=null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					context.startActivity(intent);
				}
			}
			catch(android.content.ActivityNotFoundException e)
			{
				// can't start activity
			}
		}
	}


	public static boolean isDownloadableFile(String url)
	{
		int index = url.indexOf("?");
		if(index>-1)
		{
			url = url.substring(0, index);
		}
		url = url.toLowerCase();

		for(String type : Utils.DOWNLOAD_FILE_TYPES)
		{
			if(url.endsWith(type)) return true;
		}

		return false;
	}


	public static String getFileName(String url)
	{
		int index = url.indexOf("?");
		if(index>-1)
		{
			url = url.substring(0, index);
		}
		url = url.toLowerCase();

		index = url.lastIndexOf("/");
		if(index>-1)
		{
			return url.substring(index+1, url.length());
		}
		else
		{
			return Long.toString(System.currentTimeMillis());
		}
	}
}
