"use client";

import React from "react";

export default function Footer() {
  return (
    <footer className="relative px-4 sm:px-8 md:px-12 py-12 bg-[#F1ECE0] border-t-[0.8px] border-[#171512] text-xs font-mono">
      <div className="max-w-7xl mx-auto flex flex-col gap-8">
        {/* Top Registration Bar */}
        <div className="flex items-center justify-between pb-6 border-b-[0.8px] border-[#171512]/20 text-[11px] uppercase tracking-[2px]">
          <div className="flex items-center gap-3">
            <span className="text-[#171512] font-bold">CINNAMON</span>
            <span className="text-[#6F6A5B]">·</span>
            <span className="text-[#E0447D]">TWO PLATES ONLY</span>
          </div>

          <div className="flex items-center gap-2">
            <div className="w-3 h-3 bg-[#0D8D9C] border-[0.8px] border-[#171512]"></div>
            <div className="w-3 h-3 bg-[#E0447D] border-[0.8px] border-[#171512]"></div>
          </div>
        </div>

        {/* Middle Navigation Links & Status */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <p className="text-xs text-[#6F6A5B] leading-relaxed font-sans">
              Detect exact, renamed, and semantic duplicate functions across
              12+ languages — powered by Tree-Sitter AST parsing, ONNX
              embeddings, and pgvector cosine search.
            </p>
          </div>

          <div>
            <div className="text-[10px] uppercase tracking-widest text-[#6F6A5B] mb-2 font-bold">
              NAVIGATION
            </div>
            <ul className="space-y-1.5 text-xs text-[#171512]">
              <li>
                <a
                  href="#visualizer"
                  className="hover:text-[#E0447D] transition-colors"
                >
                  Clone Visualizer
                </a>
              </li>
              <li>
                <a
                  href="#how-it-works"
                  className="hover:text-[#E0447D] transition-colors"
                >
                  4-Stage Pipeline
                </a>
              </li>
              <li>
                <a
                  href="#mcp-setup"
                  className="hover:text-[#E0447D] transition-colors"
                >
                  MCP Installation
                </a>
              </li>
              <li>
                <a
                  href="#pricing"
                  className="hover:text-[#E0447D] transition-colors"
                >
                  Developer Pricing
                </a>
              </li>
            </ul>
          </div>

          <div>
            <div className="text-[10px] uppercase tracking-widest text-[#6F6A5B] mb-2 font-bold">
              RESOURCES
            </div>
            <ul className="space-y-1.5 text-xs text-[#171512]">
              <li>
                <a
                  href="https://modelcontextprotocol.io"
                  target="_blank"
                  rel="noreferrer"
                  className="hover:text-[#0D8D9C] transition-colors"
                >
                  MCP Protocol Spec ↗
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/benpointdex/cinnamon"
                  target="_blank"
                  rel="noreferrer"
                  className="hover:text-[#0D8D9C] transition-colors"
                >
                  GitHub Repository ↗
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Registration Baseline */}
        <div className="flex items-center justify-between pt-6 border-t-[0.8px] border-[#171512]/20 text-[10px] text-[#6F6A5B] uppercase tracking-[1.5px]">
          <span>+ CINNAMON</span>
          <div className="reg-target"></div>
          <span>© 2026 CINNAMON · ALL RIGHTS RESERVED +</span>
        </div>
      </div>
    </footer>
  );
}