/**
 * Unit test for NestedTypeFlagProjector - the projection of the nested POJOs
 * derived from the contract into the custom_nested_type_definitions flag.
 *
 * The component under test is the production module:
 *   src/main/pre-processamento-maven-flags-via-llm/domain/NestedTypeFlagProjector.groovy
 *
 * No JUnit infra in this repo; runnable directly:
 *   groovy src/test/pre-processamento-maven-flags-via-llm/domain/NestedTypeFlagProjectorTest.groovy
 * Exits 0 when every check passes, 1 otherwise.
 */

def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def entryClass = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry')
def policyClass = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagValidationPolicy')
def projectorClass = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.NestedTypeFlagProjector')
def policy = policyClass.newInstance()
def projector = projectorClass.newInstance(policy)

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def FLAG = 'custom_nested_type_definitions'
def catalog = [
        (FLAG)                 : entryClass.newInstance(FLAG, 'none', '^.*$', ''),
        enable_custom_attributes: entryClass.newInstance('enable_custom_attributes', 'false', '^(true|false)$', ''),
        custom_attributes_list : entryClass.newInstance('custom_attributes_list', 'none', '^.*$', '')
]
def strictCatalog = [
        (FLAG): entryClass.newInstance(FLAG, 'none', '^none$', '')
]

def nestedType = { String name, Map<String, String> fields ->
    List<Map<String, Object>> list = []
    fields.each { String fieldName, String fieldType ->
        list.add([name: fieldName, type: fieldType])
    }
    return [name: name, fields: list]
}

def nested = [
        nestedType('Endereco', [logradouro: 'String', numero: 'Integer']),
        nestedType('Contato', [email: 'String'])
]

def fresh = { String attributeList, boolean enabled, List<Map<String, Object>> nestedTypes ->
    [values: [enable_custom_attributes: enabled ? 'true' : 'false',
              custom_attributes_list: attributeList,
              (FLAG): 'none'],
     sources: [:], notes: [:], diagnostics: [],
     derived: [nestedTypes: nestedTypes]]
}

// ---------------------------------------------------------------------------
// 1) The format is deterministic: "Tipo|Campo:Tipo,...;Tipo|..."
// ---------------------------------------------------------------------------
check("1.1 the derived types are serialized in order",
        projector.format(nested) ==
                "Endereco|logradouro:String,numero:Integer;Contato|email:String")

// ---------------------------------------------------------------------------
// 2) A nested type referenced by the entity list projects the flag
// ---------------------------------------------------------------------------
def s2 = fresh('String:nome,Endereco:endereco', true, nested)
projector.apply(s2.values, s2.sources, s2.notes, catalog, s2.derived, s2.diagnostics)
check("2.1 the nested definition is projected",
        s2.values.get(FLAG) == "Endereco|logradouro:String,numero:Integer;Contato|email:String")
check("2.2 ...with CONTRACT_DERIVED provenance", s2.sources.get(FLAG) == "CONTRACT_DERIVED")
check("2.3 ...and no note", !s2.notes.containsKey(FLAG))

// ---------------------------------------------------------------------------
// 3) Suppression rules
// ---------------------------------------------------------------------------
def s3 = fresh('String:nome', true, nested)
projector.apply(s3.values, s3.sources, s3.notes, catalog, s3.derived, s3.diagnostics)
check("3.1 a list without a nested type keeps the flag at 'none'",
        s3.values.get(FLAG) == "none" && s3.sources.get(FLAG) == "CONTRACT_DERIVED")

def s3b = fresh('Endereco:endereco', false, nested)
projector.apply(s3b.values, s3b.sources, s3b.notes, catalog, s3b.derived, s3b.diagnostics)
check("3.2 enable_custom_attributes=false suppresses the flag",
        s3b.values.get(FLAG) == "none")

def s3c = fresh('false', true, nested)
projector.apply(s3c.values, s3c.sources, s3c.notes, catalog, s3c.derived, s3c.diagnostics)
check("3.3 a 'false' list suppresses the flag", s3c.values.get(FLAG) == "none")

def s3d = fresh('   ', true, nested)
projector.apply(s3d.values, s3d.sources, s3d.notes, catalog, s3d.derived, s3d.diagnostics)
check("3.4 an empty list suppresses the flag", s3d.values.get(FLAG) == "none")

def s3e = fresh('Endereco:endereco', true, [])
projector.apply(s3e.values, s3e.sources, s3e.notes, catalog, s3e.derived, s3e.diagnostics)
check("3.5 no derived nested type leaves the flag untouched",
        s3e.values.get(FLAG) == "none" && !s3e.sources.containsKey(FLAG))

// ---------------------------------------------------------------------------
// 4) An explicit CONTRACT value always wins
// ---------------------------------------------------------------------------
def s4 = fresh('Endereco:endereco', true, nested)
s4.values.put(FLAG, 'Explicito|a:String')
s4.sources.put(FLAG, 'CONTRACT')
projector.apply(s4.values, s4.sources, s4.notes, catalog, s4.derived, s4.diagnostics)
check("4.1 an explicit CONTRACT value is preserved",
        s4.values.get(FLAG) == 'Explicito|a:String' && s4.sources.get(FLAG) == 'CONTRACT')

// ---------------------------------------------------------------------------
// 5) A derived value rejected by the catalog regex falls back to the default
// ---------------------------------------------------------------------------
def s5 = fresh('Endereco:endereco', true, nested)
projector.apply(s5.values, s5.sources, s5.notes, strictCatalog, s5.derived, s5.diagnostics)
check("5.1 an invalid derived value falls back to the default (FR-006)",
        s5.values.get(FLAG) == 'none' && s5.sources.get(FLAG) == 'DEFAULT')
check("5.2 ...the rejection is noted and diagnosed",
        s5.notes.get(FLAG)?.contains('FR-006') && !s5.diagnostics.isEmpty())

// ---------------------------------------------------------------------------
// 6) Only the types actually present in the entity list are projected
// ---------------------------------------------------------------------------
def s6 = fresh('Endereco:endereco', true, nested)
projector.apply(s6.values, s6.sources, s6.notes, catalog, s6.derived, s6.diagnostics)
check("6.1 the whole derived tree is serialized (the template imports the intersection)",
        s6.values.get(FLAG).contains('Endereco|') && s6.values.get(FLAG).contains('Contato|'))

// ---------------------------------------------------------------------------
// 7) A malformed list entry does not match any nested type
// ---------------------------------------------------------------------------
def s7 = fresh('Endereco', true, nested)
projector.apply(s7.values, s7.sources, s7.notes, catalog, s7.derived, s7.diagnostics)
check("7.1 an entry without ':' does not reference a nested type",
        s7.values.get(FLAG) == 'none')

// ---------------------------------------------------------------------------
// 8) Missing flag/catalog are no-ops; an explicit CONTRACT 'none' wins
// ---------------------------------------------------------------------------
def s8 = fresh('Endereco:endereco', true, nested)
s8.values.remove(FLAG)
projector.apply(s8.values, s8.sources, s8.notes, catalog, s8.derived, s8.diagnostics)
check("8.1 a values map without the flag is left untouched", !s8.values.containsKey(FLAG))

def s8b = fresh('Endereco:endereco', true, nested)
projector.apply(s8b.values, s8b.sources, s8b.notes,
        [enable_custom_attributes: catalog.get('enable_custom_attributes')],
        s8b.derived, s8b.diagnostics)
check("8.2 a catalog without the flag is a no-op",
        s8b.values.get(FLAG) == 'none' && !s8b.sources.containsKey(FLAG))

def s8c = fresh('Endereco:endereco', true, nested)
s8c.values.put(FLAG, 'none')
s8c.sources.put(FLAG, 'CONTRACT')
projector.apply(s8c.values, s8c.sources, s8c.notes, catalog, s8c.derived, s8c.diagnostics)
check("8.3 an explicit CONTRACT 'none' suppresses the derivation",
        s8c.values.get(FLAG) == 'none' && s8c.sources.get(FLAG) == 'CONTRACT')

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
