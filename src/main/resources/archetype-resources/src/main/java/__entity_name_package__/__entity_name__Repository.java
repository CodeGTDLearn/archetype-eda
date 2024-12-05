#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name_package};


#if(${enable_denormalization} == "true")
import ${package}.${entity_name_package}.model.${entity_name};
#end
##
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($decapitalizedProjectName = "${firstChar}${restOfName}")
#set($componentAliasPrefix = $decapitalizedProjectName)
@Repository(value = "${componentAliasPrefix}Repository")
public interface ${entity_name}Repository extends JpaRepository<${entity_name}, Long> {
}