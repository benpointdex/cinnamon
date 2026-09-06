"use client";

import React from "react";

const LANGUAGES = [
  {
    name: "TypeScript",
    ext: ".ts, .tsx",
    desc: "Tree-sitter parser with AST arrow functions & class methods.",
  },
  {
    name: "Python",
    ext: ".py",
    desc: "Async definitions, lambda normalization, and docstring stripping.",
  },
  {
    name: "Java",
    ext: ".java",
    desc: "Spring, Jakarta, records, and Lombok structural AST normalization.",
  },
  {
    name: "Go",
    ext: ".go",
    desc: "Goroutine closures, struct receivers, and interface methods.",
  },
  {
    name: "Rust",
    ext: ".rs",
    desc: "Impl blocks, trait functions, macro expansions, and match arm blocks.",
  },
  {
    name: "Dart",
    ext: ".dart",
    desc: "Flutter widget state methods, async/await futures, and isolates.",
  },
  {
    name: "C / C++",
    ext: ".c, .cpp",
    desc: "Header declaration vs definition alignment and template matching.",
  },
  {
    name: "C#",
    ext: ".cs",
    desc: ".NET LINQ queries, async task signatures, and controller actions.",
  },
  {
    name: "Kotlin",
    ext: ".kt",
    desc: "Extension functions, suspend coroutines, and companion object methods.",
  },
  {
    name: "PHP",
    ext: ".php",
    desc: "Modern PHP 8+ typed attributes, match expressions, and closures.",
  },
  {
    name: "Ruby",
    ext: ".rb",
    desc: "Rails model hooks, block yields, and module singleton methods.",
  },
  {
    name: "JavaScript",
    ext: ".js, .jsx",
    desc: "ESNext modules, CommonJS exports, and dynamic function expressions.",
  },
];

const MCP_TOOLS = [
  {
    name: "scan_repository_duplicates",
    badge: "FULL REPO",
    desc: "Clones or reads an entire directory tree, extracts AST functions, builds vector clusters, and returns duplicate groups.",
  },
  {
    name: "find_similar_functions",
    badge: "SNIPPET SEARCH",
    desc: "Computes the 384-dim ONNX vector of any given code block and queries Supabase pgvector for similar implementations.",
  },
  {
    name: "ingest_files",
    badge: "DIRECT INGEST",
    desc: "Accepts raw file contents from AI coding assistants and ingests them into the persistent vector store asynchronously.",
  },
  {
    name: "ingest_github_repository",
    badge: "GIT CLONE",
    desc: "Clones any public GitHub repository directly inside the backend container and indexes all functions in the background.",
  },
  {
    name: "get_duplicate_report",
    badge: "ANALYSIS",
    desc: "Produces formatted markdown reports detailing duplicate clusters, similarity percentages, and refactoring candidates.",
  },
  {
    name: "get_ingestion_status",
    badge: "ASYNC TRACKING",
    desc: "Polls live background job status with percentage completed, AST functions parsed, and embedding batch duration.",
  },
  {
    name: "record_duplicate",
    badge: "FEEDBACK",
    desc: "Allows developers to confirm, dismiss, or tag duplicate detections for continuous model refinement.",
  },
];

export default function EcosystemSection() {
  return (
    <section
      id="ecosystem"
      className="relative px-4 sm:px-8 md:px-12 py-20 border-b-[0.8px] border-[#171512] bg-[#F1ECE0]"
    >
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-16 pb-6 border-b-[0.8px] border-[#171512]/30">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-sm text-[#171512]">+</span>
              <span className="text-xs font-semibold tracking-[2.4px] text-[#0D8D9C] uppercase">
                ECOSYSTEM &amp; MCP TOOL SUITE
              </span>
            </div>
            <h2 className="text-3xl sm:text-5xl font-[family-name:var(--font-newsreader)] text-[#171512] tracking-[-0.02em]">
              Polyglot Parsers &amp; 7 Native MCP Tools.
            </h2>
          </div>
          <p className="text-sm text-[#6F6A5B] max-w-md mt-4 md:mt-0 font-[family-name:var(--font-inter)] leading-relaxed">
            Built from the ground up for modern polyglot codebases. One unified
            engine supporting all major programming languages and standard MCP
            capabilities.
          </p>
        </div>

        {/* 1. MCP Tools Grid */}
        <div className="mb-16">
          <div className="text-xs font-mono tracking-[2px] uppercase text-[#171512] font-semibold mb-6 flex items-center gap-2">
            <span>SEVEN NATIVE MCP TOOLS</span>
            <div className="h-[0.8px] flex-1 bg-[#171512]/20"></div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {MCP_TOOLS.map((tool, idx) => (
              <div
                key={idx}
                className="border-[0.8px] border-[#171512] bg-[#FAF6EE] p-5 flex flex-col justify-between hover:border-[#E0447D] transition-colors"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-[10px] font-mono tracking-widest px-2 py-0.5 bg-[#171512] text-white">
                      {tool.badge}
                    </span>
                    <span className="text-xs font-mono text-[#6F6A5B]">
                      #{idx + 1}
                    </span>
                  </div>

                  <h3 className="text-sm font-mono font-bold text-[#171512] mb-2">
                    {tool.name}
                  </h3>

                  <p className="text-xs text-[#6F6A5B] leading-relaxed font-[family-name:var(--font-inter)]">
                    {tool.desc}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 2. Languages Grid */}
        <div>
          <div className="text-xs font-mono tracking-[2px] uppercase text-[#171512] font-semibold mb-6 flex items-center gap-2">
            <span>TWELVE SUPPORTED LANGUAGES</span>
            <div className="h-[0.8px] flex-1 bg-[#171512]/20"></div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {LANGUAGES.map((lang, idx) => (
              <div
                key={idx}
                className="border-[0.8px] border-[#171512]/40 bg-[#FAF6EE] p-4 text-center hover:border-[#0D8D9C] transition-colors"
              >
                <div className="text-sm font-bold text-[#171512] mb-1">
                  {lang.name}
                </div>
                <div className="text-[11px] font-mono text-[#0D8D9C]">
                  {lang.ext}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}