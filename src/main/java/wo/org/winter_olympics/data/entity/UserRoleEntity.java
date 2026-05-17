package wo.org.winter_olympics.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import wo.org.winter_olympics.data.common.BaseEntityModel;
import wo.org.winter_olympics.data.entity.enums.UserRole;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class UserRoleEntity extends BaseEntityModel {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private UserRole name;

    @OneToMany(mappedBy = "role")
    private Set<AppUserEntity> users = new HashSet<>();

    public UserRole getName() {
        return name;
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    public Set<AppUserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<AppUserEntity> users) {
        this.users = users;
    }
}
