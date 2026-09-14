import { StyleSheet, View } from 'react-native';

import { Text } from '@/components/Themed';
import type { CapsuleStatus } from '@/types/api';

const COLORS: Record<CapsuleStatus, string> = {
  DRAFT: '#64748b',
  LOCKED: '#b45309',
  UNLOCKING: '#2563eb',
  UNLOCKED: '#15803d',
  EXPIRED: '#9f1239',
  CANCELLED: '#475569',
};

export function StatusBadge({ status }: { status: CapsuleStatus }) {
  return (
    <View style={[styles.badge, { backgroundColor: COLORS[status] }]}>
      <Text style={styles.text}>{status}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 999,
  },
  text: {
    color: '#fff',
    fontSize: 11,
    fontWeight: '700',
  },
});
