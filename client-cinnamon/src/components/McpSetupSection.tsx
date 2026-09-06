"use client";

import React, { useState } from "react";

const CONFIGS: Record<
  string,
  { target: string; snippet: string }
> = {
  cursor: {
    target: ".cursor/mcp.json",
    snippet: `{
  "mcpServers": {
    "cinnamon": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "https://cinnamon-l7jf.onrender.com/sse"],
      "env": {
        "CINNAMON_API_KEY": "cin_live_YOUR_API_KEY_HERE"
      }
    }
  }
}`,
  },
  claude: {
    target: "claude_desktop_config.json",
    snippet: `{
  "mcpServers": {
    "cinnamon": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "https://cinnamon-l7jf.onrender.com/sse"],
      "env": {
        "CINNAMON_API_KEY": "cin_live_YOUR_API_KEY_HERE"
      }
    }
  }
}`,
  },
  antigravity: {
    target: ".gemini/settings.json",
    snippet: `{
  "mcpServers": {
    "cinnamon": {
      "url": "https://cinnamon-l7jf.onrender.com/sse",
      "headers": {
        "X-Api-Key": "cin_live_YOUR_API_KEY_HERE"
      }
    }
  }
}`,
  },
};

type ClientKey = "cursor" | "claude" | "antigravity";

export default function McpSetupSection() {
  const [activeClient, setActiveClient] = useState<ClientKey>("cursor");
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(CONFIGS[activeClient].snippet);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <section
      id="mcp-setup"
      className="relative px-4 sm:px-8 md:px-12 py-20 border-b-[0.8px] border-[#171512] bg-[#FAF6EE]"
    >
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-12 pb-6 border-b-[0.8px] border-[#171512]/30">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-sm text-[#171512]">+</span>
              <span className="text-xs font-semibold tracking-[2.4px] text-[#E0447D] uppercase">
                ONE-CLICK MCP INTEGRATION
              </span>
            </div>
            <h2 className="text-3xl sm:text-5xl font-[family-name:var(--font-newsreader)] text-[#171512] tracking-[-0.02em]">
              Plug directly into your AI workflow.
            </h2>
          </div>
          <p className="text-sm text-[#6F6A5B] max-w-md mt-4 md:mt-0 font-[family-name:var(--font-inter)] leading-relaxed">
            Cinnamon runs as an official Model Context Protocol (MCP) server
            over SSE. Copy your configuration into Cursor, Claude, Windsurf,
            Codex, or Antigravity in seconds.
          </p>
        </div>

        {/* Client Selection Tabs */}
        <div className="flex gap-2 mb-6 flex-wrap">
          {(["cursor", "claude", "antigravity"] as const).map((client) => {
            const isSelected = activeClient === client;
            return (
              <button
                key={client}
                onClick={() => {
                  setActiveClient(client);
                  setCopied(false);
                }}
                className={`px-6 py-3 text-xs tracking-[2px] font-semibold uppercase transition-all duration-150 border-[0.8px] ${
                  isSelected
                    ? "bg-[#171512] text-[#FAF6EE] border-[#171512]"
                    : "bg-[#F1ECE0] text-[#171512] border-[#171512]/40 hover:border-[#171512]"
                }`}
              >
                {({ cursor: "CURSOR", claude: "CLAUDE", antigravity: "ANTIGRAVITY" } as const)[client]} SETUP
              </button>
            );
          })}
        </div>

        {/* Code Snippet Box */}
        <div className="border-[0.8px] border-[#171512] bg-[#171512] text-[#F1ECE0] shadow-md relative">
          <div className="px-5 py-3 border-b-[0.8px] border-white/10 flex items-center justify-between text-xs font-mono">
            <span className="text-[#0D8D9C] flex items-center gap-2">
              <span>●</span>
              <span>FILE: {CONFIGS[activeClient].target}</span>
            </span>

            <button
              onClick={handleCopy}
              className="px-3 py-1 bg-white/10 hover:bg-white/20 text-[#FAF6EE] text-xs font-mono uppercase tracking-wider transition-colors"
            >
              {copied ? "✓ COPIED TO CLIPBOARD" : "COPY CONFIGURATION"}
            </button>
          </div>

          <pre className="p-6 text-xs sm:text-sm font-mono overflow-x-auto text-[#D9D2BF] leading-relaxed">
            <code>{CONFIGS[activeClient].snippet}</code>
          </pre>
        </div>

        {/* Instruction Steps */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
          <div className="p-5 border-[0.8px] border-[#171512]/30 bg-[#F1ECE0]">
            <div className="text-[10px] font-mono tracking-widest text-[#E0447D] uppercase mb-1">
              STEP 01
            </div>
            <h4 className="font-semibold text-sm mb-1 text-[#171512]">
              Get Your API Key
            </h4>
            <p className="text-xs text-[#6F6A5B] leading-relaxed">
              Click the Get Free API Key button above to get an instant tenant
              key.
            </p>
          </div>

          <div className="p-5 border-[0.8px] border-[#171512]/30 bg-[#F1ECE0]">
            <div className="text-[10px] font-mono tracking-widest text-[#0D8D9C] uppercase mb-1">
              STEP 02
            </div>
            <h4 className="font-semibold text-sm mb-1 text-[#171512]">
              Paste Configuration
            </h4>
            <p className="text-xs text-[#6F6A5B] leading-relaxed">
              Copy the JSON block above into your IDE settings file and save.
            </p>
          </div>

          <div className="p-5 border-[0.8px] border-[#171512]/30 bg-[#F1ECE0]">
            <div className="text-[10px] font-mono tracking-widest text-[#171512] uppercase mb-1">
              STEP 03
            </div>
            <h4 className="font-semibold text-sm mb-1 text-[#171512]">
              Ask Cursor / Claude
            </h4>
            <p className="text-xs text-[#6F6A5B] leading-relaxed">
              Type: &quot;Find duplicate functions across this project using
              cinnamon.&quot;
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}