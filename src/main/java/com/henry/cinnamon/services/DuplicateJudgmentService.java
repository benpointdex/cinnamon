package com.henry.cinnamon.services;

import com.henry.cinnamon.model.CodeSnippetPair;
import com.henry.cinnamon.model.DuplicateFinding;
import com.henry.cinnamon.model.DuplicateVerdict;
import com.henry.cinnamon.repository.DuplicateFindingRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DuplicateJudgmentService {

    private final ChatClient chatClient;
    private final DuplicateFindingRepository findingRepository;

    public DuplicateJudgmentService(ChatClient.Builder builder, DuplicateFindingRepository findingRepository) {
        this.chatClient = builder.build();
        this.findingRepository = findingRepository;
    }

    /**
     * Prompts Gemini to analyze two code snippets and records the finding if duplicate.
     */
    public DuplicateFinding judgeAndRecord(String repository, CodeSnippetPair pair, String tenantId) {
        DuplicateVerdict verdict = chatClient.prompt()
                .user(u -> u.text("""
                    Compare these two functions and decide if they implement the
                    same underlying business logic, even if variable names, formatting, or control structures differ.

                    Function A:
                    {newCode}

                    Function B:
                    {matchedCode}

                    Respond with structured JSON containing:
                    - duplicate: boolean (true if genuine duplicate, false otherwise)
                    - confidence: double (0.0 to 1.0)
                    - reasoning: a concise one-sentence explanation of why they are or are not duplicate logic.
                    """)
                    .param("newCode", pair.newCode())
                    .param("matchedCode", pair.matchedCode()))
                .call()
                .entity(DuplicateVerdict.class);

        if (verdict == null || !verdict.duplicate()) {
            return null; // Not a confirmed duplicate, nothing to save
        }

        // Save confirmed duplicate finding to Postgres
        DuplicateFinding finding = new DuplicateFinding();
        finding.setTenantId(tenantId);
        finding.setRepository(repository);
        finding.setNewFilePath(pair.newFilePath());
        finding.setNewFunctionName(pair.newFunctionName());
        finding.setMatchedFilePath(pair.matchedFilePath());
        finding.setMatchedFunctionName(pair.matchedFunctionName());
        finding.setSimilarityScore(verdict.confidence());
        finding.setConfirmedDuplicate(true);
        finding.setJudgmentReasoning(verdict.reasoning());
        finding.setCommitSha(pair.commitSha());

        return findingRepository.save(finding);
    }
}
