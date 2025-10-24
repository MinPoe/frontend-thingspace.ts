import { Request, Response } from 'express';
import { CreateNoteRequest, UpdateNoteRequest } from './notes.types';
import { noteService } from './notes.service';

export class NotesController {
  async createNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteData = req.body as CreateNoteRequest;

      // NOTE: We probably need to add some verification thing checking if the author id has access to the workspace
      const newNote = await noteService.createNote(userId, noteData);

      res.status(201).json({
        message: 'Note created successfully',
        data: { note: newNote },
      });
    } catch (error) {
      console.error('Error creating note:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to create note' });
    }
  }

  async updateNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteId = req.params.id;
      const updateData = req.body as UpdateNoteRequest;

      const updatedNote = await noteService.updateNote(noteId, userId, updateData);

      res.status(200).json({
        message: 'Note successfully updated',
        data: { note: updatedNote },
      });
    } catch (error) {
      console.error('Error updating note:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to update note' });
    }
  }

  async deleteNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }
      const noteId = req.params.id;
      const deletedNote = await noteService.deleteNote(noteId, userId);

      res.status(200).json({
        message: 'Note successfully deleted',
        data: { note: deletedNote },
      });
    } catch (error) {
      console.error('Error deleting note:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to delete note' });
    }
  }

  async getNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }
      const noteId = req.params.id;
      const note = await noteService.getNote(noteId, userId);

      if (!note) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }

      res.status(200).json({
        message: 'Note successfully retrieved',
        data: { note },
      });
    } catch (error) {
      console.error('Error retrieving note:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve note' });
    }
  }

  async getAuthors(req: Request, res: Response): Promise<void> {
    try {
      const { noteIds } = req.body;
      
      if (!noteIds || !Array.isArray(noteIds)) {
        res.status(400).json({ error: 'noteIds array is required' });
        return;
      }

      const authors = await noteService.getAuthors(noteIds);

      res.status(200).json({
        message: 'Authors retrieved successfully',
        data: { authors },
      });
    } catch (error) {
      console.error('Error retrieving authors:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve authors' });
    }
  }

  async shareNoteToWorkspace(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteId = req.params.id;
      const { workspaceId } = req.body;

      if (!workspaceId) {
        res.status(400).json({ error: 'workspaceId is required' });
        return;
      }

      const sharedNote = await noteService.shareNoteToWorkspace(noteId, userId, workspaceId);

      res.status(200).json({
        message: 'Note shared to workspace successfully',
        data: { note: sharedNote },
      });
    } catch (error) {
      console.error('Error sharing note:', error);
      if (error instanceof Error) {
        if (error.message === 'Workspace not found') {
          res.status(404).json({ error: 'Workspace not found' });
          return;
        }
        if (error.message === 'Note not found') {
          res.status(404).json({ error: 'Note not found' });
          return;
        }
        if (error.message.includes('Access denied')) {
          res.status(403).json({ error: error.message });
          return;
        }
      }
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to share note' });
    }
  }

  async getWorkspacesForNote(req: Request, res: Response): Promise<void> {
    try {
      const noteId = req.params.id;
      const workspaceId = await noteService.getWorkspacesForNote(noteId);

      res.status(200).json({
        message: 'Workspace retrieved successfully',
        data: { workspaceId },
      });
    } catch (error) {
      console.error('Error retrieving workspace:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve workspace' });
    }
  }

  async findNotes(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      // Required filters: workspaceId, noteType, tags (array of strings, can be empty)
      // TODO: Implement filtering by workspaceId, noteType, tags, searchQuery, pagination
      const { workspaceId, noteType, tags, query } = req.query;

      if (!workspaceId || typeof workspaceId !== 'string') {
        res.status(400).json({ error: 'workspaceId is required' });
        return;
      }
      if (!noteType || typeof noteType !== 'string') {
        res.status(400).json({ error: 'noteType is required' });
        return;
      }


      const q = typeof query === 'string' ? query : '';
      const notes = await noteService.getNotes(userId, workspaceId, noteType, tags as string[] || [], q);

      res.status(200).json({
        message: 'Notes retrieved successfully',
        data: { notes },
      });
    } catch (error) {
      console.error('Error retrieving notes:', error);
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve notes' });
    }
  }

}