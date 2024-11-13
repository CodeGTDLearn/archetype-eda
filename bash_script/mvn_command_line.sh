#!/bin/bash

maven_command_line="mvn archetype:generate --batch-mode \
-DarchetypeGroupId=\"$ARCHETYPE_GROUP_ID\" \
\
-DarchetypeArtifactId=\"$ARCHETYPE_ARTIFACT_ID\" \
-DarchetypeVersion=\"$ARCHETYPE_VERSION\" \
\
-Dpackage=\"$groupid.$artifactId\" \
-DgroupId=\"$groupid\" \
-DartifactId=\"$artifactId\" \
-Dversion=\"$version\" \
-Dproject_name=\"$project_name\" \
-Dproject_description=\"$project_description\" \
\
-Denable_folder_src_test_java=\"$enable_folder_src_test_java\" \
-Denable_folder_src_test_resources=\"$enable_folder_src_test_resources\" \
-Denable_lombok=\"$enable_lombok\" \
-Denable_yml_file_in_main_resources=\"$enable_yml_file_in_main_resources\" \
-Denable_security_authentication_support=\"$enable_security_authentic_support\" \
-Denable_custom_attributes=\"$enable_custom_attributes\" \
\
-Dcustom_attributes_list=\"$custom_attributes_list\" \
\
-Dextra_POM_dependencies=\"$extra_dependencies\" \
\
-Dshow_archetype_posprocessor_log=\"$ADD_POS_PROCESSOR_LOG_IN_MAVEN_BASIC_LOG\""