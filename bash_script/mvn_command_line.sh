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
-Denable_folder_src_test_java=\"$enable_folder_src_test_java\" \
-Denable_folder_src_test_resources=\"$enable_folder_src_test_resources\" \
\
-Denable_lombok=\"$enable_lombok\" \
\
-Denable_logback=\"$enable_logback\" \
\
-Denable_docker=\"$enable_docker\" \
\
-Denable_jwt_authentication=\"$enable_jwt_authentication\" \
\
-Denable_custom_attributes=\"$enable_custom_attributes\" \
-Dcustom_attributes_list=\"$custom_attributes_list\" \
\
-Dextra_POM_dependencies=\"$extra_dependencies\" \
\
-Dshow_archetype_posprocessor_log=\"$SHOW_POS_PROCESSOR_LOG\""