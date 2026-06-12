package cl.dsoto.profile.webservice.impl;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class LocalMediaStorageProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "profile.media.storage.provider", "local",
                "profile.media.local-storage-path", "target/local-media-test",
                "profile.media.local-public-base-url", "http://localhost:8081/media/files"
        );
    }
}
