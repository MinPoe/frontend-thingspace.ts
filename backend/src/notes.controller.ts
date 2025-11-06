import { Request, Response } from 'express';
import { CreateNoteRequest, UpdateNoteRequest } from './notes.types';
import { noteService } from './notes.service';

export class NotesController {
  async createNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user!._id;

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
      const userId = req.user!._id;

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
      const userId = req.user!._id;
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
      const userId = req.user!._id;
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

  async shareNoteToWorkspace(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user!._id;

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

  async copyNoteToWorkspace(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user!._id;
  
      const noteId = req.params.id;
      const { workspaceId } = req.body;
  
      if (!workspaceId) {
        res.status(400).json({ error: 'workspaceId is required' });
        return;
      }
  
      const copiedNote = await noteService.copyNoteToWorkspace(noteId, userId, workspaceId);
  
      res.status(201).json({
        message: 'Note copied to workspace successfully',
        data: { note: copiedNote },
      });
    } catch (error) {
      console.error('Error copying note:', error);
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
      res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to copy note' });
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
      const userId = req.user!._id;

      const { workspaceId, noteType, tags, query } = req.query;

      if (!workspaceId) {
        res.status(400).json({ error: 'workspaceId is required' });
        return;
      }
      if (!noteType) {
        res.status(400).json({ error: 'noteType is required' });
        return;
      }

      // Query param is optional, default to empty string if not provided
      const q = query || '';
      const notes = await noteService.getNotes(userId, workspaceId as string, noteType as string, tags as string[] || [], q as string);

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