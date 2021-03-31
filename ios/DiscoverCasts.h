#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <GoogleCast/GoogleCast.h>

static NSString *const CAST_ROUTE_ADDED = @"onRouteAdded";
static NSString *const CAST_ROUTE_CHANGED = @"onRouteChanged";
static NSString *const CAST_ROUTE_REMOVED = @"onRouteRemoved";

@interface DiscoverCasts : RCTEventEmitter <RCTBridgeModule, GCKDiscoveryManagerListener>

@property(strong, nonatomic) GCKDeviceProvider *deviceProvider;

@end
