package eda.postgenerate.pre_processamento_maven_flags_via_llm.contract

import eda.postgenerate.pre_processamento_maven_flags_via_llm.Sha256

/**
 * Materializes the contract on disk so the archetype can copy it into the
 * generated module (FR-023).
 *
 * Responsibility: contract persistence only. It returns a warning instead of
 * printing, keeping presentation out of this component.
 */
class ContractStore {

    MaterializationResult materialize(String contractDir, String canonicalContract, String contractJson) {
        try {
            File dir = new File(contractDir)
            if (!dir.exists()) dir.mkdirs()
            String name = Sha256.hex(canonicalContract) + ".json"
            File target = new File(dir, name)
            String content = contractJson == null ? "" : contractJson.trim()
            if (!target.exists() || target.getText("UTF-8") != content) {
                target.setText(content, "UTF-8")
            }
            // The path travels on the command line, where a backslash would be
            // escaped (and PowerShell does not unescape it). Forward slashes
            // are understood by java.nio on every platform and by every shell.
            return new MaterializationResult(target.absolutePath.replace('\\', '/'), null)
        } catch (Exception e) {
            return new MaterializationResult(null,
                    "WARN: the contract could not be materialized: " + e.message)
        }
    }
}
