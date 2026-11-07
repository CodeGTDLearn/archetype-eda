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
## NESTED TYPES (contract): the POJOs derived from the nested objects of the
## contract live in the 'nested' subpackage. The entity references each one by
## its FULLY QUALIFIED name, so no import is emitted and a nested type can
## never collide with a template import (Entity, Id, Data...). The
## "@Transient" import exists only when at least one nested type is used.
#set($nested_types = {})
#if(${enable_custom_attributes} == "true" && ${custom_nested_type_definitions} != "none" && ${custom_nested_type_definitions} != "null")
#set($nested_defs = ${custom_nested_type_definitions})
#foreach($nested_def in $nested_defs.split(";"))
#set($nested_def_trim = $nested_def.trim())
#if($nested_def_trim.length() > 0 && $nested_def_trim.indexOf("|") > 0)
#set($nested_type = $nested_def_trim.substring(0, $nested_def_trim.indexOf("|")).trim())
#set($dummy = $nested_types.put($nested_type, true))
#end
#end
#end
#set($nested_package_full = "${package}")
#if(${entity_name_package} != "" && ${entity_name_package} != "null")
#set($nested_package_full = "${package}.${entity_name_package}")
#end
#set($nested_package_full = "${nested_package_full}.nested")
#set($nested_types_used_list = [])
#if(${enable_custom_attributes} == "true" && ${custom_attributes_list} != "none" && ${custom_attributes_list} != "false")
#set($custom_attributes_value = ${custom_attributes_list})
#foreach($attrib_import in $custom_attributes_value.split(","))
#set($attrib_import_parts = $attrib_import.split(":"))
#if($attrib_import_parts.size() > 1 && $nested_types.containsKey($attrib_import_parts[0].trim()))
#set($dummy = $nested_types_used_list.add($attrib_import_parts[0].trim()))
#end
#end
#end
#if($nested_types_used_list.size() > 0)
import jakarta.persistence.Transient;
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
  #if($nested_types.containsKey($attrib.type))
  @Transient
  private ${nested_package_full}.${attrib.type} ${attrib.name};
  #else
  private ${attrib.type} ${attrib.name};
  #end
    ##
  #end

  #if(${enable_lombok} == "false" && $attributes.size() > 0)
    #set($canonic_constructor_params = "")
    #foreach($attrib in $attributes)
      #set($attrib_type = $attrib.type)
      #if($nested_types.containsKey($attrib.type))
        #set($attrib_type = "${nested_package_full}.${attrib.type}")
      #end
      #set($canonic_constructor_params = "${canonic_constructor_params}${attrib_type}:${attrib.name},")
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
  #set($attrib_type = $attrib.type)
  #if($nested_types.containsKey($attrib.type))
    #set($attrib_type = "${nested_package_full}.${attrib.type}")
  #end
  public ${attrib_type} get${attrib.name.substring(0,1).toUpperCase()}${attrib.name.substring(1)}() {
    return this.${attrib.name};
  }

  public void set${attrib.name.substring(0,1).toUpperCase()}${attrib.name.substring(1)}(${attrib_type} ${attrib.name}) {
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