import { router } from 'expo-router';
import { useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  TextInput,
} from 'react-native';

import { Text, View } from '@/components/Themed';
import { api, ApiError } from '@/services/api';
import { useAuthStore } from '@/store/authStore';

export default function NewCapsuleScreen() {
  const token = useAuthStore((s) => s.token);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [sendToOther, setSendToOther] = useState(false);
  const [recipientEmail, setRecipientEmail] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onCreate() {
    if (!token) return;
    setError(null);
    setLoading(true);
    try {
      let recipientId: string | undefined;
      if (sendToOther) {
        const user = await api.lookupUser(token, recipientEmail.trim());
        recipientId = user.id;
      }
      const capsule = await api.createCapsule(token, {
        title,
        content,
        recipientId,
      });
      router.replace(`/capsules/${capsule.id}`);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not create capsule');
    } finally {
      setLoading(false);
    }
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.label}>Title</Text>
      <TextInput value={title} onChangeText={setTitle} style={styles.input} placeholder="Birthday surprise" />

      <Text style={styles.label}>Message</Text>
      <TextInput
        value={content}
        onChangeText={setContent}
        style={[styles.input, styles.multiline]}
        multiline
        placeholder="Write what the future should read..."
      />

      <View style={styles.row}>
        <Text>Send to another user</Text>
        <Switch value={sendToOther} onValueChange={setSendToOther} />
      </View>

      {sendToOther ? (
        <>
          <Text style={styles.label}>Recipient email</Text>
          <TextInput
            autoCapitalize="none"
            keyboardType="email-address"
            value={recipientEmail}
            onChangeText={setRecipientEmail}
            style={styles.input}
            placeholder="friend@example.com"
          />
        </>
      ) : null}

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Pressable style={styles.button} onPress={onCreate} disabled={loading || !title || !content}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Create draft</Text>}
      </Pressable>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 16, gap: 10 },
  label: { fontWeight: '600', marginTop: 4 },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 10,
    padding: 12,
    fontSize: 16,
  },
  multiline: { minHeight: 120, textAlignVertical: 'top' },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginVertical: 8,
  },
  button: {
    backgroundColor: '#1a5cff',
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 12,
  },
  buttonText: { color: '#fff', fontWeight: '600' },
  error: { color: '#c0392b' },
});
