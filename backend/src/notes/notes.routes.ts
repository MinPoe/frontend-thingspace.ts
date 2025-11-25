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
  asyncHandler(authenticateToken),
  validateBody<CreateNoteRequest>(createNoteSchema),
  asyncHandler(notesController.createNote.bind(notesController))
);


router.put(
  '/:id',
  asyncHandler(authenticateToken),
  validateBody<UpdateNoteRequest>(updateNoteSchema),
  asyncHandler(notesController.updateNote.bind(notesController))
);

router.delete(
  '/:id',
  asyncHandler(authenticateToken),
  asyncHandler(notesController.deleteNote.bind(notesController))
);


router.get(
  '/:id/workspaces',
  asyncHandler(authenticateToken),
  asyncHandler(notesController.getWorkspacesForNote.bind(notesController))
)

router.get(
  '/:id',
  asyncHandler(authenticateToken),
  asyncHandler(notesController.getNote.bind(notesController))
);

router.get(
  '/',
  asyncHandler(authenticateToken),
  asyncHandler(notesController.findNotes.bind(notesController))
);

router.post(
  '/:id/share',
  asyncHandler(authenticateToken),
  asyncHandler(notesController.shareNoteToWorkspace.bind(notesController))
);

router.post(
  '/:id/copy',
  asyncHandler(authenticateToken),
  asyncHandler(notesController.copyNoteToWorkspace.bind(notesController))
);

export default router;
