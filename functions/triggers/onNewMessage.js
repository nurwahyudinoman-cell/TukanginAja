const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { sendNotification } = require("../utils/notification");
const { logEvent } = require("../utils/logger");
const { getUserFcmToken } = require("../utils/logger");

/**
 * Cloud Function triggered when a new message is created in order chat
 * Sends notification to receiver and logs the event
 */
exports.onNewMessage = functions.firestore
  .document("orders/{orderId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const messageId = context.params.messageId;
    const orderId = context.params.orderId;

    console.log(`💬 New message created: ${messageId} in order ${orderId}`);

    try {
      const receiverId = message.receiverId;
      const senderId = message.senderId;

      if (!receiverId) {
        console.log("⚠️ No receiver ID found in message, skipping notification");
        return null;
      }

      // Get receiver FCM token
      const fcmToken = await getUserFcmToken(receiverId);

      // Prepare notification
      const title = "Pesan Baru";
      const messageText = message.message || "Anda mendapat pesan baru";
      const body =
        messageText.length > 100
          ? messageText.substring(0, 100) + "..."
          : messageText;

      // Send notification if token exists
      if (fcmToken) {
        await sendNotification({
          title,
          body,
          token: fcmToken,
          data: {
            type: "new_message",
            orderId: orderId,
            messageId: messageId,
            senderId: senderId,
            receiverId: receiverId,
          },
        });
      } else {
        console.log(
          `⚠️ No FCM token found for receiver ${receiverId}, notification skipped`
        );
      }

      // Log the event
      await logEvent("NewMessage", {
        orderId: orderId,
        messageId: messageId,
        sender: senderId,
        receiver: receiverId,
        timestamp: Date.now(),
      });

      console.log(`✅ New message notification processed: ${messageId}`);
    } catch (error) {
      console.error(`❌ Error processing new message:`, error);
      // Don't throw - log the error but don't break the function
    }
  });

