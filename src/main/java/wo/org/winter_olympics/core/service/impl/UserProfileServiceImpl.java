package wo.org.winter_olympics.core.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wo.org.winter_olympics.core.service.UserProfileService;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.dto.UserProfileDto;
import wo.org.winter_olympics.exception.UserNotFoundException;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final AppUserRepository appUserRepository;

    public UserProfileServiceImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String username) {
        AppUserEntity user = getUserByUsername(username);

        return mapToProfileDto(user);
    }

    @Override
    @Transactional
    public void updateProfile(String username, UserProfileDto userProfileDto) {
        AppUserEntity user = getUserByUsername(username);

        user.setFullName(userProfileDto.getFullName());
        user.setCountry(userProfileDto.getCountry());
        user.setGender(userProfileDto.getGender());

        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        AppUserEntity user = getUserByUsername(username);
        appUserRepository.delete(user);
    }

    private AppUserEntity getUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private UserProfileDto mapToProfileDto(AppUserEntity user) {
        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setUsername(user.getUsername());
        profileDto.setFullName(user.getFullName());
        profileDto.setCountry(user.getCountry());
        profileDto.setGender(user.getGender());
        profileDto.setDateOfBirth(user.getDateOfBirth());

        return profileDto;
    }
}
