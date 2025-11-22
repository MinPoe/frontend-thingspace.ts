/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import fs from 'fs';
import path from 'path';
import os from 'os';

import type { Request, Response } from 'express';

import { MediaController } from '../../media/media.controller';
import * as sanitizeModule from '../../utils/sanitizeInput.util';
import { IMAGES_DIR, MAX_FILE_SIZE } from '../../utils/constants';
import { mediaService } from '../../media/media.service';
import { createTestApp, setupTestDatabase, TestData } from '../test-utils/test-helpers';

// ---------------------------
// Test suite
// ---------------------------
describe('Media API – Normal Tests (No Mocking)', () => {
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

  // Tear down DB
  afterAll(async () => {
    // Ensure mongoose connection is properly closed
    if (mongoose.connection.readyState !== 0) {
      await mongoose.disconnect();
    }
    
    // Stop MongoDB memory server
    if (mongo) {
      await mongo.stop({ doCleanup: true, force: true });
    }

    // Clean up test images
    try {
      if (fs.existsSync(IMAGES_DIR)) {
        const files = fs.readdirSync(IMAGES_DIR);
        files.forEach(file => {
          const filePath = path.join(IMAGES_DIR, file);
          try {
            if (fs.statSync(filePath).isFile()) {
              fs.unlinkSync(filePath);
            }
          } catch (error) {
            // Ignore errors during cleanup
          }
        });
      }
    } catch (error) {
      // Ignore errors during cleanup
    }
  }, 10000); // 10 second timeout for cleanup

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase(app);
  });

  describe('POST /api/media/upload - Upload Image', () => {
    afterEach(() => {
      jest.restoreAllMocks();
    });
    test('200 – uploads image successfully', async () => {
      // Input: image file in multipart/form-data
      // Expected status code: 200
      // Expected behavior: image is saved to IMAGES_DIR
      // Expected output: success response with image path
      // Create a test image file with proper path (must be absolute)
      const testImagePath = path.resolve(IMAGES_DIR, 'test-upload.png');
      // Ensure directory exists
      if (!fs.existsSync(IMAGES_DIR)) {
        fs.mkdirSync(IMAGES_DIR, { recursive: true });
      }
      fs.writeFileSync(testImagePath, Buffer.from('fake-image-data'));

      const sanitizeSpy = jest.spyOn(sanitizeModule, 'sanitizeInput');

      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .attach('media', testImagePath);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Image uploaded successfully');
      expect(res.body.data.image).toBeDefined();
      expect(sanitizeSpy).toHaveBeenCalled();
      sanitizeSpy.mock.calls.forEach(([filePath]) => {
        expect(typeof filePath).toBe('string');
        expect(filePath).not.toMatch(/\r|\n/);
      });

      // Clean up uploaded file and test file
      if (fs.existsSync(testImagePath)) {
        fs.unlinkSync(testImagePath);
      }
      if (res.body.data.image && fs.existsSync(res.body.data.image)) {
        fs.unlinkSync(res.body.data.image);
      }

      sanitizeSpy.mockRestore();
    });

    test('400 – returns 400 when no file uploaded', async () => {
      // Input: request without file attachment
      // Expected status code: 400
      // Expected behavior: error message returned
      // Expected output: error message "No file uploaded"
      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(400);
      expect(res.body.message).toBe('No file uploaded');
    });

    test('400 – returns 400 when non-image file is uploaded', async () => {
      // Input: non-image file streamed from memory (tests storage.ts fileFilter)
      // Expected status code: >=400
      // Expected behavior: fileFilter rejects non-image files and request ends with error
      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .attach('media', Buffer.from('not an image'), { filename: 'test.txt', contentType: 'text/plain' });

      expect(res.status).toBeGreaterThanOrEqual(400);
      expect(res.status).toBeLessThan(600);
    });

    test('413 – returns error when file exceeds MAX_FILE_SIZE', async () => {
      // Input: image file larger than MAX_FILE_SIZE
      // Expected status code: 400-500 range due to Multer file size limit
      // Expected behavior: upload middleware rejects oversized file
      // Expected output: error response and no file saved
      const largeFilePath = path.join(os.tmpdir(), `oversized-upload-${Date.now()}.png`);
      const oversizedBuffer = Buffer.alloc(MAX_FILE_SIZE + 1, 0);
      fs.writeFileSync(largeFilePath, oversizedBuffer);

      const res = await request(app)
        .post('/api/media/upload')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .attach('media', largeFilePath);

      expect(res.status).toBeGreaterThanOrEqual(400);
      expect(res.status).toBeLessThan(600);
      expect(res.body.message).toBeDefined();

      if (fs.existsSync(largeFilePath)) {
        fs.unlinkSync(largeFilePath);
      }
    });

    test('401 – returns 401 when user is not authenticated', async () => {
      // Input: request without authentication token
      // Expected status code: 401
      // Expected behavior: authenticateToken middleware blocks request
      const res = await request(app)
        .post('/api/media/upload');

      expect(res.status).toBe(401);
      expect(res.body.error).toBeDefined();
    });

    test('500 – returns 500 when sanitizeInput rejects CRLF path', async () => {
      // Input: calling controller with CRLF-laced file path (simulates API failure branch)
      // Expected status code: 500
      // Expected behavior: sanitizeInput throws error, controller responds 500, saveImage never called
      const maliciousPath = 'C:/temp/evil\r\nfile.png';

      const req = {
        file: { path: maliciousPath },
        user: { _id: new mongoose.Types.ObjectId() },
      } as unknown as Request;

      const jsonMock = jest.fn();
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jsonMock,
      } as unknown as Response;

      const next = jest.fn();
      const saveImageSpy = jest.spyOn(mediaService, 'saveImage');

      const controller = new MediaController();
      await controller.uploadImage(req, res, next);

      expect(res.status).toHaveBeenCalledWith(500);
      expect(jsonMock).toHaveBeenCalledWith({ message: 'CRLF injection attempt detected' });
      expect(saveImageSpy).not.toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();

      saveImageSpy.mockRestore();
    });

    test('401 – returns 401 when user is undefined in request (line 24)', async () => {
      // Input: request object without user field (testing controller logic directly)
      // Expected status code: 401
      // Expected behavior: controller checks req.user and returns 401 if undefined
      // Expected output: error message "User not authenticated"
      const testImagePath = path.resolve(IMAGES_DIR, 'test-no-user.png');
      if (!fs.existsSync(IMAGES_DIR)) {
        fs.mkdirSync(IMAGES_DIR, { recursive: true });
      }
      fs.writeFileSync(testImagePath, Buffer.from('fake-image-data'));

      const req = {
        file: { path: testImagePath },
        user: undefined, // This tests line 24
      } as unknown as Request;

      const jsonMock = jest.fn();
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jsonMock,
      } as unknown as Response;

      const next = jest.fn();

      const controller = new MediaController();
      await controller.uploadImage(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(jsonMock).toHaveBeenCalledWith({ message: 'User not authenticated' });
      expect(next).not.toHaveBeenCalled();

      // Clean up test file
      if (fs.existsSync(testImagePath)) {
        fs.unlinkSync(testImagePath);
      }
    });
  });

  describe('Media Service - Direct Service Tests', () => {
    describe('saveImage', () => {
      test('saveImage throws error when file rename fails', async () => {
        // Input: non-existent file path
        // Expected behavior: Error thrown with message
        // Expected output: Error with "Failed to save profile picture"
        const nonExistentPath = path.resolve(IMAGES_DIR, 'non-existent-file.png');

        await expect(
          mediaService.saveImage(nonExistentPath, testData.testUserId)
        ).rejects.toThrow('Failed to save profile picture');
      });

    });

    describe('deleteImage', () => {
      test('deleteImage deletes file when URL resolves to a valid path within IMAGES_DIR', async () => {
        // Input: URL that resolves to a file within IMAGES_DIR
        // Expected behavior: File is deleted (line 61: fs.unlinkSync)
        // Expected output: File no longer exists
        
        // Create test file in IMAGES_DIR
        const testFile = path.resolve(IMAGES_DIR, 'test-delete.png');
        if (!fs.existsSync(IMAGES_DIR)) {
          fs.mkdirSync(IMAGES_DIR, { recursive: true });
        }
        fs.writeFileSync(testFile, Buffer.from('test data'));
        
        // The new implementation resolves the URL from process.cwd()
        // So we need a relative path from process.cwd() to the file
        // IMAGES_DIR is now an absolute path, so we need to get the relative path
        const relativePath = path.relative(process.cwd(), testFile);
        const url = relativePath.replace(/\\/g, '/');
        
        // Verify the file exists before deletion
        expect(fs.existsSync(testFile)).toBe(true);
        
        await mediaService.deleteImage(url);
        
        // File should be deleted
        expect(fs.existsSync(testFile)).toBe(false);
      });

      test('deleteImage does nothing when URL resolves outside IMAGES_DIR', async () => {
        // Input: URL that resolves to a path outside IMAGES_DIR
        // Expected behavior: validatePath returns false, method returns early
        // Expected output: No error thrown, no file deleted
        const url = '../../some/other/path/image.png';

        await expect(mediaService.deleteImage(url)).resolves.not.toThrow();
      });

      test('deleteImage handles errors gracefully when file operation fails', async () => {
        // Input: URL that causes error during file operation
        // Expected behavior: Error is caught and logged (line 63-65: console.error)
        // Expected output: No error thrown
        // Use a valid path within IMAGES_DIR that might cause issues
        const testFile = path.resolve(IMAGES_DIR, 'test-invalid.png');
        const relativePath = path.relative(process.cwd(), testFile);
        const url = relativePath.replace(/\\/g, '/');

        await expect(mediaService.deleteImage(url)).resolves.not.toThrow();
      });

      test('deleteImage handles URL starting with slash', async () => {
        // Input: URL that starts with '/' (tests line 52: normalizedUrl.startsWith('/') branch)
        // Expected behavior: URL path has leading slash removed before resolution
        // Expected output: File is deleted if it exists
        const testFile = path.resolve(IMAGES_DIR, 'test-slash.png');
        if (!fs.existsSync(IMAGES_DIR)) {
          fs.mkdirSync(IMAGES_DIR, { recursive: true });
        }
        fs.writeFileSync(testFile, Buffer.from('test data'));
        
        // Create URL with leading slash
        const relativePath = path.relative(process.cwd(), testFile);
        const url = '/' + relativePath.replace(/\\/g, '/');
        
        // Verify the file exists before deletion
        expect(fs.existsSync(testFile)).toBe(true);
        
        await mediaService.deleteImage(url);
        
        // File should be deleted
        expect(fs.existsSync(testFile)).toBe(false);
      });
    });

    describe('private validation methods', () => {
      test('safeExistsSync returns false when requireValidation is true and path is invalid (line 22)', () => {
        // Input: path outside IMAGES_DIR with requireValidation=true
        // Expected behavior: validatePath returns false, safeExistsSync returns false (line 22)
        // Expected output: false
        // We need to access the private method through reflection
        const invalidPath = '/etc/passwd'; // Path outside IMAGES_DIR
        
        // Cast to any to access private method
        const service = mediaService as any;
        const result = service.safeExistsSync(invalidPath, true);
        
        expect(result).toBe(false);
      });

      test('safeUnlinkSync throws error when requireValidation is true and path is invalid (line 34)', () => {
        // Input: path outside IMAGES_DIR with requireValidation=true
        // Expected behavior: validatePath returns false, safeUnlinkSync throws error (line 34)
        // Expected output: Error with message "Invalid file path for deletion"
        const invalidPath = '/etc/passwd'; // Path outside IMAGES_DIR
        
        // Cast to any to access private method
        const service = mediaService as any;
        
        expect(() => service.safeUnlinkSync(invalidPath, true)).toThrow('Invalid file path for deletion');
      });

      test('safeRenameSync throws error when requireValidation is true and newPath is invalid (line 45)', () => {
        // Input: newPath outside IMAGES_DIR with requireValidation=true
        // Expected behavior: validatePath returns false, safeRenameSync throws error (line 45)
        // Expected output: Error with message "Invalid destination path"
        const validOldPath = path.resolve(IMAGES_DIR, 'test.png');
        const invalidNewPath = '/etc/test.png'; // Path outside IMAGES_DIR
        
        // Cast to any to access private method
        const service = mediaService as any;
        
        expect(() => service.safeRenameSync(validOldPath, invalidNewPath, true)).toThrow('Invalid destination path');
      });
    });

    describe('deleteAllUserImages', () => {
      test('deleteAllUserImages filters user files and deletes them', async () => {
        // Input: userId with multiple images
        // Expected behavior: Method filters files by userId prefix and deletes them via deleteImage
        // Expected output: User files deleted, other files remain
        const userId = testData.testUserId;
        const file1 = path.resolve(IMAGES_DIR, `${userId}-1.png`);
        const file2 = path.resolve(IMAGES_DIR, `${userId}-2.png`);
        const otherFile = path.resolve(IMAGES_DIR, 'other-user-1.png');

        // Ensure IMAGES_DIR exists
        if (!fs.existsSync(IMAGES_DIR)) {
          fs.mkdirSync(IMAGES_DIR, { recursive: true });
        }

        fs.writeFileSync(file1, Buffer.from('test1'));
        fs.writeFileSync(file2, Buffer.from('test2'));
        fs.writeFileSync(otherFile, Buffer.from('test3'));

        // Verify files exist before deletion
        expect(fs.existsSync(file1)).toBe(true);
        expect(fs.existsSync(file2)).toBe(true);
        expect(fs.existsSync(otherFile)).toBe(true);

        await mediaService.deleteAllUserImages(userId);

        // User files should be deleted
        expect(fs.existsSync(file1)).toBe(false);
        expect(fs.existsSync(file2)).toBe(false);
        // Other user's file should remain
        expect(fs.existsSync(otherFile)).toBe(true);

        // Clean up remaining file
        if (fs.existsSync(otherFile)) {
          fs.unlinkSync(otherFile);
        }
      });

    });
  });
});

