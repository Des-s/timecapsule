import { Link, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
} from 'react-native';

import { StatusBadge } from '@/components/StatusBadge';
import { Text, View } from '@/components/Themed';
import { api, ApiError } from '@/services/api';
import { useAuthStore } from '@/store/authStore';
import type { Capsule } from '@/types/api';

export default function CapsulesScreen() {
  const token = useAuthStore((s) => s.token);
  const user = useAuthStore((s) => s.user);
  const [capsules, setCapsules] = useState<Capsule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!token) return;
    setError(null);
    try {
      const data = await api.listCapsules(token);
      setCapsules(data);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Failed to load capsules');
    } finally {
      setLoading(false);
    }
  }, [token]);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
      load();
    }, [load]),
  );

  return (
    <View style={styles.container}>
      <View style={styles.headerRow}>
        <Text style={styles.greeting}>Hi, {user?.displayName ?? 'there'}</Text>
        <Link href="/capsules/new" asChild>
          <Pressable style={styles.newButton}>
            <Text style={styles.newButtonText}>+ New</Text>
          </Pressable>
        </Link>
      </View>

      {loading && capsules.length === 0 ? (
        <ActivityIndicator style={{ marginTop: 40 }} />
      ) : error ? (
        <Text style={styles.error}>{error}</Text>
      ) : (
        <FlatList
          data={capsules}
          keyExtractor={(item) => item.id}
          refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}
          ListEmptyComponent={<Text style={styles.empty}>No capsules yet. Create your first one.</Text>}
          renderItem={({ item }) => (
            <Link href={`/capsules/${item.id}`} asChild>
              <Pressable style={styles.card}>
                <View style={styles.cardTop}>
                  <Text style={styles.cardTitle}>{item.title}</Text>
                  <StatusBadge status={item.status} />
                </View>
                <Text style={styles.meta}>
                  {item.recipientId === user?.id ? 'For you' : 'Sent to recipient'} ·{' '}
                  {new Date(item.createdAt).toLocaleDateString()}
                </Text>
              </Pressable>
            </Link>
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16 },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  greeting: { fontSize: 20, fontWeight: '600' },
  newButton: {
    backgroundColor: '#1a5cff',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 8,
  },
  newButtonText: { color: '#fff', fontWeight: '600' },
  card: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
  },
  cardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 8,
  },
  cardTitle: { fontSize: 17, fontWeight: '600', flex: 1 },
  meta: { marginTop: 6, opacity: 0.65, fontSize: 13 },
  empty: { textAlign: 'center', marginTop: 48, opacity: 0.6 },
  error: { color: '#c0392b', marginTop: 24, textAlign: 'center' },
});
