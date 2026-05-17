package wo.org.winter_olympics.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String roleName) {
        super("Required role was not found: " + roleName);
    }
}
