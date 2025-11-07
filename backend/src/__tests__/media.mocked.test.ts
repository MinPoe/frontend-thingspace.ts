/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import fs from 'fs';
import path from 'path';
import os from 'os';

import { mediaService } from '../media.service';
import { IMAGES_DIR } from '../constants';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

// ---------------------------
// Test suite
// ---------------------------
describe('Media API – Mocked Tests (Jest Mocks)', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;
  let app: ReturnType<typeof createTestApp>;

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');

    // Ensure images directory exists
    if (!fs.existsSync(IMAGES_DIR)) {
      fs.mkdirSync(IMAGES_DIR, { recursive: true });
    }
    
    // Create app after DB connection
    app = createTestApp();
  });

  // Clean mocks every test
  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  // Tear down DB
  afterAll(async () => {
    await mongoose.disconnect();
    await mongo.stop();
  });

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase(app);
  });

  describe('POST /api/media/upload - Upload Image, with mocks', () => {
    test('500 – returns 500 when MediaService.saveImage throws error', async () => {
      // Mocked behavior: MediaService.saveImage throws error
      // Input: image file
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(mediaService, 'saveImage').mockRejectedValue(new Error('Failed to save image'));

      const testImagePath = path.resolve(IMAGES_DIR, 'test-upload.png');
      // Ensure directory exists
      if (!fs.existsSync(IMAGES_DIR)) {
        fs.mkdirSync(IMAGES_DIR, { recursive: true });
      }
      fs.writeFileSync(testImagePath, Buffer.from('fake-image-data'));

      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .attach('media', testImagePath);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to save image');

      // Clean up
      if (fs.existsSync(testImagePath)) {
        fs.unlinkSync(testImagePath);
      }
    });

    test('500 – returns 500 with fallback message when error has no message', async () => {
      // Mocked behavior: MediaService.saveImage throws error with empty message
      // Input: image file
      // Expected status code: 500
      // Expected behavior: error handled gracefully with fallback message
      // Expected output: error message "Failed to upload profile picture"
      const errorWithoutMessage = new Error('');
      jest.spyOn(mediaService, 'saveImage').mockRejectedValue(errorWithoutMessage);

      const testImagePath = path.resolve(IMAGES_DIR, 'test-upload.png');
      // Ensure directory exists
      if (!fs.existsSync(IMAGES_DIR)) {
        fs.mkdirSync(IMAGES_DIR, { recursive: true });
      }
      fs.writeFileSync(testImagePath, Buffer.from('fake-image-data'));

      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .attach('media', testImagePath);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to upload profile picture');

      // Clean up
      if (fs.existsSync(testImagePath)) {
        fs.unlinkSync(testImagePath);
      }
    });

    test('500 – handles non-Error thrown value', async () => {
      // Mocked behavior: MediaService.saveImage throws non-Error value
      // Input: image file
      // Expected status code: 500 or handled by error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(mediaService, 'saveImage').mockRejectedValue('String error');

      const testImagePath = path.resolve(IMAGES_DIR, 'test-upload.png');
      // Ensure directory exists
      if (!fs.existsSync(IMAGES_DIR)) {
        fs.mkdirSync(IMAGES_DIR, { recursive: true });
      }
      fs.writeFileSync(testImagePath, Buffer.from('fake-image-data'));

      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .attach('media', testImagePath);

      expect(res.status).toBeGreaterThanOrEqual(500);

      // Clean up
      if (fs.existsSync(testImagePath)) {
        fs.unlinkSync(testImagePath);
      }
    });

    test('200 – recreates images directory when missing before upload route is registered', async () => {
      // Mocked behavior: IMAGES_DIR does not exist when storage module initializes
      // Input: image upload request with directory removed before re-importing storage module
      // Expected status code: 200
      // Expected behavior: storage.ts creates directory via fs.mkdirSync and upload succeeds
      // Expected output: upload succeeds and mkdirSync invoked with IMAGES_DIR

      const originalExistsSync = fs.existsSync.bind(fs);
      const originalMkdirSync = fs.mkdirSync.bind(fs);

      if (fs.existsSync(IMAGES_DIR)) {
        fs.rmSync(IMAGES_DIR, { recursive: true, force: true });
      }

      const existsSyncSpy = jest.spyOn(fs, 'existsSync').mockImplementation(targetPath => {
        if (targetPath === IMAGES_DIR) {
          return false;
        }
        return originalExistsSync(targetPath as any);
      });
      const mkdirSyncSpy = jest.spyOn(fs, 'mkdirSync').mockImplementation((targetPath, options) => {
        return originalMkdirSync(targetPath as any, options as any);
      });

      try {
        jest.isolateModules(() => {
          // Re-require storage module so the top-level directory creation runs with our spies
          require('../storage');
        });

        // Check spy calls before restoring
        expect(existsSyncSpy).toHaveBeenCalledWith(IMAGES_DIR);
        expect(mkdirSyncSpy).toHaveBeenCalledWith(IMAGES_DIR, { recursive: true });
        
        // Restore spies so we can check the real directory state
        existsSyncSpy.mockRestore();
        mkdirSyncSpy.mockRestore();
        
        expect(fs.existsSync(IMAGES_DIR)).toBe(true);

        const tmpFilePath = path.join(os.tmpdir(), `storage-recreate-${Date.now()}.png`);
        fs.writeFileSync(tmpFilePath, Buffer.from('fake-image-data'));

        try {
          const res = await request(app)
            .post('/api/media/upload')
            .set('Authorization', `Bearer ${testData.testUserToken}`)
            .attach('media', tmpFilePath);

          expect(res.status).toBe(200);
        } finally {
          if (fs.existsSync(tmpFilePath)) {
            fs.unlinkSync(tmpFilePath);
          }
        }
      } catch (error) {
        // Ensure spies are restored even if test fails
        existsSyncSpy.mockRestore();
        mkdirSyncSpy.mockRestore();
        throw error;
      }
    });
  });

  describe('Media Service - Direct Service Tests with Mocks', () => {
    beforeEach(async () => {
      testData = await setupTestDatabase(app);
    });

    test('saveImage throws error when path validation fails', async () => {
      // Input: file path that fails validation (line 30-31)
      // Expected behavior: validatePath returns false, error is thrown
      // Expected output: Error "Invalid file path"
      const testFile = path.resolve(IMAGES_DIR, 'test-validation.png');
      fs.writeFileSync(testFile, Buffer.from('test data'));

      // Mock validatePath to return false to trigger the validation error
      const validatePathSpy = jest.spyOn(mediaService as any, 'validatePath').mockReturnValue(false);

      try {
        await expect(
          mediaService.saveImage(testFile, testData.testUserId)
        ).rejects.toThrow('Invalid file path');

        // Verify validatePath was called
        expect(validatePathSpy).toHaveBeenCalled();
      } finally {
        // Restore original function
        validatePathSpy.mockRestore();
        // Clean up if file still exists
        if (fs.existsSync(testFile)) {
          fs.unlinkSync(testFile);
        }
      }
    });

    test('saveImage cleans up file when rename fails and file exists', async () => {
      // Input: file that exists but rename fails
      // Expected behavior: File is deleted if it exists when error occurs (line 18: fs.unlinkSync)
      // Expected output: Error thrown, file cleaned up
      const testFile = path.resolve(IMAGES_DIR, 'test-cleanup.png');
      fs.writeFileSync(testFile, Buffer.from('test data'));

      // Mock fs.renameSync to throw error
      const renameSyncSpy = jest.spyOn(fs, 'renameSync').mockImplementation(() => {
        throw new Error('Rename failed');
      });

      try {
        // Try to save - rename will fail due to mock
        // The file exists, so the cleanup branch (line 17-18) will be executed
        await expect(
          mediaService.saveImage(testFile, testData.testUserId)
        ).rejects.toThrow('Failed to save profile picture');

        // File should be cleaned up (deleted) - line 18
        expect(fs.existsSync(testFile)).toBe(false);
        expect(renameSyncSpy).toHaveBeenCalled();
      } finally {
        // Restore original function
        renameSyncSpy.mockRestore();
        // Clean up if file still exists
        if (fs.existsSync(testFile)) {
          fs.unlinkSync(testFile);
        }
      }
    });

    test('deleteImage catches and logs error when unlinkSync fails', async () => {
      // Input: URL that triggers error in deleteImage
      // Expected behavior: Error is caught and logged (line 33: console.error)
      // Expected output: No error thrown, error logged
      const testFile = path.resolve(IMAGES_DIR, 'test-delete-error.png');
      fs.writeFileSync(testFile, Buffer.from('test data'));
      // Use relative path from process.cwd() as deleteImage expects
      const relativePath = path.relative(process.cwd(), testFile);
      const url = relativePath.replace(/\\/g, '/');
      const constructedPath = path.resolve(process.cwd(), url);
      
      // Create file at the path that will be checked
      if (!fs.existsSync(path.dirname(constructedPath))) {
        fs.mkdirSync(path.dirname(constructedPath), { recursive: true });
      }
      fs.writeFileSync(constructedPath, Buffer.from('test data'));

      // Mock validatePath to return true (so we can test the error handling)
      // We need to access the private method via bracket notation or spy on the class
      const validatePathSpy = jest.spyOn(mediaService as any, 'validatePath').mockReturnValue(true);
      
      // Mock fs.unlinkSync to throw error
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      const existsSyncSpy = jest.spyOn(fs, 'existsSync').mockReturnValue(true);
      const unlinkSyncSpy = jest.spyOn(fs, 'unlinkSync').mockImplementation(() => {
        throw new Error('Unlink failed');
      });

      try {
        // deleteImage should catch the error and log it
        await expect(mediaService.deleteImage(url)).resolves.not.toThrow();
        
        // Verify error was logged
        expect(consoleErrorSpy).toHaveBeenCalledWith('Failed to delete old profile picture:', expect.any(Error));
        expect(unlinkSyncSpy).toHaveBeenCalled();
      } finally {
        // Restore
        validatePathSpy.mockRestore();
        existsSyncSpy.mockRestore();
        unlinkSyncSpy.mockRestore();
        consoleErrorSpy.mockRestore();
        // Clean up
        if (fs.existsSync(constructedPath)) {
          try {
            fs.unlinkSync(constructedPath);
          } catch {
            // Ignore
          }
        }
        if (fs.existsSync(testFile)) {
          fs.unlinkSync(testFile);
        }
      }
    });

    test('deleteAllUserImages returns early when IMAGES_DIR does not exist', async () => {
      // Input: IMAGES_DIR does not exist
      // Expected behavior: Method returns early (line 39-40: return)
      // Expected output: No error thrown, method completes
      const originalExistsSync = fs.existsSync;
      const existsSyncSpy = jest.spyOn(fs, 'existsSync').mockImplementation((filePath) => {
        // Return false for IMAGES_DIR, use original for other paths
        if (filePath === IMAGES_DIR) {
          return false;
        }
        return originalExistsSync(filePath);
      });

      try {
        // deleteAllUserImages should return early without error
        await expect(
          mediaService.deleteAllUserImages(testData.testUserId)
        ).resolves.not.toThrow();
        
        // Verify existsSync was called with IMAGES_DIR
        expect(existsSyncSpy).toHaveBeenCalledWith(IMAGES_DIR);
      } finally {
        existsSyncSpy.mockRestore();
      }
    });

    test('deleteAllUserImages catches and logs error when readdirSync fails', async () => {
      // Input: Operation that causes readdirSync to fail
      // Expected behavior: Error is caught and logged (line 48: console.error)
      // Expected output: No error thrown, error logged
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
      const readdirSyncSpy = jest.spyOn(fs, 'readdirSync').mockImplementation(() => {
        throw new Error('readdirSync failed');
      });

      try {
        // deleteAllUserImages should catch the error and log it
        await expect(
          mediaService.deleteAllUserImages(testData.testUserId)
        ).resolves.not.toThrow();
        
        // Verify error was logged - line 48
        expect(consoleErrorSpy).toHaveBeenCalledWith('Failed to delete user images:', expect.any(Error));
        expect(readdirSyncSpy).toHaveBeenCalled();
      } finally {
        // Restore
        readdirSyncSpy.mockRestore();
        consoleErrorSpy.mockRestore();
      }
    });

    test('storage.ts creates IMAGES_DIR when it does not exist', async () => {
      // Input: IMAGES_DIR does not exist (tests storage.ts lines 8-10)
      // Expected behavior: Directory is created when storage module loads
      // Expected output: fs.mkdirSync is called with IMAGES_DIR
      // Note: This tests the module-level code in storage.ts
      
      // Set up mocks BEFORE isolating modules
      const originalExistsSync = fs.existsSync;
      const originalMkdirSync = fs.mkdirSync;
      
      const existsSyncSpy = jest.spyOn(fs, 'existsSync').mockImplementation((dirPath) => {
        // Return false for IMAGES_DIR to simulate it doesn't exist
        if (dirPath === IMAGES_DIR) {
          return false;
        }
        // Use original for other paths
        return originalExistsSync.call(fs, dirPath);
      });
      
      const mkdirSyncSpy = jest.spyOn(fs, 'mkdirSync').mockImplementation((dirPath, options) => {
        // Call original to actually create directory (needed for cleanup)
        return originalMkdirSync.call(fs, dirPath, options);
      });

      try {
        // Use jest.isolateModules to ensure module code executes with our mocks
        jest.isolateModules(() => {
          // Re-require storage module - this will execute lines 8-10
          // The module will call fs.existsSync(IMAGES_DIR) which returns false (mocked)
          // Then it will call fs.mkdirSync(IMAGES_DIR, { recursive: true })
          require('../storage');
        });
        
        // Verify existsSync was called with IMAGES_DIR - line 8
        expect(existsSyncSpy).toHaveBeenCalledWith(IMAGES_DIR);
        // Verify mkdirSync was called with IMAGES_DIR and recursive: true - line 9
        expect(mkdirSyncSpy).toHaveBeenCalledWith(IMAGES_DIR, { recursive: true });
      } finally {
        // Restore
        mkdirSyncSpy.mockRestore();
        existsSyncSpy.mockRestore();
      }
    });
  });
});

