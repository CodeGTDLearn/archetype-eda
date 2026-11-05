def moduleRoot = new File("src/main/pre-processamento-maven-flags-via-llm")
def index = [:]
moduleRoot.eachFileRecurse { File f -> if (f.isFile() && f.name.endsWith('.groovy')) index[f.name] = f }
def gcl = new GroovyClassLoader(this.class.classLoader)
gcl.setResourceLoader({ String cn ->
    def src = index[cn.substring(cn.lastIndexOf('.') + 1) + '.groovy']
    return src == null ? null : src.toURI().toURL()
} as groovy.lang.GroovyResourceLoader)
def entryClass = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry')
def policy = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagValidationPolicy').newInstance()
def projector = gcl.loadClass('eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.DerivedFlagProjector').newInstance(policy)

int failures = 0
def check = { String n, boolean c -> if (c) { println "PASS: ${n}" } else { println "FAIL: ${n}"; failures++ } }

def catalog = [
        custom_attribute_imports: entryClass.newInstance('custom_attribute_imports', 'none', '^.*$', ''),
        custom_enum_definitions : entryClass.newInstance('custom_enum_definitions', 'none', '^.*$', '')
]
def fresh = { details ->
    [values: [custom_attribute_imports: 'none', custom_enum_definitions: 'none'],
     sources: [:], notes: [:], warnings: [], diagnostics: [],
     derived: [attributeDetails: details, warnings: []]]
}

def s1 = fresh([[projectType: true, qualifiedType: 'br.com.StatusPedido', type: 'StatusPedido', enumType: true, enumConstants: ['ATIVO']]])
projector.apply(s1.values, s1.sources, s1.notes, catalog, s1.derived, s1.diagnostics)
check("1.1 the qualified import is projected", s1.values.custom_attribute_imports == 'br.com.StatusPedido')
check("1.2 the enum definition is projected",
        s1.values.custom_enum_definitions == 'br.com.StatusPedido:ATIVO')
check("1.3 both flags carry CONTRACT_DERIVED provenance",
        s1.sources.custom_attribute_imports == 'CONTRACT_DERIVED' &&
                s1.sources.custom_enum_definitions == 'CONTRACT_DERIVED')

def s2 = fresh([])
projector.apply(s2.values, s2.sources, s2.notes, catalog, s2.derived, s2.diagnostics)
check("2.1 no attribute details leaves the flags unchanged",
        s2.values.custom_attribute_imports == 'none' && s2.sources.isEmpty())

def s3 = fresh([[projectType: true, qualifiedType: 'br.com.StatusPedido', type: 'StatusPedido', enumType: true, enumConstants: ['ATIVO']]])
s3.values.custom_enum_definitions = 'X:Y'
s3.sources.custom_enum_definitions = 'CONTRACT'
projector.apply(s3.values, s3.sources, s3.notes, catalog, s3.derived, s3.diagnostics)
check("3.1 an explicit CONTRACT value wins over the derived value", s3.values.custom_enum_definitions == 'X:Y')

def strictCatalog = [
        custom_attribute_imports: entryClass.newInstance('custom_attribute_imports', 'none', '^none$', ''),
        custom_enum_definitions : entryClass.newInstance('custom_enum_definitions', 'none', '^none$', '')
]
def s4 = fresh([[projectType: true, qualifiedType: 'br.com.StatusPedido', type: 'StatusPedido', enumType: false, enumConstants: []]])
projector.apply(s4.values, s4.sources, s4.notes, strictCatalog, s4.derived, s4.diagnostics)
check("4.1 an invalid derived value falls back to the default (FR-006)",
        s4.values.custom_attribute_imports == 'none' && s4.sources.custom_attribute_imports == 'DEFAULT')
check("4.2 the rejection is noted and diagnosed",
        s4.notes.custom_attribute_imports?.contains('FR-006') && !s4.diagnostics.isEmpty())

def s5 = fresh([
        [projectType: true, qualifiedType: 'br.com.a.StatusPedido', type: 'StatusPedido', enumType: false, enumConstants: []],
        [projectType: true, qualifiedType: 'br.com.b.StatusPedido', type: 'StatusPedido', enumType: false, enumConstants: []]
])
projector.apply(s5.values, s5.sources, s5.notes, catalog, s5.derived, s5.diagnostics)
check("5.1 two simple-name clashes produce a loud warning",
        s5.derived.warnings.any { it.contains('mesmo nome simples') })

def s6 = fresh([[projectType: false, qualifiedType: null, type: 'String', enumType: false, enumConstants: []]])
projector.apply(s6.values, s6.sources, s6.notes, catalog, s6.derived, s6.diagnostics)
check("6.1 non-project attributes leave both flags at 'none'",
        s6.values.custom_attribute_imports == 'none' && s6.values.custom_enum_definitions == 'none')

println ""
println failures == 0 ? "ALL CHECKS PASSED" : "${failures} CHECK(S) FAILED"
if (failures > 0) throw new RuntimeException("${failures} CHECK(S) FAILED")
