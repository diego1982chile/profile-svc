# Profile Service Status

## Current State

`profile-service` is a Quarkus service for provider profile management and
profile media metadata.

The repository is initialized and pushed to:

```text
https://github.com/diego1982chile/profile-svc.git
```

Current branch:

```text
main
```

Initial commit:

```text
026d922 Initial profile service implementation
```

## Implemented

### Profile API

```http
POST /profiles
GET /profiles/me
PUT /profiles/me
GET /public/profiles/{profileId}
```

Profiles currently support:

- Basic public data: `displayName`, `description`, `age`, `birthDate`,
  `commune`.
- Publication status: `DRAFT`, `PUBLISHED`, `SUSPENDED`.
- Age verification status: `NOT_STARTED`, `PENDING`, `VERIFIED`, `REJECTED`.
- Storage quota and usage.
- Offered services.
- Rates.

### Media API

```http
POST /media/upload-url
POST /media/confirm
GET /profiles/me/media
DELETE /profiles/me/media/{mediaId}
```

Media currently supports:

- `PHOTO` and `VIDEO`.
- `PENDING`, `AVAILABLE`, `REJECTED`, `DELETED` statuses.
- Upload intent generation through `MediaStorageService`.
- Confirm flow that registers metadata and updates `storageUsed`.
- Logical delete plus storage delete call.
- Limits:
  - 20 photos.
  - 5 videos.
  - 500 MB total storage.

### Storage Abstraction

Storage is behind:

```java
MediaStorageService
```

Current implementations:

```java
FakeMediaStorageService
LocalMediaStorageService
```

They are selected by:

```properties
profile.media.storage.provider=fake
profile.media.storage.provider=local
```

`local` is used by dev and Docker profiles. It receives binary uploads through
the service, stores files under `storage/profile-media`, serves them over HTTP,
and lets `POST /media/confirm` validate real file metadata.

`fake` remains available for tests and fast integration flows that do not need
binary uploads. It records expected metadata in memory after generating an
upload intent.

### Fixtures

Dev fixtures are enabled in `application-dev.properties`:

```properties
profile.fixtures.enabled=true
```

Fixture users:

```text
provider.basic@example.com
provider.profile@example.com
```

The current authentication placeholder is:

```http
X-User-Id: provider.basic@example.com
```

### Persistence

Dev and Docker profiles use SQLite:

```properties
quarkus.datasource.db-kind=sqlite
quarkus.datasource.jdbc.url=jdbc:sqlite:profile.db
```

Test profile uses H2 in memory.

### Location Catalog

The service includes a Chile regions/communes catalog seeded from:

```text
src/main/resources/data/chile-communes.csv
```

The source dataset was generated from Gobierno Digital's public DPA API:

```text
https://api.digital.gob.cl/dpa/regiones
```

Available endpoints:

```http
GET /locations/regions?countryCode=CL
GET /locations/regions/{regionCode}/communes?countryCode=CL
```

Profile location follows the normalized model:

```text
Profile -> Commune -> Region
```

The profile stores a commune reference. Region and display names come from the
catalog and are not duplicated on the profile. Service modalities such as
online, hotel, out-call, or to-agree are a separate future concept.

### Docker

The Docker setup follows the same local pattern as `token-svc`:

- `docker-compose.yml`
- `src/main/docker/Dockerfile.local`
- build context: `target/quarkus-app`
- runtime profile: `QUARKUS_PROFILE=docker`

Build and run:

```shell
./mvnw clean install -Pproduction -Dquarkus.profile=docker -DskipTests
docker-compose up --build
```

Docker Compose exposes:

```text
http://localhost:9092/profile-service
```

CORS for local UI development is configured through `docker-compose.yml`:

```text
http://localhost
http://localhost:8088
http://127.0.0.1:8088
```

## Tests

Current test suite:

```text
10 tests
0 failures
0 errors
```

Covered behavior:

- Service liveness.
- Profile create/read/update.
- Duplicate profile rejection.
- Draft public profile not exposed.
- Media upload intent.
- Media confirm.
- Media list.
- Media delete.
- Photo count limit.
- Storage quota limit.
- User without profile rejection for media upload.

## Known Gaps

- JWT authentication is not implemented in `profile-service`.
- `X-User-Id` is only a temporary development placeholder.
- S3/LocalStack storage implementation is not implemented yet.
- Binary upload is not performed while using fake storage.
- `publish` and `unpublish` endpoints are not implemented.
- Publication rules are not implemented.
- Profile completion status is not implemented.
- Profile completion event/contract for onboarding is not implemented.
- Subscription integration is not implemented.
- Age verification integration is not implemented.
- Search/discovery is out of scope for now.
- Moderation is out of scope for now.

## Next Steps

1. Build the profile UI against the current backend contract.
   - Use `docs/profile-ui-context.md` as the UI session context.
   - Use `X-User-Id` for fixture user selection.
   - Skip actual binary PUT while storage provider is `fake`.

2. Add profile completion semantics.
   - Define what fields make a profile complete.
   - Add a `profileStatus` or equivalent model.
   - Add tests for incomplete vs complete profiles.
   - Prepare a `ProfileCompleted` event or service contract for onboarding.

3. Implement publication.
   - Add:
     ```http
     POST /profiles/me/publish
     POST /profiles/me/unpublish
     ```
   - Validate profile completeness.
   - Validate required media.
   - Validate age verification status when the final rule is decided.

4. Replace fake storage with a real provider implementation.
   - Add `S3MediaStorageService` behind `MediaStorageService`.
   - Support upload intent with S3 pre-signed PUT URL.
   - Support object metadata lookup.
   - Support object deletion.
   - Consider LocalStack for local integration tests.

5. Replace `X-User-Id` with JWT-based identity.
   - Align with `token-svc` issuer/audience.
   - Use JWT subject as `userId`.
   - Add role/scope checks where needed.

6. Revisit Docker profile naming before real deployment.
   - Current `docker` profile is suitable for Docker Compose local work.
   - If production diverges from local Compose, introduce a separate production
     config profile.
