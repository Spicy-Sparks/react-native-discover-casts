import * as React from 'react';
import { StyleSheet, View, Text } from 'react-native';
import DiscoverCasts from 'react-native-discover-casts';
import GoogleCast, { CastButton } from 'react-native-google-cast';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>();

  React.useEffect(() => {
    (async () => {
      /*const discoveryManager = GoogleCast.getDiscoveryManager()
      await discoveryManager.startDiscovery()

      setTimeout(async () => {
        const devices = await DiscoverCasts.getAvailableDevices()
        console.log(devices)
      }, 10000);*/

      DiscoverCasts.addEventListener("onRouteAdded", (data) => console.log("ad", data))
      DiscoverCasts.addEventListener("onRouteChanged", (data) => console.log("ch", data))
      DiscoverCasts.addEventListener("onRouteRemoved", (data) => console.log("rm", data))
    })()
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <CastButton
        tintColor="black"
        style={{
          marginVertical: 16,
          width: 24,
          height: 24,
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
