package wo.org.winter_olympics.core.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wo.org.winter_olympics.core.service.UserRegistrationService;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.UserRoleEntity;
import wo.org.winter_olympics.data.entity.enums.UserRole;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.UserRoleRepository;
import wo.org.winter_olympics.dto.UserRegisterDto;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationServiceImpl(
            AppUserRepository appUserRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(UserRegisterDto userRegisterDto) {
        if (appUserRepository.existsByUsername(userRegisterDto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserRoleEntity athleteRole = userRoleRepository.findByName(UserRole.ATHLETE)
                .orElseThrow(() -> new IllegalStateException("Athlete role is not initialized"));

        AppUserEntity user = new AppUserEntity();
        user.setUsername(userRegisterDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.setFullName(userRegisterDto.getFullName());
        user.setCountry(userRegisterDto.getCountry());
        user.setGender(userRegisterDto.getGender());
        user.setDateOfBirth(userRegisterDto.getDateOfBirth());
        user.setRole(athleteRole);
        user.setEnabled(true);

        appUserRepository.save(user);
    }
}
