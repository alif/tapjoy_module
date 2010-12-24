//
//  TapjoyConnect.h
//
//  Created by Tapjoy.
//  Copyright 2010 Tapjoy.com All rights reserved.


/*! \mainpage Tapjoy Connect User Guide
 *
 *
 * \section intro_sec Introduction
 * By following this guide, integrating the Tapjoy Connect SDK into your application can easily be done in a few minutes.
 *
 *
 * \n
 * \section integration_sec SDK Integration
 * After downloading and unzipping the Tapjoy Connect SDK, you should see the following folders and files:
 *
 * \image html file_view.png fig. 1
 *
 * The EasyApp folder contains a sample project that demonstrates the Tapjoy Connect integration. You’ll be performing the following steps in your own application.
 *
 *
 * To add the files to your project, right click the project icon in your project explorer in Xcode, then select Add -> Existing Files.. -> select TapjoyConnect Folder -> Add. A dialogue will appear. Make sure your dialogue looks like the following:
 *
 * \image html xcode_prompt.png fig. 2
 *
 * Next, you will need to get your credentials for you application. In order for Tapjoy to work, you need to include the appId, appPassword, and appVersion. You can find this data by logging in to your account at http://www.tapjoy.com/ and clicking Partner Home at the top of the page.  Find your application in the list (or click Add an Application if it isn't there).  Click on the application icon and find the Integrate tab in order to retrieve this information:
 *
 * \image html app_dashboard.png fig. 3
 *
 * Now that you have the required info, go back to Xcode and add the following line of code to the applicationDidFinishLaunching method in your application’s delegate class file:
 * \n \c [[TapjoyConnect requestTapjoyConnectWithAppId:@"appID" WithPassword:@"appPassword " WithVersion:@"appVersion"] connect];
 *
 * For this example, it will look like the following in Xcode:
 *
 * \image html connect_code.png fig. 4
 *
 * \n
 * Congratulations! You are successfully connected with Tapjoy Connect.
 * In 2-3 hours you should see at least 1 new user in your http://tapjoy.com/ dashboard.  You will also see a success message in your Console.
 * Make sure to email support@tapjoy.com as soon as this version of your app is live in the app store, so that your account can be activated.
 *
 */
#import <UIKit/UIKit.h>


#define TJC_CONNECT_SUCCESS					@"TJC_Connect_Success"
#define TJC_CONNECT_FAILED					@"TJC_Connect_Failed"

#define TJC_TAP_POINTS_RESPONSE_NOTIFICATION	@"TJC_TAP_POINTS_RESPONSE_NOTIFICATION"
#define TJC_FEATURED_APP_RESPONSE_NOTIFICATION	@"TJC_FEATURED_APP_RESPONSE_NOTIFICATION"

#define TJC_AVAILABLE_ITEMS_RESPONSE_NOTIFICATION @"TJC_AVAILABLE_ITEMS_RESPONSE_NOTIFICATION"
#define TJC_AVAILABLE_ITEMS_RESPONSE_NOTIFICATION_ERROR @"TJC_AVAILABLE_ITEMS_RESPONSE_NOTIFICATION_ERROR"

#define TJC_SERVICE_URL						@"https://ws.tapjoyads.com/"
#define TJC_SERVICE_URL_ALTERNATE			@"https://ws1.tapjoyads.com/"

#define TJC_DEVICE_TAG_NAME					@"udid"			/*!< The unique device identifier. */
#define TJC_DEVICE_NAME						@"device_name"	/*!< This is the specific device name ("iPhone1,1", "iPod1,1"...) */
#define TJC_DEVICE_TYPE_NAME				@"device_type"	/*!< The model name of the device. This is less descriptive than the device name. */
#define TJC_DEVICE_OS_VERSION_NAME			@"os_version"	/*!< The device system version. */
#define TJC_DEVICE_COUNTRY_CODE				@"country_code"	/*!< The country code is retrieved from the locale object, from user data (not device). */
#define TJC_DEVICE_LANGUAGE					@"language_code"/*!< The language is retrieved from the locale object, from user data (not device). */
#define TJC_DEVICE_LAD						@"lad"			/*!< Little Alien Dude. */
#define TJC_APP_ID_NAME						@"app_id"		/*!< The application id is set by the developer, and is a unique id provided by Tapjoy. */
#define TJC_APP_VERSION_NAME				@"app_version"	/*!< The application version is retrieved from the application plist file, from the bundle version. */
#define TJC_CONNECT_LIBRARY_VERSION_NAME	@"library_version"	/*!< The library version is the SDK version number. */
#define TJC_LIBRARY_VERSION_NUMBER			@"7.3.0"		/*!< The SDK version number. */


/*!	\interface TapjoyConnect
 *	\brief The Tapjoy Connect Main class.
 *
 */
@interface TapjoyConnect :  NSObject
#if __IPHONE_4_0
<NSXMLParserDelegate>
#endif
{
@private
	NSString *appId_;				/*!< The application ID unique to this app. */
	NSData *data_;					/*!< Holds data for any data that comes back from a URL request. */
	NSURLConnection *connection_;	/*!< Used to provide support to perform the loading of a URL request. Delegate methods are defined to handle when a response is recieve with associated data. This is used for asynchronous requests only. */
	NSTimeInterval timeStamp_;		/*!< Contains the current time when a URL request is made. This value is used to help generate a unique md5sum key. */
	NSString *currentXMLElement_;	/*!< Contains @"Success when a connection is successfully made, nil otherwise. */
	int connectAttempts_;			/*!< The connect attempts is used to determine whether the alternate URL will be used. */
	BOOL isInitialConnect_;			/*!< Used to keep track of an initial connect call to prevent multiple repeated calls. */
}

@property (nonatomic,readonly) NSString* appId;
@property (nonatomic) BOOL isInitialConnect;


/*!	\fn requestTapjoyConnectWithAppId:(NSString*)appId
 *	\brief This method is called to initialize the TapjoyConnect system.
 *
 * This method should be called upon app delegate initialization in the applicationDidFinishLaunching method.
 *	\param appId The application ID.
 *	\return The globally accessible #TapjoyConnect object.
 */
+ (TapjoyConnect*) requestTapjoyConnectWithAppId:(NSString*)appId;

/*!	\fn sharedTapjoyConnect
 *	\brief Retrieves the globally accessible #TapjoyConnect singleton object.
 *
 *	\param n/a
 *	\return The globally accessible #TapjoyConnect singleton object.
 */
+ (TapjoyConnect*) sharedTapjoyConnect;

/*!
 *	\brief Simple check to detect jail broken devices/apps.
 *
 * Note that this is NOT guaranteed to be accurate! There are very likely going to be ways to circumvent this check in the future.
 *	\param n/a
 *	\return YES for indicating that the device/app has been jailbroken, NO otherwise.
 */ 
- (BOOL) isJailBroken;

/*!
 *	\brief Simple check to detect jail broken devices/apps.
 *
 * Note that this is NOT guaranteed to be accurate! There are very likely going to be ways to circumvent this check in the future.
 *	\param n/a
 *	\return A string "YES" for indicating that the device/app has been jailbroken, "NO" otherwise.
 */ 
- (NSString*) isJailBrokenStr;







#pragma mark Deprecated Methods

/*!	\fn requestTapjoyConnectWithAppId:(NSString*)appId
 *	\brief This method is called to initialize the TapjoyConnect system.
 *
 * This method should be called upon app delegate initialization in the applicationDidFinishLaunching method.
 *	\param appId The application ID.
 *	\param password The application password.
 *	\param version The application version.
 *	\return The globally accessible #TapjoyConnect object.
 */
+ (TapjoyConnect*) requestTapjoyConnectWithAppId:(NSString*)appId WithPassword:(NSString*)password WithVersion:(NSString*)version	__deprecated;

/*!	\fn connect
 *	\brief Initiates a URL request with the application identifier data.
 *	\deprecated Updated for version 7.0.0. Do not use this method, requestTapjoyConnectWithAppId will automatically initiate a URL request.
 *	\param n/a
 *	\return n/a
 */
- (void) connect	__deprecated;







#pragma mark TapjoyConnect NSXMLParser delegate methods
- (void) startParsing:(NSData*) myData;
- (NSMutableDictionary*) genericParameters;
- (NSString*) createQueryStringFromDict:(NSDictionary*) paramDict;

@end
