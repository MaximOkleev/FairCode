package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismGroupResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismMatchResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismSolutionMemberResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismUserMemberResponse
import com.team.antiplagiat.models.PlagiarismMatch

object PlagiarismGrouping {

    fun buildGroups(matches: List<PlagiarismMatch>): List<PlagiarismGroupResponse> {
        if (matches.isEmpty()) {
            return emptyList()
        }

        val solutionsById = matches
            .flatMap { listOf(it.firstSolution, it.secondSolution) }
            .associateBy { it.id }
        val dsu = DisjointSet(solutionsById.keys)

        matches.forEach {
            dsu.union(it.firstSolution.id, it.secondSolution.id)
        }

        val matchesByRoot = matches.groupBy { dsu.find(it.firstSolution.id) }

        return dsu.groups()
            .values
            .mapNotNull { solutionIds ->
                if (solutionIds.size < 2) {
                    return@mapNotNull null
                }

                val sortedSolutions = solutionIds
                    .mapNotNull { solutionsById[it] }
                    .sortedBy { it.id }
                val firstSolution = sortedSolutions.first()
                val root = dsu.find(firstSolution.id)
                val groupMatches = matchesByRoot[root].orEmpty().sortedByDescending { it.similarity }

                PlagiarismGroupResponse(
                    groupId = sortedSolutions.minOf { it.id },
                    problemId = firstSolution.problem.id,
                    language = firstSolution.language,
                    maxSimilarity = groupMatches.maxOf { it.similarity },
                    users = sortedSolutions
                        .distinctBy { it.user.id }
                        .map { PlagiarismUserMemberResponse.fromEntity(it) },
                    members = sortedSolutions.map { PlagiarismSolutionMemberResponse.fromEntity(it) },
                    matches = groupMatches.map { PlagiarismMatchResponse.fromEntity(it) }
                )
            }
            .sortedWith(compareByDescending<PlagiarismGroupResponse> { it.maxSimilarity }.thenBy { it.groupId })
    }

    private class DisjointSet(ids: Collection<Long>) {
        private val parent = ids.associateWith { it }.toMutableMap()
        private val size = ids.associateWith { 1 }.toMutableMap()

        fun find(id: Long): Long {
            var current = parent[id] ?: error("Unknown DSU node: $id")

            while (parent[current] != current) {
                current = parent[current] ?: error("Unknown DSU node: $current")
            }

            val root = current
            current = id
            while (parent[current] != current) {
                val next = parent[current] ?: error("Unknown DSU node: $current")
                parent[current] = root
                current = next
            }

            return root
        }

        fun union(firstId: Long, secondId: Long) {
            var firstRoot = find(firstId)
            var secondRoot = find(secondId)

            if (firstRoot == secondRoot) {
                return
            }

            if (size.getValue(firstRoot) < size.getValue(secondRoot)) {
                val tmp = firstRoot
                firstRoot = secondRoot
                secondRoot = tmp
            }

            parent[secondRoot] = firstRoot
            size[firstRoot] = size.getValue(firstRoot) + size.getValue(secondRoot)
        }

        fun groups(): Map<Long, List<Long>> =
            parent.keys.groupBy { find(it) }
    }
}
