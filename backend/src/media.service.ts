import fs from 'fs';
import path from 'path';

import { IMAGES_DIR } from './constants';

// Using static methods only - class structure for organization
export class MediaService {
  /**
   * Validates that a file path is within the IMAGES_DIR directory
   * to prevent path traversal attacks
   */
  private static validatePath(filePath: string): boolean {
    const resolvedPath = path.resolve(filePath);
    // IMAGES_DIR is already an absolute path from constants.ts
    return resolvedPath.startsWith(IMAGES_DIR + path.sep) || resolvedPath === IMAGES_DIR;
  }

  static saveImage(filePath: string, userId: string): Promise<string> {
    // Validate the source file path is safe (from multer, should be in temp directory)
    const resolvedFilePath = path.resolve(filePath);
    
    try {
      const fileExtension = path.extname(resolvedFilePath);
      const fileName = `${userId}-${Date.now()}${fileExtension}`;
      const newPath = path.join(IMAGES_DIR, fileName);
      const resolvedNewPath = path.resolve(newPath);

      // Ensure the new path is within IMAGES_DIR
      if (!this.validatePath(resolvedNewPath)) {
        throw new Error('Invalid file path');
      }

      fs.renameSync(resolvedFilePath, resolvedNewPath);

      return Promise.resolve(resolvedNewPath.split(path.sep).join('/'));
    } catch (error) {
      // Clean up the uploaded file if it exists
      if (fs.existsSync(resolvedFilePath)) {
        fs.unlinkSync(resolvedFilePath);
      }
      return Promise.reject(new Error(`Failed to save profile picture: ${error instanceof Error ? error.message : String(error)}`));
    }
  }

  static deleteImage(url: string): Promise<void> {
    try {
      // Resolve the URL to a file path and validate it's within IMAGES_DIR
      const normalizedUrl = url.replace(/\\/g, '/');
      const urlPath = normalizedUrl.startsWith('/') ? normalizedUrl.substring(1) : normalizedUrl;
      const filePath = path.resolve(process.cwd(), urlPath);

      // Validate path is within IMAGES_DIR before deletion
      if (!this.validatePath(filePath)) {
        return Promise.resolve(); // Silently skip if path is invalid (security)
      }

      if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
      }
      return Promise.resolve();
    } catch (error) {
      console.error('Failed to delete old profile picture:', error);
      return Promise.resolve();
    }
  }

  static async deleteAllUserImages(userId: string): Promise<void> {
    try {
      if (!fs.existsSync(IMAGES_DIR)) {
        return;
      }

      const files = fs.readdirSync(IMAGES_DIR);
      const userFiles = files.filter(file => file.startsWith(userId + '-'));

      // Delete files using relative paths from process.cwd()
      await Promise.all(
        userFiles.map(file => {
          const filePath = path.join(IMAGES_DIR, file);
          // Convert absolute path to relative path from process.cwd() for deleteImage
          const relativePath = path.relative(process.cwd(), filePath);
          return this.deleteImage(relativePath);
        })
      );
    } catch (error) {
      console.error('Failed to delete user images:', error);
    }
  }
}
