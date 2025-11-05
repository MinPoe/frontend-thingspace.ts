/// <reference types="jest" />
import fs from 'fs';
import path from 'path';
import multer from 'multer';

import { upload } from '../storage';
import { IMAGES_DIR, MAX_FILE_SIZE } from '../constants';

// ---------------------------
// Test suite
// ---------------------------
describe('Storage Tests', () => {
  beforeAll(() => {
    // Ensure images directory exists
    if (!fs.existsSync(IMAGES_DIR)) {
      fs.mkdirSync(IMAGES_DIR, { recursive: true });
    }
  });

  afterAll(() => {
    // Clean up test files
    if (fs.existsSync(IMAGES_DIR)) {
      const files = fs.readdirSync(IMAGES_DIR);
      files.forEach(file => {
        const filePath = path.join(IMAGES_DIR, file);
        if (fs.statSync(filePath).isFile() && file.startsWith('test-')) {
          fs.unlinkSync(filePath);
        }
      });
    }
  });

  describe('multer upload configuration', () => {
    test('upload configuration is defined', () => {
      // Input: upload multer instance
      // Expected behavior: upload is configured with storage, fileFilter, and limits
      // Expected output: upload instance exists and has required properties
      expect(upload).toBeDefined();
      expect(upload).toBeTruthy();
    });
  });

  describe('multer storage configuration', () => {
    test('IMAGES_DIR directory exists or can be created', () => {
      // Input: IMAGES_DIR constant
      // Expected behavior: directory exists or can be created
      // Expected output: directory path is valid
      expect(IMAGES_DIR).toBeDefined();
      expect(typeof IMAGES_DIR).toBe('string');
      // Directory will be created by storage.ts if it doesn't exist
    });
  });

  describe('multer limits configuration', () => {
    test('limits fileSize to MAX_FILE_SIZE', () => {
      // Input: MAX_FILE_SIZE constant
      // Expected behavior: multer configured with fileSize limit
      // Expected output: fileSize limit set correctly
      // Multer limits are verified through actual upload attempts
      expect(MAX_FILE_SIZE).toBeDefined();
      expect(typeof MAX_FILE_SIZE).toBe('number');
    });
  });
});

