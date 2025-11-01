# Firebase Cloud Function - FCM Notification

## Overview

This document describes the Firebase Cloud Function required for sending FCM push notifications in Tahap 16. **DO NOT** embed server keys in the client app. This function should be deployed to Firebase Functions.

## Required Cloud Function

### Function Name: `sendNotification`

### Trigger Options:

#### Option 1: Firestore Trigger (Recommended)
- **Trigger**: `onCreate` of `notification_queue` collection
- **Action**: Process queued notifications and send via FCM

#### Option 2: HTTP Callable
- **Trigger**: HTTP callable function
- **Action**: Direct invocation from client (currently used by NotificationService)

### Function Code Template:

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { token, title, body, type, orderId, chatId, ...otherData } = data;

  if (!token || !title || !body) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields');
  }

  // Prepare FCM data message
  const message = {
    token: token,
    notification: {
      title: title,
      body: body,
    },
    data: {
      title: title,
      body: body,
      type: type || 'generic',
      ...(orderId && { orderId: orderId }),
      ...(chatId && { chatId: chatId }),
      ...otherData,
    },
    android: {
      priority: 'high',
    },
    apns: {
      headers: {
        'apns-priority': '10',
      },
    },
  };

  try {
    // Send notification via FCM Admin SDK
    const response = await admin.messaging().send(message);
    console.log('Successfully sent notification:', response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error('Error sending notification:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification', error);
  }
});

// Alternative: Firestore Trigger
exports.processNotificationQueue = functions.firestore
  .document('notification_queue/{notificationId}')
  .onCreate(async (snap, context) => {
    const data = snap.data();
    
    // Same logic as above
    // Then delete the document after processing
    await snap.ref.delete();
  });
```

### Deployment:

```bash
cd functions
npm install
firebase deploy --only functions:sendNotification
```

## Alternative: Firestore Queue Processing

If Cloud Functions are not available, notifications are queued to `notification_queue` collection. You can process these manually or with a separate backend service.

### Queue Document Structure:

```json
{
  "token": "fcm_token_here",
  "title": "Notification Title",
  "body": "Notification Body",
  "type": "chat|order|proximity",
  "orderId": "order_id_if_applicable",
  "chatId": "chat_id_if_applicable",
  "timestamp": 1234567890
}
```

## Client Fallback

The client app (`NotificationService.kt`) will:
1. Try to call Cloud Function `sendNotification`
2. If Cloud Function fails, queue notification to `notification_queue`
3. Queue will be processed by Cloud Function trigger (if deployed)

## Security Notes

- ✅ **DO NOT** embed FCM server key in client code
- ✅ Use Firebase Admin SDK in Cloud Functions
- ✅ Verify authentication in Cloud Function
- ✅ Validate all input data in Cloud Function

## Testing

1. Deploy Cloud Function to Firebase
2. Test with Firebase Console Functions logs
3. Verify notifications received on device
4. Check Firestore `notification_queue` if Cloud Function unavailable

