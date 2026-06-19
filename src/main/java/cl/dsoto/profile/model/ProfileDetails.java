package cl.dsoto.profile.model;

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
