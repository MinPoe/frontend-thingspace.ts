# Backend API Test Documentation

## 2.1. Locations of your back-end test and instructions to run them

### 2.1.1 Notes API Endpoints

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|----------------------------------|-------------------------------------|-------------------|
| `POST /api/notes` | `src/__tests__/notes.normal.test.ts#L38` | `src/__tests__/notes.mocked.test.ts#L63` | `Notes Service`, `Notes DB`, `OpenAI API` |
| `PUT /api/notes/:id` | `src/__tests__/notes.normal.test.ts#L135` | `src/__tests__/notes.mocked.test.ts#L85` | `Notes Service` |
| `DELETE /api/notes/:id` | `src/__tests__/notes.normal.test.ts#L197` | `src/__tests__/notes.mocked.test.ts#L116` | `Notes Service` |
| `GET /api/notes/:id` | `src/__tests__/notes.normal.test.ts#L248` | `src/__tests__/notes.mocked.test.ts#L102` | `Notes Service`, `Notes DB` |
| `GET /api/notes` | `src/__tests__/notes.normal.test.ts#L301` | `src/__tests__/notes.mocked.test.ts#L130`, `src/__tests__/notes.mocked.test.ts#L147` | `Notes Service`, `Workspace DB`, `OpenAI API` |
| `GET /api/notes/:id/workspaces` | `src/__tests__/notes.normal.test.ts#L605` | `src/__tests__/notes.mocked.test.ts#L280` | `Notes Service` |
| `POST /api/notes/:id/share` | `src/__tests__/notes.normal.test.ts#L402` | `src/__tests__/notes.mocked.test.ts#L294`, `src/__tests__/notes.mocked.test.ts#L541`, `src/__tests__/notes.mocked.test.ts#L623` | `Notes Service`, `Notes DB`, `Workspace DB` |
| `POST /api/notes/:id/copy` | `src/__tests__/notes.normal.test.ts#L510` | `src/__tests__/notes.mocked.test.ts#L311`, `src/__tests__/notes.mocked.test.ts#L328`, `src/__tests__/notes.mocked.test.ts#L566` | `Notes Service`, `Notes DB` |

### 2.1.2 Workspace API Endpoints

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|----------------------------------|-------------------------------------|-------------------|
| `POST /api/workspaces` | `src/__tests__/workspace.normal.test.ts#L41` | `src/__tests__/workspace.mocked.test.ts#L44` | `Workspace Service` |
| `GET /api/workspaces/personal` | `src/__tests__/workspace.normal.test.ts#L101` | `src/__tests__/workspace.mocked.test.ts#L84` | `Workspace Service` |
| `GET /api/workspaces/user` | `src/__tests__/workspace.normal.test.ts#L183` | `src/__tests__/workspace.mocked.test.ts#L118` | `Workspace Service` |
| `GET /api/workspaces/:id` | `src/__tests__/workspace.normal.test.ts#L231` | `src/__tests__/workspace.mocked.test.ts#L152` | `Workspace Service` |
| `GET /api/workspaces/:id/members` | `src/__tests__/workspace.normal.test.ts#L275` | `src/__tests__/workspace.mocked.test.ts#L186` | `Workspace Service` |
| `GET /api/workspaces/:id/tags` | `src/__tests__/workspace.normal.test.ts#L306` | `src/__tests__/workspace.mocked.test.ts#L236` | `Workspace Service` |
| `GET /api/workspaces/:id/membership/:userId` | `src/__tests__/workspace.normal.test.ts#L441` | `src/__tests__/workspace.mocked.test.ts#L270` | `Workspace Service` |
| `POST /api/workspaces/:id/members` | `src/__tests__/workspace.normal.test.ts#L524` | `src/__tests__/workspace.mocked.test.ts#L304` | `Workspace Service` |
| `POST /api/workspaces/:id/leave` | `src/__tests__/workspace.normal.test.ts#L738` | `src/__tests__/workspace.mocked.test.ts#L374` | `Workspace Service` |
| `PUT /api/workspaces/:id` | `src/__tests__/workspace.normal.test.ts#L836` | `src/__tests__/workspace.mocked.test.ts#L392` | `Workspace Service` |
| `PUT /api/workspaces/:id/picture` | `src/__tests__/workspace.normal.test.ts#L897` | `src/__tests__/workspace.mocked.test.ts#L428` | `Workspace Service` |
| `DELETE /api/workspaces/:id/members/:userId` | `src/__tests__/workspace.normal.test.ts#L955` | `src/__tests__/workspace.mocked.test.ts#L464` | `Workspace Service` |
| `DELETE /api/workspaces/:id` | `src/__tests__/workspace.normal.test.ts#L1112` | `src/__tests__/workspace.mocked.test.ts#L498` | `Workspace Service` |
| `GET /api/workspaces/:id/poll` | `src/__tests__/workspace.normal.test.ts#L1204` | `src/__tests__/workspace.mocked.test.ts#L532` | `Workspace Service` |

### 2.1.3 Auth API Endpoints

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|----------------------------------|-------------------------------------|-------------------|
| `POST /api/auth/signup` | `src/__tests__/auth.normal.test.ts#L123` | `src/__tests__/auth.mocked.test.ts#L77` | `Auth Service`, `Workspace Service`, `User DB`, `Google OAuth` |
| `POST /api/auth/signin` | `src/__tests__/auth.normal.test.ts#L151` | `src/__tests__/auth.mocked.test.ts#L290` | `Auth Service` |
| `POST /api/auth/dev-login` | `src/__tests__/auth.normal.test.ts#L68` | `src/__tests__/auth.mocked.test.ts#L394` | `Auth Service` |

### 2.1.4 User API Endpoints

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|----------------------------------|-------------------------------------|-------------------|
| `GET /api/users/profile` | `src/__tests__/user.normal.test.ts#L39` | - | - |
| `PUT /api/users/profile` | `src/__tests__/user.normal.test.ts#L57` | `src/__tests__/user.mocked.test.ts#L48` | `User DB`, `Workspace DB` |
| `DELETE /api/users/profile` | `src/__tests__/user.normal.test.ts#L166` | `src/__tests__/user.mocked.test.ts#L104` | `Workspace DB` |
| `POST /api/users/fcm-token` | `src/__tests__/user.normal.test.ts#L230` | `src/__tests__/user.mocked.test.ts#L155` | `User DB` |
| `GET /api/users/:id` | `src/__tests__/user.normal.test.ts#L273` | `src/__tests__/user.mocked.test.ts#L209` | `User DB` |
| `GET /api/users/email/:email` | `src/__tests__/user.normal.test.ts#L318` | `src/__tests__/user.mocked.test.ts#L260` | `User DB` |

### 2.1.5 Message API Endpoints

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|----------------------------------|-------------------------------------|-------------------|
| `GET /api/messages/workspace/:workspaceId` | `src/__tests__/message.normal.test.ts#L61` | `src/__tests__/message.mocked.test.ts#L67` | `Message DB` |
| `POST /api/messages/workspace/:workspaceId` | `src/__tests__/message.normal.test.ts#L188` | `src/__tests__/message.mocked.test.ts#L91` | `Message DB`, `Workspace DB` |
| `DELETE /api/messages/:messageId` | `src/__tests__/message.normal.test.ts#L261` | `src/__tests__/message.mocked.test.ts#L134` | `Message DB`, `Workspace DB` |

### 2.1.6 Media API Endpoints

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|----------------------------------|-------------------------------------|-------------------|
| `POST /api/media/upload` | `src/__tests__/media.normal.test.ts#L56` | `src/__tests__/media.mocked.test.ts#L55` | `Media Service`, `File System` |

