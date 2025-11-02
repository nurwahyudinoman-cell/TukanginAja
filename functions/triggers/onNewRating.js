const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { updateTrustScore } = require("../utils/trustCalculator");
const { logEvent } = require("../utils/logger");

/**
 * Cloud Function triggered when a new rating is created
 * Updates trust score for the rated tukang and logs the event
 */
exports.onNewRating = functions.firestore
  .document("ratings/{ratingId}")
  .onCreate(async (snap, context) => {
    const rating = snap.data();
    const ratingId = context.params.ratingId;
    const tukangId = rating.tukangId;

    console.log(`⭐ New rating created: ${ratingId} for tukang ${tukangId}`);

    if (!tukangId) {
      console.log("⚠️ No tukang ID found in rating, skipping");
      return null;
    }

    try {
      // Update trust score
      const trustScoreResult = await updateTrustScore(tukangId);

      // Log the event
      await logEvent("RatingAdded", {
        ratingId: ratingId,
        tukangId: tukangId,
        userId: rating.userId || "",
        orderId: rating.orderId || "",
        score: rating.score || 0,
        trustScore: trustScoreResult.trustScore,
        avgRating: trustScoreResult.avgRating,
        completionRate: trustScoreResult.completionRate,
        timestamp: Date.now(),
      });

      console.log(
        `✅ Trust score updated for tukang ${tukangId}: ${trustScoreResult.trustScore.toFixed(2)}`
      );
    } catch (error) {
      console.error(`❌ Error processing new rating:`, error);
      // Log the error event
      await logEvent("RatingError", {
        ratingId: ratingId,
        tukangId: tukangId,
        error: error.message,
        timestamp: Date.now(),
      });
    }
  });

