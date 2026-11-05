package eda.postgenerate.pre_processamento_maven_flags_via_llm.contract

import groovy.json.JsonSlurper

/**
 * Reads and parses the operator's JSON contract.
 *
 * Responsibility: stdin ingestion and JSON shape validation only.
 */
class ContractReader {

    String readFromStdin() {
        StringBuilder sb = new StringBuilder()
        BufferedReader br = new BufferedReader(System.in.newReader())
        String line
        while ((line = br.readLine()) != null) {
            if (sb.length() > 0) sb.append(System.lineSeparator())
            sb.append(line)
        }
        return sb.toString()
    }

    Map<String, Object> parse(String contractJson) {
        Object parsed = new JsonSlurper().parseText(contractJson)
        if (!(parsed instanceof Map)) {
            throw new ContractFormatException("Contract root MUST be a JSON object.")
        }
        return (Map<String, Object>) parsed
    }
}
