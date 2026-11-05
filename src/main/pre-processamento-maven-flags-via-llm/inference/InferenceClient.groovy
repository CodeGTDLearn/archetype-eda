package eda.postgenerate.pre_processamento_maven_flags_via_llm.inference

import groovy.json.JsonSlurper

/**
 * Shells out to the operator-supplied inference script.
 *
 * Responsibility: process invocation and output parsing. It does not decide
 * the application flow, the cache policy or the merge rules.
 */
class InferenceClient {

    @SuppressWarnings("unchecked")
    Map<String, String> run(String inferenceBin, String description, String contractJson) {
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
