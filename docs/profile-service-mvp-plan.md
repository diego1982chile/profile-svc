# Profile Service MVP Plan

## Scope

`profile-service` owns the public provider profile and the media metadata
associated with that profile.

The MVP focuses on:

- Creating and editing a provider profile.
- Managing offered services.
- Managing rates.
- Requesting media upload URLs.
- Confirming uploaded media metadata.
- Publishing and unpublishing the profile.
- Exposing published profiles publicly.

Out of scope for the first implementation:

- Full KYC provider integration.
- Subscription provider integration.
- Moderation workflow.
- Real image/video processing.
- Search and discovery.

## Domain Model

### Profile

Represents the public profile published by a service provider.

Fields:

- `profileId`
- `userId`
- `displayName`
- `description`
- `age`
- `location`
- `publicationStatus`
- `ageVerificationStatus`
- `storageQuota`
- `storageUsed`
- `createdAt`
- `updatedAt`

Relationships:

- Profile has many offered services.
- Profile has many rates.
- Profile has many media items.

### Offered Service

Represents a service offered by the profile.

Fields:

- `serviceId`
- `profileId`
- `name`
- `description`
- `active`

Examples:

- Massage
- Couples
- Video call

### Rate

Represents a pricing option. Rates may be general to the profile or linked to a
specific offered service.

Fields:

- `rateId`
- `profileId`
- `serviceId` nullable
- `label`
- `amount`
- `currency`
- `durationAmount` nullable
- `durationUnit` nullable
- `displayOrder`
- `active`

### Media

Represents uploaded content associated with a profile.

Fields:

- `mediaId`
- `profileId`
- `mediaType`
- `mediaStatus`
- `objectKey`
- `fileSize`
- `displayOrder`
- `primaryMedia`
- `uploadedAt`

Media types:

- `PHOTO`
- `VIDEO`

MVP constraints:

- Maximum 20 photos.
- Maximum 5 videos.
- Maximum 500 MB total storage.

## Statuses

### Publication Status

- `DRAFT`
- `PUBLISHED`
- `SUSPENDED`

### Age Verification Status

- `NOT_STARTED`
- `PENDING`
- `VERIFIED`
- `REJECTED`

### Media Status

- `PENDING`
- `AVAILABLE`
- `REJECTED`
- `DELETED`

## S3 Upload Flow

The binary file is uploaded directly from the frontend to S3. `profile-service`
stores metadata only.

1. Frontend requests an upload URL.

```http
POST /media/upload-url
```

`profile-service` validates:

- Authenticated user has a profile.
- Requested media type is supported.
- Requested size fits quota.
- Photo/video count limits are not exceeded.

It returns a generated object key and presigned upload URL.

2. Frontend uploads the file directly to S3.

```http
PUT <presigned-upload-url>
```

3. Frontend confirms the upload.

```http
POST /media/confirm
```

`profile-service` registers media metadata and updates `storageUsed`.

## Initial API Direction

Profile aggregate:

```http
POST /profiles
GET /profiles/me
PUT /profiles/me
GET /public/profiles/{profileId}
```

Media:

```http
POST /media/upload-url
POST /media/confirm
GET /profiles/me/media
DELETE /profiles/me/media/{mediaId}
```

Publication:

```http
POST /profiles/me/publish
POST /profiles/me/unpublish
```

## Implementation Phases

1. Project conventions and configuration profiles.
2. Entities, enums, repositories, and service interfaces.
3. Profile create/read/update API.
4. Media upload URL and confirm flow.
5. Publication and public profile API.
6. Focused tests for profile, media limits, and publication rules.
