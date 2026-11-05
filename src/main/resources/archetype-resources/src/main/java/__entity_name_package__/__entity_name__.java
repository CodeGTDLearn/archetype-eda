#set($symbol_pound = '#' )
#set($symbol_dollar = '$' )
#set($symbol_escape = '\' )
##
package ${package}.${entity_name_package};

import jakarta.persistence.Entity;
##
#if(${enable_custom_attributes} == "true" && ${custom_attribute_imports} != "none")
#set($entity_package_full = "${package}.${entity_name_package}")
#set($custom_imports_list = ${custom_attribute_imports})
#foreach($custom_import in $custom_imports_list.split(","))
#set($custom_import_trim = $custom_import.trim())
#if($custom_import_trim.length() > 0 && $custom_import_trim.lastIndexOf(".") > 0)
#set($custom_import_pkg = $custom_import_trim.substring(0, $custom_import_trim.lastIndexOf(".")))
#if($custom_import_pkg != $entity_package_full)
import $custom_import_trim;
#end
#end
#end
#end
##
##
##
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
#set($id_base = "${entity_name.substring(0,1).toLowerCase()}${entity_name.substring(1)}")
#if($entity_name == $entity_name.toUpperCase())
  #set($id_base = $entity_name.toLowerCase())
#end
#set($id_field = "${id_base}Id")
#set($id_field_lower = $id_field.toLowerCase())
#set($id_suffix = "${id_field.substring(0,1).toUpperCase()}${id_field.substring(1)}")
@Entity
public class ${entity_name} {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long ${id_field};

#if(${enable_lombok} == "false" && ${enable_custom_attributes} == "true")
  public Long get${id_suffix}() {
    return this.${id_field};
  }

  public void set${id_suffix}(Long ${id_field}) {
    this.${id_field} = ${id_field};
  }

#end
##
#if(${enable_custom_attributes} == "true")
  ##
  #set($attribute_list = ${custom_attributes_list})
  #set($attributes = [])
  ##
  #foreach($attrib in $attribute_list.split(","))
    #set($parts = $attrib.split(":"))
    #set($attrib_name_lower = $parts[1].toLowerCase())
    #if($attrib_name_lower != "id" && $attrib_name_lower != $id_field_lower)
      #set($dummy = $attributes.add({"type": $parts[0], "name": $parts[1]}))
    #end
  #end
  ##

  #foreach($attrib in $attributes)
    ##
  private ${attrib.type} ${attrib.name};
    ##
  #end

  #if(${enable_lombok} == "false" && $attributes.size() > 0)
    #set($canonic_constructor_params = "")
    #foreach($attrib in $attributes)
      #set($canonic_constructor_params = "${canonic_constructor_params}${attrib.type}:${attrib.name},")
    #end
    #set($canonic_constructor_params = $canonic_constructor_params.replaceAll(":"," "))
    #set($final_constructor_params = $canonic_constructor_params.replaceAll(",$", ""))

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
  private String username;
  private String password;

  #if(${enable_lombok} == "false")
    ##
  public ${entity_name}(Long ${id_field}, String username, String password) {
    this.${id_field} = ${id_field};
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
  public Long get${id_suffix}() {
        return this.${id_field};
      }

  public void set${id_suffix}(Long ${id_field}) {
        this.${id_field} = ${id_field};
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