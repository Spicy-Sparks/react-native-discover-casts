#import "DiscoverCasts.h"
#import <GoogleCast/GoogleCast.h>

@implementation DiscoverCasts



RCT_EXPORT_MODULE()

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

- (void)didInsertDevice:(GCKDevice *)device atIndex:(NSUInteger)index{

}

- (void)didRemoveDeviceAtIndex:(NSUInteger)index{

}

- (void)didUpdateDevice:(GCKDevice *)device atIndex:(NSUInteger)index{

}

- (void)didUpdateDevice:(GCKDevice *)device atIndex:(NSUInteger)index andMoveToIndex:(NSUInteger)newIndex{

}


RCT_REMAP_METHOD(getAvailableDevices,
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSUInteger count = GCKCastContext.sharedInstance.discoveryManager.deviceCount;
        resolve(nil);
    });
  //NSNumber *result = @([a floatValue] * [b floatValue]);
}


@end
