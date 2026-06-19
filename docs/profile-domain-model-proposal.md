# Profile Domain Model Proposal

## Purpose

This document proposes the next profile model expansion needed by the public
profile UI.

The current backend model supports the MVP basics:

- public identity: `displayName`, `description`, `age`, `birthDate`
- administrative location: `commune`
- publication and age-verification statuses
- media quota and usage
- offered services
- rates
- media metadata

The UI now needs additional public-profile data such as contact information,
physical attributes, availability, service modalities, tags, and profile
completion.

## Design Direction

Keep `Profile` as the aggregate root.

Do not keep adding unrelated columns directly to `ProfileEntity`. Instead:

- use a 1:1 `ProfileDetails` entity for extended descriptive data,
- use child collections for list-like data,
- keep services, rates, and media as existing child collections,
- calculate profile completion from the aggregate instead of persisting it at
  first.

Target aggregate:

```text
Profile
├── ProfileDetails              1:1
├── ProfileAvailabilitySlot     1:N
├── ProfileServiceModality      1:N
├── ProfileTag                  1:N
├── OfferedService              1:N existing
├── Rate                        1:N existing
└── Media                       1:N existing
```

## Entities

### Profile

Existing aggregate root.

Current fields stay:

```text
profileId
userId
displayName
description
age
birthDate
commune
publicationStatus
ageVerificationStatus
storageQuota
storageUsed
createdAt
updatedAt
```

New relationships:

```text
details: ProfileDetails
availabilitySlots: List<ProfileAvailabilitySlot>
modalities: List<ProfileServiceModality>
tags: List<ProfileTag>
```

Suggested entity additions:

```java
@OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
private ProfileDetailsEntity details;

@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProfileAvailabilitySlotEntity> availabilitySlots = new ArrayList<>();

@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProfileServiceModalityEntity> modalities = new ArrayList<>();

@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProfileTagEntity> tags = new ArrayList<>();
```

### ProfileDetails

One row per profile. Stores data that has a single value for the profile.

Suggested fields:

```text
profileId
profile
contactPhone
whatsappEnabled
shortTitle
experience
rules
heightCm
weightKg
measurements
bodyType
hairColor
eyeColor
smokes
tattoos
piercings
grooming
languages
```

Suggested entity:

```java
@Entity
@Table(name = "PROFILE_DETAILS")
public class ProfileDetailsEntity {

    @Id
    private String profileId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    private String contactPhone;
    private Boolean whatsappEnabled;

    private String shortTitle;
    private String experience;
    private String rules;

    private Integer heightCm;
    private Integer weightKg;
    private String measurements;
    private String bodyType;
    private String hairColor;
    private String eyeColor;

    private Boolean smokes;
    private Boolean tattoos;
    private Boolean piercings;
    private String grooming;

    private String languages;
}
```

Notes:

- `languages` can start as a string for MVP. If search/filtering by language
  becomes important, move it to a child table later.
- `measurements` can start as one formatted string. Split into bust/waist/hips
  only if validation/filtering is needed.

### ProfileAvailabilitySlot

Stores weekly availability.

Suggested fields:

```text
availabilitySlotId
profile
dayOfWeek
startTime
endTime
available
displayOrder
```

Suggested entity:

```java
@Entity
@Table(name = "PROFILE_AVAILABILITY_SLOT")
public class ProfileAvailabilitySlotEntity {

    @Id
    private String availabilitySlotId;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean available;

    private Integer displayOrder;
}
```

Notes:

- This supports multiple slots per day later.
- For MVP, the UI can create one slot per day.
- `available=false` can be used for explicit closed days.

### ProfileServiceModality

Stores where/how the provider can offer services.

Suggested enum:

```java
public enum ServiceModality {
    OWN_PLACE,
    HOTEL,
    OUTCALL,
    ONLINE,
    TO_AGREE
}
```

Suggested fields:

```text
modalityId
profile
modality
active
displayOrder
```

Suggested entity:

```java
@Entity
@Table(name = "PROFILE_SERVICE_MODALITY")
public class ProfileServiceModalityEntity {

    @Id
    private String modalityId;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    private ServiceModality modality;

    private Boolean active;

    private Integer displayOrder;
}
```

Notes:

- Prefer this over boolean columns like `hotelAvailable`,
  `outcallAvailable`, etc.
- It is easier to filter/search by modality later.

### ProfileTag

Stores public tags/keywords used by the UI and future discovery.

Suggested fields:

```text
tagId
profile
tag
displayOrder
```

Suggested entity:

```java
@Entity
@Table(name = "PROFILE_TAG")
public class ProfileTagEntity {

    @Id
    private String tagId;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    private String tag;

    private Integer displayOrder;
}
```

Notes:

- Normalize tags to lowercase/trimmed values in the service layer.
- Add uniqueness per profile later if needed.

## API Models

### Profile Response

Extend the current `Profile` response with:

```java
ProfileDetails details
List<ProfileAvailabilitySlot> availability
List<ServiceModality> modalities
List<String> tags
ProfileCompletion completion
```

Suggested record shape:

```java
public record Profile(
        String profileId,
        String userId,
        String displayName,
        String description,
        Integer age,
        LocalDate birthDate,
        Commune commune,
        PublicationStatus publicationStatus,
        AgeVerificationStatus ageVerificationStatus,
        Long storageQuota,
        Long storageUsed,
        ProfileDetails details,
        List<ProfileAvailabilitySlot> availability,
        List<ServiceModality> modalities,
        List<String> tags,
        ProfileCompletion completion,
        List<OfferedService> services,
        List<Rate> rates
) {
}
```

### ProfileDetails API Model

```java
public record ProfileDetails(
        String contactPhone,
        Boolean whatsappEnabled,
        String shortTitle,
        String experience,
        String rules,
        Integer heightCm,
        Integer weightKg,
        String measurements,
        String bodyType,
        String hairColor,
        String eyeColor,
        Boolean smokes,
        Boolean tattoos,
        Boolean piercings,
        String grooming,
        String languages
) {
}
```

### ProfileAvailabilitySlot API Model

```java
public record ProfileAvailabilitySlot(
        String availabilitySlotId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Boolean available,
        Integer displayOrder
) {
}
```

### ProfileCompletion API Model

Computed response object. Do not persist initially.

```java
public record ProfileCompletion(
        boolean complete,
        int percent,
        List<String> missingFields
) {
}
```

## Update Request Shape

Suggested `ProfileUpdate` JSON:

```json
{
  "displayName": "Camila Rojas",
  "description": "Descripción pública",
  "age": 30,
  "birthDate": "1996-03-12",
  "countryCode": "CL",
  "regionCode": "13",
  "communeCode": "13101",
  "details": {
    "contactPhone": "+56912345678",
    "whatsappEnabled": true,
    "shortTitle": "Atención elegante y discreta",
    "experience": "5 años de experiencia",
    "rules": "Reservas con anticipación",
    "heightCm": 168,
    "weightKg": 58,
    "measurements": "90-60-90",
    "bodyType": "Delgada",
    "hairColor": "Castaño",
    "eyeColor": "Café",
    "smokes": false,
    "tattoos": true,
    "piercings": false,
    "grooming": "Depilada",
    "languages": "Español, Inglés"
  },
  "availability": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "10:00",
      "endTime": "22:00",
      "available": true,
      "displayOrder": 1
    }
  ],
  "modalities": [
    "OWN_PLACE",
    "HOTEL",
    "OUTCALL"
  ],
  "tags": [
    "masajes",
    "elegante",
    "discreta"
  ],
  "services": [],
  "rates": []
}
```

## Publication Completion Rules

Initial publish rules can require:

```text
displayName
description
birthDate or age
commune
at least one PHOTO media item
at least one active OfferedService
at least one active Rate
contactPhone if whatsappEnabled is true
at least one active ServiceModality
at least one available AvailabilitySlot
```

Recommended response when incomplete:

```json
{
  "complete": false,
  "percent": 72,
  "missingFields": [
    "contactPhone",
    "availability",
    "modalities"
  ]
}
```

## Deferred Model Areas

Do not model these in the first expansion:

- reviews and ratings,
- private media,
- moderation workflows,
- online booking/reservation history,
- live availability,
- search indexes,
- payment/subscription state.

These likely belong to separate bounded contexts or later integrations.
