const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Import new automation and monitoring functions
const { onOrderStatusUpdate } = require("./triggers/onOrderStatusUpdate");
const { onNewMessage } = require("./triggers/onNewMessage");
const { onNewRating } = require("./triggers/onNewRating");
const { onPaymentSuccess } = require("./triggers/onPaymentSuccess");

/**
 * Cloud Function triggered when a new user is created
 * Creates tukang/{uid} document if user role is 'tukang'
 */
exports.createTukangDocOnUserCreate = functions.firestore
  .document("users/{uid}")
  .onCreate(async (snap, context) => {
    const uid = context.params.uid;
    const userData = snap.data();
    const role = userData.role;

    console.log(`👤 New user created: ${uid}, role: ${role}`);

    // Only create tukang document if role is 'tukang'
    if (role !== "tukang") {
      console.log(`⏭️  Skipping - user role is not 'tukang'`);
      return null;
    }

    try {
      const db = admin.firestore();
      
      // Check if tukang document already exists
      const tukangDoc = await db.collection("tukang").doc(uid).get();
      
      if (tukangDoc.exists) {
        console.log(`✅ tukang/${uid} already exists, skipping`);
        return null;
      }

      // Create tukang document
      const tukangData = {
        id: uid,
        email: userData.email || "",
        name: userData.name || "",
        phoneNumber: userData.phoneNumber || "",
        profileImageUrl: userData.profileImageUrl || "",
        role: "tukang",
        verified: userData.verified || false,
        available: userData.available || false,
        skills: userData.skills || [],
        isActive: userData.isActive !== undefined ? userData.isActive : true,
        rating: userData.rating || 0.0,
        completedJobs: userData.completedJobs || 0,
        createdAt: userData.createdAt || admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        createdByFunction: true,
        functionTriggeredAt: admin.firestore.FieldValue.serverTimestamp()
      };

      await db.collection("tukang").doc(uid).set(tukangData);
      console.log(`✅ Created tukang/${uid} document`);
      
      return { success: true, uid: uid };
    } catch (error) {
      console.error(`❌ Error creating tukang/${uid}:`, error);
      throw error;
    }
  });

/**
 * Cloud Function triggered when user role changes to 'tukang'
 * Creates tukang/{uid} document if it doesn't exist
 */
exports.createTukangOnRoleChange = functions.firestore
  .document("users/{uid}")
  .onUpdate(async (change, context) => {
    const uid = context.params.uid;
    const beforeData = change.before.data();
    const afterData = change.after.data();
    const beforeRole = beforeData.role;
    const afterRole = afterData.role;

    console.log(`👤 User role changed: ${uid}, ${beforeRole} → ${afterRole}`);

    // Only create tukang document if role changed TO 'tukang'
    if (beforeRole === afterRole || afterRole !== "tukang") {
      console.log(`⏭️  Skipping - role did not change to 'tukang'`);
      return null;
    }

    try {
      const db = admin.firestore();
      
      // Check if tukang document already exists
      const tukangDoc = await db.collection("tukang").doc(uid).get();
      
      if (tukangDoc.exists) {
        console.log(`✅ tukang/${uid} already exists, skipping`);
        return null;
      }

      // Create tukang document
      const tukangData = {
        id: uid,
        email: afterData.email || "",
        name: afterData.name || "",
        phoneNumber: afterData.phoneNumber || "",
        profileImageUrl: afterData.profileImageUrl || "",
        role: "tukang",
        verified: afterData.verified || false,
        available: afterData.available || false,
        skills: afterData.skills || [],
        isActive: afterData.isActive !== undefined ? afterData.isActive : true,
        rating: afterData.rating || 0.0,
        completedJobs: afterData.completedJobs || 0,
        createdAt: afterData.createdAt || admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        createdByFunction: true,
        functionTriggeredAt: admin.firestore.FieldValue.serverTimestamp(),
        roleChangedFrom: beforeRole
      };

      await db.collection("tukang").doc(uid).set(tukangData);
      console.log(`✅ Created tukang/${uid} document after role change`);
      
      return { success: true, uid: uid, previousRole: beforeRole };
    } catch (error) {
      console.error(`❌ Error creating tukang/${uid}:`, error);
      throw error;
    }
  });

/**
 * Cloud Function triggered when a new order is created
 * Sends FCM notification to all verified tukang
 */
exports.onOrderCreate = functions.firestore
  .document("orders/{orderId}")
  .onCreate(async (snap, context) => {
    const order = snap.data();
    
    console.log(`📦 New order created: ${context.params.orderId}`);
    console.log(`📝 Order data:`, order);
    
    try {
      // Get all verified tukang users
      const tukangSnapshot = await admin.firestore()
        .collection("users")
        .where("role", "==", "tukang")
        .where("verified", "==", true)
        .get();
      
      if (tukangSnapshot.empty) {
        console.log("⚠️ Tidak ada tukang aktif untuk menerima notifikasi.");
        return null;
      }
      
      // Extract FCM tokens from tukang profiles
      const tokens = tukangSnapshot.docs
        .map(doc => {
          const userData = doc.data();
          return userData.fcmToken;
        })
        .filter(token => token != null && token.trim() !== "");
      
      if (tokens.length === 0) {
        console.log("⚠️ Tidak ada FCM token yang valid.");
        return null;
      }
      
      // Prepare notification payload
      const payload = {
        notification: {
          title: "Pesanan Baru!",
          body: `Order baru dari ${order.userName || "Pelanggan"}`,
        },
        data: {
          type: "new_order",
          orderId: order.id || context.params.orderId,
          userId: order.userId || "",
          serviceType: order.serviceType || "",
          status: order.status || "requested",
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
      
      // Send notification to all tukang tokens
      const response = await admin.messaging().sendToDevice(tokens, payload);
      
      console.log(`📩 Notifikasi dikirim ke ${tokens.length} tukang.`);
      console.log(`✅ Success count: ${response.successCount}`);
      console.log(`❌ Failure count: ${response.failureCount}`);
      
      // Handle failed tokens (optional cleanup)
      if (response.failureCount > 0) {
        const failedTokens = [];
        response.results.forEach((result, index) => {
          if (!result.success) {
            console.error(`Failed to send to token ${index}:`, result.error);
            if (result.error && 
                (result.error.code === "messaging/invalid-registration-token" ||
                 result.error.code === "messaging/registration-token-not-registered")) {
              failedTokens.push(tokens[index]);
            }
          }
        });
        
        // Optional: Clean up invalid tokens from Firestore
        if (failedTokens.length > 0) {
          console.log(`🧹 Cleaning up ${failedTokens.length} invalid tokens...`);
          // You can add logic here to remove invalid tokens from user documents
        }
      }
      
      return { success: true, sentCount: response.successCount };
    } catch (error) {
      console.error("❌ Error sending notification:", error);
      throw error;
    }
  });

// Export automation and monitoring functions
exports.onOrderStatusUpdate = onOrderStatusUpdate;
exports.onNewMessage = onNewMessage;
exports.onNewRating = onNewRating;
exports.onPaymentSuccess = onPaymentSuccess;
