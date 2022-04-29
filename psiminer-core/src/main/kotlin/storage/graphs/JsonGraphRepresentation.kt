package storage.graphs

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.utils.mapToIndex
import psi.graphs.CodeGraph
import psi.nodeProperties.nodeType
import psi.nodeProperties.token

object JsonGraphRepresentation {

    @Serializable
    data class GraphRepresentation(
        val edges: List<EdgesTypeGroup>,
        val nodes: NodesRepresentation,
    )

    @Serializable
    data class EdgesTypeGroup(
        val edgeType: String,
        val from: List<Int>,
        val to: List<Int>,
    )

    @Serializable
    data class NodesRepresentation(
        val tokens: List<String>,
        val nodeTypes: List<Long>
    )

    fun convertCodeGraph(codeGraph: CodeGraph, nodeTypeMapper: (PsiElement) -> Long): GraphRepresentation {
        val graphNodes = codeGraph.getAllNodes()
        val nodeToId = graphNodes.mapToIndex()
        val edges = codeGraph.getAllEdges().filter { !it.reversed }
        val edgesRepresentation = edges.groupBy { it.type }.map { (edgeType, edges) ->
            EdgesTypeGroup(
                edgeType = "$edgeType",
                from = edges.map { nodeToId[it.from] ?: throw IncorrectlyParsedGraph() },
                to = edges.map { nodeToId[it.to] ?: throw IncorrectlyParsedGraph() },
            )
        }
        val nodesRepresentation = NodesRepresentation(
            tokens = graphNodes.map { it.token ?: "_" },
            nodeTypes = graphNodes.map { nodeTypeMapper(it) }
        )
        return GraphRepresentation(
            edgesRepresentation,
            nodesRepresentation,
        )
    }

    class IncorrectlyParsedGraph : Exception("CodeGraph parsed incorrectly")
}
