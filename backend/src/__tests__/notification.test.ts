/// <reference types="jest" />
import { notificationService } from '../notification.service';
import { mockSend } from './setup';

// Mock the logger to avoid console output in tests
jest.mock('../logger.util', () => ({
  __esModule: true,
  default: {
    info: jest.fn(),
    error: jest.fn(),
  },
}));

describe('notification.service', () => {
  // Test for FIREBASE_JSON environment variable check (line 7)
  describe('Module initialization', () => {
    test('throws error when FIREBASE_JSON is not set (covers line 7)', () => {
      // Save the original value
      const originalFirebaseJson = process.env.FIREBASE_JSON;
      
      // Temporarily delete FIREBASE_JSON
      delete process.env.FIREBASE_JSON;
      
      // Clear the module cache so it re-imports
      delete require.cache[require.resolve('../notification.service')];
      
      // Try to import the module and expect it to throw
      expect(() => {
        // Use jest.isolateModules to import in isolation
        jest.isolateModules(() => {
          require('../notification.service');
        });
      }).toThrow('FIREBASE_JSON environment variable is not set');
      
      // Restore the original value
      process.env.FIREBASE_JSON = originalFirebaseJson;
    });
  });
  beforeEach(() => {
    jest.clearAllMocks();
    mockSend.mockClear();
  });

  describe('sendNotification', () => {
    test('sends notification successfully', async () => {
      const mockResponse = 'mock-message-id';
      mockSend.mockResolvedValueOnce(mockResponse);

      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body'
      );

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith({
        token: 'test-token',
        notification: {
          title: 'Test Title',
          body: 'Test Body',
        },
        data: {},
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'workspace_invites',
          },
        },
      });
    });

    test('sends notification with data', async () => {
      const mockResponse = 'mock-message-id';
      mockSend.mockResolvedValueOnce(mockResponse);

      const data = { workspaceId: '123', type: 'invite' };
      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body',
        data
      );

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith({
        token: 'test-token',
        notification: {
          title: 'Test Title',
          body: 'Test Body',
        },
        data: data,
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'workspace_invites',
          },
        },
      });
    });

    test('returns false when notification fails', async () => {
      const error = new Error('Failed to send notification');
      mockSend.mockRejectedValueOnce(error);

      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body'
      );

      expect(result).toBe(false);
      expect(mockSend).toHaveBeenCalled();
    });
  });

  describe('isTokenValid', () => {
    test('returns true when token is valid', async () => {
      mockSend.mockResolvedValueOnce('success');

      const result = await notificationService.isTokenValid('valid-token');

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith(
        {
          token: 'valid-token',
          notification: { title: '', body: '' },
        },
        true // dry run
      );
    });

    test('returns false when token is invalid', async () => {
      mockSend.mockRejectedValueOnce(new Error('Invalid token'));

      const result = await notificationService.isTokenValid('invalid-token');

      expect(result).toBe(false);
      expect(mockSend).toHaveBeenCalledWith(
        {
          token: 'invalid-token',
          notification: { title: '', body: '' },
        },
        true // dry run
      );
    });
  });
});

