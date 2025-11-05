/// <reference types="jest" />
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { connectDB, disconnectDB } from '../database';

describe('Database Connection', () => {
  let mongo: MongoMemoryServer;
  const originalEnv = process.env;

  beforeEach(() => {
    // Clear mongoose connection state
    if (mongoose.connection.readyState !== 0) {
      mongoose.connection.close();
    }
    // Reset process.env
    process.env = { ...originalEnv };
    // Reset process.exitCode
    process.exitCode = undefined;
  });

  afterEach(async () => {
    // Restore original env
    process.env = originalEnv;
    // Clean up connections
    if (mongoose.connection.readyState !== 0) {
      await mongoose.disconnect();
    }
    if (mongo) {
      await mongo.stop();
    }
  });

  describe('connectDB', () => {
    test('successfully connects to MongoDB when MONGODB_URI is set', async () => {
      // Input: valid MONGODB_URI
      // Expected behavior: mongoose connects successfully
      mongo = await MongoMemoryServer.create();
      process.env.MONGODB_URI = mongo.getUri();

      await connectDB();

      // Verify connection
      expect(mongoose.connection.readyState).toBe(1); // 1 = connected
      
      // Clean up
      await mongoose.disconnect();
    });

    test('handles error when MONGODB_URI is not defined', async () => {
      // Input: MONGODB_URI is undefined
      // Expected behavior: catches error internally, sets process.exitCode to 1
      delete process.env.MONGODB_URI;

      // Mock console.error to verify error is logged
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      // connectDB() doesn't throw - it catches the error internally
      await connectDB();
      
      // Verify process.exitCode is set to 1
      expect(process.exitCode).toBe(1);
      
      // Verify error was logged
      expect(consoleErrorSpy).toHaveBeenCalledWith('❌ Failed to connect to MongoDB:', expect.any(Error));
      
      consoleErrorSpy.mockRestore();
    });

    test('handles connection failure gracefully', async () => {
      // Input: invalid MONGODB_URI that causes mongoose.connect to fail
      // Expected behavior: catches error, sets process.exitCode to 1
      process.env.MONGODB_URI = 'mongodb://invalid-host:27017/test';

      // Mock mongoose.connect to immediately reject to avoid timeout
      const connectSpy = jest.spyOn(mongoose, 'connect').mockRejectedValueOnce(new Error('Connection failed'));
      
      // Mock console.error to verify error is logged
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      await connectDB();

      // Verify process.exitCode is set to 1
      expect(process.exitCode).toBe(1);
      
      // Verify error was logged
      expect(consoleErrorSpy).toHaveBeenCalledWith('❌ Failed to connect to MongoDB:', expect.any(Error));
      
      connectSpy.mockRestore();
      consoleErrorSpy.mockRestore();
    });
  });

  describe('disconnectDB', () => {
    test('successfully disconnects from MongoDB when connected', async () => {
      // Input: connected mongoose instance
      // Expected behavior: closes connection successfully
      mongo = await MongoMemoryServer.create();
      const uri = mongo.getUri();
      await mongoose.connect(uri);
      
      expect(mongoose.connection.readyState).toBe(1); // Verify connected

      await disconnectDB();

      // Verify disconnected
      expect(mongoose.connection.readyState).toBe(0); // 0 = disconnected
    });

    test('handles disconnect gracefully when already disconnected', async () => {
      // Input: mongoose not connected
      // Expected behavior: completes without error (mongoose.disconnect doesn't throw when already disconnected)
      if (mongoose.connection.readyState !== 0) {
        await mongoose.disconnect();
      }

      // Should complete without throwing
      await expect(disconnectDB()).resolves.not.toThrow();
    });

    test('handles disconnect error gracefully when connection.close fails', async () => {
      // Input: mongoose connection that fails to close
      // Expected behavior: catches error and logs it
      mongo = await MongoMemoryServer.create();
      const uri = mongo.getUri();
      await mongoose.connect(uri);

      // Mock mongoose.connection.close to throw an error
      const closeSpy = jest.spyOn(mongoose.connection, 'close').mockRejectedValueOnce(new Error('Close failed'));
      
      // Mock console.error to verify it's called
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      await disconnectDB();

      // Verify error was logged
      expect(consoleErrorSpy).toHaveBeenCalledWith('❌ Error disconnecting from MongoDB:', expect.any(Error));
      
      closeSpy.mockRestore();
      consoleErrorSpy.mockRestore();
      
      // Clean up
      await mongoose.disconnect();
    });
  });
});

