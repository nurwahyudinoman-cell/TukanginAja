const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { sendNotification } = require("../utils/notification");
const { logEvent } = require("../utils/logger");
const { getUserFcmToken } = require("../utils/logger");

/**
 * Cloud Function triggered when a payment transaction status changes to SUCCESS
 * Sends notification to user and logs the event
 */
exports.onPaymentSuccess = functions.firestore
  .document("transactions/{trxId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const trxId = context.params.trxId;

    // Only process if status changed from non-SUCCESS to SUCCESS
    if (before.status !== "SUCCESS" && after.status === "SUCCESS") {
      console.log(`💳 Payment successful: ${trxId} for order ${after.orderId}`);

      try {
        const userId = after.userId;
        const orderId = after.orderId;
        const amount = after.amount || 0;

        if (!userId) {
          console.log("⚠️ No user ID found in transaction, skipping notification");
          return null;
        }

        // Get user FCM token
        const fcmToken = await getUserFcmToken(userId);

        // Prepare notification
        const title = "Pembayaran Berhasil";
        const amountFormatted = new Intl.NumberFormat("id-ID", {
          style: "currency",
          currency: "IDR",
        }).format(amount);
        const body = `Pembayaran untuk order #${orderId.substring(0, 8)} telah berhasil. Total: ${amountFormatted}`;

        // Send notification if token exists
        if (fcmToken) {
          await sendNotification({
            title,
            body,
            token: fcmToken,
            data: {
              type: "payment_success",
              transactionId: trxId,
              orderId: orderId,
              amount: amount.toString(),
            },
          });
        } else {
          console.log(
            `⚠️ No FCM token found for user ${userId}, notification skipped`
          );
        }

        // Log the event
        await logEvent("PaymentSuccess", {
          transactionId: trxId,
          orderId: orderId,
          userId: userId,
          amount: amount,
          commission: after.commission || 0,
          platformFee: after.platformFee || 0,
          timestamp: Date.now(),
        });

        console.log(`✅ Payment success notification processed: ${trxId}`);
      } catch (error) {
        console.error(`❌ Error processing payment success:`, error);
        // Don't throw - log the error but don't break the function
      }
    }
  });

