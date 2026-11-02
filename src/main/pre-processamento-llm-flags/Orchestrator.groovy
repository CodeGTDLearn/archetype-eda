package eda.postgenerate.llmflag

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.xml.XmlSlurper
import java.io.BufferedReader
import java.security.MessageDigest

/**
 * CLI entry point for the LLM flag inference mechanism. See spec.md FR-001..FR-019;
 * contracts/cli-contract.md; data-model.md.
 *
 * This script is a CLI tool: the operator invokes it directly (per
 * cli-contract.md) to obtain the mvn archetype:generate command line. It is
 * NOT wired into the archetype's own build lifecycle.
 *
 * Why a single-file implementation: gmaven:execute 2.1.1 bundles Groovy 2.4.9
 * internally; sibling .class files compiled with Groovy 4.0.28 are not on its
 * classpath, so static class references resolve to ClassNotFoundException at
 * compile time. The sibling classes (CatalogEntry, FlagCatalogExtractor, etc.)
 * still exist as unit-testable modules; the Orchestrator inlines the runtime
 * logic here to keep the build-time invocation self-contained.
 *
 * RESPONSIBILITY: this file is the CLI entry point (argument gates, contract
 * reading, cache, inference shell-out and command line assembly). The
 * DERIVATION of the entity from the contract and the TYPE INFERENCE are
 * dedicated components - ContractEntityDeriver.groovy and
 * ContractTypeResolver.groovy - mirrored INLINE at the bottom of this file
 * (OrchestratorTypeResolution, OrchestratorContractTypeResolver,
 * OrchestratorContractEntityDeriver), because a sibling source file is not
 * resolvable at compile time here. The canonical modules are the source of
 * truth; ContractDerivationTest.groovy asserts the parity between this mirror
 * and the canonical modules (through FlagProposalMerger).
 *
 * The mirrors carry the "Orchestrator" prefix on purpose: they live in the
 * same package as the canonical modules, and a GroovyClassLoader holds ONE
 * class per name - an identically named mirror would shadow the canonical
 * class in the unit tests.
 *
 * Exit codes (per cli-contract.md Section 4):
 *   0  success
 *   2  JSON contract missing (FR-003)
 *   3  External inference script failed
 *   4  Flag catalog malformed/unreadable (FR-018)
 *   5  Internal mechanism error
 *
 * Contract persistence (FR-021): the contract is materialized on disk and
 * its path is emitted in the command line (flag entity_contract_path). The
 * archetype's post-processor copies that file into the generated project
 * (src/test/resources/contracts/contract.json), so every generated module
 * carries the contract it was built from.
 */
class Orchestrator {

    static void main(String[] args) {
        new Orchestrator().run(args)
    }

    void run(String[] args) {
        Map<String, String> cliArgs
        try {
            cliArgs = parseArgs(args)
        } catch (Exception e) {
            System.err.println("ERROR: " + e.message)
            System.exit(5)
            return
        }

        String description = cliArgs.get("description")
        String contractJson = cliArgs.get("contract")
        String cacheDir = cliArgs.getOrDefault("cache-dir", defaultCacheDir())
        String inferenceBin = cliArgs.getOrDefault("inference-bin", System.getenv("EDA_LLM_INFERENCE_BIN"))
        boolean debug = Boolean.parseBoolean(System.getProperty("eda.llm.debug", "false"))

        // FR-001: use-case-description is mandatory and MUST be checked FIRST.
        // We do not proceed to read the contract or to inference until a non-blank
        // description is present, so the operator gets a clear, specific error.
        if (description == null || description.trim().isEmpty()) {
            System.err.println("ERROR: USE CASE DESCRIPTION is missing or blank.")
            System.err.println("Provide it via --use-case-description \"<text>\".")
            System.err.println("Processing stopped BEFORE reading the contract and BEFORE inference.")
            System.err.println("See spec.md FR-001.")
            System.exit(2)
            return
        }

        // FR-002/FR-003: contract is read from stdin ONLY when --use-case-contract
        // was passed. We read it here (after the description gate) so a missing
        // description never blocks on or consumes stdin input.
        if (cliArgs.containsKey("use-case-contract")) {
            try {
                StringBuilder sb = new StringBuilder()
                BufferedReader br = new BufferedReader(System.in.newReader())
                String line
                while ((line = br.readLine()) != null) {
                    if (sb.length() > 0) sb.append(System.lineSeparator())
                    sb.append(line)
                }
                contractJson = sb.toString()
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read contract from stdin: " + e.message)
                System.exit(5)
                return
            }
        }
        if (contractJson == null || contractJson.isEmpty()) {
            System.err.println("ERROR: USE CASE CONTRACT is missing or empty.")
            System.err.println("Provide it via --use-case-contract, passing the multi-line JSON through stdin.")
            System.err.println("Processing stopped BEFORE inference.")
            System.err.println("See spec.md FR-003.")
            System.exit(2)
            return
        }

        // Parse the contract
        Map<String, Object> contract
        try {
            Object parsed = new JsonSlurper().parseText(contractJson)
            if (!(parsed instanceof Map)) {
                System.err.println("ERROR: Contract root MUST be a JSON object.")
                System.exit(5)
                return
            }
            contract = (Map<String, Object>) parsed
        } catch (Exception e) {
            System.err.println("ERROR: Contract is not valid JSON: " + e.message)
            System.exit(5)
            return
        }

        // Read catalog (FR-005, FR-013, FR-018)
        Map<String, Map<String, String>> catalog
        try {
            java.io.File meta = new java.io.File("target/classes/META-INF/maven/archetype-metadata.xml")
            catalog = extractCatalog(meta)
            if (debug) {
                System.out.println("Catalog: " + catalog.size() + " flags from " + meta.absolutePath)
            }
        } catch (Exception e) {
            System.err.println("ERROR: Flag catalog is malformed or unreadable.")
            System.err.println("  " + e.message)
            System.exit(4)
            return
        }

        // Cache check (FR-011)
        // The cache key embeds a FINGERPRINT OF THE CATALOG: the produced command
        // line is a function of the catalog (defaults + validation rules), so a
        // catalog change MUST invalidate previously cached command lines. Without
        // the fingerprint, an entry written before a catalog change keeps being
        // served with stale -D flags.
        String canonicalContract = canonicalize(contract)
        String catalogFingerprint = fingerprintCatalog(catalog)
        String cacheKey = makeCacheKey(description, canonicalContract, catalogFingerprint)

        // FR-021 (contract persistence): the contract is materialized on disk
        // BEFORE the cache lookup, so the command line can carry its path on a
        // cache HIT as well (the archetype copies that file into the generated
        // project). The file name is derived from the canonical contract, so
        // the produced command line stays byte-equivalent across invocations
        // (FR-012).
        String contractDir = cliArgs.getOrDefault("contract-dir",
                new File(cacheDir, "contracts").absolutePath)
        String contractFilePath = materializeContract(contractDir, canonicalContract, contractJson)
        warnOnSecretLikeContractKeys(contract)

        // Values the mechanism derives from the contract itself (not literals
        // of the contract, not LLM proposals): today the path of the
        // materialized contract file.
        Map<String, String> derivedOverrides = new LinkedHashMap<>()
        if (contractFilePath != null) {
            derivedOverrides.put("entity_contract_path", contractFilePath)
        }

        Map<String, String> hit = cacheLookup(cacheDir, cacheKey)
        if (hit != null) {
            System.out.println(hit.get("v"))
            System.out.flush()
            // FR-020: provenance report goes to stderr (stdout stays contract-pure).
            System.err.println("## Relatorio de Proveniencia - cache: HIT")
            System.err.println("")
            String cachedReport = hit.get("r")
            if (cachedReport == null) {
                System.err.println("(relatorio indisponivel: entrada de cache legada, gravada antes da FR-020)")
            } else {
                System.err.println(cachedReport)
            }
            System.exit(0)
            return
        }

        // Run external inference (FR-004, FR-019)
        Map<String, String> llmInference
        try {
            llmInference = runInference(inferenceBin, description, contractJson)
        } catch (Exception e) {
            System.err.println("ERROR: External inference script failed: " + e.message)
            System.exit(3)
            return
        }

        // Merge (FR-006..FR-009, FR-020, FR-021); merge also returns per-flag provenance.
        Map<String, Object> merged = merge(llmInference, contract, catalog, derivedOverrides)
        Map<String, String> proposal = (Map<String, String>) merged.get("values")

        // Assemble (FR-010)
        String commandLine = assemble(proposal)

        // FR-020: provenance report body (no description/contract payload - FR-017).
        String reportBody = renderProvenanceReport(merged)

        // Cache write AFTER stdout flush, BEFORE exit (FR-011/012, T022 order rule)
        System.out.println(commandLine)
        System.out.flush()
        // FR-020: report goes to stderr (stdout stays contract-pure).
        System.err.println("## Relatorio de Proveniencia - cache: MISS")
        System.err.println("")
        System.err.println(reportBody)
        try {
            cacheWrite(cacheDir, cacheKey, commandLine, reportBody)
        } catch (Exception e) {
            System.err.println("WARN: cache write failed (non-fatal): " + e.message)
        }
        System.exit(0)
    }

    // ----------------------------------------------------------------------
    // Inlined CatalogEntry logic
    // ----------------------------------------------------------------------

    private Map<String, Map<String, String>> extractCatalog(java.io.File metadataFile) {
        if (!metadataFile.exists()) {
            throw new IllegalStateException("Flag catalog not found: ${metadataFile.absolutePath}")
        }
        def parsed = new XmlSlurper().parse(metadataFile)
        Map<String, Map<String, String>> catalog = new LinkedHashMap<>()
        parsed.requiredProperties.requiredProperty.each { node ->
            String key = node.@key.text()
            String defaultValue = node.defaultValue.text()
            String validationRegex = node.validationRegex.text()
            String description = node.description.text()

            // FR-018: empty defaultValue is a build-time catalog error.
            if (defaultValue == null || defaultValue.isEmpty()) {
                throw new IllegalStateException(
                    "Flag '${key}' has empty defaultValue. Catalog is invalid per FR-018. " +
                    "Every <requiredProperty> MUST have a non-empty <defaultValue>."
                )
            }
            // Validate regex compilation.
            if (validationRegex != null && !validationRegex.isEmpty()) {
                try { java.util.regex.Pattern.compile(validationRegex) }
                catch (Exception e) {
                    throw new IllegalStateException(
                        "Flag '${key}': validationRegex '${validationRegex}' does not compile: ${e.message}")
                }
            }

            Map<String, String> entry = new LinkedHashMap<>()
            entry.put("key", key)
            entry.put("defaultValue", defaultValue)
            entry.put("validationRegex", (validationRegex == null || validationRegex.isEmpty()) ? null : validationRegex)
            entry.put("description", description == null ? "" : description)
            catalog.put(key, entry)
        }
        return catalog
    }

    private boolean catalogMatches(Map<String, String> entry, String value) {
        String regex = entry.get("validationRegex")
        if (regex == null) return true
        return value ==~ regex
    }

    // ----------------------------------------------------------------------
    // Inlined FlagProposalMerger logic
    // ----------------------------------------------------------------------

    /**
     * FR-009 (amended): flags whose semantic can be derived from the contract
     * even when the contract does not carry the literal key. The gate used to
     * require contract.containsKey(flag), which silently discarded EVERY
     * proposal made from a domain contract (entityName / attributes[]), leaving
     * the catalog defaults in place ("0 CONTRACT / 0 INFERENCE / 32 DEFAULT").
     */
    private static final List<String> FR009_ALLOWLIST = ["entity_name"]

    /**
     * The component that derives the entity semantics from the contract. It
     * is the INLINE MIRROR of ContractEntityDeriver.groovy (see the header).
     */
    private OrchestratorContractEntityDeriver _deriver

    private OrchestratorContractEntityDeriver getDeriver() {
        if (_deriver == null) {
            _deriver = new OrchestratorContractEntityDeriver(new OrchestratorContractTypeResolver())
        }
        return _deriver
    }

    private Map<String, Object> merge(Map<String, String> llmInference,
                                      Map<String, Object> contract,
                                      Map<String, Map<String, String>> catalog) {
        return merge(llmInference, contract, catalog, new LinkedHashMap<String, String>())
    }

    private Map<String, Object> merge(Map<String, String> llmInference,
                                      Map<String, Object> contract,
                                      Map<String, Map<String, String>> catalog,
                                      Map<String, String> derivedOverrides) {
        if (catalog == null || catalog.isEmpty()) {
            throw new IllegalStateException("FlagProposalMerger: catalog is empty or null")
        }
        Map<String, String> values = new LinkedHashMap<>()
        Map<String, String> sources = new LinkedHashMap<>()
        Map<String, String> notes = new LinkedHashMap<>()

        // FR-021: the entity semantics (name, package, attributes) are derived
        // from the contract BEFORE the merge loop, because the package depends
        // on the resolved entity name (and must not depend on the catalog
        // iteration order).
        Map<String, Object> derived = getDeriver().deriveFromContract(contract, llmInference, catalog)

        for (Map.Entry<String, Map<String, String>> e : catalog.entrySet()) {
            String key = e.key
            Map<String, String> catEntry = e.value
            String source = null

            // Step 1: contract value (highest precedence, FR-007)
            String value = pickFromContract(key, contract)
            if (value != null) source = "CONTRACT"

            // Step 2: value derived from the contract by the mechanism itself
            // (FR-021: the path of the materialized contract file).
            if (value == null && derivedOverrides != null && derivedOverrides.containsKey(key)) {
                value = derivedOverrides.get(key)
                if (value != null) source = "CONTRACT_DERIVED"
            }

            // Step 3: entity semantics derived from the contract
            if (value == null) {
                switch (key) {
                    case "entity_name":
                        if (derived.get("entityName") != null) {
                            value = (String) derived.get("entityName")
                            source = (String) derived.get("entityNameSource")
                        }
                        break
                    case "entity_name_package":
                        if (derived.get("entityName") != null) {
                            String pkg = ((String) derived.get("entityName")).toLowerCase(Locale.ROOT)
                            // The catalog regex is ^[a-z][a-z0-9_.]+$ - a
                            // one-letter entity name would not match; in that
                            // case the default applies (FR-008).
                            if (pkg ==~ /^[a-z][a-z0-9_.]+$/) {
                                value = pkg
                                source = "CONTRACT_DERIVED"
                            }
                        }
                        break
                    case "enable_custom_attributes":
                        // The attributes come from the contract, so the feature
                        // is mandatory (the template renders the fields only in
                        // this branch).
                        if (!(derived.get("attributes") as List).isEmpty()) {
                            value = "true"
                            source = "CONTRACT_DERIVED"
                        }
                        break
                    case "custom_attributes_list":
                        if (!(derived.get("attributes") as List).isEmpty()) {
                            value = (String) derived.get("attributeList")
                            source = "CONTRACT_DERIVED"
                        }
                        break
                }
            }

            // Step 4: LLM-inferred (FR-009). A proposal is eligible when the
            // contract carries the literal key OR the key is on the allowlist.
            if (value == null && isInferenceEligible(key, contract)) {
                value = llmInference == null ? null : llmInference.get(key)
                if (value != null) source = "INFERENCE"
            }

            // Step 5: catalog default (FR-008)
            if (value == null) {
                value = catEntry.get("defaultValue")
                source = "DEFAULT"
            }

            // FR-006: replace with default if value fails validationRegex
            if (value != null && !catalogMatches(catEntry, value)) {
                System.err.println(
                    "ERROR: Flag '" + key + "' value '" + value +
                    "' fails validation regex '" + catEntry.get("validationRegex") +
                    "'. Default value '" + catEntry.get("defaultValue") + "' will be used instead."
                )
                notes.put(key, "valor '" + value + "' rejeitado pela regex; default aplicado (FR-006)")
                value = catEntry.get("defaultValue")
                source = "DEFAULT"
            }

            values.put(key, value)
            sources.put(key, source)
        }

        // The entity semantics may carry PROJECT types (types outside
        // java.lang). They are referenced by their simple name in the entity,
        // so the metadata the archetype needs travels in two derived flags:
        // the imports of the referenced types and the definitions of the
        // enums it must generate.
        applyDerivedTypeFlags(values, sources, notes, catalog, derived)

        // FR-009/FR-020: LLM proposals whose key is absent from the contract
        // are discarded - unless the key is on the allowlist AND the proposal
        // was accepted (an accepted proposal must not be listed as discarded).
        Map<String, String> discarded = new LinkedHashMap<>()
        if (llmInference != null) {
            for (Map.Entry<String, String> e2 : llmInference.entrySet()) {
                if (contract == null || !contract.containsKey(e2.key)) {
                    if (FR009_ALLOWLIST.contains(e2.key) && "INFERENCE".equals(sources.get(e2.key))) {
                        continue
                    }
                    discarded.put(e2.key, e2.value)
                }
            }
        }

        // FR-021: loud warnings about the derivation (a rejected type, a
        // dropped attribute). They are printed to stderr and kept OUT of the
        // provenance report body, which must not carry contract fields absent
        // from the catalog (FR-017).
        List<String> warnings = (List<String>) derived.get("warnings")
        if (warnings != null) {
            for (String w : warnings) {
                System.err.println("WARNING: " + w)
            }
        }

        Map<String, Object> out = new LinkedHashMap<>()
        out.put("values", values)
        out.put("sources", sources)
        out.put("notes", notes)
        out.put("discarded", discarded)
        return out
    }

    /**
     * Derives the flags that carry the metadata of the PROJECT types:
     *   - custom_attribute_imports: the FQNs of the referenced types, so the
     *     entity template can import them;
     *   - custom_enum_definitions: the enums the archetype must generate
     *     ("Type:CONSTANT|CONSTANT", separated by ';').
     */
    private void applyDerivedTypeFlags(Map<String, String> values, Map<String, String> sources,
                                       Map<String, String> notes, Map<String, Map<String, String>> catalog,
                                       Map<String, Object> derived) {
        List<Map<String, Object>> details = (List<Map<String, Object>>) derived.get("attributeDetails")
        if (details == null || details.isEmpty()) return

        List<String> imports = new ArrayList<String>()
        Map<String, List<String>> enums = new LinkedHashMap<String, List<String>>()
        Map<String, String> simpleNames = new LinkedHashMap<String, String>()
        List<String> warnings = (List<String>) derived.get("warnings")
        for (Map<String, Object> detail : details) {
            if (!(Boolean) detail.get("projectType")) continue
            String qualified = (String) detail.get("qualifiedType")
            String type = (String) detail.get("type")
            if (qualified != null && qualified.contains(".") && !imports.contains(qualified)) {
                imports.add(qualified)
                // Two distinct types with the same simple name cannot both be
                // imported: the entity would not compile. The clash is loud.
                String simple = qualified.substring(qualified.lastIndexOf(".") + 1)
                String known = simpleNames.get(simple)
                if (known == null) {
                    simpleNames.put(simple, qualified)
                } else if (known != qualified && warnings != null) {
                    warnings.add("tipos com o mesmo nome simples e pacotes diferentes: '" +
                            known + "' e '" + qualified + "' - a entidade pode nao compilar")
                }
            }
            if ((Boolean) detail.get("enumType")) {
                String key = (qualified != null && qualified.contains(".")) ? qualified : type
                if (key == null) continue
                List<String> constants = enums.get(key)
                if (constants == null) {
                    constants = new ArrayList<String>()
                    enums.put(key, constants)
                }
                for (Object constant : (List<Object>) detail.get("enumConstants")) {
                    if (!constants.contains(constant)) constants.add((String) constant)
                }
            }
        }

        List<String> enumDefinitions = new ArrayList<String>()
        for (Map.Entry<String, List<String>> entry : enums.entrySet()) {
            enumDefinitions.add(entry.getKey() + ":" + entry.getValue().join("|"))
        }
        applyDerivedFlag(values, sources, notes, catalog, "custom_attribute_imports",
                imports.isEmpty() ? "none" : imports.join(","))
        applyDerivedFlag(values, sources, notes, catalog, "custom_enum_definitions",
                enumDefinitions.isEmpty() ? "none" : enumDefinitions.join(";"))
    }

    /**
     * Applies a derived flag value: the operator's explicit value (CONTRACT)
     * always wins; a value that violates the catalog rule falls back to the
     * catalog default (FR-006); otherwise the derived value is emitted with
     * the CONTRACT_DERIVED provenance.
     */
    private void applyDerivedFlag(Map<String, String> values, Map<String, String> sources,
                                  Map<String, String> notes, Map<String, Map<String, String>> catalog,
                                  String key, String derivedValue) {
        if (values == null || !values.containsKey(key)) return
        if ("CONTRACT".equals(sources.get(key))) return
        Map<String, String> entry = catalog.get(key)
        if (entry == null) return
        String regex = entry.get("validationRegex")
        if (regex != null && !regex.isEmpty() && !(derivedValue ==~ regex)) {
            System.err.println(
                    "ERROR: Flag '" + key + "' value '" + derivedValue +
                            "' fails validation regex '" + regex +
                            "'. Default value '" + entry.get("defaultValue") + "' will be used instead."
            )
            notes.put(key, "valor derivado '" + derivedValue + "' rejeitado pela regex; default aplicado (FR-006)")
            values.put(key, entry.get("defaultValue"))
            sources.put(key, "DEFAULT")
            return
        }
        values.put(key, derivedValue)
        sources.put(key, "CONTRACT_DERIVED")
    }

    /**
     * FR-009 (amended): a flag proposed by the LLM is eligible when the
     * contract carries the literal key, or when the flag is on the allowlist
     * (its semantic is derivable from the contract).
     */
    private boolean isInferenceEligible(String key, Map<String, Object> contract) {
        if (FR009_ALLOWLIST.contains(key)) return true
        return contract != null && contract.containsKey(key)
    }

    private String pickFromContract(String key, Map<String, Object> contract) {
        if (contract == null) return null
        Object raw = contract.get(key)
        if (raw == null) return null
        if (raw instanceof String) return (String) raw
        if (raw instanceof Number || raw instanceof Boolean) return raw.toString()
        try { return JsonOutput.toJson(raw) } catch (Exception e) { return raw.toString() }
    }

    // ----------------------------------------------------------------------
    // FR-020 provenance report rendering
    // ----------------------------------------------------------------------

    private String renderProvenanceReport(Map<String, Object> merged) {
        Map<String, String> values = (Map<String, String>) merged.get("values")
        Map<String, String> sources = (Map<String, String>) merged.get("sources")
        Map<String, String> notes = (Map<String, String>) merged.get("notes")
        Map<String, String> discarded = (Map<String, String>) merged.get("discarded")

        String nl = System.lineSeparator()
        StringBuilder sb = new StringBuilder()
        sb.append("| flag | valor final | fonte | notas |").append(nl)
        sb.append("|---|---|---|---|").append(nl)
        int cContract = 0
        int cDerived = 0
        int cInference = 0
        int cDefault = 0
        for (Map.Entry<String, String> e : values.entrySet()) {
            String src = sources.get(e.key)
            if (src == "CONTRACT") cContract++
            else if (src == "CONTRACT_DERIVED") cDerived++
            else if (src == "INFERENCE") cInference++
            else cDefault++
            String note = notes.containsKey(e.key) ? notes.get(e.key) : ""
            sb.append("| ").append(mdCell(e.key))
              .append(" | ").append(mdCell(e.value))
              .append(" | ").append(src)
              .append(" | ").append(mdCell(note))
              .append(" |").append(nl)
        }
        if (discarded != null && !discarded.isEmpty()) {
            sb.append(nl)
            sb.append("### Propostas do LLM descartadas (ausentes do contrato - FR-009)").append(nl)
            sb.append(nl)
            sb.append("| flag | valor proposto |").append(nl)
            sb.append("|---|---|").append(nl)
            for (Map.Entry<String, String> e : discarded.entrySet()) {
                sb.append("| ").append(mdCell(e.key))
                  .append(" | ").append(mdCell(e.value))
                  .append(" |").append(nl)
            }
        }
        sb.append(nl)
        sb.append("**Resumo**: ").append(cContract).append(" CONTRACT / ")
          .append(cDerived).append(" CONTRACT_DERIVED / ")
          .append(cInference).append(" INFERENCE / ")
          .append(cDefault).append(" DEFAULT / ")
          .append(discarded == null ? 0 : discarded.size()).append(" descartada(s)")
        return sb.toString()
    }

    private String mdCell(String s) {
        if (s == null) return ""
        return s.replace("|", "\\|")
    }

    // ----------------------------------------------------------------------
    // Inlined CommandLineAssembler logic
    // ----------------------------------------------------------------------

    private static final String[] META_CHARS = [
        ' ', '\t', '$', '`', '\\', '"', '&', '|', '<', '>', ';',
        '(', ')', '*', '?', '[', ']', '{', '}'
    ] as String[]

    private String assemble(Map<String, String> validatedProposal) {
        if (validatedProposal == null) {
            throw new IllegalArgumentException("CommandLineAssembler: validatedProposal is null")
        }
        StringBuilder sb = new StringBuilder("mvn archetype:generate --batch-mode")
        for (Map.Entry<String, String> entry : validatedProposal.entrySet()) {
            sb.append(' ')
            sb.append("-D").append(entry.key).append('=')
            sb.append(escapeAssembledValue(entry.value))
        }
        return sb.toString()
    }

    private String escapeAssembledValue(String value) {
        if (value == null || value.isEmpty()) return ""
        boolean needsQuotes = false
        for (String m : META_CHARS) {
            if (value.contains(m)) { needsQuotes = true; break }
        }
        if (!needsQuotes) return value
        StringBuilder sb = new StringBuilder("\"")
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i)
            if (c == '"') sb.append("\\\"")
            else if (c == '\\') sb.append("\\\\")
            else sb.append(c)
        }
        sb.append("\"")
        return sb.toString()
    }

    // ----------------------------------------------------------------------
    // Inlined JsonCanonicalizer logic
    // ----------------------------------------------------------------------

    private String canonicalize(Object value) {
        return canonicalizeValue(value)
    }

    private String canonicalizeValue(Object value) {
        if (value == null) return "null"
        if (value instanceof Map) return canonicalizeMap((Map) value)
        if (value instanceof List) return canonicalizeList((List) value)
        if (value instanceof String) return canonicalizeString((String) value)
        if (value instanceof Number || value instanceof Boolean) return value.toString()
        return canonicalizeString(value.toString())
    }

    private String canonicalizeMap(Map<?, ?> map) {
        List<String> keys = new ArrayList<>(map.keySet())
        Collections.sort(keys)
        StringBuilder sb = new StringBuilder("{")
        boolean first = true
        for (String key : keys) {
            if (!first) sb.append(",")
            sb.append(canonicalizeString(key))
            sb.append(":")
            sb.append(canonicalizeValue(map.get(key)))
            first = false
        }
        sb.append("}")
        return sb.toString()
    }

    private String canonicalizeList(List<?> list) {
        StringBuilder sb = new StringBuilder("[")
        boolean first = true
        for (Object item : list) {
            if (!first) sb.append(",")
            sb.append(canonicalizeValue(item))
            first = false
        }
        sb.append("]")
        return sb.toString()
    }

    private String canonicalizeString(String s) {
        StringBuilder sb = new StringBuilder("\"")
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i)
            switch (c) {
                case '"':  sb.append("\\\""); break
                case '\\': sb.append("\\\\"); break
                case '\b': sb.append("\\b"); break
                case '\f': sb.append("\\f"); break
                case '\n': sb.append("\\n"); break
                case '\r': sb.append("\\r"); break
                case '\t': sb.append("\\t"); break
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c))
                    else sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    /**
     * Stable fingerprint of the flag catalog: SHA-256 over every flag's key,
     * default value and validation rule, in catalog order.
     *
     * Only the fields that can change the produced command line take part in
     * the digest (the description is excluded). Changing the catalog therefore
     * changes the fingerprint, which changes the cache key, which makes every
     * previously cached command line unreachable - no cache migration needed.
     */
    private String fingerprintCatalog(Map<String, Map<String, String>> catalog) {
        if (catalog == null || catalog.isEmpty()) return ""
        StringBuilder sb = new StringBuilder()
        for (Map.Entry<String, Map<String, String>> e : catalog.entrySet()) {
            Map<String, String> entry = e.getValue()
            sb.append(e.getKey()).append('\u0001')
              .append(entry == null ? null : entry.get("defaultValue")).append('\u0001')
              .append(entry == null ? null : entry.get("validationRegex")).append('\u0002')
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        byte[] hash = digest.digest(sb.toString().getBytes("UTF-8"))
        return hash.collect { String.format("%02x", it) }.join()
    }

    private String makeCacheKey(String description, String canonicalContract, String catalogFingerprint) {
        String payload = (catalogFingerprint == null ? "" : catalogFingerprint) + "\u0000" +
                         (description == null ? "" : description) + "\u0000" +
                         (canonicalContract == null ? "" : canonicalContract)
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        byte[] hash = digest.digest(payload.getBytes("UTF-8"))
        return hash.collect { String.format("%02x", it) }.join()
    }

    // ----------------------------------------------------------------------
    // Inlined InvocationCache logic
    // ----------------------------------------------------------------------

    private String defaultCacheDir() {
        String xdg = System.getenv("XDG_CACHE_HOME")
        if (xdg != null && !xdg.isEmpty()) {
            return new File(xdg, "eda-archetype/llm-flag-inference").absolutePath
        }
        String localAppData = System.getenv("LOCALAPPDATA")
        if (localAppData != null && !localAppData.isEmpty()) {
            return new File(localAppData, "eda-archetype/llm-flag-inference").absolutePath
        }
        String userHome = System.getProperty("user.home")
        return new File(userHome, ".cache/eda-archetype/llm-flag-inference").absolutePath
    }

    private java.io.File cacheFile(String cacheDir) {
        return new File(cacheDir, "cache.jsonl")
    }

    /**
     * Reads the cache file line by line.
     *
     * The on-disk format is JSON (cli-contract.md Section 3), so each line is
     * parsed with JsonSlurper. The previous regex-based parser
     * (^\{"k":...,"r":"((?:[^"\\]|\\.)*)"...\}$) compiled the alternation into
     * a RECURSIVE Pattern$Loop - one stack frame per character - and overflowed
     * the stack on a realistic provenance report (~1965 characters), turning
     * every cache HIT into exit code 1.
     *
     * v1 entries (no "r" field) are still readable: they yield r == null and
     * the caller prints the legacy-entry note. A corrupted line is skipped -
     * one poisoned line must not invalidate the whole cache.
     */
    private Map<String, String> cacheLookup(String cacheDir, String key) {
        if (key == null) return null
        java.io.File f = cacheFile(cacheDir)
        if (!f.exists()) return null
        Map<String, String> found = null
        f.eachLine("UTF-8") { String line ->
            if (line == null || line.trim().isEmpty()) return
            Map parsed = null
            try {
                Object obj = new JsonSlurper().parseText(line)
                if (obj instanceof Map) parsed = (Map) obj
            } catch (Exception e) {
                parsed = null
            }
            if (parsed == null) return
            String lineKey = parsed.get("k") == null ? null : parsed.get("k").toString()
            if (lineKey == null || lineKey != key) return
            Map<String, String> hit = new LinkedHashMap<>()
            hit.put("v", parsed.get("v") == null ? null : parsed.get("v").toString())
            Object report = parsed.get("r")
            hit.put("r", report == null ? null : report.toString())
            found = hit
        }
        return found
    }

    private void cacheWrite(String cacheDir, String key, String commandLine, String report) {
        if (key == null || commandLine == null || report == null) {
            throw new IllegalArgumentException("InvocationCache.write: key, commandLine and report MUST be non-null")
        }
        java.io.File dir = new File(cacheDir)
        if (!dir.exists()) dir.mkdirs()
        java.io.File target = cacheFile(cacheDir)
        java.io.File tmp = new File(target.absolutePath + ".tmp." + System.nanoTime())
        long now = System.currentTimeMillis() / 1000L
        // NOTE: escapeJsonString already returns a quoted JSON string; do NOT add
        // extra quotes here (the pre-FR-020 version did, which made cache lines
        // malformed and lookups never match - round-trip was broken).
        tmp.withWriter("UTF-8") { writer ->
            writer.write("{\"k\":\"" + key + "\",\"v\":" + escapeJsonString(commandLine) + ",\"r\":" + escapeJsonString(report) + ",\"t\":" + now + "}")
            writer.write(System.lineSeparator())
        }
        if (!tmp.renameTo(target)) {
            tmp.withReader("UTF-8") { reader ->
                target.append(reader.text, "UTF-8")
            }
            tmp.delete()
        }
    }

    private String escapeJsonString(String s) {
        StringBuilder sb = new StringBuilder("\"")
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i)
            switch (c) {
                case '"':  sb.append("\\\""); break
                case '\\': sb.append("\\\\"); break
                case '\b': sb.append("\\b"); break
                case '\f': sb.append("\\f"); break
                case '\n': sb.append("\\n"); break
                case '\r': sb.append("\\r"); break
                case '\t': sb.append("\\t"); break
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c))
                    else sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    private String unescapeJsonString(String s) {
        // Kept for backwards compatibility of the module API. The cache
        // reader no longer needs it: the on-disk format is JSON and is parsed
        // with JsonSlurper, which unescapes natively.
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i)
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1)
                switch (next) {
                    case '"':  sb.append('"');  i++; break
                    case '\\': sb.append('\\'); i++; break
                    case 'b':  sb.append('\b'); i++; break
                    case 'f':  sb.append('\f'); i++; break
                    case 'n':  sb.append('\n'); i++; break
                    case 'r':  sb.append('\r'); i++; break
                    case 't':  sb.append('\t'); i++; break
                    case 'u':
                        if (i + 5 < s.length()) {
                            String hex = s.substring(i + 2, i + 6)
                            sb.append((char) Integer.parseInt(hex, 16))
                            i += 5
                        }
                        break
                    default:
                        sb.append(next)
                        i++
                }
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    // ----------------------------------------------------------------------
    // FR-021: contract materialization (contract persistence channel)
    // ----------------------------------------------------------------------

    /**
     * Writes the contract to disk so the archetype can persist it inside the
     * generated project (src/test/resources/contracts/contract.json).
     *
     * The file name is SHA-256 of the canonical contract: deterministic, so
     * two invocations with the same contract produce the same path and
     * therefore the same command line (FR-012). The content is the contract
     * exactly as the operator delivered it.
     *
     * @return the absolute path of the materialized file, or null on failure.
     */
    private String materializeContract(String contractDir, String canonicalContract, String contractJson) {
        try {
            File dir = new File(contractDir)
            if (!dir.exists()) dir.mkdirs()
            String name = sha256Hex(canonicalContract) + ".json"
            File target = new File(dir, name)
            String content = contractJson == null ? "" : contractJson.trim()
            if (!target.exists() || target.getText("UTF-8") != content) {
                target.setText(content, "UTF-8")
            }
            // The path travels on the command line, where a backslash would be
            // escaped (and PowerShell does not unescape it). Forward slashes
            // are understood by java.nio on every platform and by every shell.
            return target.absolutePath.replace('\\', '/')
        } catch (Exception e) {
            System.err.println("WARN: the contract could not be materialized: " + e.message)
            return null
        }
    }

    private String sha256Hex(String payload) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        byte[] hash = digest.digest((payload == null ? "" : payload).getBytes("UTF-8"))
        return hash.collect { String.format("%02x", it) }.join()
    }

    /**
     * Principle V (credentials never enter the repository): the contract is
     * persisted inside the generated project, which IS versioned. A contract
     * carrying secret-looking keys must produce a loud warning.
     *
     * The warning goes to stderr only - the provenance report body must not
     * carry contract fields absent from the catalog (FR-017).
     */
    private void warnOnSecretLikeContractKeys(Object node) {
        List<String> suspicious = new ArrayList<>()
        collectSecretLikeKeys(node, "", suspicious)
        if (suspicious.isEmpty()) return
        System.err.println(
            "WARNING: the contract contains keys that look like secrets: " + suspicious.join(", "))
        System.err.println(
            "         The contract is persisted in the generated project " +
            "(src/test/resources/contracts/contract.json) and WILL be versioned.")
        System.err.println(
            "         Do not put credentials in the contract (Principle V).")
    }

    private void collectSecretLikeKeys(Object node, String path, List<String> out) {
        if (node instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) node).entrySet()) {
                String key = e.key?.toString() ?: ""
                String childPath = path.isEmpty() ? key : (path + "." + key)
                if (key.toLowerCase(Locale.ROOT) ==~ /.*(password|senha|secret|token|credential|api[_-]?key|private[_-]?key).*/) {
                    if (!out.contains(childPath)) out.add(childPath)
                }
                collectSecretLikeKeys(e.value, childPath, out)
            }
        } else if (node instanceof List) {
            List<?> list = (List<?>) node
            for (int i = 0; i < list.size(); i++) {
                collectSecretLikeKeys(list.get(i), path + "[" + i + "]", out)
            }
        }
    }

    // ----------------------------------------------------------------------
    // Argument parsing
    // ----------------------------------------------------------------------

    private Map<String, String> parseArgs(String[] args) {
        Map<String, String> out = new LinkedHashMap<>()
        for (int i = 0; i < args.length; i++) {
            String a = args[i]
            switch (a) {
                case "--use-case-description":
                    // If --use-case-description is the last argument, OR the next
                    // token starts with "--" (i.e. it is another flag, not a value),
                    // do NOT advance i. The caller (run) will detect the missing
                    // value via the description gate and emit a clear FR-001 error
                    // with exit code 2. This prevents `--use-case-description
                    // --use-case-contract` from swallowing the contract flag as a
                    // description value.
                    if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        out.put("description", args[++i])
                    }
                    break
                case "--use-case-contract":          out.put("use-case-contract", "true"); break
                case "--cache-dir":                  out.put("cache-dir", args[++i]); break
                case "--contract-dir":               out.put("contract-dir", args[++i]); break
                case "--inference-bin":              out.put("inference-bin", args[++i]); break
                default:
                    if (a.startsWith("--")) {
                        throw new IllegalArgumentException("Unknown argument: " + a)
                    }
                    break
            }
        }
        // NOTE: stdin is intentionally NOT read here. The caller (run) reads
        // stdin ONLY after the description gate passes (see FR-001), so a
        // missing description never blocks on or consumes stdin input.
        return out
    }

    // ----------------------------------------------------------------------
    // External inference shell-out
    // ----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, String> runInference(String inferenceBin, String description, String contractJson) {
        if (inferenceBin == null || inferenceBin.isEmpty()) {
            inferenceBin = "scripts/inference-stub.sh"
        }
        java.io.File tmpDesc = java.io.File.createTempFile("desc-", ".txt")
        java.io.File tmpContract = java.io.File.createTempFile("contract-", ".json")
        tmpDesc.text = description == null ? "" : description
        tmpContract.text = contractJson
        try {
            ProcessBuilder pb = new ProcessBuilder(
                inferenceBin,
                "--use-case-description-file", tmpDesc.absolutePath,
                "--use-case-contract-file", tmpContract.absolutePath
            )
            pb.redirectErrorStream(false)
            Process p = pb.start()
            String stdout = p.inputStream.text
            int code = p.waitFor()
            if (code != 0) {
                throw new RuntimeException("inference script exited with code " + code)
            }
            Object parsed = new JsonSlurper().parseText(stdout)
            if (!(parsed instanceof Map)) {
                throw new RuntimeException("inference script stdout is not a JSON object")
            }
            return (Map<String, String>) parsed
        } finally {
            tmpDesc.delete()
            tmpContract.delete()
        }
    }
}

// ----------------------------------------------------------------------
// Inlined runtime MIRRORS of the canonical modules.
//
// The canonical sources of truth are:
//   - ContractTypeResolver.groovy  (type inference)
//   - ContractEntityDeriver.groovy (contract -> entity derivation)
//
// They are mirrored here because a sibling source file is NOT resolvable at
// compile time in the environments this script runs in (gmaven:execute 2.1.1
// bundles Groovy 2.4.9 and groovy.ui.GroovyMain compiles a single file).
// Keep the bodies in sync with the canonical modules: the behaviour is
// asserted by ContractDerivationTest.groovy, which compares the outputs of
// this mirror (Orchestrator) against the canonical modules
// (FlagProposalMerger).
//
// The mirrors carry the "Orchestrator" prefix on purpose: they live in the
// same package as the canonical modules, and a GroovyClassLoader holds ONE
// class per name - an identically named mirror would shadow the canonical
// class in the unit tests.
// ----------------------------------------------------------------------

/**
 * The outcome of resolving the type of one attribute: the Java type that will
 * be rendered in the entity, plus an optional warning (set when a declared
 * type had to be rejected).
 *
 * The extra fields carry the metadata the archetype needs for a PROJECT type:
 *   - qualifiedType: the type as declared (with its package, when declared)
 *   - projectType:   the type is a domain type (outside java.lang)
 *   - enumType:      the type is an enum that the archetype must generate
 *   - enumConstants: the constants of that enum
 */
class OrchestratorTypeResolution {
    String type
    String warning
    String qualifiedType
    boolean projectType
    boolean enumType
    List<String> enumConstants
}

/**
 * Resolves the Java type of a single entity attribute and sanitizes attribute
 * names. SINGLE RESPONSIBILITY: type inference.
 *
 * Rules:
 *   1. A DECLARED java.lang type wins (FQN -> simple name, generics dropped).
 *   2. A declared PROJECT type (a type outside java.lang - a domain class or
 *      an enum) is KEPT: the entity references it by its simple name, the
 *      archetype imports it (flag custom_attribute_imports) and/or generates
 *      the enum source (flag custom_enum_definitions). The attribute NAME
 *      then comes from the type itself (EnderecoCliente -> enderecoCliente).
 *   3. Any OTHER declared type (a JDK type outside java.lang, a generic
 *      type) produces a LOUD WARNING and falls back to the type estimated
 *      from the VALUE.
 *   4. The estimate maps the runtime class of the value to a java.lang type;
 *      nested objects, arrays and null all fall back to String.
 */
class OrchestratorContractTypeResolver {

    /**
     * Types the entity template can render without emitting an extra import
     * (the template writes "private ${attrib.type} ${attrib.name};" verbatim).
     */
    private static final List<String> JAVA_LANG_TYPES = [
            "String", "Integer", "Long", "Double", "Float", "Short", "Byte",
            "Boolean", "Character", "Number", "Object", "StringBuilder", "StringBuffer"
    ]

    /**
     * Well-known JDK type names declared WITHOUT a package (e.g. "List").
     * A package-less name cannot be told apart from a project type by its
     * spelling, so the usual JDK names keep the legacy behaviour (rejected
     * + estimate from the value) instead of being treated as project types.
     */
    private static final List<String> STANDARD_JDK_TYPE_NAMES = [
            "List", "ArrayList", "LinkedList", "Map", "HashMap", "LinkedHashMap",
            "TreeMap", "Set", "HashSet", "LinkedHashSet", "TreeSet", "Collection",
            "Iterable", "Date", "Calendar", "Timestamp", "Instant", "LocalDate",
            "LocalDateTime", "LocalTime", "OffsetDateTime", "ZonedDateTime",
            "Duration", "Period", "UUID", "BigDecimal", "BigInteger", "Optional",
            "Stream", "Thread"
    ]

    /**
     * Resolves the Java type of ONE attribute.
     */
    OrchestratorTypeResolution resolve(String name, Object declared, Object value) {
        return resolve(name, declared, value, null)
    }

    /**
     * Resolves the Java type of ONE attribute.
     *
     * @param enumValues the explicit enum constants declared in the contract
     *        (may be null): a List of constants or a single constant
     */
    OrchestratorTypeResolution resolve(String name, Object declared, Object value, Object enumValues) {
        String type = null
        String warning = null
        String qualifiedType = null
        boolean projectType = false
        boolean enumType = false
        List<String> enumConstants = new ArrayList<String>()
        if (declared != null) {
            String raw = declared.toString().trim()
            if (!raw.isEmpty()) qualifiedType = raw
            String normalized = normalizeType(raw)
            if (normalized != null) {
                type = normalized
            } else if (isProjectType(raw)) {
                // A PROJECT type (outside java.lang) is a valid domain type:
                // it is kept, referenced by its simple name and carried over
                // to the archetype (import + enum generation).
                type = simpleTypeName(raw)
                projectType = true
                List<String> constants = enumConstantsFor(value, enumValues)
                if (!constants.isEmpty()) {
                    enumType = true
                    enumConstants = constants
                }
            } else {
                warning = "atributo '" + name + "': tipo declarado '" + declared +
                        "' nao e um tipo de java.lang - tipo estimado pelo valor"
            }
        }
        if (type == null) type = estimateType(value)
        OrchestratorTypeResolution out = new OrchestratorTypeResolution()
        out.type = type
        out.warning = warning
        out.qualifiedType = qualifiedType
        out.projectType = projectType
        out.enumType = enumType
        out.enumConstants = enumConstants
        return out
    }

    /**
     * Tells a PROJECT type (a domain class or an enum) from the types that
     * keep the legacy behaviour (java.lang types, JDK types outside
     * java.lang and generic types).
     */
    boolean isProjectType(String raw) {
        if (raw == null) return false
        String t = raw.trim()
        if (t.isEmpty()) return false
        if (t.contains("<") || t.contains(">") || t.contains("[")) return false
        String simple = simpleTypeName(t)
        if (simple == null) return false
        // A Java type is a class-like name.
        if (!(simple ==~ /^[A-Z][A-Za-z0-9_]*$/)) return false
        if (JAVA_LANG_TYPES.contains(simple)) return false
        if (t.contains(".")) {
            String pkg = t.substring(0, t.lastIndexOf(".")).trim()
            if (pkg.isEmpty()) return false
            String p = pkg.toLowerCase(Locale.ROOT)
            // A JDK type outside java.lang keeps the legacy behaviour.
            if (p.startsWith("java.") || p.startsWith("javax.") || p.startsWith("jakarta.")) return false
            return true
        }
        return !STANDARD_JDK_TYPE_NAMES.contains(simple)
    }

    /**
     * The simple name of a declared type: "br.com.projeto.StatusPedido" ->
     * "StatusPedido".
     */
    String simpleTypeName(String raw) {
        if (raw == null) return null
        String t = raw.trim()
        if (t.isEmpty()) return null
        if (t.contains(".")) t = t.substring(t.lastIndexOf(".") + 1)
        return t.trim()
    }

    /**
     * The enum constants of a PROJECT type: the explicit ones when the
     * contract declares them, otherwise the observed textual value.
     */
    List<String> enumConstantsFor(Object value, Object enumValues) {
        List<Object> raw = new ArrayList<Object>()
        if (enumValues instanceof Collection) {
            raw.addAll((Collection<Object>) enumValues)
        } else if (enumValues != null) {
            raw.add(enumValues)
        }
        if (raw.isEmpty() && value instanceof String) {
            raw.add(value)
        }
        List<String> out = new ArrayList<String>()
        for (Object o : raw) {
            String constant = normalizeEnumConstant(o == null ? null : o.toString())
            if (constant != null && !out.contains(constant)) out.add(constant)
        }
        return out
    }

    /**
     * Normalizes a value into a Java enum constant: "em processamento" ->
     * "EM_PROCESSAMENTO".
     */
    String normalizeEnumConstant(String raw) {
        if (raw == null) return null
        String s = raw.trim().toUpperCase(Locale.ROOT)
        if (s.isEmpty()) return null
        s = s.replaceAll("[^A-Z0-9]+", "_").replaceAll('^_+|_+$', "")
        if (s.isEmpty()) return null
        if (Character.isDigit(s.charAt(0))) s = "VALUE_" + s
        return s
    }

    /**
     * Normalizes a declared attribute type: FQN -> simple name, generics
     * dropped. Returns null when the type is not a java.lang type.
     */
    String normalizeType(String raw) {
        if (raw == null) return null
        String t = raw.trim()
        if (t.isEmpty()) return null
        if (t.contains(".")) t = t.substring(t.lastIndexOf(".") + 1)
        if (t.contains("<")) t = t.substring(0, t.indexOf("<"))
        if (!JAVA_LANG_TYPES.contains(t)) return null
        return t
    }

    /**
     * Estimates the attribute type from the VALUE carried by the contract.
     */
    String estimateType(Object value) {
        if (value == null) return "String"
        if (value instanceof Boolean) return "Boolean"
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) return "Integer"
        if (value instanceof Long || value instanceof java.math.BigInteger) return "Long"
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) return "Double"
        // Arrays and objects are kept as their serialized String form: the
        // template emits no import, so java.util types would not compile.
        return "String"
    }

    /**
     * Normalizes an attribute name into a valid Java identifier that also
     * satisfies the catalog regex ^[a-z][a-zA-Z0-9_]*$.
     */
    String sanitizeAttributeName(String raw) {
        if (raw == null) return null
        String s = raw.trim().replaceAll("[^A-Za-z0-9_]", "_")
        if (s.isEmpty()) return null
        if (Character.isDigit(s.charAt(0))) s = "field_" + s
        if (Character.isUpperCase(s.charAt(0))) s = Character.toLowerCase(s.charAt(0)).toString() + s.substring(1)
        if (!(s ==~ /^[a-z][a-zA-Z0-9_]*$/)) return null
        return s
    }

    /**
     * The attribute name of a PROJECT type: the simple name of the type with
     * its first letter lowercased ("StatusPedido" -> "statusPedido",
     * "EnderecoCliente" -> "enderecoCliente"). It satisfies the same catalog
     * regex as sanitizeAttributeName.
     */
    String fieldNameForType(String simpleTypeName) {
        if (simpleTypeName == null) return null
        String s = simpleTypeName.trim()
        if (s.isEmpty()) return null
        if (!(s ==~ /^[A-Za-z_][A-Za-z0-9_]*$/)) return null
        if (Character.isDigit(s.charAt(0))) s = "field_" + s
        if (Character.isUpperCase(s.charAt(0))) s = Character.toLowerCase(s.charAt(0)).toString() + s.substring(1)
        if (!(s ==~ /^[a-z][a-zA-Z0-9_]*$/)) return null
        return s
    }
}

/**
 * Derives the ENTITY semantics from the operator's JSON contract: the entity
 * name, the entity attributes (type resolved by the injected resolver) and the
 * strict "Type:name,Type:name" list expected by the catalog.
 *
 * SINGLE RESPONSIBILITY: contract -> entity/attribute derivation.
 */
class OrchestratorContractEntityDeriver {

    /**
     * Contract keys that carry meta-information about the module; they are
     * never entity attributes.
     */
    private static final List<String> CONTRACT_META_KEYS = [
            "entityName", "entity", "package", "description", "attributes", "fields"
    ]

    private final OrchestratorContractTypeResolver typeResolver

    OrchestratorContractEntityDeriver(OrchestratorContractTypeResolver typeResolver) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("OrchestratorContractEntityDeriver: typeResolver MUST be non-null")
        }
        this.typeResolver = typeResolver
    }

    /**
     * Derives the entity semantics from the contract.
     *
     * @return a Map with keys: entityName, entityNameSource, attributes (List
     *         of [name:.., type:..]), attributeList (String), warnings (List).
     */
    Map<String, Object> deriveFromContract(Map<String, Object> contract,
                                           Map<String, String> llmInference,
                                           Map<String, ?> catalog) {
        Map<String, Object> derived = new LinkedHashMap<>()
        List<String> warnings = new ArrayList<>()
        derived.put("warnings", warnings)

        // ---- entity name -------------------------------------------------
        String entityName = null
        String entityNameSource = null
        if (contract != null) {
            for (String metaKey : ["entity_name", "entityName", "entity"]) {
                Object raw = contract.get(metaKey)
                if (raw instanceof String && !((String) raw).trim().isEmpty()) {
                    entityName = ((String) raw).trim()
                    entityNameSource = "CONTRACT"
                    break
                }
            }
        }
        if (entityName == null && llmInference != null) {
            String proposed = llmInference.get("entity_name")
            if (proposed != null && !proposed.trim().isEmpty()) {
                entityName = proposed.trim()
                entityNameSource = "INFERENCE"
            }
        }
        derived.put("entityName", entityName)
        derived.put("entityNameSource", entityNameSource)

        // ---- attributes --------------------------------------------------
        List<Map<String, Object>> rawAttributes = new ArrayList<>()
        Object attrs = contract == null ? null : contract.get("attributes")
        if (attrs == null && contract != null) attrs = contract.get("fields")

        if (attrs instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) attrs).entrySet()) {
                rawAttributes.add([name: entry.key?.toString(), value: entry.value, type: null])
            }
        } else if (attrs instanceof List) {
            for (Object element : (List<Object>) attrs) {
                if (element instanceof Map) {
                    Map<?, ?> em = (Map<?, ?>) element
                    Object name = em.get("name")
                    if (name == null) name = em.get("attribute")
                    if (name == null) name = em.get("field")
                    Object value = null
                    if (em.containsKey("value")) value = em.get("value")
                    else if (em.containsKey("default")) value = em.get("default")
                    // The explicit enum constants of the attribute (when declared).
                    Object enumValues = em.get("enumValues")
                    if (enumValues == null) enumValues = em.get("values")
                    rawAttributes.add([name: name?.toString(), value: value, type: em.get("type"),
                                       enumValues: enumValues])
                } else if (element instanceof String) {
                    rawAttributes.add([name: (String) element, value: null, type: null])
                }
            }
        } else if (attrs == null && contract != null) {
            // No explicit attribute collection: the business fields of the
            // contract (top-level keys that are neither catalog flags nor
            // meta keys) ARE the entity attributes.
            for (Map.Entry<?, ?> entry : contract.entrySet()) {
                String key = entry.key?.toString()
                if (key == null) continue
                if (catalog.containsKey(key)) continue
                if (CONTRACT_META_KEYS.contains(key)) continue
                rawAttributes.add([name: key, value: entry.value, type: null])
            }
        }

        List<Map<String, Object>> attributes = new ArrayList<>()
        List<Map<String, Object>> attributeDetails = new ArrayList<>()
        List<String> parts = new ArrayList<>()
        Set<String> usedNames = new LinkedHashSet<String>()
        for (Map<String, Object> raw : rawAttributes) {
            String contractName = typeResolver.sanitizeAttributeName((String) raw.get("name"))
            if (contractName == null) {
                warnings.add("atributo descartado: nome invalido '" + raw.get("name") + "'")
                continue
            }
            // The explicit enum constants of the attribute (when declared).
            Object enumValues = raw.get("enumValues")
            if (enumValues == null) enumValues = raw.get("values")
            // The type is resolved by the dedicated resolver component: a
            // declared java.lang type wins; a declared PROJECT type is kept;
            // any other declared type is estimated from the value and the
            // rejection is reported as a warning.
            def resolution = typeResolver.resolve(contractName, raw.get("type"), raw.get("value"), enumValues)
            if (resolution.warning != null) warnings.add(resolution.warning)
            String type = resolution.type
            String name = contractName
            // A PROJECT type takes its attribute name from the TYPE itself:
            // EnderecoCliente -> enderecoCliente, StatusPedido -> statusPedido.
            if (resolution.projectType) {
                String typeName = typeResolver.fieldNameForType(type)
                if (typeName == null) {
                    warnings.add("atributo descartado: nome invalido para o tipo '" + type + "'")
                    continue
                }
                name = typeName
                // The type already named another attribute: the name of the
                // contract is kept (the entity MUST NOT repeat a field).
                if (usedNames.contains(name) && !usedNames.contains(contractName)) {
                    name = contractName
                }
            }
            if (usedNames.contains(name)) {
                warnings.add("atributo descartado: nome duplicado '" + name + "'")
                continue
            }
            usedNames.add(name)
            attributes.add([name: name, type: type])
            parts.add(type + ":" + name)
            attributeDetails.add([name: name, type: type,
                                  qualifiedType: resolution.qualifiedType,
                                  projectType: resolution.projectType,
                                  enumType: resolution.enumType,
                                  enumConstants: resolution.enumConstants])
        }
        derived.put("attributes", attributes)
        derived.put("attributeDetails", attributeDetails)
        derived.put("attributeList", parts.join(","))
        return derived
    }
}
