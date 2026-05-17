package wo.org.winter_olympics.core.service;

import wo.org.winter_olympics.dto.UserProfileDto;

public interface UserProfileService {

    UserProfileDto getProfile(String username);

    void updateProfile(String username, UserProfileDto userProfileDto);

    void deleteProfile(String username);
}
