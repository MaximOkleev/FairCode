package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismCheckSummaryResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismGroupResponse
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.plagiarism.CodePlagiarismDetector
import com.team.antiplagiat.repository.PlagiarismCheckRunRepository
import com.team.antiplagiat.repository.PlagiarismMatchRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismSimpleResultResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismCheckStartResponse


@Service
class PlagiarismService(
    private val plagiarismCheckRunRepository: PlagiarismCheckRunRepository,
    private val plagiarismMatchRepository: PlagiarismMatchRepository,
    private val plagiarismCheckRunner: PlagiarismCheckRunner
) {

    fun startFullCheck(
        threshold: Double = CodePlagiarismDetector.DEFAULT_THRESHOLD
    ): PlagiarismCheckStartResponse {
        require(threshold in 0.0..1.0) { "threshold must be in range 0.0..1.0" }

        val run = plagiarismCheckRunRepository.save(
            PlagiarismCheckRun(threshold = threshold)
        )

        plagiarismCheckRunner.run(run.id)

        return PlagiarismCheckStartResponse(
            runId = run.id,
            status = run.status.name,
            message = "Plagiarism check started. Use GET /api/plagiarism/runs/${run.id} to check status."
        )
    }

    @Transactional(readOnly = true)
    fun findRun(runId: Long): PlagiarismCheckSummaryResponse =
        plagiarismCheckRunRepository.findById(runId)
            .map { PlagiarismCheckSummaryResponse.fromEntity(it) }
            .orElseThrow { ResourceNotFoundException("Plagiarism check run with id=$runId not found") }

    @Transactional(readOnly = true)
    fun findCheaterGroups(): List<PlagiarismGroupResponse> {
        val latestCompletedRun = plagiarismCheckRunRepository
            .findFirstByStatusOrderByFinishedAtDesc(PlagiarismCheckRunStatus.COMPLETED)
            ?: return emptyList()

        return PlagiarismGrouping.buildGroups(
            plagiarismMatchRepository.findAllByCheckRunWithSolutions(latestCompletedRun)
        )
    }

    @Transactional(readOnly = true)
    fun findLatestResults(): List<PlagiarismSimpleResultResponse> {
        val latestCompletedRun = plagiarismCheckRunRepository
            .findFirstByStatusOrderByFinishedAtDesc(PlagiarismCheckRunStatus.COMPLETED)
            ?: return emptyList()

        val matches = plagiarismMatchRepository.findAllByCheckRunWithSolutions(latestCompletedRun)
        return matches.map { m ->
            PlagiarismSimpleResultResponse(
                problem = m.firstSolution.problem.name,
                user1 = m.firstSolution.user.login,
                user2 = m.secondSolution.user.login,
                similarity = m.similarity * 100.0
            )
        }
    }
}
