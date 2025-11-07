import { Express, Request } from 'express';
import crypto from 'crypto';
import fs from 'fs';
import multer from 'multer';
import path from 'path';

import { IMAGES_DIR, MAX_FILE_SIZE } from './constants';

// IMAGES_DIR is a constant resolved from constants.ts, safe to use
if (!fs.existsSync(IMAGES_DIR)) {
  fs.mkdirSync(IMAGES_DIR, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, IMAGES_DIR);
  },
  filename: (req, file, cb) => {
    // Use crypto for secure random generation instead of Math.random()
    const randomBytes = crypto.randomBytes(4).readUInt32BE(0);
    const uniqueSuffix = Date.now() + '-' + randomBytes;
    cb(null, `${uniqueSuffix}${path.extname(file.originalname as string)}`);
  },
});

const fileFilter = (
  req: Request,
  file: Express.Multer.File,
  cb: multer.FileFilterCallback
) => {
  if (file.mimetype.startsWith('image/')) {
    cb(null, true);
  } else {
    cb(new Error('Only image files are allowed!'));
  }
};

export const upload = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: MAX_FILE_SIZE,
  },
});
