import DateTimePicker from '@react-native-community/datetimepicker';
import * as Location from 'expo-location';
import { useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  TextInput,
} from 'react-native';

import { StatusBadge } from '@/components/StatusBadge';
import { Text, View } from '@/components/Themed';
import { api, ApiError } from '@/services/api';
import { useAuthStore } from '@/store/authStore';
import type { Capsule, CapsuleEvent, Condition } from '@/types/api';

export default function CapsuleDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const token = useAuthStore((s) => s.token);
  const user = useAuthStore((s) => s.user);

  const [capsule, setCapsule] = useState<Capsule | null>(null);
  const [conditions, setConditions] = useState<Condition[]>([]);
  const [events, setEvents] = useState<CapsuleEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [unlockDate, setUnlockDate] = useState(new Date(Date.now() + 3600_000));
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [lat, setLat] = useState('6.6745');
  const [lon, setLon] = useState('-1.5712');
  const [radius, setRadius] = useState('250');

  const load = useCallback(async () => {
    if (!token || !id) return;
    setError(null);
    try {
      const [c, cond, ev] = await Promise.all([
        api.getCapsule(token, id),
        api.listConditions(token, id),
        api.listEvents(token, id),
      ]);
      setCapsule(c);
      setConditions(cond);
      setEvents(ev);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Failed to load capsule');
    } finally {
      setLoading(false);
    }
  }, [token, id]);

  useEffect(() => {
    load();
  }, [load]);

  const isCreator = capsule && user && capsule.creatorId === user.id;
  const isRecipient = capsule && user && capsule.recipientId === user.id;
  const isDraft = capsule?.status === 'DRAFT';
  const isLocked = capsule?.status === 'LOCKED';
  const isUnlocked = capsule?.status === 'UNLOCKED';

  async function runAction(action: () => Promise<void>) {
    setBusy(true);
    setMessage(null);
    setError(null);
    try {
      await action();
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Action failed');
    } finally {
      setBusy(false);
    }
  }

  async function addDateCondition() {
    if (!token || !id) return;
    await api.addCondition(token, id, {
      type: 'DATE',
      config: { unlockAt: unlockDate.toISOString() },
    });
    setMessage('Date condition added');
  }

  async function addIdentityCondition() {
    if (!token || !id) return;
    await api.addCondition(token, id, { type: 'IDENTITY', config: {} });
    setMessage('Identity condition added');
  }

  async function addLocationCondition() {
    if (!token || !id) return;
    await api.addCondition(token, id, {
      type: 'LOCATION',
      config: {
        latitude: Number(lat),
        longitude: Number(lon),
        radiusMeters: Number(radius),
      },
    });
    setMessage('Location condition added');
  }

  async function checkLocation() {
    if (!token || !id) return;
    const permission = await Location.requestForegroundPermissionsAsync();
    if (!permission.granted) {
      throw new ApiError('Location permission denied', 400);
    }
    const position = await Location.getCurrentPositionAsync({});
    await api.locationCheck(token, id, {
      latitude: position.coords.latitude,
      longitude: position.coords.longitude,
      accuracyMeters: position.coords.accuracy ?? undefined,
    });
    setMessage('Location submitted to server');
  }

  if (loading || !capsule) {
    return (
      <View style={styles.center}>
        {error ? <Text style={styles.error}>{error}</Text> : <ActivityIndicator />}
      </View>
    );
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.titleRow}>
        <Text style={styles.title}>{capsule.title}</Text>
        <StatusBadge status={capsule.status} />
      </View>

      {isUnlocked && capsule.content ? (
        <View style={styles.contentBox}>
          <Text style={styles.contentLabel}>Message</Text>
          <Text>{capsule.content}</Text>
        </View>
      ) : (
        <Text style={styles.hint}>
          {isLocked ? 'This capsule is locked until conditions pass.' : 'Draft — add conditions, then lock.'}
        </Text>
      )}

      <Text style={styles.section}>Conditions</Text>
      {conditions.length === 0 ? (
        <Text style={styles.muted}>No conditions yet</Text>
      ) : (
        conditions.map((c) => (
          <View key={c.id} style={styles.conditionRow}>
            <Text style={styles.conditionType}>{c.type}</Text>
            <Text>{c.status}</Text>
          </View>
        ))
      )}

      {isDraft && isCreator ? (
        <View style={styles.block}>
          <Text style={styles.section}>Add conditions</Text>

          <Pressable style={styles.secondaryButton} onPress={() => setShowDatePicker(true)}>
            <Text>Pick unlock date: {unlockDate.toLocaleString()}</Text>
          </Pressable>
          {showDatePicker ? (
            <DateTimePicker
              value={unlockDate}
              mode="datetime"
              onChange={(_, date) => {
                setShowDatePicker(Platform.OS === 'ios');
                if (date) setUnlockDate(date);
              }}
            />
          ) : null}
          <Pressable style={styles.secondaryButton} disabled={busy} onPress={() => runAction(addDateCondition)}>
            <Text>Add date condition</Text>
          </Pressable>

          <Pressable style={styles.secondaryButton} disabled={busy} onPress={() => runAction(addIdentityCondition)}>
            <Text>Add identity condition</Text>
          </Pressable>

          <Text style={styles.label}>Location (lat / lon / radius m)</Text>
          <View style={styles.locationRow}>
            <TextInput value={lat} onChangeText={setLat} style={styles.smallInput} />
            <TextInput value={lon} onChangeText={setLon} style={styles.smallInput} />
            <TextInput value={radius} onChangeText={setRadius} style={styles.smallInput} />
          </View>
          <Pressable style={styles.secondaryButton} disabled={busy} onPress={() => runAction(addLocationCondition)}>
            <Text>Add location condition</Text>
          </Pressable>

          <Pressable
            style={styles.primaryButton}
            disabled={busy || conditions.length === 0}
            onPress={() =>
              runAction(async () => {
                if (!token) return;
                await api.lockCapsule(token, id);
                setMessage('Capsule locked');
              })
            }>
            <Text style={styles.primaryText}>Lock capsule</Text>
          </Pressable>
        </View>
      ) : null}

      {isLocked && isRecipient ? (
        <View style={styles.block}>
          <Pressable
            style={styles.primaryButton}
            disabled={busy}
            onPress={() =>
              runAction(async () => {
                if (!token) return;
                await api.openCapsule(token, id);
                setMessage('Opened');
              })
            }>
            <Text style={styles.primaryText}>Try to open</Text>
          </Pressable>

          {conditions.some((c) => c.type === 'LOCATION') ? (
            <Pressable style={styles.secondaryButton} disabled={busy} onPress={() => runAction(checkLocation)}>
              <Text>Check my location (foreground)</Text>
            </Pressable>
          ) : null}
        </View>
      ) : null}

      <Text style={styles.section}>Audit timeline</Text>
      {events.map((event) => (
        <Text key={event.id} style={styles.event}>
          {new Date(event.createdAt).toLocaleString()} · {event.eventType}
        </Text>
      ))}

      {message ? <Text style={styles.message}>{message}</Text> : null}
      {error ? <Text style={styles.error}>{error}</Text> : null}
      {busy ? <ActivityIndicator style={{ marginTop: 8 }} /> : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  container: { padding: 16, gap: 10, paddingBottom: 40 },
  titleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 8 },
  title: { fontSize: 22, fontWeight: '700', flex: 1 },
  hint: { opacity: 0.7 },
  contentBox: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 10,
    padding: 12,
    gap: 6,
  },
  contentLabel: { fontWeight: '600' },
  section: { fontSize: 17, fontWeight: '700', marginTop: 8 },
  muted: { opacity: 0.6 },
  conditionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 6,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#ccc',
  },
  conditionType: { fontWeight: '600' },
  block: { gap: 8, marginTop: 4 },
  label: { fontWeight: '600' },
  locationRow: { flexDirection: 'row', gap: 6 },
  smallInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    padding: 8,
  },
  primaryButton: {
    backgroundColor: '#1a5cff',
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 8,
  },
  primaryText: { color: '#fff', fontWeight: '600' },
  secondaryButton: {
    borderWidth: 1,
    borderColor: '#bbb',
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 10,
  },
  event: { fontSize: 13, opacity: 0.85 },
  message: { color: '#15803d', marginTop: 8 },
  error: { color: '#c0392b', marginTop: 8 },
});
