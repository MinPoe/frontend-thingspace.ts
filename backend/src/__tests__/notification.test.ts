/// <reference types="jest" />
import admin from 'firebase-admin';
import { notificationService } from '../notification.service';

// Mock Firebase Admin before importing the service
const mockSend = jest.fn();
const mockMessaging = {
  send: mockSend,
};

jest.mock('firebase-admin', () => {
  return {
    __esModule: true,
    default: {
      messaging: jest.fn(() => mockMessaging),
      credential: {
        cert: jest.fn(),
      },
      initializeApp: jest.fn(),
    },
  };
});

// Mock the logger to avoid console output in tests
jest.mock('../logger.util', () => ({
  __esModule: true,
  default: {
    info: jest.fn(),
    error: jest.fn(),
  },
}));

describe('notification.service', () => {
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

