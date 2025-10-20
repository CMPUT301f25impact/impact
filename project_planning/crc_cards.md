
| **Class**            | WaitingListFragment |
| -------------------- | ------------------- |
| **Responsibilities** | **Collaborators**   |
|                      |                     |

| **Class**            | SendNotificationsFragment |
| -------------------- | ------------------------- |
| **Responsibilities** | **Collaborators**         |
|                      |                           |

| **Class**            | CreateEventFragment |
| -------------------- | ------------------- |
| **Responsibilities** | **Collaborators**   |
|                      |                     |

| **Class**            | OrganizersEventView |
| -------------------- | ------------------- |
| **Responsibilities** | **Collaborators**   |
|                      |                     |

| **Class**            | OrganizerDashboard |
| -------------------- | ------------------- |
| **Responsibilities** | **Collaborators**   |
|                      |                     |

| **Class**            | Invitation        |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Store user and event they were invited to | User |
| Store any other invitation data (message etc.) | Event |

| **Class**            | Notification     |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Model notification information | Event |
| Store notification type | User |
| Store linked event or waitlist | |

| **Class**            | NotificationsFragment |
| -------------------- | --------------------- |
| **Responsibilities** | **Collaborators**     |
|                      |                       |

| **Class**            | ProfileFragment   |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Form with the ability to edit profile data | User |
| Ability to delete profile | |

| **Class**            | EntrantEventFragment |
| -------------------- | -------------------- |
| **Responsibilities** | **Collaborators**    |
|                      |                      |

| **Class**            | Image             |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Store raw image data | Event |
| Store image metadata | |

| **Class**            | EntrantDashboard  |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
|                      |                   |

| **Class**            | Event             |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Model event information | User |
| Keep track of event image/QR code | Image |
| Store enrolled users | |

| **Class**            | AdminDashboard    |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
|                      |                   |

| **Class**            | User              |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Model profile info | Notification |
| Model authentication info | Invitation |
| Store settings or preferences | |
| Keep track of notifications and invitations  |  |

| **Class**            | Organizer              |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Store created events | User |
| | Event |

| **Class**            | **MainActivity**  |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Main view that displays appropriate toolbar/dashboard | AdminDashboard |
| | EntrantDashboard |
| | OrganizerDashboard |
