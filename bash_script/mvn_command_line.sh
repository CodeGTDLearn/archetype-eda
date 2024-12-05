#!/bin/bash

maven_command_line="mvn archetype:generate --batch-mode \
-DarchetypeGroupId=\"$ARCHETYPE_GROUP_ID\" \
-DarchetypeArtifactId=\"$ARCHETYPE_ARTIFACT_ID\" \
-DarchetypeVersion=\"$ARCHETYPE_VERSION\" \
\
-Dpackage=\"$project_package\" \
-DgroupId=\"$groupid\" \
-Dversion=\"$version\" \
-DartifactId=\"$artifactId\" \
-Dentity_name=\"$entity_name\" \
-Dproject_description=\"$project_description\" \
\
-Denable_binding_properties=\"$enable_binding_properties\" \
-Dproperties_filetype=\"$properties_filetype\" \
\
-Denable_folder_src_test_java=\"$enable_folder_src_test_java\" \
\
-Denable_lombok=\"$enable_lombok\" \
\
-Denable_logback=\"$enable_logback\" \
-Dlogback_console_type=\"$logback_console_type\" \
-Denable_logback_appender_smtp=\"$enable_logback_appender_smtp\" \
-Denable_logback_appender_file=\"$enable_logback_appender_file\" \
\
-Denable_api_cpanel=\"$enable_api_cpanel\" \
-Denable_api_cpanel_elk_log_system=\"$enable_api_cpanel_elk_log_system\" \
\
-Denable_docker=\"$enable_docker\" \
\
-Denable_jwt_authentication=\"$enable_jwt_authentication\" \
\
-Denable_custom_attributes=\"$enable_custom_attributes\" \
-Dcustom_attributes_list=\"$custom_attributes_list\" \
\
-Dextra_POM_dependencies=\"$extra_POM_dependencies\" \
\
-Dshow_archetype_pos_processor_log=\"$SHOW_ARCHETYPE_POS_PROCESSOR_LOG\""