const admin = require("firebase-admin");

/**
 * Update trust score for a tukang
 * Formula: (avgRating × 0.7) + (completionRate × 100 × 0.3)
 * @param {string} tukangId - Tukang ID
 */
exports.updateTrustScore = async (tukangId) => {
  try {
    const db = admin.firestore();

    // Get all ratings for this tukang
    const ratingsSnapshot = await db
      .collection("ratings")
      .where("tukangId", "==", tukangId)
      .get();

    // Get all orders for this tukang
    const ordersSnapshot = await db
      .collection("orders")
      .where("tukangId", "==", tukangId)
      .get();

    // Calculate average rating
    const ratings = ratingsSnapshot.docs.map((doc) => doc.data().score);
    const avgRating = ratings.length
      ? ratings.reduce((a, b) => a + b, 0) / ratings.length
      : 0;

    // Calculate completion rate
    const totalOrders = ordersSnapshot.size;
    const completedOrders = ordersSnapshot.docs.filter(
      (o) => o.data().status === "Selesai" || o.data().status === "done"
    ).length;
    const completionRate = totalOrders > 0 ? completedOrders / totalOrders : 0;

    // Calculate trust score: (avgRating × 0.7) + (completionRate × 100 × 0.3)
    const trustScore = avgRating * 0.7 + completionRate * 100 * 0.3;

    // Update trust score in tukang_locations collection
    await db
      .collection("tukang_locations")
      .doc(tukangId)
      .update({
        trustScore: Math.round(trustScore * 100) / 100, // Round to 2 decimal places
        trustScoreUpdatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

    console.log(
      `✅ Trust score updated for tukang ${tukangId}: ${trustScore.toFixed(2)}`
    );
    console.log(`   - Avg Rating: ${avgRating.toFixed(2)}`);
    console.log(`   - Completion Rate: ${(completionRate * 100).toFixed(2)}%`);

    return { trustScore, avgRating, completionRate };
  } catch (error) {
    console.error(`❌ Error updating trust score for ${tukangId}:`, error);
    throw error;
  }
};

