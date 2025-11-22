import admin from 'firebase-admin';
import logger from '../utils/logger.util';

// Initialize Firebase Admin
const firebaseJson = process.env.FIREBASE_JSON;
if (!firebaseJson) {
  throw new Error('FIREBASE_JSON environment variable is not set');
}
const serviceAccount = JSON.parse(firebaseJson);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

export class NotificationService {
  /**
   * Send a push notification to a specific user
   */
  async sendNotification(
    fcmToken: string, 
    title: string, 
    body: string, 
    data?: Record<string, string>
  ): Promise<boolean> {
    try {
      const message: admin.messaging.Message = {
        token: fcmToken,
        notification: {
          title,
          body,
        },
        data: data ?? {},
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'workspace_invites'
          }
        }
      };

      const response = await admin.messaging().send(message);
      logger.info(`Successfully sent notification: ${response}`);
      return true;
    } catch (error) {
      logger.error('Error sending notification:', error);
      return false;
    }
  }

  /**
   * Validate if a token is still valid
   */
  async isTokenValid(fcmToken: string): Promise<boolean> {
    try {
      // Try sending a dry-run message
      await admin.messaging().send({
        token: fcmToken,
        notification: { title: '', body: '' }
      }, true); // dry run
      return true;
    } catch (error) {
      return false;
    }
  }
}

export const notificationService = new NotificationService();

