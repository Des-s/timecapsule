import { router } from 'expo-router';
import { Pressable, StyleSheet } from 'react-native';

import { Text, View } from '@/components/Themed';
import { API_URL } from '@/constants/config';
import { useAuthStore } from '@/store/authStore';

export default function ProfileScreen() {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);

  async function onLogout() {
    await logout();
    router.replace('/(auth)/login');
  }

  return (
    <View style={styles.container}>
      <Text style={styles.label}>Signed in as</Text>
      <Text style={styles.name}>{user?.displayName}</Text>
      <Text style={styles.email}>{user?.email}</Text>
      <Text style={styles.api}>API: {API_URL}</Text>

      <Pressable style={styles.button} onPress={onLogout}>
        <Text style={styles.buttonText}>Sign out</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, gap: 8 },
  label: { opacity: 0.6 },
  name: { fontSize: 22, fontWeight: '700' },
  email: { fontSize: 16, marginBottom: 16 },
  api: { fontSize: 12, opacity: 0.5, marginBottom: 24 },
  button: {
    backgroundColor: '#334155',
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: 'center',
  },
  buttonText: { color: '#fff', fontWeight: '600' },
});
