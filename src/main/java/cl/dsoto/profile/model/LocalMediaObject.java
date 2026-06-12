package cl.dsoto.profile.model;

import java.nio.file.Path;

public record LocalMediaObject(
        Path path,
        String contentType,
        Long fileSize
) {
}
