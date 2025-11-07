// Jest setup file - runs before all tests
process.env.MONGOMS_VERSION = '6.0.4';
process.env.MONGOMS_SKIP_MD5 = '1';
if (!process.env.JWT_SECRET) {
  process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-only';
}

if (!process.env.GOOGLE_CLIENT_ID) {
  process.env.GOOGLE_CLIENT_ID = 'test-google-client-id';
}

// Set FIREBASE_JSON environment variable for all tests
// NOTE: These are FAKE test values only
process.env.FIREBASE_JSON = JSON.stringify({
  type: 'service_account',
  project_id: 'fake-test-project-do-not-use',
  private_key_id: 'fake-test-key-id',
  private_key: '-----BEGIN PRIVATE KEY-----\nFAKE_TEST_KEY_ONLY\n-----END PRIVATE KEY-----\n',
  client_email: 'fake-test@fake-test-project.iam.gserviceaccount.com',
  client_id: '000000000000000000000',
  auth_uri: 'https://accounts.google.com/o/oauth2/auth',
  token_uri: 'https://oauth2.googleapis.com/token',
  auth_provider_x509_cert_url: 'https://www.googleapis.com/oauth2/v1/certs',
  client_x509_cert_url: 'https://www.googleapis.com/robot/v1/metadata/x509/fake-test%40fake-test-project.iam.gserviceaccount.com',
  universe_domain: 'googleapis.com'
});

// Mock Firebase Admin globally for all tests
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

// Export mock for use in tests
export { mockSend, mockMessaging };

