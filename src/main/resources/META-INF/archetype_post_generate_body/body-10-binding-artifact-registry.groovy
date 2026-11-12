// -----------------------------------------------------------------------------







// BODY-10-BINDING-ARTIFACT-REGISTRY.GROOVY ----------------------------------
// Single inventory of the BINDING EXTERNAL PROPERTIES artifacts.
//
// The binding snippets never repeat these file lists: each snippet keeps its
// own gate and reads the inventory from here. No gating lives in this file.
//
// Folder keys:
//   root       -> <entity>/config/properties
//   list       -> <entity>/config/properties/list_properties
//   map        -> <entity>/config/properties/map_properties
//   properties -> src/(main|test)/resources/properties

final Map<String, Map<String, List<String>>> binding_source_artifacts = [
        default  : [
                root: ["ApplicationProperties.java", "ApplicationPropertiesRecord.java"]
        ],
        list     : [
                root: [
                        "ListObjects.java",
                        "ListProperties.java",
                        "ListPropertiesImmutable.java",
                        "ListPropertiesRecord.java"
                ]
        ],
        map      : [
                root: [
                        "MapItem.java",
                        "MapProperties.java",
                        "MapPropertiesImmutable.java",
                        "MapPropertiesRecord.java"
                ]
        ],
        immutable: [
                root: ["ApplicationProperties.java"],
                list: ["ListProperties.java"],
                map : ["MapProperties.java"]
        ],
        mutable  : [
                root: ["ApplicationPropertiesRecord.java"],
                list: ["ListPropertiesImmutable.java", "ListPropertiesRecord.java"],
                map : ["MapPropertiesImmutable.java", "MapPropertiesRecord.java"]
        ]
]

final Map<String, Map<String, List<String>>> binding_resource_artifacts = [
        list: [properties: ["application_list.properties", "application_list.yml"]],
        map : [properties: ["application_map.properties", "application_map.yml"]]
]

final Map<String, Map<String, List<String>>> binding_test_source_artifacts = [
        default  : [
                root: [
                        "ApplicationPropertiesRecordTest.java",
                        "ApplicationPropertiesRecordValidationTest.java",
                        "ApplicationPropertiesTest.java",
                        "ApplicationPropertiesValidationTest.java"
                ]
        ],
        immutable: [
                root: ["ApplicationPropertiesTest.java", "ApplicationPropertiesValidationTest.java"],
                list: ["ListPropertiesTest.java"],
                map : ["MapPropertiesTest.java"]
        ],
        mutable  : [
                root: [
                        "ApplicationPropertiesRecordValidationTest.java",
                        "ApplicationPropertiesRecordTest.java"
                ],
                list: ["ListPropertiesImmutableTest.java", "ListPropertiesRecordTest.java"],
                map : ["MapPropertiesImmutableTest.java", "MapPropertiesRecordTest.java"]
        ],
        validated: [
                root: [
                        "ApplicationPropertiesRecordValidationTest.java",
                        "ApplicationPropertiesValidationTest.java"
                ]
        ]
]

final Map<String, Map<String, List<String>>> binding_test_resource_artifacts = [
        list: [properties: ["application_list_test.properties", "application_list_test.yml"]],
        map : [properties: ["application_map_test.properties", "application_map_test.yml"]]
]
