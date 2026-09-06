"use client";

import React from "react";

const PIPELINE_STEPS = [
  {
    step: "PLATE 01",
    title: "Tree-Sitter AST Parsing",
    accent: "#0D8D9C",
    description:
      "Extracts individual function boundaries across 12+ programming languages. Strips comments, whitespace, and formatting while preserving true syntax trees.",
    metric: "0.2ms / file",
    tag: "SYNTAX EXTRACTION",
  },
  {
    step: "PLATE 02",
    title: "SHA-256 Exact Filter",
    accent: "#171512",
    description:
      "Computes cryptographic hashes on normalized function bodies. Exact clones (Type 1) are caught in microsecond lookups before hitting vector inference.",
    metric: "100% precision",
    tag: "EXACT CLONE PASS",
  },
  {
    step: "PLATE 03",
    title: "ONNX Vector Inference",
    accent: "#E0447D",
    description:
      "Runs all-MiniLM-L6-v2 directly inside the Java runtime via ONNX Runtime. Generates dense 384-dimensional embeddings in batches of 50 asynchronously.",
    metric: "384 Dimensions",
    tag: "NEURAL EMBEDDINGS",
  },
  {
    step: "PLATE 04",
    title: "pgvector HNSW Cosine Search",
    accent: "#0D8D9C",
    description:
      "Executes indexed HNSW vector distance queries and self-joins in Supabase PostgreSQL, clustering semantic clones with sub-second nearest-neighbor math.",
    metric: "< 80ms query time",
    tag: "COSINE SIMILARITY",
  },
];

export default function PipelineSection() {
  return (
    <section
      id="how-it-works"
      className="relative px-4 sm:px-8 md:px-12 py-20 border-b-[0.8px] border-[#171512] bg-[#F1ECE0]"
    >
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-16 pb-6 border-b-[0.8px] border-[#171512]/30">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-sm text-[#171512]">+</span>
              <span className="text-xs font-semibold tracking-[2.4px] text-[#0D8D9C] uppercase">
                ENGINEERING PIPELINE · FOUR PLATES
              </span>
            </div>
            <h2 className="text-3xl sm:text-5xl font-[family-name:var(--font-newsreader)] text-[#171512] tracking-[-0.02em]">
              The Cascading Vector Detection Engine.
            </h2>
          </div>
          <p className="text-sm text-[#6F6A5B] max-w-md mt-4 md:mt-0 font-[family-name:var(--font-inter)] leading-relaxed">
            By cascading from lightweight abstract syntax tree hashing down to
            high-dimensional matrix embeddings, Cinnamon balances sub-second
            responsiveness with deep semantic accuracy.
          </p>
        </div>

        {/* 4 Pipeline Plates Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {PIPELINE_STEPS.map((step, idx) => (
            <div
              key={idx}
              className="border-[0.8px] border-[#171512] bg-[#FAF6EE] p-6 flex flex-col justify-between transition-all duration-300 hover:-translate-y-1 hover:shadow-md relative"
            >
              {/* Corner Registration Mark */}
              <div className="absolute top-2 right-2 text-xs font-mono text-[#171512]/40">
                +
              </div>

              <div>
                <div className="flex items-center justify-between mb-4">
                  <span className="text-[10px] font-mono tracking-[2px] uppercase text-[#6F6A5B]">
                    {step.step}
                  </span>
                  <span
                    className="w-2.5 h-2.5 border-[0.8px] border-[#171512]"
                    style={{ backgroundColor: step.accent }}
                  ></span>
                </div>

                <h3 className="text-lg font-semibold text-[#171512] mb-3 leading-snug">
                  {step.title}
                </h3>

                <p className="text-xs text-[#6F6A5B] leading-relaxed mb-6 font-[family-name:var(--font-inter)]">
                  {step.description}
                </p>
              </div>

              <div className="pt-4 border-t-[0.8px] border-[#171512]/20 flex items-center justify-between text-[11px] font-mono">
                <span className="text-[#0D8D9C] font-semibold">
                  {step.tag}
                </span>
                <span className="text-[#171512]">{step.metric}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}