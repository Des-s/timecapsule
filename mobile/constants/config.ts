import { Platform } from 'react-native';

/**
 * Android emulator: use http://10.0.2.2:8080
 * Physical device: use your machine's LAN IP, e.g. http://192.168.1.10:8080
 */
const defaultHost =
  Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';

export const API_URL = process.env.EXPO_PUBLIC_API_URL ?? defaultHost;
