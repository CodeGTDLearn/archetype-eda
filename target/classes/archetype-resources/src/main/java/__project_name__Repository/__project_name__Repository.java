#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Repository;

import ${package}.${project_name}.${project_name};

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($decapitalizedProjectName = "${firstChar}${restOfName}")
#set($componentAliasPrefix = $decapitalizedProjectName)
@Repository(value = "${componentAliasPrefix}Repository")
public interface ${project_name}Repository extends JpaRepository<${project_name}, Long> {
}