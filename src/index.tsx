import {
  EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';

type DeviceType = {
  friendlyName: string,
  deviceId: string,
  deviceVersion: string,
  isOnLocalNetwork: string,
  modelName: string,
  ipAddress: string,
}

type DiscoverCastsType = {
  getAvailableDevices(): Promise<Array<DeviceType>>;
  addEventListener(event: string, listener: (event: any) => any): EmitterSubscription;
  removeEventListener(listener: EmitterSubscription): void;
  removeAllListeners(event: string): void;
};

const { DiscoverCasts } = NativeModules;

const emitter = new NativeEventEmitter(DiscoverCasts);

DiscoverCasts.addEventListener = function (
  event: string,
  listener: (event: any) => any
): EmitterSubscription {
  return emitter.addListener(event, listener);
};

DiscoverCasts.removeEventListener = function (listener: EmitterSubscription) {
  return emitter.removeSubscription(listener);
};

DiscoverCasts.removeAllListeners = function (event: string) {
  return emitter.removeAllListeners(event);
};

export default DiscoverCasts as DiscoverCastsType;
