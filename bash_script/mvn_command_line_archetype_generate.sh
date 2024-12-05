#!/bin/bash

maven_command_line_arch_generate_var="mvn archetype:generate --batch-mode \
-DarchetypeGroupId=\"$ARCHETYPE_GROUP_ID\" \
-DarchetypeArtifactId=\"$ARCHETYPE_ARTIFACT_ID\" \
-DarchetypeVersion=\"$ARCHETYPE_VERSION\" \
\
-Dpackage=\"$project_package\" \
-DgroupId=\"$groupId\" \
-Dversion=\"$version\" \
-DartifactId=\"$artifactId\" \
-Dproject_description=\"$project_description\" \
-Djava_version=\"$java_version\" \
\
-Dentity_name=\"$entity_name\" \
-Dentity_name_package=\"$entity_name_package\" \
-Denable_denormalization=\"$enable_denormalization\" \
\
-Dapi_port=\"$api_port\" \
-Denable_git_support=\"$enable_git_support\" \
-Dgit_url_repo=\"$git_url_repo\" \
-Dgit_branch_repo=\"$git_branch_repo\" \
\
-Dproperties_filetype=\"$properties_filetype\" \
-Denable_binding_properties=\"$enable_binding_properties\" \
-Ddefault_binding_properties=\"$default_binding_properties\" \
-Dlist_binding_properties=\"$list_binding_properties\" \
-Dmap_binding_properties=\"$map_binding_properties\" \
-Denable_binding_properties_tests=\"$enable_binding_properties_tests\" \
-Dmutable_binding_properties=\"$mutable_binding_properties\" \
-Dvalidated_binding_properties=\"$validated_binding_properties\" \
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