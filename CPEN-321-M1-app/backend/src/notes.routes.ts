import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import { NotesController } from './notes.controller';
import { CreateNoteRequest, UpdateNoteRequest, createNoteSchema, updateNoteSchema, minimalCreateNoteSchema, minimalUpdateNoteSchema } from './notes.types';
import { validateBody } from './validation.middleware';

const router = Router();
const notesController = new NotesController();

// Option 1: With validation (recommended)
router.post(
  '/',
  authenticateToken,
  validateBody<CreateNoteRequest>(createNoteSchema),
  notesController.createNote
);

router.put(
  '/:id',
  authenticateToken,
  validateBody<UpdateNoteRequest>(updateNoteSchema),
  notesController.updateNote
);

// Option 2: Minimal validation (uncomment to use)
// router.post(
//   '/',
//   authenticateToken,
//   validateBody<CreateNoteRequest>(minimalCreateNoteSchema),
//   notesController.createNote
// );

// router.put(
//   '/:id',
//   authenticateToken,
//   validateBody<UpdateNoteRequest>(minimalUpdateNoteSchema),
//   notesController.updateNote
// );

// Option 3: No validation (uncomment to use)
// router.post(
//   '/',
//   authenticateToken,
//   notesController.createNote
// );

// router.put(
//   '/:id',
//   authenticateToken,
//   notesController.updateNote
// );

router.delete(
  '/:id',
  authenticateToken,
  notesController.deleteNote
);

router.get(
  '/',
  authenticateToken,
  notesController.getNotes
);

export default router;
