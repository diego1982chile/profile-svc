package cl.dsoto.profile.model;

import java.util.List;

public record ProfileCompletion(
        boolean complete,
        int percent,
        List<String> missingFields
) {
}
