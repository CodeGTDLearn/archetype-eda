package eda.postgenerate.pre_processamento_maven_flags_via_llm.inference

import eda.postgenerate.pre_processamento_maven_flags_via_llm.cache.CacheKeyFactory
import eda.postgenerate.pre_processamento_maven_flags_via_llm.cache.CacheStore
import eda.postgenerate.pre_processamento_maven_flags_via_llm.cache.JsonCanonicalizer
import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.CatalogEntry
import eda.postgenerate.pre_processamento_maven_flags_via_llm.catalog.FlagCatalogExtractor
import eda.postgenerate.pre_processamento_maven_flags_via_llm.command.CommandLineArguments
import eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractFormatException
import eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractReader
import eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractSecretScanner
import eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.ContractStore
import eda.postgenerate.pre_processamento_maven_flags_via_llm.contract.MaterializationResult
import eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractEntityDeriver
import eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.ContractTypeResolver
import eda.postgenerate.pre_processamento_maven_flags_via_llm.domain.FlagProposalMerger
import eda.postgenerate.pre_processamento_maven_flags_via_llm.presentation.CliOutput
import eda.postgenerate.pre_processamento_maven_flags_via_llm.command.CommandLineAssembler
import eda.postgenerate.pre_processamento_maven_flags_via_llm.presentation.ProvenanceReportRenderer

/**
 * Application service for the LLM flag inference use case.
 *
 * Responsibility: orchestrate the flow and map failures to exit codes. All
 * infrastructure, domain and presentation work is delegated to collaborators.
 *
 * Exit codes (per cli-contract.md Section 4):
 *   0 success / 2 required input missing / 3 inference failed /
 *   4 catalog malformed / 5 internal error.
 */
class InferenceApplication {

    private final CommandLineArguments arguments
    private final ContractReader contractReader
    private final FlagCatalogExtractor catalogExtractor
    private final JsonCanonicalizer canonicalizer
    private final CacheKeyFactory cacheKeyFactory
    private final CacheStore cacheStore
    private final ContractStore contractStore
    private final ContractSecretScanner secretScanner
    private final InferenceClient inferenceClient
    private final CommandLineAssembler assembler
    private final ProvenanceReportRenderer reportRenderer
    private final FlagProposalMerger merger
    private final CliOutput output

    InferenceApplication() {
        this(new CliOutput())
    }

    InferenceApplication(CliOutput output) {
        this.arguments = new CommandLineArguments()
        this.contractReader = new ContractReader()
        this.catalogExtractor = new FlagCatalogExtractor()
        this.canonicalizer = new JsonCanonicalizer()
        this.cacheKeyFactory = new CacheKeyFactory(canonicalizer)
        this.cacheStore = new CacheStore()
        this.contractStore = new ContractStore()
        this.secretScanner = new ContractSecretScanner()
        this.inferenceClient = new InferenceClient()
        this.assembler = new CommandLineAssembler()
        this.reportRenderer = new ProvenanceReportRenderer()
        this.merger = new FlagProposalMerger(new ContractEntityDeriver(new ContractTypeResolver()))
        this.output = output
    }

    int run(String[] args) {
        Map<String, String> cliArgs
        try {
            cliArgs = arguments.parse(args)
        } catch (Exception e) {
            output.error("ERROR: " + e.message)
            return 5
        }

        InferenceConfig config = InferenceConfig.from(cliArgs)
        String description = cliArgs.get("description")

        // FR-001: use-case-description is mandatory and MUST be checked FIRST.
        if (description == null || description.trim().isEmpty()) {
            output.error("ERROR: USE CASE DESCRIPTION is missing or blank.")
            output.error("Provide it via --use-case-description \"<text>\".")
            output.error("Processing stopped BEFORE reading the contract and BEFORE inference.")
            output.error("See spec.md FR-001.")
            return 2
        }

        // FR-002/FR-003: contract is read from stdin ONLY when --use-case-contract
        // was passed. It is read here (after the description gate) so a missing
        // description never blocks on or consumes stdin input.
        String contractJson = null
        if (cliArgs.containsKey("use-case-contract")) {
            try {
                contractJson = contractReader.readFromStdin()
            } catch (Exception e) {
                output.error("ERROR: Failed to read contract from stdin: " + e.message)
                return 5
            }
        }
        if (contractJson == null || contractJson.isEmpty()) {
            output.error("ERROR: USE CASE CONTRACT is missing or empty.")
            output.error("Provide it via --use-case-contract, passing the multi-line JSON through stdin.")
            output.error("Processing stopped BEFORE inference.")
            output.error("See spec.md FR-003.")
            return 2
        }

        Map<String, Object> contract
        try {
            contract = contractReader.parse(contractJson)
        } catch (ContractFormatException e) {
            output.error("ERROR: " + e.message)
            return 5
        } catch (Exception e) {
            output.error("ERROR: Contract is not valid JSON: " + e.message)
            return 5
        }

        // Read catalog (FR-005, FR-013, FR-018)
        Map<String, CatalogEntry> catalog
        try {
            File meta = config.catalogFile()
            catalog = catalogExtractor.extract(meta)
            if (config.debug) {
                output.info("Catalog: " + catalog.size() + " flags from " + meta.absolutePath)
            }
        } catch (Exception e) {
            output.error("ERROR: Flag catalog is malformed or unreadable.")
            output.error("  " + e.message)
            return 4
        }

        // Cache key (FR-011): catalog fingerprint + description + canonical contract.
        String canonicalContract = cacheKeyFactory.canonicalize(contract)
        String catalogFingerprint = cacheKeyFactory.fingerprint(catalog)
        String cacheKey = cacheKeyFactory.makeKey(description, canonicalContract, catalogFingerprint)

        // FR-021 (contract persistence): the contract is materialized BEFORE the
        // cache lookup, so the command line can carry its path on a cache HIT.
        MaterializationResult materialized = contractStore.materialize(
                config.contractDir, canonicalContract, contractJson)
        if (materialized.warning != null) {
            output.warning(materialized.warning)
        }
        String contractFilePath = materialized.path

        List<String> secretLikeKeys = secretScanner.scan(contract)
        if (!secretLikeKeys.isEmpty()) {
            output.warning(
                "WARNING: the contract contains keys that look like secrets: " + secretLikeKeys.join(", "))
            output.warning(
                "         The contract is persisted in the generated project " +
                "(src/test/resources/contracts/contract.json) and WILL be versioned.")
            output.warning(
                "         Do not put credentials in the contract (Principle V).")
        }

        // Values the mechanism derives from the contract itself (not literals
        // of the contract, not LLM proposals): today the path of the
        // materialized contract file.
        Map<String, String> derivedOverrides = new LinkedHashMap<>()
        if (contractFilePath != null) {
            derivedOverrides.put("entity_contract_path", contractFilePath)
        }

        Map<String, String> hit = cacheStore.lookup(config.cacheDir, cacheKey)
        if (hit != null) {
            output.commandLine(hit.get("v"))
            output.reportHeader(true)
            String cachedReport = hit.get("r")
            if (cachedReport == null) {
                output.warning("(relatorio indisponivel: entrada de cache legada, gravada antes da FR-020)")
            } else {
                output.reportBody(cachedReport)
            }
            return 0
        }

        // Run external inference (FR-004, FR-019)
        Map<String, String> llmInference
        try {
            llmInference = inferenceClient.run(config.inferenceBin, description, contractJson)
        } catch (Exception e) {
            output.error("ERROR: External inference script failed: " + e.message)
            return 3
        }

        // Merge (FR-006..FR-009, FR-020, FR-021)
        Map<String, Object> merged = merger.mergeWithProvenance(
                llmInference, contract, catalog, derivedOverrides)
        Map<String, String> proposal = (Map<String, String>) merged.get("values")

        // FR-006 diagnostics and derivation warnings go to stderr, never to stdout.
        List<String> diagnostics = (List<String>) merged.get("diagnostics")
        if (diagnostics != null) {
            for (String diagnostic : diagnostics) {
                output.warning(diagnostic)
            }
        }
        List<String> warnings = (List<String>) merged.get("warnings")
        if (warnings != null) {
            for (String warning : warnings) {
                output.warning("WARNING: " + warning)
            }
        }

        // Assemble (FR-010)
        String commandLine = assembler.assemble(proposal)

        // FR-020: provenance report body (no description/contract payload - FR-017).
        String reportBody = reportRenderer.render(merged)

        output.commandLine(commandLine)
        output.reportHeader(false)
        output.reportBody(reportBody)
        try {
            cacheStore.write(config.cacheDir, cacheKey, commandLine, reportBody)
        } catch (Exception e) {
            output.warning("WARN: cache write failed (non-fatal): " + e.message)
        }
        return 0
    }
}
