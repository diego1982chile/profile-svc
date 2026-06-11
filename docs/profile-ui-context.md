# Profile UI Context

## Purpose

This document captures the current backend contract for building the profile UI
in a separate session.

The UI can be developed against `profile-service` without waiting for the full
onboarding integration, real JWT authentication, or real cloud storage.

## Current Backend Base

`profile-service` runs with:

```shell
./mvnw compile quarkus:dev
```

In dev profile it uses:

```properties
quarkus.http.port=9092
quarkus.http.root-path=/profile-service
quarkus.datasource.db-kind=sqlite
quarkus.datasource.jdbc.url=jdbc:sqlite:profile.db
profile.fixtures.enabled=true
profile.media.storage.provider=fake
```

Dev base URL:

```text
http://localhost:9092/profile-service
```

## Authentication Placeholder

The backend does not use JWT yet.

Current authenticated user is passed with:

```http
X-User-Id: provider.basic@example.com
```

Available dev fixture users:

```text
provider.basic@example.com
provider.profile@example.com
```

These represent profile/onboarding fixture users, not identity admin users.

## Profile Contract

Endpoints:

```http
POST /profiles
GET /profiles/me
PUT /profiles/me
GET /public/profiles/{profileId}
```

Current profile fields:

```json
{
  "profileId": "uuid",
  "userId": "provider.basic@example.com",
  "displayName": "Camila Rojas",
  "description": "Perfil base creado desde fixture de onboarding.",
  "age": 30,
  "birthDate": "1996-03-12",
  "location": "Santiago",
  "publicationStatus": "DRAFT",
  "ageVerificationStatus": "NOT_STARTED",
  "storageQuota": 524288000,
  "storageUsed": 0,
  "services": [],
  "rates": []
}
```

Create/update request shape:

```json
{
  "displayName": "Camila Rojas",
  "description": "Descripción pública",
  "age": 30,
  "birthDate": "1996-03-12",
  "location": "Santiago",
  "services": [
    {
      "serviceId": null,
      "name": "Massage",
      "description": "Relax",
      "active": true
    }
  ],
  "rates": [
    {
      "rateId": null,
      "serviceId": null,
      "label": "30 Minutes",
      "amount": 50000,
      "currency": "CLP",
      "durationAmount": 30,
      "durationUnit": "MINUTES",
      "displayOrder": 1,
      "active": true
    }
  ]
}
```

Notes:

- `birthDate` was added for the intended onboarding basic-profile step.
- `age` still exists for compatibility.
- Fixture profiles start as `DRAFT`.
- `GET /public/profiles/{profileId}` only returns profiles with
  `publicationStatus=PUBLISHED`.
- Publish/unpublish endpoints are not implemented yet.

## Media Contract

Endpoints:

```http
POST /media/upload-url
POST /media/confirm
GET /profiles/me/media
DELETE /profiles/me/media/{mediaId}
```

Upload intent request:

```json
{
  "mediaType": "PHOTO",
  "contentType": "image/jpeg",
  "fileSize": 1000
}
```

Upload intent response:

```json
{
  "storageKey": "profiles/{profileId}/photo/{uuid}",
  "uploadUrl": "https://storage.local/upload/profiles/{profileId}/photo/{uuid}",
  "method": "PUT",
  "headers": {
    "Content-Type": "image/jpeg"
  },
  "expiresAt": "2026-06-11T03:00:00Z"
}
```

Confirm request:

```json
{
  "storageKey": "profiles/{profileId}/photo/{uuid}",
  "mediaType": "PHOTO",
  "displayOrder": 1,
  "primaryMedia": true
}
```

Media response:

```json
{
  "mediaId": "uuid",
  "profileId": "uuid",
  "mediaType": "PHOTO",
  "mediaStatus": "AVAILABLE",
  "storageKey": "profiles/{profileId}/photo/{uuid}",
  "fileSize": 1000,
  "displayOrder": 1,
  "primaryMedia": true,
  "uploadedAt": "2026-06-11T03:00:00Z"
}
```

Media constraints:

```text
PHOTO max count: 20
VIDEO max count: 5
Total storage quota: 500 MB
```

## Fake Storage Behavior

`profile.media.storage.provider=fake` is currently used.

The fake storage implementation:

- returns an upload intent,
- records expected metadata internally,
- does not expose a real binary upload endpoint,
- allows `POST /media/confirm` to succeed using the returned `storageKey`.

For UI development, while storage provider is `fake`:

1. Call `POST /media/upload-url`.
2. Skip or mock the actual `PUT uploadUrl`.
3. Call `POST /media/confirm` with the returned `storageKey`.

This preserves the future cloud-storage flow without requiring S3 or LocalStack.

## Out Of Scope For Initial UI

Do not depend on these yet:

- JWT authentication.
- Real S3/LocalStack upload.
- Publish/unpublish.
- Profile completion events.
- Onboarding integration.
- Subscription integration.
- Search/discovery.
- Moderation.

## Recommended First UI Scope

Build the actual profile management UI first:

- Load current fixture profile with `GET /profiles/me`.
- Edit display name, description, birth date, age, and location.
- Manage offered services.
- Manage rates.
- Request/confirm fake media uploads.
- List media.
- Delete media.
- Show quota usage and photo/video counts.

Use `X-User-Id` as a temporary dev selector.
