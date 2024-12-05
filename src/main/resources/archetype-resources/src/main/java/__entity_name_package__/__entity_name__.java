#set($symbol_pound = '#' )
#set($symbol_dollar = '$' )
#set($symbol_escape = '\' )
##
package ${package}.${entity_name_package};

import jakarta.persistence.Entity;
##
##
#if(${enable_custom_attributes} == "false")
##
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


#end
##
##
#if(${enable_lombok} == "true")
##
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
##
#end
##
##
@Entity
public class ${entity_name} {

##
#if(${enable_custom_attributes} == "true")
  ##
  #set($attribute_list = ${custom_attributes_list})
  #set($attributes = [])
  ##
  #foreach($attrib in $attribute_list.split(","))
    #set($parts = $attrib.split(":"))
    #set($dummy = $attributes.add({"type": $parts[0], "name": $parts[1]}))
  #end
  ##

  #foreach($attrib in $attributes)
    ##
  private ${attrib.type} ${attrib.name};
    ##
  #end

  #if(${enable_lombok} == "false")
    #set($canonic_constructor_params = ${custom_attributes_list})
    #set($canonic_constructor_params = $$canonic_constructor_params.replaceAll(":"," "))
    #set($final_constructor_params = $$canonic_constructor_params.replaceAll(",$", ""))

  public ${entity_name}(#evaluate($$final_constructor_params)){
    ##
    #foreach($attrib in $attributes)
      ##
    this.${attrib.name} = ${attrib.name};
      ##
    #end
    ##
  }
    ##
  #end

##
#end
##
##
#if(${enable_custom_attributes} == "false")
  ##
  ##
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String password;

  #if(${enable_lombok} == "false")
    ##
  public ${entity_name}(Long id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }
    ##
  #end
##
##
#end

#if(${enable_lombok} == "false")
##
  public ${entity_name}(){

  }
##
#end

#if(${enable_lombok} == "false" && ${enable_custom_attributes} == "true")
##
  #foreach($attrib in $attributes)
  ##
  public ${attrib.type} get${attrib.name.substring(0,1).toUpperCase()}${attrib.name.substring(1)}() {
    return this.${attrib.name};
  }

  public void set${attrib.name.substring(0,1).toUpperCase()}${attrib.name.substring(1)}(${attrib.type} ${attrib.name}) {
    this.${attrib.name} = ${attrib.name};
  }
  ##
  #end
##
#end

## uses de standard attributes / accessors + mutators: id, username and password
#if(${enable_lombok} == "false" && ${enable_custom_attributes} == "false")
##
  public Long getId() {
        return this.id;
      }

  public void setId(Long id) {
        this.id = id;
      }

  public String getUsername() {
        return this.username;
      }

  public void setUsername(String username) {
        this.username = username;
      }

   public String getPassword() {
        return this.password;
      }

  public void setPassword(String password) {
        this.password = password;
      }
##
#end
}