package com.runt9.cashflow.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

// Example for password field in entity, taken from https://stackoverflow.com/a/30723658
class BCryptPasswordDeserializer : JsonDeserializer<String>() {
    override fun deserialize(jsonParser: JsonParser?, ctxt: DeserializationContext?): String {
        val oc = jsonParser?.codec
        val node: JsonNode? = oc?.readTree(jsonParser)
        val encoder = BCryptPasswordEncoder()
        return encoder.encode(node?.asText())
    }
}