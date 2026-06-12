package cl.dsoto.profile.webservice.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class DefaultMediaWebServiceTest {

    @Test
    void shouldCreateUploadIntentAndConfirmMedia() {
        String userId = userId();
        createProfile(userId, "Provider Media");

        String storageKey = requestUpload(userId, "PHOTO", 1000L);

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(Map.of(
                        "storageKey", storageKey,
                        "mediaType", "PHOTO",
                        "displayOrder", 1,
                        "primaryMedia", true
                ))
                .when()
                .post("/media/confirm")
                .then()
                .statusCode(201)
                .body("mediaType", is("PHOTO"))
                .body("mediaStatus", is("AVAILABLE"))
                .body("storageKey", is(storageKey))
                .body("url", is("http://localhost:9092/profile-service/media/files/" + storageKey))
                .body("fileSize", is(1000))
                .body("displayOrder", is(1))
                .body("primaryMedia", is(true));

        given()
                .header("X-User-Id", userId)
                .when()
                .get("/profiles/me/media")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].storageKey", is(storageKey))
                .body("[0].url", is("http://localhost:9092/profile-service/media/files/" + storageKey));
    }

    @Test
    void shouldRejectUploadIntentWhenPhotoLimitIsReached() {
        String userId = userId();
        createProfile(userId, "Provider Photo Limit");

        for (int index = 0; index < 20; index++) {
            confirmUpload(userId, "PHOTO", 1000L, index, index == 0);
        }

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(uploadRequest("PHOTO", 1000L))
                .when()
                .post("/media/upload-url")
                .then()
                .statusCode(409)
                .body("message", is("media type limit exceeded"));
    }

    @Test
    void shouldRejectUploadIntentWhenStorageQuotaWouldBeExceeded() {
        String userId = userId();
        createProfile(userId, "Provider Quota");

        given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(uploadRequest("VIDEO", 524288001L))
                .when()
                .post("/media/upload-url")
                .then()
                .statusCode(409)
                .body("message", is("storage quota exceeded"));
    }

    @Test
    void shouldDeleteMediaAndHideItFromListing() {
        String userId = userId();
        createProfile(userId, "Provider Delete");
        String mediaId = confirmUpload(userId, "PHOTO", 1000L, 1, true);

        given()
                .header("X-User-Id", userId)
                .when()
                .delete("/profiles/me/media/" + mediaId)
                .then()
                .statusCode(204);

        given()
                .header("X-User-Id", userId)
                .when()
                .get("/profiles/me/media")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void shouldRejectMediaUploadWhenUserHasNoProfile() {
        given()
                .contentType("application/json")
                .header("X-User-Id", userId())
                .body(uploadRequest("PHOTO", 1000L))
                .when()
                .post("/media/upload-url")
                .then()
                .statusCode(400)
                .body("message", is("profile not found"));
    }

    private String requestUpload(String userId, String mediaType, Long fileSize) {
        return given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(uploadRequest(mediaType, fileSize))
                .when()
                .post("/media/upload-url")
                .then()
                .statusCode(200)
                .body("storageKey", startsWith("profiles/"))
                .body("method", is("PUT"))
                .body("headers.Content-Type", is(contentType(mediaType)))
                .extract()
                .path("storageKey");
    }

    private String confirmUpload(String userId, String mediaType, Long fileSize, Integer displayOrder, boolean primaryMedia) {
        String storageKey = requestUpload(userId, mediaType, fileSize);
        return given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(Map.of(
                        "storageKey", storageKey,
                        "mediaType", mediaType,
                        "displayOrder", displayOrder,
                        "primaryMedia", primaryMedia
                ))
                .when()
                .post("/media/confirm")
                .then()
                .statusCode(201)
                .extract()
                .path("mediaId");
    }

    private Map<String, Object> uploadRequest(String mediaType, Long fileSize) {
        return Map.of(
                "mediaType", mediaType,
                "contentType", contentType(mediaType),
                "fileSize", fileSize
        );
    }

    private String contentType(String mediaType) {
        return "VIDEO".equals(mediaType) ? "video/mp4" : "image/jpeg";
    }

    private String createProfile(String userId, String displayName) {
        return given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(profilePayload(displayName))
                .when()
                .post("/profiles")
                .then()
                .statusCode(201)
                .extract()
                .path("profileId");
    }

    private Map<String, Object> profilePayload(String displayName) {
        return Map.of(
                "displayName", displayName,
                "description", "Profile description",
                "age", 29,
                "countryCode", "CL",
                "regionCode", "13",
                "communeCode", "13101",
                "services", List.of(
                        Map.of("name", "Massage", "description", "Relax", "active", true)
                ),
                "rates", List.of(
                        Map.of(
                                "label", "30 Minutes",
                                "amount", new BigDecimal("50000"),
                                "currency", "CLP",
                                "durationAmount", 30,
                                "durationUnit", "MINUTES",
                                "displayOrder", 1,
                                "active", true
                        )
                )
        );
    }

    private String userId() {
        return "user-" + UUID.randomUUID();
    }
}
