package com.team.antiplagiat.controller.dto.ai

import com.team.antiplagiat.models.AiPlagiarismCheckRun
import com.team.antiplagiat.models.AiProvider
import java.time.LocalDateTime

data class AiPlagiarismCheckResponse(
    val runId: Long,
    val checkedSolutions: Int,
    val comparedPairs: Int,
    val matches: Int,
    val threshold: Double,
    val generatedAiSolutions: Int,
    val pairs: List<AiPlagiarismMatchResponse>
)

data class AiPlagiarismMatchResponse(
    val solutionId: Long,
    val contestId: Long?,
    val problemId: Long,
    val language: String,
    val filePath: String,
    val aiSolutionId: Long,
    val provider: AiProvider,
    val modelName: String,
    val similarity: Double,
    val plagiarism: Boolean,
    val matchedFragments: List<AiPlagiarismMatchedFragmentResponse> = emptyList()
)

data class AiPlagiarismMatchedFragmentResponse(
    val solutionStartLine: Int,
    val solutionEndLine: Int,
    val aiSolutionStartLine: Int,
    val aiSolutionEndLine: Int
)

data class AiPlagiarismCheckSummaryResponse(
    val runId: Long,
    val ownerId: Long,
    val contestId: Long?,
    val solutionId: Long?,
    val checkedSolutions: Int,
    val comparedPairs: Int,
    val matches: Int,
    val threshold: Double,
    val generatedAiSolutions: Int,
    val createdAt: LocalDateTime,
    val finishedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(run: AiPlagiarismCheckRun): AiPlagiarismCheckSummaryResponse =
            AiPlagiarismCheckSummaryResponse(
                runId = run.id,
                ownerId = run.ownerId,
                contestId = run.contestId,
                solutionId = run.solutionId,
                checkedSolutions = run.checkedSolutions,
                comparedPairs = run.comparedPairs,
                matches = run.matches,
                threshold = run.threshold,
                generatedAiSolutions = run.generatedAiSolutions,
                createdAt = run.createdAt,
                finishedAt = run.finishedAt
            )
    }
}
