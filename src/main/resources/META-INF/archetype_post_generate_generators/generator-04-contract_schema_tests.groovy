// -----------------------------------------------------------------------------

// GENERATOR-04-CONTRACT_SCHEMA_TESTS.GROOVY -----------------------------------
// FR-024..FR-029: emits one test file per SUBJECT of the persisted contract.
// Fixed templates live in archetype-resources; this generator only emits the
// files that vary with the flags. The guard is derived: it runs only when
// src/test/resources/contracts/contract.json was persisted by generator-03.

def _contractFile = srcTestResources.resolve("contracts").resolve("contract.json").toFile()
if (!_contractFile.isFile()) {
    println "Contract schema tests skipped: no persisted contract"
    log_text += "\nCONTRACT_SCHEMA_TESTS: skipped (no persisted contract)\n"
} else {
    def _basePackage = "${properties?.package}".trim()
    if (!isPackageKnown(_basePackage)) {
        println "WARNING: the package of the project is unknown - the contract schema tests were NOT generated."
        log_text += "\nCONTRACT_SCHEMA_TESTS: package unknown - not generated\n"
    } else {
        def _entityPackage = resolveEntityPackage(_basePackage, entity_package_name)
        def _contractPackage = _entityPackage + ".contract"
        Path _folder = srcTestJava.resolve(_entityPackage.replace('.', '/')).resolve("contract")
        _folder.toFile().mkdirs()

        def _javaLang = ['String', 'Boolean', 'Integer', 'Long', 'Double', 'Float',
                         'Short', 'Byte', 'Character'] as Set

        // ---- flags -----------------------------------------------------------
        def _attributes = []
        def _rawAttributes = (properties?["custom_entity_attributes_list"] ?: "").toString().trim()
        if (_rawAttributes && _rawAttributes != 'none' && _rawAttributes != 'null') {
            _rawAttributes.split(',').each { String entry ->
                String text = entry.trim()
                if (!text) return
                int separator = text.indexOf(':')
                if (separator <= 0) return
                String type = text.substring(0, separator).trim()
                String name = text.substring(separator + 1).trim()
                if (!name || type.isEmpty()) return
                _attributes << [type: type, name: name]
            }
        }

        def _businessAttributes = _attributes.findAll { !it.name.equalsIgnoreCase('id') }
        def _nestedTypes = [:]           // type -> [[name, type]]
        def _nestedOrder = []
        def _rawNested = (contract_nested_type_definitions ?: "").toString().trim()
        if (_rawNested && _rawNested != 'none' && _rawNested != 'null') {
            _rawNested.split(';').each { String definition ->
                String text = definition.trim()
                if (!text) return
                int separator = text.indexOf('|')
                if (separator <= 0) return
                String type = text.substring(0, separator).trim()
                String fieldList = text.substring(separator + 1).trim()
                if (!type || !fieldList) return
                def fields = []
                fieldList.split(',').each { String rawField ->
                    String field = rawField.trim()
                    if (!field) return
                    int fs = field.indexOf(':')
                    if (fs <= 0) return
                    fields << [name: field.substring(0, fs).trim(), type: field.substring(fs + 1).trim()]
                }
                _nestedOrder << type
                _nestedTypes[type] = fields
            }
        }

        def _enums = [:]                 // simple type -> [constants, nested]
        def _enumOrder = []
        def _rawEnums = (contract_enum_definitions ?: "").toString().trim()
        if (_rawEnums && _rawEnums != 'none' && _rawEnums != 'null') {
            _rawEnums.split(';').each { String definition ->
                String text = definition.trim()
                if (!text) return
                int separator = text.indexOf(':')
                if (separator <= 0) return
                String rawType = text.substring(0, separator).trim()
                String constants = text.substring(separator + 1).trim()
                boolean nested = rawType.startsWith('nested.')
                String type = nested ? rawType.substring('nested.'.length()).trim()
                        : (rawType.contains('.') ? rawType.substring(rawType.lastIndexOf('.') + 1) : rawType)
                def values = constants.split('\\|').collect { it.trim() }.findAll { it }
                _enumOrder << type
                _enums[type] = [constants: values, nested: nested]
            }
        }

        def _nestedNames = _nestedTypes.keySet() as Set
        def _enumNames = _enums.keySet() as Set
        def _simple = _businessAttributes.findAll { !_nestedNames.contains(it.type) && !_enumNames.contains(it.type) }
        def _nestedRefs = _businessAttributes.findAll { _nestedNames.contains(it.type) }
        def _enumRefs = _businessAttributes.findAll { _enumNames.contains(it.type) }
        boolean _custom = !"false".equalsIgnoreCase(("" + (properties?["enable_custom_entity_attributes"] ?: "true")).trim())

        String _entity = "${entity_name}"
        String _techId = _entity.substring(0, 1).toLowerCase() + _entity.substring(1) + "Id"
        String _entityFqn = _entityPackage + "." + _entity

        // ---- helpers ---------------------------------------------------------
        def _cap = { String value -> value.substring(0, 1).toUpperCase() + value.substring(1) }
        def _typeRef = { String type -> (type in _javaLang || type in _nestedNames || type in _enumNames) ? type + ".class" : null }
        def _assertType = { String owner, String field, String type ->
            String ref = _typeRef(type)
            ref != null
                    ? "        EntityFields.assertFieldType(" + owner + ", \"" + field + "\", " + ref + ");\n"
                    : "        EntityFields.assertFieldTypeName(" + owner + ", \"" + field + "\", \"" + type + "\");\n"
        }
        def _importsFor = { List<String> types ->
            def typeImports = [] as Set
            types.each { String type ->
                if (_javaLang.contains(type)) return
                if (_nestedNames.contains(type)) { typeImports << _entityPackage + ".nested." + type; return }
                if (_enumNames.contains(type)) {
                    typeImports << (_enums[type].nested ? _entityPackage + ".nested." + type : _entityPackage + "." + type)
                    return
                }
            }
            return typeImports.toList().sort()
        }
        def _testHeader = { String className, List<String> extraImports ->
            def headerImports = ['org.junit.jupiter.api.BeforeAll', 'org.junit.jupiter.api.Test',
                                 'org.junit.jupiter.api.TestInstance', 'org.junit.jupiter.api.TestMethodOrder',
                                 'org.junit.jupiter.api.MethodOrderer',
                                 'static org.assertj.core.api.Assertions.assertThat',
                                 'static org.assertj.core.api.Assertions.assertThatCode'] + extraImports
            StringBuilder source = new StringBuilder()
            source.append("package ").append(_contractPackage).append(";\n\n")
            headerImports.unique().each { source.append("import ").append(it).append(";\n") }
            source.append("\n@TestInstance(TestInstance.Lifecycle.PER_CLASS)\n")
            source.append("@TestMethodOrder(MethodOrderer.MethodName.class)\n")
            source.append("class ").append(className).append(" {\n\n")
            source.append("    private ContractJson contract;\n\n")
            source.append("    @BeforeAll\n    void loadContract() {\n        contract = ContractJson.load();\n    }\n\n")
            return source
        }
        def _planned = [] as Set
        def _emit = { String className, StringBuilder source ->
            source.append("}\n")
            if (!_planned.add(className)) {
                throw new IllegalStateException("CONTRACT_SCHEMA_TESTS: duplicate test file '" + className + "'")
            }
            Path written = writeJavaSourceIfAbsent(_folder, className, source.toString())
            if (written == null) {
                throw new IllegalStateException("CONTRACT_SCHEMA_TESTS: file '" + className
                        + ".java' already exists; refusing to overwrite")
            }
            log_text += "\nCONTRACT_SCHEMA_TEST_FILE: " + className + " -> " + written + "\n"
            println "Contract schema test generated: " + className + ".java"
        }

        // ---- SimpleAttributeFieldsTest ---------------------------------------
        if (!_simple.isEmpty()) {
            StringBuilder source = _testHeader("SimpleAttributeFieldsTest",
                    [_entityFqn] + _importsFor(_simple*.type))
            _simple.each { attribute ->
                source.append("    @Test\n    void ").append(attribute.name).append("Is")
                        .append(_cap(attribute.type.equalsIgnoreCase('String') ? 'String' : attribute.type))
                        .append("() {\n")
                source.append(_assertType(_entity + ".class", attribute.name, attribute.type))
                source.append("    }\n\n")
            }
            _emit("SimpleAttributeFieldsTest", source)
        }

        // ---- NestedAttributeReferenceTest ------------------------------------
        if (!_nestedRefs.isEmpty()) {
            StringBuilder source = _testHeader("NestedAttributeReferenceTest",
                    [_entityFqn] + _importsFor(_nestedRefs*.type))
            _nestedRefs.each { attribute ->
                source.append("    @Test\n    void ").append(attribute.name).append("IsThe")
                        .append(attribute.type).append("Type() {\n")
                source.append(_assertType(_entity + ".class", attribute.name, attribute.type))
                source.append("    }\n\n")
            }
            _emit("NestedAttributeReferenceTest", source)
        }

        // ---- EnumAttributeReferenceTest --------------------------------------
        if (!_enumRefs.isEmpty()) {
            StringBuilder source = _testHeader("EnumAttributeReferenceTest",
                    [_entityFqn] + _importsFor(_enumRefs*.type))
            _enumRefs.each { attribute ->
                source.append("    @Test\n    void ").append(attribute.name).append("IsThe")
                        .append(attribute.type).append("Enum() {\n")
                source.append(_assertType(_entity + ".class", attribute.name, attribute.type))
                source.append("    }\n\n")
            }
            _emit("EnumAttributeReferenceTest", source)
        }

        // ---- NoExtraEntityFieldTest ------------------------------------------
        def _expectedEntityFields = [ _techId ] + _businessAttributes.collect { it.name }
        if (!_custom) {
            _expectedEntityFields = [ _techId, 'username', 'password' ]
        }
        StringBuilder noExtra = _testHeader("NoExtraEntityFieldTest", [_entityFqn])
        noExtra.append("    @Test\n    void entityFieldsAreExactlyTheExpectedSet() {\n")
        noExtra.append("        assertThat(EntityFields.fieldNames(").append(_entity).append(".class))\n")
        noExtra.append("                .as(\"declared entity fields\")\n")
        noExtra.append("                .containsExactlyInAnyOrder(")
                .append(_expectedEntityFields.collect { '"' + it + '"' }.join(', ')).append(");\n")
        noExtra.append("    }\n\n")
        if (_custom) {
            noExtra.append("    @Test\n    void entityHasNoScaffoldFields() {\n")
            noExtra.append("        assertThat(EntityFields.fieldNames(").append(_entity).append(".class))\n")
            noExtra.append("                .as(\"scaffold fields\")\n")
            noExtra.append("                .doesNotContain(\"username\", \"password\");\n")
            noExtra.append("    }\n\n")
        } else {
            noExtra.append("    @Test\n    void entityDeclaresTheKnownScaffoldFields() {\n")
            noExtra.append("        assertThat(EntityFields.fieldNames(").append(_entity).append(".class))\n")
            noExtra.append("                .as(\"scaffold fields\")\n")
            noExtra.append("                .contains(\"username\", \"password\");\n")
            noExtra.append("    }\n\n")
        }
        _emit("NoExtraEntityFieldTest", noExtra)

        // ---- ContractRoundTripTest -------------------------------------------
        def _jsonValue
        _jsonValue = { String type ->
            if (_nestedTypes.containsKey(type)) {
                def fields = _nestedTypes[type]
                return "{" + fields.collect { '"' + it.name + '":' + _jsonValue(it.type) }.join(',') + "}"
            }
            if (_enums.containsKey(type)) {
                return '"' + _enums[type].constants[0] + '"'
            }
            if (type == 'Boolean') return 'true'
            if (type == 'Integer' || type == 'Long' || type == 'Short' || type == 'Byte') return '1'
            if (type == 'Double' || type == 'Float') return '1.0'
            if (_javaLang.contains(type)) return '"x"'
            return null
        }
        def _payloadEntries = []
        _payloadEntries << ('"' + _techId + '":1')
        _businessAttributes.each { attribute ->
            String value = _jsonValue(attribute.type)
            if (value != null) {
                _payloadEntries << ('"' + attribute.name + '":' + value)
            }
        }
        String _payload = "{" + _payloadEntries.join(',') + "}"
        StringBuilder roundTrip = _testHeader("ContractRoundTripTest", [
                _entityFqn,
                'tools.jackson.databind.DeserializationFeature',
                'tools.jackson.databind.JsonNode',
                'tools.jackson.databind.json.JsonMapper'])
        roundTrip.append("    private static JsonMapper mapper() {\n")
        roundTrip.append("        return JsonMapper.builder()\n")
        roundTrip.append("                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();\n")
        roundTrip.append("    }\n\n")
        roundTrip.append("    private static String payload() {\n")
        roundTrip.append("        return \"\"\"\n").append(_payload).append("\n\"\"\";\n")
        roundTrip.append("    }\n\n")
        roundTrip.append("    @Test\n    void contractPayloadDeserializesIntoTheEntity() {\n")
        roundTrip.append("        assertThatCode(() -> mapper().readValue(payload(), ").append(_entity).append(".class))\n")
        roundTrip.append("                .doesNotThrowAnyException();\n")
        roundTrip.append("    }\n\n")
        roundTrip.append("    @Test\n    void entitySerializesBackWithTheSameKeys() throws Exception {\n")
        roundTrip.append("        ").append(_entity).append(" entity = mapper().readValue(payload(), ").append(_entity).append(".class);\n")
        roundTrip.append("        JsonNode actual = mapper().readTree(mapper().writeValueAsString(entity));\n")
        roundTrip.append("        assertThat(actual.properties().stream().map(e -> e.getKey()).toList())\n")
        roundTrip.append("                .as(\"serialized keys\")\n")
        roundTrip.append("                .containsExactlyInAnyOrder(")
                .append(([_techId] + _businessAttributes.collect { it.name }).collect { '"' + it + '"' }.join(', ')).append(");\n")
        roundTrip.append("    }\n\n")
        _emit("ContractRoundTripTest", roundTrip)

        // ---- SavedContractMatchesGeneratedModelTest --------------------------
        def _contractKeys = _attributes.collect { it.name.equalsIgnoreCase('id') ? _techId : it.name }
        StringBuilder saved = _testHeader("SavedContractMatchesGeneratedModelTest", [])
        saved.append("    @Test\n    void savedContractMatchesTheGeneratedModel() {\n")
        saved.append("        assertThat(contract.attributes().stream()\n")
        saved.append("                .map(attribute -> ContractKeys.topLevelName(attribute.key()))\n")
        saved.append("                .filter(java.util.Objects::nonNull)\n")
        saved.append("                .toList())\n")
        saved.append("                .as(\"contract keys\")\n")
        saved.append("                .containsExactlyInAnyOrder(")
                .append(_contractKeys.collect { '"' + it + '"' }.join(', ')).append(");\n")
        saved.append("    }\n\n")
        _emit("SavedContractMatchesGeneratedModelTest", saved)

        // ---- ContractTypeMappingTest -----------------------------------------
        StringBuilder types = _testHeader("ContractTypeMappingTest", [
                'tools.jackson.databind.JsonNode'])
        types.append("    private static final java.util.Map<String, String> EXPECTED = new java.util.LinkedHashMap<>();\n")
        types.append("    private static final java.util.Set<String> JAVA_LANG = java.util.Set.of(\n")
        types.append("            \"String\", \"Boolean\", \"Integer\", \"Long\", \"Double\", \"Float\",\n")
        types.append("            \"Short\", \"Byte\", \"Character\");\n\n")
        types.append("    static {\n")
        types.append("        EXPECTED.put(\"").append(_techId).append("\", \"Long\");\n")
        _businessAttributes.each { attribute ->
            types.append("        EXPECTED.put(\"").append(attribute.name).append("\", \"")
                    .append(attribute.type).append("\");\n")
        }
        types.append("    }\n\n")
        types.append("    @Test\n    void effectiveTypesMatchTheGeneratedModel() {\n")
        types.append("        java.util.List<String> mismatches = contract.attributes().stream()\n")
        types.append("                .filter(attribute -> !EXPECTED.getOrDefault(\n")
        types.append("                        ContractKeys.topLevelName(attribute.key()), \"\")\n")
        types.append("                        .equals(effectiveType(attribute)))\n")
        types.append("                .map(attribute -> attribute.key() + \"=\" + effectiveType(attribute))\n")
        types.append("                .toList();\n")
        types.append("        assertThat(mismatches).as(\"effective type mismatches\").isEmpty();\n")
        types.append("    }\n\n")
        types.append("    private static String effectiveType(ContractJson.Attribute attribute) {\n")
        types.append("        if (attribute.key().equalsIgnoreCase(\"id\")) return \"Long\";\n")
        types.append("        String declared = attribute.declaredType();\n")
        types.append("        String simple = declared == null ? null : simpleName(declared);\n")
        types.append("        if (simple != null && JAVA_LANG.contains(simple)) return simple;\n")
        types.append("        if (simple != null && accepted(declared)) {\n")
        types.append("            if (attribute.value() != null && attribute.value().isArray()\n")
        types.append("                    && !isEnumArray(attribute.value())) return \"String\";\n")
        types.append("            return simple;\n")
        types.append("        }\n")
        types.append("        if (attribute.value() != null && attribute.value().isObject()) {\n")
        types.append("            return ContractKeys.typeName(attribute.key());\n")
        types.append("        }\n")
        types.append("        return infer(attribute.value());\n")
        types.append("    }\n\n")
        types.append("    private static String infer(JsonNode value) {\n")
        types.append("        if (value == null || value.isNull()) return \"String\";\n")
        types.append("        if (value.isBoolean()) return \"Boolean\";\n")
        types.append("        if (value.isIntegralNumber()) return value.canConvertToInt() ? \"Integer\" : \"Long\";\n")
        types.append("        if (value.isFloatingPointNumber()) return \"Double\";\n")
        types.append("        return \"String\";\n")
        types.append("    }\n\n")
        types.append("    private static boolean isEnumArray(JsonNode value) {\n")
        types.append("        if (value == null || !value.isArray() || value.isEmpty()) return false;\n")
        types.append("        for (JsonNode element : value) {\n")
        types.append("            if (!element.isTextual() || !element.asText().matches(\"^[A-Z][A-Z0-9_]*\$\")) {\n")
        types.append("                return false;\n")
        types.append("            }\n")
        types.append("        }\n")
        types.append("        return true;\n")
        types.append("    }\n\n")
        types.append("    private static boolean accepted(String declared) {\n")
        types.append("        String simple = simpleName(declared);\n")
        types.append("        if (JAVA_LANG.contains(simple)) return true;\n")
        types.append("        String value = declared.trim();\n")
        types.append("        if (value.startsWith(\"java.\") || value.startsWith(\"javax.\")\n")
        types.append("                || value.startsWith(\"jakarta.\")) return false;\n")
        types.append("        return simple.matches(\"^[A-Z][A-Za-z0-9_]*\$\");\n")
        types.append("    }\n\n")
        types.append("    private static String simpleName(String declared) {\n")
        types.append("        int dot = declared.lastIndexOf('.');\n")
        types.append("        return dot >= 0 ? declared.substring(dot + 1) : declared;\n")
        types.append("    }\n\n")
        _emit("ContractTypeMappingTest", types)

        // ---- one file per nested type ----------------------------------------
        _nestedOrder.each { String type ->
            def fields = _nestedTypes[type]
            if (fields.isEmpty()) return
            StringBuilder source = _testHeader(type + "FieldsTest",
                    [_entityPackage + ".nested." + type] + _importsFor(fields*.type))
            fields.each { field ->
                source.append("    @Test\n    void ").append(field.name).append("Is")
                        .append(_cap(field.type)).append("() {\n")
                source.append(_assertType(type + ".class", field.name, field.type))
                source.append("    }\n\n")
            }
            _emit(type + "FieldsTest", source)
        }

        // ---- one file per enum -----------------------------------------------
        _enumOrder.each { String type ->
            def info = _enums[type]
            if (info.constants.isEmpty()) return
            def enumFqn = info.nested ? _entityPackage + ".nested." + type : _entityPackage + "." + type
            StringBuilder source = _testHeader(type + "EnumTest", [enumFqn])
            source.append("    @Test\n    void enumHasTheContractConstants() {\n")
            source.append("        assertThat(java.util.Arrays.stream(").append(type).append(".values())\n")
            source.append("                .map(Enum::name).toList())\n")
            source.append("                .as(\"enum constants\")\n")
            source.append("                .containsAll(java.util.List.of(")
                    .append(info.constants.collect { '"' + it + '"' }.join(', ')).append("));\n")
            source.append("    }\n\n")
            source.append("    @Test\n    void enumHasNoUnexpectedConstant() {\n")
            source.append("        assertThat(java.util.Arrays.stream(").append(type).append(".values())\n")
            source.append("                .map(Enum::name).toList())\n")
            source.append("                .as(\"enum constants\")\n")
            source.append("                .containsOnly(")
                    .append(info.constants.collect { '"' + it + '"' }.join(', ')).append(");\n")
            source.append("    }\n\n")
            _emit(type + "EnumTest", source)
        }

        // ---- scaffold (only when custom attributes are OFF) ------------------
        if (!_custom) {
            StringBuilder source = _testHeader("ScaffoldFieldsTest", [_entityFqn])
            source.append("    @Test\n    void scaffoldDeclaresUsername() {\n")
            source.append("        EntityFields.assertFieldType(").append(_entity).append(".class, \"username\", String.class);\n")
            source.append("    }\n\n")
            source.append("    @Test\n    void scaffoldDeclaresPassword() {\n")
            source.append("        EntityFields.assertFieldType(").append(_entity).append(".class, \"password\", String.class);\n")
            source.append("    }\n\n")
            _emit("ScaffoldFieldsTest", source)
        }

        log_text += "\nCONTRACT_SCHEMA_TESTS: generated " + _planned.size() + " file(s)\n"
        println "Contract schema tests generated: " + _planned.size() + " file(s)"
    }
}
