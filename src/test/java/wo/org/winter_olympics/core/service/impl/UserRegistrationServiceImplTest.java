package wo.org.winter_olympics.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.UserRoleEntity;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.data.entity.enums.UserRole;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.UserRoleRepository;
import wo.org.winter_olympics.dto.UserRegisterDto;
import wo.org.winter_olympics.exception.PasswordMismatchException;
import wo.org.winter_olympics.exception.RoleNotFoundException;
import wo.org.winter_olympics.exception.UsernameAlreadyExistsException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserRegistrationServiceImpl userRegistrationService;

    @BeforeEach
    void setUp() {
        userRegistrationService = new UserRegistrationServiceImpl(
                appUserRepository,
                userRoleRepository,
                passwordEncoder
        );
    }

    @Test
    void registerCreatesAthleteUser() {
        UserRegisterDto registerDto = createRegisterDto();
        UserRoleEntity athleteRole = new UserRoleEntity();
        athleteRole.setName(UserRole.ATHLETE);

        when(appUserRepository.existsByUsername("athlete1")).thenReturn(false);
        when(userRoleRepository.findByName(UserRole.ATHLETE)).thenReturn(Optional.of(athleteRole));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

        userRegistrationService.register(registerDto);

        ArgumentCaptor<AppUserEntity> userCaptor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserRepository).save(userCaptor.capture());

        AppUserEntity savedUser = userCaptor.getValue();
        assertEquals("athlete1", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("Ivan Petrov", savedUser.getFullName());
        assertEquals("Bulgaria", savedUser.getCountry());
        assertEquals(Gender.MALE, savedUser.getGender());
        assertEquals(LocalDate.of(2000, 1, 15), savedUser.getDateOfBirth());
        assertEquals(athleteRole, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        UserRegisterDto registerDto = createRegisterDto();
        when(appUserRepository.existsByUsername("athlete1")).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userRegistrationService.register(registerDto)
        );

        assertEquals("Username is already taken: athlete1", exception.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void registerThrowsWhenPasswordsDoNotMatch() {
        UserRegisterDto registerDto = createRegisterDto();
        registerDto.setConfirmPassword("different");

        when(appUserRepository.existsByUsername("athlete1")).thenReturn(false);

        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> userRegistrationService.register(registerDto)
        );

        assertEquals("Passwords do not match", exception.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void registerThrowsWhenAthleteRoleIsMissing() {
        UserRegisterDto registerDto = createRegisterDto();

        when(appUserRepository.existsByUsername("athlete1")).thenReturn(false);
        when(userRoleRepository.findByName(UserRole.ATHLETE)).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> userRegistrationService.register(registerDto)
        );

        assertEquals("Required role was not found: ATHLETE", exception.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    private UserRegisterDto createRegisterDto() {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("athlete1");
        registerDto.setPassword("secret123");
        registerDto.setConfirmPassword("secret123");
        registerDto.setFullName("Ivan Petrov");
        registerDto.setCountry("Bulgaria");
        registerDto.setGender(Gender.MALE);
        registerDto.setDateOfBirth(LocalDate.of(2000, 1, 15));

        return registerDto;
    }
}
