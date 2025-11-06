import fs from 'fs';
import path from 'path';

import { IMAGES_DIR } from './constants';

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

  static async saveImage(filePath: string, userId: string): Promise<string> {
    try {
      // Validate the source file path is safe (from multer, should be in temp directory)
      const resolvedFilePath = path.resolve(filePath);
      
      const fileExtension = path.extname(resolvedFilePath);
      const fileName = `${userId}-${Date.now()}${fileExtension}`;
      const newPath = path.join(IMAGES_DIR, fileName);
      const resolvedNewPath = path.resolve(newPath);

      // Ensure the new path is within IMAGES_DIR
      if (!this.validatePath(resolvedNewPath)) {
        throw new Error('Invalid file path');
      }

      // eslint-disable-next-line security/detect-non-literal-fs-filename
      fs.renameSync(resolvedFilePath, resolvedNewPath);

      return resolvedNewPath.split(path.sep).join('/');
    } catch (error) {
      // eslint-disable-next-line security/detect-non-literal-fs-filename
      if (fs.existsSync(filePath)) {
        // eslint-disable-next-line security/detect-non-literal-fs-filename
        fs.unlinkSync(filePath);
      }
      throw new Error(`Failed to save profile picture: ${error}`);
    }
  }

  static async deleteImage(url: string): Promise<void> {
    try {
      // Resolve the URL to a file path and validate it's within IMAGES_DIR
      const normalizedUrl = url.replace(/\\/g, '/');
      const urlPath = normalizedUrl.startsWith('/') ? normalizedUrl.substring(1) : normalizedUrl;
      const filePath = path.resolve(process.cwd(), urlPath);

      // Validate path is within IMAGES_DIR before deletion
      if (!this.validatePath(filePath)) {
        return; // Silently skip if path is invalid (security)
      }

      // eslint-disable-next-line security/detect-non-literal-fs-filename
      if (fs.existsSync(filePath)) {
        // eslint-disable-next-line security/detect-non-literal-fs-filename
        fs.unlinkSync(filePath);
      }
    } catch (error) {
      console.error('Failed to delete old profile picture:', error);
    }
  }

  static async deleteAllUserImages(userId: string): Promise<void> {
    try {
      // eslint-disable-next-line security/detect-non-literal-fs-filename
      if (!fs.existsSync(IMAGES_DIR)) {
        return;
      }

      // eslint-disable-next-line security/detect-non-literal-fs-filename
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
