//
//  TapjoyConnect.m
//
//  Created by Tapjoy.
//  Copyright 2010 Tapjoy.com All rights reserved.
//

#import "TapjoyConnect.h"

static TapjoyConnect * _sharedInstance=nil; //To make TapjoyConnect Singleton
static NSString * orignalRequest = TJC_SERVICE_URL;

@implementation TapjoyConnect
	
@synthesize appId = appId_;
@synthesize isInitialConnect = isInitialConnect_;


#pragma mark GlobalMethods Required for Connect 
// Needed here to make connect a SINGLE FILE

- (NSMutableDictionary*) genericParameters
{
	// Device info.
	UIDevice *device = [UIDevice currentDevice];
	NSString *identifier = [device uniqueIdentifier];
	NSString *model = [device model];
	NSString *systemVersion = [device systemVersion];
	
	//NSString *device_name = [device platform];
	//NSLog(@"device name: %@", device_name);
	
	// Locale info.
	NSLocale *locale = [NSLocale currentLocale];
	NSString *countryCode = [locale objectForKey:NSLocaleCountryCode];
	NSString *language = [[NSLocale currentLocale] objectForKey: NSLocaleLanguageCode];	

	// App info.
	NSString *bundleVersion = [[NSBundle mainBundle] objectForInfoDictionaryKey:(NSString *)kCFBundleVersionKey];
	
	NSString *lad = [self isJailBrokenStr];
	
	NSMutableDictionary * genericDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
										 identifier, TJC_DEVICE_TAG_NAME,
										 model, TJC_DEVICE_TYPE_NAME,
										 //device_name, TJC_DEVICE_NAME,
										 systemVersion, TJC_DEVICE_OS_VERSION_NAME,
										 appId_, TJC_APP_ID_NAME,
										 bundleVersion, TJC_APP_VERSION_NAME,
										 TJC_LIBRARY_VERSION_NUMBER, TJC_CONNECT_LIBRARY_VERSION_NAME,
										 countryCode, TJC_DEVICE_COUNTRY_CODE,
										 language, TJC_DEVICE_LANGUAGE,
										 lad, TJC_DEVICE_LAD,
										 nil];
	
	return [genericDict autorelease];
	
}


static NSString *toString(id object) 
{
	return [NSString stringWithFormat: @"%@", object];
}


static NSString* urlEncode(id object) 
{
	NSString *string = toString(object);
	return [string stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
}


- (NSString*) createQueryStringFromDict:(NSDictionary*) paramDict
{
	if(!paramDict)
	{
		paramDict = [[TapjoyConnect sharedTapjoyConnect]genericParameters];
	}
	NSMutableArray *parts = [NSMutableArray array];
	for (id key in [paramDict allKeys]) 
	{
		id value = [paramDict objectForKey: key];
		NSString *part = [NSString stringWithFormat: @"%@=%@", urlEncode(key), urlEncode(value)];
		[parts addObject: part];
	}
	return [parts componentsJoinedByString: @"&"];
}


- (void) connectWithParam:(NSMutableDictionary *)genericDict
{
	NSString *requestString1 = [NSString stringWithFormat:@"%@%@?%@",orignalRequest,@"Connect", [self createQueryStringFromDict:genericDict]];
	
	NSString *requestString = [requestString1 stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
	
	NSURL * myURL = [[NSURL alloc] initWithString:requestString];
	NSMutableURLRequest *myRequest = [NSMutableURLRequest requestWithURL: myURL
															 cachePolicy: NSURLRequestReloadIgnoringLocalAndRemoteCacheData
														 timeoutInterval: 30];
	[myURL release];
	
	connection_ = [[NSURLConnection alloc] initWithRequest: myRequest delegate: self];
	connectAttempts_++;	
}


- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace 
{
	return [protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust];
}


- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge 
{
	if ([challenge.protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust])
	{
		//if ([trustedHosts containsObject:challenge.protectionSpace.host])
		if (([@"ws.tapjoyads.com" isEqualToString:challenge.protectionSpace.host]) ||
			([@"ws1.tapjoyads.com" isEqualToString:challenge.protectionSpace.host]))
		{
			[challenge.sender useCredential:[NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust] forAuthenticationChallenge:challenge];
			return;
		}
	}
	
	[challenge.sender continueWithoutCredentialForAuthenticationChallenge:challenge];
}


- (NSCachedURLResponse *) connection:(NSURLConnection *)connection willCacheResponse:(NSCachedURLResponse *)cachedResponse 
{
	// Returning nil will ensure that no cached response will be stored for the connection.
	// This is in case the cache is being used by something else.
	return nil;
}


+ (void) deviceNotificationReceived
{
	// Since we're relying on UIApplicationDidBecomeActiveNotification, we need to make sure we don't call connect twice in a row
	// upon initial start-up of the applicaiton.
	if ([_sharedInstance isInitialConnect])
	{
		_sharedInstance.isInitialConnect = NO;
	}
	else
	{
		// Call connect when the app 
		[_sharedInstance connectWithParam:[_sharedInstance genericParameters]];
	}
}


static const char* jailbreak_apps[] =
{
	"/Applications/Cydia.app", 
	"/Applications/limera1n.app", 
	"/Applications/greenpois0n.app", 
	"/Applications/blackra1n.app",
	"/Applications/blacksn0w.app",
	"/Applications/redsn0w.app",
	NULL,
};

- (BOOL) isJailBroken
{
	// Now check for known jailbreak apps. If we encounter one, the device is jailbroken.
	for (int i = 0; jailbreak_apps[i] != NULL; ++i)
	{
		if ([[NSFileManager defaultManager] fileExistsAtPath:[NSString stringWithUTF8String:jailbreak_apps[i]]])
		{
			//NSLog(@"isjailbroken: %s", jailbreak_apps[i]);
			return YES;
		}		
	}
	
	// TODO: Add more checks? This is an arms-race we're bound to lose.
	
	return NO;
}


- (NSString*) isJailBrokenStr
{
	if ([self isJailBroken])
	{
		return @"42";
	}
	
	return @"0";
}







#pragma mark delegate methods for asynchronous requests

- (void) connection:(NSURLConnection*) myConnection didReceiveResponse:(NSURLResponse*) myResponse;
{
	
}


- (void) connection:(NSURLConnection*) myConnection didReceiveData:(NSData*) myData;
{   
	if (data_) 
	{
        if (![data_ isKindOfClass: [NSMutableData class]]) 
		{
            data_ = [data_ mutableCopy];
            [data_ release];
        }
        
        [(NSMutableData *) data_ appendData: myData];
    } 
	else
	{
        data_ = [myData mutableCopy];
    }
}


- (void) connection:(NSURLConnection*) myConnection didFailWithError:(NSError*) myError;
{
	[connection_ release];
	connection_ = nil;
	
	if (connectAttempts_ >=2)
	{	
		[[NSNotificationCenter defaultCenter] postNotificationName:TJC_CONNECT_FAILED object:nil];
		return;
	}
	
	if(connectAttempts_ < 2)
	{	
		orignalRequest = TJC_SERVICE_URL_ALTERNATE;
		[_sharedInstance connectWithParam:[_sharedInstance genericParameters]];
	}
}


- (void) connectionDidFinishLoading:(NSURLConnection*) myConnection;
{
	[connection_ release];
	connection_ = nil;
	[self startParsing:data_];
}


- (void) startParsing:(NSData*) myData 
{
    NSData *xmlData = myData;//(Get XML as NSData)
    NSXMLParser *parser = [[[NSXMLParser alloc] initWithData:xmlData] autorelease];
    [parser setDelegate:self];
    [parser parse];
}


- (void) parser:(NSXMLParser*)parser 
didStartElement:(NSString*)elementName 
   namespaceURI:(NSString*)namespaceURI 
  qualifiedName:(NSString*)qualifiedName 
	 attributes:(NSDictionary*)attributeDict 
{
	currentXMLElement_ = elementName;
	if ([elementName isEqualToString:@"ErrorMessage"])
	{
		[[NSNotificationCenter defaultCenter] postNotificationName:TJC_CONNECT_FAILED object:nil];
	}
}


- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string
{
	if([currentXMLElement_ isEqualToString:@"Success"] && [string isEqualToString:@"true"])
	{
		[[NSNotificationCenter defaultCenter] postNotificationName:TJC_CONNECT_SUCCESS object:nil];
	}
}


- (void) parser:(NSXMLParser*)parser 
  didEndElement:(NSString*)elementName
   namespaceURI:(NSString*)namespaceURI 
  qualifiedName:(NSString*)qName
{
	
}







#pragma mark TapjoyConnect Initialize methods

- (id) initWithAppId:(NSString*)appId
{
		appId_ = [appId retain];
		connection_ = nil;
		timeStamp_ = 100;
		connectAttempts_ = 0;
		
	return self;
}


// Simplified Function for End User 
+ (TapjoyConnect*) requestTapjoyConnectWithAppId:(NSString*)appId
{
	//NSLog(@"[INFO] %@ sent for Tapjoy",appId);
	if(!_sharedInstance)
	{
		_sharedInstance = [[super allocWithZone:NULL] initWithAppId:appId];
	}
	
	// This should really only be set to YES here ever.
	_sharedInstance.isInitialConnect = YES;
	
	// 7.0.0 update: Call connect here to simplify the method call.
	[_sharedInstance connectWithParam:[_sharedInstance genericParameters]];
	
	return _sharedInstance;
}


+ (TapjoyConnect*) sharedTapjoyConnect;
{
	return _sharedInstance;
}








#pragma mark Deprecated Methods

+ (TapjoyConnect*) requestTapjoyConnectWithAppId:(NSString*)appId WithPassword:(NSString*)password WithVersion:(NSString*)version
{
	return [TapjoyConnect requestTapjoyConnectWithAppId:appId];
}

- (void) connect
{
	// 7.0.0 update: This method is deprecated. It no longer does anything.
	//[self connectWithParam:[self genericParameters]];
}







#pragma mark TapjoyConnect Singleton Required Methods

- (void) dealloc 
{
	[_sharedInstance release];
	[super dealloc];
}


+ (id) alloc
{
	return [[self sharedTapjoyConnect] retain];
}


+ (id) allocWithZone:(NSZone *)zone
{
	return [[self sharedTapjoyConnect] retain];
}


- (id) copyWithZone:(NSZone *)zone
{
	return self;
}


- (id) retain
{
	return self;
}


- (void) release
{
	//do nothing, for Pure Singleton
}


- (NSUInteger) retainCount
{
	//denotes an object that cannot be released
	return NSUIntegerMax;
}


- (id) autorelease
{
	return self;
}

@end


