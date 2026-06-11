package cl.dsoto.profile.services;

import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.ProfileUpdate;

import java.util.Optional;

public interface ProfileService {

    Profile createProfile(String userId, ProfileUpdate profileUpdate);

    Optional<Profile> getProfileByUserId(String userId);

    Profile updateProfile(String userId, ProfileUpdate profileUpdate);

    Optional<Profile> getPublishedProfile(String profileId);
}
