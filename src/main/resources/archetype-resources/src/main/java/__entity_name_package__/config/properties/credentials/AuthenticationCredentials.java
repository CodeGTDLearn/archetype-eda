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
@ConfigurationProperties(prefix = "authentication.credentials")
public class AuthenticationCredentials {

    private String user1password;
    private String userPasswordNoOwner;
    private String unknownPassword;

    private String userNameNoOwner;

    private String user1ID;
    private String user1name;
    private String rbacOwner;
    private String rbacNoOwner;

    private String unknownUser;

#if(${enable_lombok} == "false")
    public String getUser1password() {

        return user1password;
    }

    public void setUser1password(String user1password) {

        this.user1password = user1password;
    }

    public String getUserPasswordNoOwner() {

        return userPasswordNoOwner;
    }

    public void setUserPasswordNoOwner(String userPasswordNoOwner) {

        this.userPasswordNoOwner = userPasswordNoOwner;
    }

    public String getUnknownPassword() {

        return unknownPassword;
    }

    public void setUnknownPassword(String unknownPassword) {

        this.unknownPassword = unknownPassword;
    }

    public String getUserNameNoOwner() {

        return userNameNoOwner;
    }

    public void setUserNameNoOwner(String userNameNoOwner) {

        this.userNameNoOwner = userNameNoOwner;
    }

    public String getUser1ID() {

        return user1ID;
    }

    public void setUser1ID(String user1ID) {

        this.user1ID = user1ID;
    }

    public String getUser1name() {

        return user1name;
    }

    public void setUser1name(String user1name) {

        this.user1name = user1name;
    }

    public String getRbacOwner() {

        return rbacOwner;
    }

    public void setRbacOwner(String rbacOwner) {

        this.rbacOwner = rbacOwner;
    }

    public String getRbacNoOwner() {

        return rbacNoOwner;
    }

    public void setRbacNoOwner(String rbacNoOwner) {

        this.rbacNoOwner = rbacNoOwner;
    }

    public String getUnknownUser() {

        return unknownUser;
    }

    public void setUnknownUser(String unknownUser) {

        this.unknownUser = unknownUser;
    }
#end
}