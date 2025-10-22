import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import { NotesController } from './notes.controller';
import { CreateNoteRequest, UpdateNoteRequest, createNoteSchema, updateNoteSchema } from './notes.types';
import { validateBody } from './validation.middleware';

const router = Router();
const notesController = new NotesController();

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

router.delete(
  '/:id',
  authenticateToken,
  notesController.deleteNote
);

router.get(
  '/:id',
  authenticateToken,
  notesController.getNote
);

router.get(
  '/',
  authenticateToken,
  notesController.findNotes
);

router.post(
  '/authors',
  authenticateToken,
  notesController.getAuthors
);

router.get(
  '/:id/workspaces',
  authenticateToken,
  notesController.getWorkspacesForNote
)

router.post(
  '/:id/share',
  authenticateToken,
  notesController.shareNoteToWorkspace
);

export default router;
