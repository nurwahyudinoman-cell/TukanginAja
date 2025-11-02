const admin = require("firebase-admin");

/**
 * Log event to Firestore system_logs collection
 * @param {string} type - Event type (e.g., "OrderStatusChange", "NewMessage")
 * @param {Object} details - Event details
 */
exports.logEvent = async (type, details) => {
  try {
    const db = admin.firestore();

    const logData = {
      type,
      details,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      timestampMillis: Date.now(),
    };

    await db.collection("system_logs").add(logData);

    console.log(`📝 Event logged: ${type}`, details);
  } catch (error) {
    console.error("❌ Error logging event:", error);
    // Don't throw error - logging should not break the main flow
  }
};

/**
 * Get user FCM token from Firestore
 * @param {string} userId - User ID
 * @returns {Promise<string|null>} - FCM token or null if not found
 */
exports.getUserFcmToken = async (userId) => {
  try {
    const db = admin.firestore();
    const userDoc = await db.collection("users").doc(userId).get();

    if (userDoc.exists) {
      const userData = userDoc.data();
      return userData.fcmToken || null;
    }

    return null;
  } catch (error) {
    console.error(`❌ Error getting FCM token for user ${userId}:`, error);
    return null;
  }
};

