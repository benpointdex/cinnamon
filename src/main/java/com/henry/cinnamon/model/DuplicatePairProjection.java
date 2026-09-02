package com.henry.cinnamon.model;

public interface DuplicatePairProjection {
    String getFilePathA();
    String getFunctionNameA();
    String getFilePathB();
    String getFunctionNameB();
    Integer getLineCountA();
    Integer getLineCountB();
    Double getSimilarityScore();
}
