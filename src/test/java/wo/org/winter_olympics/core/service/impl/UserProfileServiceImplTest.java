package wo.org.winter_olympics.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.dto.UserProfileDto;
import wo.org.winter_olympics.exception.UserNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    private UserProfileServiceImpl userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileServiceImpl(appUserRepository);
    }

    @Test
    void getProfileReturnsUserData() {
        AppUserEntity user = createUser();
        when(appUserRepository.findByUsername("athlete1")).thenReturn(Optional.of(user));

        UserProfileDto profileDto = userProfileService.getProfile("athlete1");

        assertEquals("athlete1", profileDto.getUsername());
        assertEquals("Ivan Petrov", profileDto.getFullName());
        assertEquals("Bulgaria", profileDto.getCountry());
        assertEquals(Gender.MALE, profileDto.getGender());
        assertEquals(LocalDate.of(2000, 1, 15), profileDto.getDateOfBirth());
    }

    @Test
    void updateProfileChangesOnlyEditableFields() {
        AppUserEntity user = createUser();
        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setUsername("changed-username");
        profileDto.setFullName("Georgi Ivanov");
        profileDto.setCountry("Norway");
        profileDto.setGender(Gender.FEMALE);
        profileDto.setDateOfBirth(LocalDate.of(1995, 5, 20));

        when(appUserRepository.findByUsername("athlete1")).thenReturn(Optional.of(user));

        userProfileService.updateProfile("athlete1", profileDto);

        ArgumentCaptor<AppUserEntity> userCaptor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserRepository).save(userCaptor.capture());

        AppUserEntity savedUser = userCaptor.getValue();
        assertEquals("athlete1", savedUser.getUsername());
        assertEquals("Georgi Ivanov", savedUser.getFullName());
        assertEquals("Norway", savedUser.getCountry());
        assertEquals(Gender.FEMALE, savedUser.getGender());
        assertEquals(LocalDate.of(2000, 1, 15), savedUser.getDateOfBirth());
    }

    @Test
    void deleteProfileDeletesUser() {
        AppUserEntity user = createUser();
        when(appUserRepository.findByUsername("athlete1")).thenReturn(Optional.of(user));

        userProfileService.deleteProfile("athlete1");

        verify(appUserRepository).delete(user);
    }

    @Test
    void getProfileThrowsWhenUserDoesNotExist() {
        when(appUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userProfileService.getProfile("missing")
        );

        assertEquals("User was not found: missing", exception.getMessage());
    }

    private AppUserEntity createUser() {
        AppUserEntity user = new AppUserEntity();
        user.setUsername("athlete1");
        user.setFullName("Ivan Petrov");
        user.setCountry("Bulgaria");
        user.setGender(Gender.MALE);
        user.setDateOfBirth(LocalDate.of(2000, 1, 15));

        return user;
    }
}
