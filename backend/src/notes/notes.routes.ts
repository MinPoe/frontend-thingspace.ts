import { Router } from 'express';

import { authenticateToken } from '../authentication/auth.middleware';
import { NotesController } from './notes.controller';
import { CreateNoteRequest, UpdateNoteRequest, createNoteSchema, updateNoteSchema } from './notes.types';
import { validateBody } from '../middleware/validation.middleware';
import { asyncHandler } from '../utils/asyncHandler.util';

const router = Router();
const notesController = new NotesController();

router.post(
  '/',
  authenticateToken,
  validateBody<CreateNoteRequest>(createNoteSchema),
  asyncHandler(notesController.createNote.bind(notesController))
);


router.put(
  '/:id',
  authenticateToken,
  validateBody<UpdateNoteRequest>(updateNoteSchema),
  asyncHandler(notesController.updateNote.bind(notesController))
);

router.delete(
  '/:id',
  authenticateToken,
  asyncHandler(notesController.deleteNote.bind(notesController))
);

router.get(
  '/:id',
  authenticateToken,
  asyncHandler(notesController.getNote.bind(notesController))
);

router.get(
  '/',
  authenticateToken,
  asyncHandler(notesController.findNotes.bind(notesController))
);

router.get(
  '/:id/workspaces',
  authenticateToken,
  asyncHandler(notesController.getWorkspacesForNote.bind(notesController))
)

router.post(
  '/:id/share',
  authenticateToken,
  asyncHandler(notesController.shareNoteToWorkspace.bind(notesController))
);

router.post(
  '/:id/copy',
  authenticateToken,
  asyncHandler(notesController.copyNoteToWorkspace.bind(notesController))
);

export default router;
