#import "DiscoverCasts.h"
#import <GoogleCast/GoogleCast.h>

@implementation DiscoverCasts



RCT_EXPORT_MODULE()

- (id)init {
  self = [super init];
  dispatch_async(dispatch_get_main_queue(), ^{
      [GCKCastContext.sharedInstance.discoveryManager addListener:self];
      self.deviceProvider = [GCKDeviceProvider alloc];
  });
  return self;
}

/*- (void)dealloc {
    dispatch_async(dispatch_get_main_queue(), ^{
        [GCKCastContext.sharedInstance.discoveryManager removeListener:self];
    });
}*/

+ (BOOL)requiresMainQueueSetup {
  return NO;
}

- (NSDictionary *)constantsToExport {
  return @{
    @"CAST_ROUTE_ADDED": CAST_ROUTE_ADDED,
    @"CAST_ROUTE_CHANGED": CAST_ROUTE_CHANGED,
    @"CAST_ROUTE_REMOVED": CAST_ROUTE_REMOVED
  };
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    CAST_ROUTE_ADDED,
    CAST_ROUTE_CHANGED,
    CAST_ROUTE_REMOVED
  ];
}

- (id) mapDevice:(GCKDevice *)device {
    return @{
        @"friendlyName": device.friendlyName,
        @"deviceId": (NSString *)device.deviceID,
        @"deviceVersion": device.deviceVersion,
        @"isOnLocalNetwork":[NSNumber numberWithBool:device.isOnLocalNetwork],
        @"modelName": device.modelName,
        @"ipAddress": device.networkAddress,
    };
}

- (void)didInsertDevice:(GCKDevice *)device atIndex:(NSUInteger)index{
    [self sendEventWithName:CAST_ROUTE_ADDED body:[self mapDevice:device]];
}

- (void)didRemoveDevice:(GCKDevice *)device atIndex:(NSUInteger)index{
    [self sendEventWithName:CAST_ROUTE_ADDED body:[self mapDevice:device]];
}

- (void)didUpdateDevice:(GCKDevice *)device atIndex:(NSUInteger)index{
    [self sendEventWithName:CAST_ROUTE_ADDED body:[self mapDevice:device]];
}

/* - (void)didUpdateDeviceList:(GCKDevice *)device atIndex:(NSUInteger)index{
    NSUInteger count = GCKCastContext.sharedInstance.discoveryManager.deviceCount;
    [GCKCastContext.sharedInstance.discoveryManager deviceAtIndex:0];
}*/


RCT_REMAP_METHOD(getAvailableDevices,
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSUInteger count = GCKCastContext.sharedInstance.discoveryManager.deviceCount;
        NSMutableArray *map = [NSMutableArray new];
        for (int i = 0; i < count; i++) {
            GCKDevice *device = [GCKCastContext.sharedInstance.discoveryManager deviceAtIndex:i];
            if(device != nil)
                [map addObject:[self mapDevice:device]];
        }
        resolve(map);
    });
}

RCT_EXPORT_METHOD(connectToDevice:(NSString*) deviceId
                 withConnectToDeviceResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        GCKDevice *device;
        NSUInteger count = GCKCastContext.sharedInstance.discoveryManager.deviceCount;

        for (int i = 0; i < count; i++) {
            GCKDevice *deviceTmp = [GCKCastContext.sharedInstance.discoveryManager deviceAtIndex:i];
            if(deviceTmp != nil && [deviceTmp.deviceID isEqualToString:deviceId])
                device = deviceTmp;
        }

        if(device == nil) {
            resolve([NSNumber numberWithBool:false]);
            return;
        }

        BOOL status = [GCKCastContext.sharedInstance.sessionManager startSessionWithDevice:device];

        if(!status) {
            resolve([NSNumber numberWithBool:false]);
            return;
        }

        resolve([NSNumber numberWithBool:true]);
    });
}

RCT_EXPORT_METHOD(getAdditionalDeviceInfos:(NSString*) deviceId
                 withConnectToDeviceResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        GCKDevice *device;
        NSUInteger count = GCKCastContext.sharedInstance.discoveryManager.deviceCount;

        for (int i = 0; i < count; i++) {
            GCKDevice *deviceTmp = [GCKCastContext.sharedInstance.discoveryManager deviceAtIndex:i];
            if(deviceTmp != nil && [deviceTmp.deviceID isEqualToString:deviceId])
                device = deviceTmp;
        }

        if(device == nil) {
            resolve([NSNumber numberWithBool:false]);
            return;
        }

        resolve(@{
            @"deviceType": [NSNumber numberWithInt:device.type]
        });
    });
}

@end
