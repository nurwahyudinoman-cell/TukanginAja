const admin = require("firebase-admin");

/**
 * Send notification using FCM
 * @param {Object} options - Notification options
 * @param {string} options.title - Notification title
 * @param {string} options.body - Notification body
 * @param {string} options.topic - FCM topic or user ID
 * @param {string} [options.token] - FCM token (optional, if provided will be used instead of topic)
 * @param {Object} [options.data] - Additional data payload (optional)
 */
exports.sendNotification = async ({ title, body, topic, token, data = {} }) => {
  try {
    const message = {
      notification: { title, body },
      data: {
        ...data,
        timestamp: Date.now().toString(),
      },
      android: {
        priority: "high",
      },
      apns: {
        headers: {
          "apns-priority": "10",
        },
      },
    };

    if (token) {
      // Send to specific token
      message.token = token;
      await admin.messaging().send(message);
      console.log(`✅ Notification sent to token: ${token}`);
    } else if (topic) {
      // Send to topic or user ID
      message.topic = topic;
      await admin.messaging().send(message);
      console.log(`✅ Notification sent to topic: ${topic}`);
    } else {
      throw new Error("Either topic or token must be provided");
    }

    return { success: true };
  } catch (error) {
    console.error("❌ Error sending notification:", error);
    throw error;
  }
};

