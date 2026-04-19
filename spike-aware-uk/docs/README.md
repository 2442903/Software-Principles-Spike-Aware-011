# Spike Aware UK - Resource Aggregator

A console-based Java application for collecting, managing, and distributing educational and research resources with a moderation layer and team management.

## Features

- **Public User Capabilities**
  - Browse published resources
  - Search for resources by keyword
  - Submit research or public resources
  - Flag resources for moderation review
  - View detailed resource information

- **Moderator Capabilities**
  - View all resources (published and pending)
  - Review pending resources in queue
  - Approve or reject submissions
  - Manage moderation workflow
  - View flagged resources
  - View system analytics
  - Edit, remove, or archive resources

- **Administrator Capabilities**
  - All moderator capabilities
  - Manage team members (add, remove, edit, activate/deactivate)

- **Resource Types**
  - **Research Resources**: Academic/scientific materials with authors and publication year
  - **Public Resources**: Broader educational materials with organization and target audience

## Architecture

- **Resource Model**: Abstract base class with polymorphic subclasses
- **Data Persistence**: JSON-based storage using Gson with polymorphic deserialization
- **CLI Interface**: Interactive console-based user interface
- **Role-Based Access**: Public user, moderator, and administrator modes with hardcoded authentication
- **Team Management**: Administrator-controlled team member management

## Project Structure

```
spike-aware-uk/
├── pom.xml
├── README.md
├── data/
│   ├── data.json
│   └── team_data.json
└── src/
    └── main/
        └── java/com/spikeaware/
            ├── Main.java
            ├── db/
            │   └── DatabaseManager.java
            ├── model/
            │   ├── Resource.java (abstract)
            │   ├── ResearchResource.java
            │   ├── PublicResource.java
            │   ├── ResourceStatus.java
            │   ├── TeamMember.java
            │   └── UserRole.java
            └── team/
                └── TeamManager.java
```

## Building the Project

### Repository

[https://github.com/2442903/Software-Principles-Spike-Aware-011](https://github.com/2442903/Software-Principles-Spike-Aware-011)

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

## Usage

### Public User Workflow

1. **View Published Resources** - List all publicly available resources
2. **Search** - Find resources by keyword
3. **Add Resource** - Submit a new research or public resource (automatically begins in "Pending" status)
4. **Flag Resource** - Mark a resource for moderator review
5. **View Details** - See detailed information about a specific resource

### Moderator Workflow

1. **Login** - Enter password (`mod123` for Moderator, `admin123` for Administrator)
2. **Access Moderation Queue** - Review all pending resources
3. **Make Decision** - Approve (publish), reject, or skip each resource
4. **Manage All Resources** - View all resources regardless of status
5. **View Flagged Resources** - See resources flagged for review
6. **View System Analytics** - Get overview statistics
7. **Edit/Remove/Archive Resources** - Modify resource status or remove/archive

### Administrator Workflow

1. **All Moderator Capabilities**
2. **Manage Team** - Add, remove, edit, or activate/deactivate team members

## Dependencies

- **com.google.code.gson:gson (2.10.1)** - JSON serialization/deserialization with polymorphic type support

## Data Persistence

All resource data is stored in `data/data.json` in the application's working directory. Team member data is stored in `data/team_data.json`. Files are automatically created and updated whenever data is modified.

### Supported Resource Statuses

- **Pending** - Awaiting moderator review
- **Approved** (Published) - Approved and publicly visible
- **Rejected** - Rejected by moderator
- **Archived** - Archived by moderator (not deleted)

### User Roles

- **Public User** - Basic access to published resources
- **Moderator** - Can moderate resources and manage content
- **Administrator** - Full access including team management

## Key Implementation Details

- **ID Generation**: Resources use system identity hash as unique identifier
- **Initial State**: New resources are set to "Pending" status with 0 views and 0 flags
- **Search Filter**: Public users only see approved resources; moderators see all
- **View Tracking**: View count increments when an approved resource is accessed
- **Moderation**: Hardcoded credentials (`mod123` for Moderator, `admin123` for Administrator)
- **Team Management**: Administrators can manage team members stored in separate JSON file

## Real World Improvements

- [ ] Database-backed credential system with encryption
- [ ] Concurrent access handling
- [ ] Extended resource types (Video, Podcast, etc.)
- [ ] Advanced search and filtering
- [ ] User activity logging
- [ ] API endpoint support

## Author Notes

This project demonstrates core object-oriented principles including:
- Polymorphism through abstract base classes and inheritance
- Encapsulation via role-based access control
- Separation of concerns between model, persistence, team management, and presentation layers
- Multiple corrections based on feedback from original pseudocode
- Object based Flags, Activity logging, and link checking not implimented due to scope and time constraints
- Any other deviations from design decisions, made in element 10-1, covered by in-line comments
