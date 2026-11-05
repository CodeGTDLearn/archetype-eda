package eda.postgenerate.pre_processamento_maven_flags_via_llm.contract

/**
 * Raised when the contract is valid JSON but its root is not an object.
 */
class ContractFormatException extends RuntimeException {
    ContractFormatException(String message) {
        super(message)
    }
}
