import path from 'path';

// Directory constants (resolved to absolute paths for security)
export const IMAGES_DIR = path.resolve(process.cwd(), 'uploads', 'images');
export const UPLOADS_DIR = path.resolve(process.cwd(), 'uploads');

// File size limits
export const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

// Other constants can be added here as needed

