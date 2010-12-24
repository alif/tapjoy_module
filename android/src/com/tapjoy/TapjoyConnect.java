package com.tapjoy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * This Class will notify the Tapjoy server whenever a device is running your application.
 * This is done automatically when you call the TapjoyConnect.getTapjoyConnectInstance method.   
 * Tapjoy needs this information in order to correctly process all of your paid installs 
 * (if you are paying for installs, and for enabling all the other features).
 *
 *<pre>
 *<b>How to connect to the Tapjoy server:</b>
 *
Step 1: In your application's first Activity class, add the following line of code:
	TapjoyConnect.getTapjoyConnectInstance(getApplicationContext());

Step 2: When your application exits, add the following line of code:
	TapjoyConnect.getTapjoyConnectInstance(getApplicationContext()).finalize();
</pre>
 */

public final class TapjoyConnect
{ 
	private ConnectTask connectTask = null;
	private Context context = null;
	
	private static TapjoyConnect tapjoyConnectInstance = null;
	
	// URL parameter variables.
	private String deviceID = "";
	private String deviceName = "";
	private String deviceType = "";
	private String deviceOSVersion = "";
	private String deviceCountryCode = "";
	private String deviceLanguage = "";
	private String appID = "";
	private String appVersion = "";
	private String libraryVersion = "";
	
	private String clientPackage = "";
	private String urlParams = "";	
	private String referralURL = "";
	
	// URL parameter names.
	final String TJC_DEVICE_ID_NAME					= "udid";				// Unique ID of the device (IMEI or MEID).
	final String TJC_DEVICE_NAME					= "device_name";		// This is the specific device name (iPod touch 2G, iPhone 3GS, ...)
	final String TJC_DEVICE_TYPE_NAME				= "device_type";		// Platform type (Android, iPhone, iPad).
	final String TJC_DEVICE_OS_VERSION_NAME			= "os_version";			// Version of Android running.
	final String TJC_DEVICE_COUNTRY_CODE			= "country_code";		// Country code.
	final String TJC_DEVICE_LANGUAGE				= "language";			// Language code.
	final String TJC_APP_ID_NAME					= "app_id";				// App ID.
	final String TJC_APP_VERSION_NAME				= "app_version";		// App version.
	final String TJC_CONNECT_LIBRARY_VERSION_NAME	= "library_version";	// Tapjoy Connect version.
	
	final static String TJC_SERVICE_URL 			= "http://ws.tapjoyads.com/";
	final static String TJC_CONNECT_URL_PATH		= "connect?";
	final static String TJC_USERDATA_URL_PATH		= "get_vg_store_items/user_account?";
	final static String TJC_LIBRARY_VERSION_NUMBER	= "7.0.1";
	final static String TJC_DEVICE_PLATFORM_TYPE	= "android";
	
	final static String TAPJOY_CONNECT 				= "TapjoyConnect";
	final static String EMULATOR_DEVICE_ID 			= "emulatorDeviceId";
	final static String TAPJOY_PRIMARY_COLOR		= "tapjoyPrimaryColor";
	final static String TJC_PREFERENCE 				= "tjcPrefrences";
	final static String REFERRAL_URL 				= "InstallReferral";
	final static String CONTAINS_EXTERNAL_DATA		= "containsExternalData";

	final static String EXTRA_URL_BASE				= "URL_BASE";
	final static String EXTRA_URL_PARAMS			= "URL_PARAMS";
	final static String EXTRA_CLIENT_PACKAGE		= "CLIENT_PACKAGE";
	final static String EXTRA_PUBLISHER_ID			= "PUBLISHER_ID";
	
	// Virtual Goods related.
	//private int primaryColor;


	
	/**
	 * Connects to and notifies the Tapjoy server that this device is
	 * running your application.
	 * @param context the Activity Context
	 * @return singleton instance of TapjoyConnect 
	 */
	public static TapjoyConnect getTapjoyConnectInstance(Context context)
	{
		if (tapjoyConnectInstance == null)
			tapjoyConnectInstance = new TapjoyConnect(context);

		return tapjoyConnectInstance;
	}

	/**
	 * Takes the device and metadata info and formats them as parameters
	 * for use in the URL connection call.
	 * @param context the Activity Context 
	 */
	private TapjoyConnect(Context ctx)
	{
		context = ctx;
		
		// Populate the data for the URL parameters.
		initMetaData();
		
		// Construct the url parameters.
		urlParams += TJC_DEVICE_ID_NAME + "=" + deviceID + "&";
		urlParams += TJC_DEVICE_NAME + "=" + deviceName + "&";
		urlParams += TJC_DEVICE_TYPE_NAME + "=" + deviceType + "&";
		urlParams += TJC_DEVICE_OS_VERSION_NAME + "=" + deviceOSVersion + "&";
		urlParams += TJC_DEVICE_COUNTRY_CODE + "=" + deviceCountryCode + "&";
		urlParams += TJC_DEVICE_LANGUAGE + "=" + deviceLanguage + "&";
		urlParams += TJC_APP_ID_NAME + "=" + appID + "&";
		urlParams += TJC_APP_VERSION_NAME + "=" + appVersion + "&";
		urlParams += TJC_CONNECT_LIBRARY_VERSION_NAME + "=" + libraryVersion;
		
		Log.i(TAPJOY_CONNECT, "URL parameters: " + urlParams);
		
		connectTask = new ConnectTask();
		connectTask.execute();
	}
	
	
	/**
	 * Initialize data from the device information and the manifest file.
	 * This data is used in our URL connection to the Tapjoy server. 
	 */
	private void initMetaData()
	{
		PackageManager manager = context.getPackageManager();
		ApplicationInfo info;
		
		try 
		{
			info = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			
			if (info != null && info.metaData != null) 
			{
				// Get the app ID.
				String metaDataValue = info.metaData.getString("APP_ID");
				if (metaDataValue != null && !metaDataValue.equals(""))
				{
					appID = metaDataValue;
				}
				else
				{
					Log.e(TAPJOY_CONNECT, "APP_ID can't be empty.");
					return;
				}

				// Get the client package.
				metaDataValue = info.metaData.getString("CLIENT_PACKAGE");
				if (metaDataValue != null && !metaDataValue.equals("")) 
				{
					clientPackage = metaDataValue;
				}
				else
				{
					Log.e(TAPJOY_CONNECT, "CLIENT_PACKAGE is missing.");
					return;
				}
				
				// Get app version.
				PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
				appVersion = packageInfo.versionName;
				
				// Device platform.
				deviceType = TJC_DEVICE_PLATFORM_TYPE;
				
				// Get the device model.
				deviceName = android.os.Build.MODEL;
				
				// Get the Android OS Version.
				deviceOSVersion = android.os.Build.VERSION.RELEASE;
				
				// Get the device country and language code.
				deviceCountryCode = Locale.getDefault().getCountry();
				deviceLanguage = Locale.getDefault().getLanguage();
				
				// Tapjoy SDK Library version.
				libraryVersion = TJC_LIBRARY_VERSION_NUMBER;
				
				SharedPreferences settings = context.getSharedPreferences(TJC_PREFERENCE, 0);
				
				// Get the device ID.
				metaDataValue = info.metaData.getString("DEVICE_ID");
				
				// Does the device ID exist in the manifest?
				if (metaDataValue != null && !metaDataValue.equals(""))
				{
					deviceID = metaDataValue;
					Log.i(TAPJOY_CONNECT, "Using manifest device id = "+deviceID);
				}
				else
				{
					TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					
					if (telephonyManager != null)
					{
						deviceID = telephonyManager.getDeviceId().toLowerCase();
						
						// Is the device ID null or empty?
						if (deviceID == null || deviceID.length() == 0)
						{
							Log.e(TAPJOY_CONNECT, "Device id is null or empty.");
						}
						else
						{
							try
							{
								Integer devTag =  Integer.parseInt(deviceID);
								StringBuffer buff = new StringBuffer();
								buff.append("EMULATOR");
								
								// Is the device ID zero?  This is probably an emulator then.
								if (devTag.intValue() == 0)
								{
									String deviceId = settings.getString(EMULATOR_DEVICE_ID, null);
									
									if( deviceId != null && !deviceId.equals(""))
									{
										deviceID = deviceId;
									}
									else
									{
										String constantChars = "1234567890abcdefghijklmnopqrstuvw";
										for (int i=0 ; i < 32; i++)
										{
											int randomChar = (int) ( Math.random()* 100) ;
											int ch = randomChar % 30;
											buff.append(constantChars.charAt(ch));
										}
										
										deviceID = buff.toString().toLowerCase();

										SharedPreferences.Editor editor = settings.edit();
										editor.putString(EMULATOR_DEVICE_ID, deviceID);
										editor.commit();
									}
								}
							}
							catch(NumberFormatException ex)
							{
								
							}
						}
					} 
					else
					{
						deviceID = null;
					}
				}
				
				//primaryColor = settings.getInt(TAPJOY_PRIMARY_COLOR, 0);
				
				// Get the referral URL
				String tempReferralURL = settings.getString(REFERRAL_URL, null);
				if (tempReferralURL != null && !tempReferralURL.equals(""))
					referralURL = tempReferralURL;
				
				Log.i(TAPJOY_CONNECT, "Metadata successfully loaded");
				
				Log.i(TAPJOY_CONNECT, "APP_ID = [" + appID + "]");
				Log.i(TAPJOY_CONNECT, "CLIENT_PACKAGE = [" + clientPackage + "]");
				
				Log.i(TAPJOY_CONNECT, "deviceID: [" + deviceID + "]");
				Log.i(TAPJOY_CONNECT, "deviceName: [" + deviceName + "]");
				Log.i(TAPJOY_CONNECT, "deviceType: [" + deviceType + "]");
				Log.i(TAPJOY_CONNECT, "libraryVersion: [" + libraryVersion + "]");
				Log.i(TAPJOY_CONNECT, "deviceOSVersion: [" + deviceOSVersion + "]");
				
				Log.i(TAPJOY_CONNECT, "COUNTRY_CODE: [" + deviceCountryCode + "]");
				Log.i(TAPJOY_CONNECT, "LANGUAGE_CODE: [" + deviceLanguage + "]");
				
				Log.i(TAPJOY_CONNECT, "referralURL: [" + referralURL + "]");
				
				//Log.i(TAPJOY_CONNECT, "primaryColor: [" + Integer.toHexString(primaryColor) + "]");
			}
			else
			{
				Log.e(TAPJOY_CONNECT, "Add APP_ID to AndroidManifest.xml file. For more detail integration document.");
			}

		}
		catch (NameNotFoundException e) 
		{
			Log.e(TAPJOY_CONNECT, "Add APP_ID to AndroidManifest.xml file. For more detail integration document.");
		}
	}

	
	/**
	 * Helper class to perform the connect call in the background.
	 */
	private class ConnectTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			return connect();
		}
	}

	
	/**
	 * Performs the URL connection to the Tapjoy server.
	 * @return true if the connect call was successful, false otherwise
	 */
	private boolean connect()
	{
		// referralURL contains both referrer parameter and its value e.g. referralURL="referrer=com.tapjoy.tapX"
		if (!referralURL.equals(""))
		{
			// Pass referral parameters to connect call to tapjoy server
			urlParams = urlParams + "&" + referralURL;
		}

		BufferedInputStream is = null;
		String url = TJC_SERVICE_URL + TJC_CONNECT_URL_PATH + urlParams;
		
		try
		{
			url = url.replaceAll(" ", "%20");

			Log.i(TAPJOY_CONNECT, url);

			URL connectURL = new URL(url);
			URLConnection urlConnection = connectURL.openConnection();
			urlConnection.setConnectTimeout(15000);	// 15 seconds
			urlConnection.setReadTimeout(30000);	// 15 seconds
			InputStream stream = urlConnection.getInputStream();
			is = new BufferedInputStream(stream);
		}
		catch (SocketTimeoutException e)
		{
			Log.e(TAPJOY_CONNECT, "Network requuest time out.");
		}
		catch (MalformedURLException e)
		{
			Log.e(TAPJOY_CONNECT, "Fail to access URL ["+url+"]");
		}
		catch (IOException e)
		{
			Log.e(TAPJOY_CONNECT, "Fail duering IO operation");
		}
		
		if (is != null)
		{			
			return buildResponse(is, context);
		}
		else
		{
			Log.e(TAPJOY_CONNECT,"Fail to connect to tapjoy site.");
		}

		return false;
	}

	
	
	/**
	 * Parses the response from the server and determines whether the
	 * connection was successful.
	 * @param is InputStream received from the server.
	 * @param ctx the Activity Context
	 * @return true if the response is valid and successful, false otherwise
	 */
	private boolean buildResponse(InputStream is, Context ctx)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try 
		{
			documentBuilder = factory.newDocumentBuilder();
			Document document = documentBuilder.parse(is);
			
			String nodeValue = getNodeTrimValue(document.getElementsByTagName("Success"));
			
			// Successful response.
			if (nodeValue!= null && nodeValue.equals("true"))
			{
				Log.i(TAPJOY_CONNECT,"Successfully connected to tapjoy site.");
				
//				// Check if we're getting tap points.
//				String pointsTotal = getNodeTrimValue(document.getElementsByTagName("TapPoints"));
//				String currencyName = getNodeTrimValue(document.getElementsByTagName("CurrencyName"));
//				
//				// Was there tap points/currency data in the response?
//				if (pointsTotal != null && currencyName != null)
//				{
//					//Log.i(TAPJOY_CONNECT, "currencyName: " + currencyName);
//					//Log.i(TAPJOY_CONNECT, "pointsTotal: " + pointsTotal);
//					
//					// Trigger the notifier to fire back.
//					tapjoyNotifier.getUpdatePoints(currencyName, Integer.parseInt(pointsTotal));
//				}
				
				return true;
			}
			else
			{
				Log.i(TAPJOY_CONNECT,"Fail to connect to tapjoy site.");
			}
		}
		catch (ParserConfigurationException e) 
		{
			Log.e(TAPJOY_CONNECT, "Connect Handler fail to parse invalid xml.");
		}
		catch (SAXException e)
		{
			Log.e(TAPJOY_CONNECT, "Connect Handler fail to parse invalid xml.");
		}
		catch (IOException e)
		{
			Log.e(TAPJOY_CONNECT, "Connect Handler fails.");
		}

		return false;
	}

	
	/**
	 * Helper class to help parse the server response.
	 * @param nodeList
	 * @return
	 */
	private String getNodeTrimValue(NodeList nodeList)
	{
		Element element = (Element) nodeList.item(0);
		String nodeValue = "";
		if (element != null)// if fetch node tag name is invalid
		{
			NodeList itemNodeList = element.getChildNodes();

			int length = itemNodeList.getLength();
			for (int i = 0; i < length; i++)
			{
				Node node = ((Node) itemNodeList.item(i));
				if (node != null)
					nodeValue += node.getNodeValue();
			}

			if (nodeValue != null && !nodeValue.equals(""))
			{
				return nodeValue.trim();
			}
			else
			{
				return null;
			}
		}
		return null;
	}

	
	/**
	 * Releases the TapjoyConnectInstance object. This must be called whenever the application exits.
	 */
	public void finalize()
	{
		tapjoyConnectInstance = null;
		Log.i(TAPJOY_CONNECT, "Cleaning resources.");
	}

	
}
