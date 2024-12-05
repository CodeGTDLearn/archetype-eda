package ${package}.${entity_name_package}.config.properties.credentials;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
#if(${enable_lombok} == "true")
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
#end
@Component
@ConfigurationProperties(prefix = "admin.credentials")
public class AdminCredentials {

    private String userAdmin;
    private String userAdminPassword;

#if(${enable_lombok} == "false")
    public String getUserAdmin() {

        return userAdmin;
    }

    public void setUserAdmin(String userAdmin) {

        this.userAdmin = userAdmin;
    }

    public String getUserAdminPassword() {

        return userAdminPassword;
    }

    public void setUserAdminPassword(String userAdminPassword) {

        this.userAdminPassword = userAdminPassword;
    }
#end
}