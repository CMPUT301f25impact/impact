| **Class**                                           | WaitingListFragment |
| --------------------------------------------------- | ------------------- |
| **Responsibilities**                                | **Collaborators**   |
| Displays Entrants for selected Event in a list view | Event               |
|                                                     | Entrant             |



| **Class**                                        | SendNotificationsFragment |
| ------------------------------------------------ | ------------------------- |
| **Responsibilities**                             | **Collaborators**         |
| Provides selection field for Event               | Notification              |
| Provides text box field for invitation message   | Event                     |
| Provides multiple selection field for recipients | Entrants                  |
| Displays recently sent Notifications             |                           |

| **Class**                                                                              | CreateEventFragment |
| -------------------------------------------------------------------------------------- | ------------------- |
| **Responsibilities**                                                                   | **Collaborators**   |
| Provides interface for creating an Event                                               | Event               |
| Text box fields for event name, description and venue location                         | Image               |
| Date selection fields for event date, registration open date and registration end date |                     |
| Integer field for event capacity limit                                                 |                     |
| Drop box for Event image upload                                                        |                     |
| Displays QR preview for created event                                                  |                     |


| **Class**                                                                          | OrganizersEventFragment |
| ---------------------------------------------------------------------------------- | ----------------------- |
| **Responsibilities**                                                               | **Collaborators**       |
| Displays Event name, registration deadline and number of Entrants in the wait list | Event                   |
| Provides a redirection button for WaitingListFragment                              | WaitingListFragment     |
| Provides edit and delete buttons                                                   |                         |


| **Class**                                                 | OrganizerDashboard       |
| --------------------------------------------------------- | ------------------------- |
| **Responsibilities**                                      | **Collaborators**         |
| Provides "Create New Event" Button                        | CreateEventFragment       |
| Displays OrganizersEventFragments for each event          | OrganizersEventFragment   |
| Provides redirection button for SendNotificationsFragment | SendNotificationsFragment |


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


| **Class**                                                                                         | NotificationsFragment |
| ------------------------------------------------------------------------------------------------- | --------------------- |
| **Responsibilities**                                                                              | **Collaborators**     |
| Provides "Receive Updates", "Lottery Results", "Marketing" toggle buttons                         | Invitation            |
| Displays invitations sent to the current Entrant in a list view, with a accept and decline button | Notification          |
| Displays the current Entrants Notifications in a list view                                        | Entrant               |


| **Class**            | ProfileFragment   |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Form with the ability to edit profile data | User |
| Ability to delete profile | |


| **Class**                                                                          | EntrantEventFragment |
| ---------------------------------------------------------------------------------- | -------------------- |
| **Responsibilities**                                                               | **Collaborators**    |
| Displays Event name, date, time, location and number of Entrants in waitlist       | Event                |
| Provides "Join Waitlist" button                                                    | Entrant              |
| If the current Entrant is already on the waitlist provides "Leave Waitlist" button |                      |


| **Class**            | Image             |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Store raw image data | Event |
| Store image metadata | |


| **Class**                                                                   | EntrantDashboard      |
| --------------------------------------------------------------------------- | --------------------- |
| **Responsibilities**                                                        | **Collaborators**     |
| Displays EntrantEventFragments for each event                               | EntrantEventFragment  |
| Provides redirections buttons for ProfileFragment and NotificationsFragment | ProfileFragment       |
|                                                                             | NotificationsFragment |


| **Class**            | Event             |
| -------------------- | ----------------- |
| **Responsibilities** | **Collaborators** |
| Model event information | User |
| Keep track of event image/QR code | Image |
| Store enrolled users | |


| **Class**                                  | AdminDashboard    |
| ------------------------------------------ | ----------------- |
| **Responsibilities**                       | **Collaborators** |
| Provides selection buttons for data types  | Events            |
| Displays selected data type in a list view | Profiles          |
| Provides delete button for each data type  | Images            |
|                                            |                   |


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
