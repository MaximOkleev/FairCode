package com.team.antiplagiat.controller.dto.plagiarism

data class PlagiarismComparisonResponse(
    val runId: Long? = null,
    val firstSolutionId: Long,
    val secondSolutionId: Long,
    val problemId: Long?,
    val contestId: Long?,
    val language: String?,
    val similarity: Double,
    val threshold: Double,
    val plagiarism: Boolean,
    val matchedFragments: List<PlagiarismMatchedFragmentResponse> = emptyList()
)

data class PlagiarismMatchedFragmentResponse(
    val firstStartLine: Int,
    val firstEndLine: Int,
    val secondStartLine: Int,
    val secondEndLine: Int
)

data class PlagiarismContestProblemCheckResponse(
    val runId: Long,
    val checkedSolutions: Int,
    val comparedPairs: Int,
    val matches: Int,
    val threshold: Double,
    val pairs: List<PlagiarismComparisonResponse>,
    val groups: List<PlagiarismPairGroupResponse>
)

data class PlagiarismPairGroupResponse(
    val groupId: Long,
    val solutionIds: List<Long>,
    val problemId: Long?,
    val contestId: Long?,
    val language: String?,
    val maxSimilarity: Double,
    val pairs: List<PlagiarismComparisonResponse>
)
