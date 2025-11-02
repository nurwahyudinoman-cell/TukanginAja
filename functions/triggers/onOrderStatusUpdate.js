const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { sendNotification } = require("../utils/notification");
const { logEvent } = require("../utils/logger");
const { getUserFcmToken } = require("../utils/logger");

/**
 * Cloud Function triggered when an order status is updated
 * Sends notification to user and logs the event
 */
exports.onOrderStatusUpdate = functions.firestore
  .document("orders/{orderId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const orderId = context.params.orderId;

    // Only process if status actually changed
    if (before.status !== after.status) {
      console.log(
        `📦 Order status changed: ${orderId}, ${before.status} → ${after.status}`
      );

      try {
        // Get user FCM token
        const userId = after.userId || before.userId;
        const fcmToken = await getUserFcmToken(userId);

        // Prepare notification message based on status
        let title = "Order Status Updated";
        let body = `Order #${orderId.substring(0, 8)} changed to ${after.status}`;

        const statusMessages = {
          Selesai: {
            title: "Order Selesai!",
            body: `Order #${orderId.substring(0, 8)} telah selesai dikerjakan.`,
          },
          Diterima: {
            title: "Order Diterima",
            body: `Tukang telah menerima order #${orderId.substring(0, 8)}.`,
          },
          Dikerjakan: {
            title: "Order Sedang Dikerjakan",
            body: `Order #${orderId.substring(0, 8)} sedang dikerjakan.`,
          },
          Dibatalkan: {
            title: "Order Dibatalkan",
            body: `Order #${orderId.substring(0, 8)} telah dibatalkan.`,
          },
        };

        if (statusMessages[after.status]) {
          title = statusMessages[after.status].title;
          body = statusMessages[after.status].body;
        }

        // Send notification if token exists
        if (fcmToken) {
          await sendNotification({
            title,
            body,
            token: fcmToken,
            data: {
              type: "order_status_update",
              orderId: orderId,
              status: after.status,
            },
          });
        } else {
          console.log(
            `⚠️ No FCM token found for user ${userId}, notification skipped`
          );
        }

        // Log the event
        await logEvent("OrderStatusChange", {
          orderId: orderId,
          userId: userId,
          oldStatus: before.status,
          newStatus: after.status,
          timestamp: Date.now(),
        });

        console.log(`✅ Order status update processed: ${orderId}`);
      } catch (error) {
        console.error(`❌ Error processing order status update:`, error);
        // Don't throw - log the error but don't break the function
      }
    }
  });

