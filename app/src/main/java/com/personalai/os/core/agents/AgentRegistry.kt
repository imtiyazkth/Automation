package com.personalai.os.core.agents

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loads every assets/agents/*.json definition at startup and keeps both the
 * descriptive [AgentDefinition]s and the executable [Agent] implementations
 * that have been registered against them. This is what lets new agents be
 * added later by dropping in a new JSON file + implementation and
 * registering it here - no changes to the Head Agent itself.
 */
class AgentRegistry(private val context: Context) {

    private val gson = Gson()
    private val definitions = mutableMapOf<String, AgentDefinition>()
    private val implementations = mutableMapOf<String, Agent>()

    fun loadDefinitions(assetFolder: String = "agents") {
        val files = context.assets.list(assetFolder).orEmpty()
        for (file in files) {
            if (!file.endsWith(".json")) continue
            context.assets.open("$assetFolder/$file").use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val def = gson.fromJson(reader, AgentDefinition::class.java)
                definitions[def.id] = def
            }
        }
    }

    fun register(agent: Agent) {
        implementations[agent.definitionId] = agent
    }

    fun definitionOf(agentId: String): AgentDefinition? = definitions[agentId]

    fun implementationOf(agentId: String): Agent? = implementations[agentId]

    fun all(): List<AgentDefinition> = definitions.values.toList()

    /** Capability-based lookup - the Head Agent never selects agents by name. */
    fun findByCapability(capability: String): List<AgentDefinition> =
        definitions.values.filter { capability in it.capabilities }
}
