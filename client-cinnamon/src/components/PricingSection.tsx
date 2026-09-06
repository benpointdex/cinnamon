"use client";

import React from "react";

interface PricingSectionProps {
  onOpenModal: (tier?: "starter" | "developer") => void;
}

export default function PricingSection({ onOpenModal }: PricingSectionProps) {
  return (
    <section
      id="pricing"
      className="relative px-4 sm:px-8 md:px-12 py-20 border-b-[0.8px] border-[#171512] bg-[#FAF6EE]"
    >
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-16 pb-6 border-b-[0.8px] border-[#171512]/30">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-sm text-[#171512]">+</span>
              <span className="text-xs font-semibold tracking-[2.4px] text-[#E0447D] uppercase">
                TRANSPARENT DEVELOPER TIERS
              </span>
            </div>
            <h2 className="text-3xl sm:text-5xl font-[family-name:var(--font-newsreader)] text-[#171512] tracking-[-0.02em]">
              Free forever for individual builders.
            </h2>
          </div>
          <p className="text-sm text-[#6F6A5B] max-w-md mt-4 md:mt-0 font-[family-name:var(--font-inter)] leading-relaxed">
            No credit cards, no trial expirations. Get an instant API key with
            50 calls/day, and verify your email to unlock 1,000 requests/day at
            zero cost.
          </p>
        </div>

        {/* 3 Pricing Plates */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* 1. Starter Tier */}
          <div className="border-[0.8px] border-[#171512] bg-[#F1ECE0] p-8 flex flex-col justify-between relative">
            <div>
              <div className="text-[10px] font-mono tracking-widest text-[#6F6A5B] uppercase mb-2">
                TIER 01
              </div>
              <h3 className="text-2xl font-bold text-[#171512] mb-1">
                Starter
              </h3>
              <div className="text-xs text-[#6F6A5B] mb-6">
                Instant anonymous development
              </div>

              <div className="text-4xl font-bold font-mono text-[#171512] mb-6">
                $0{" "}
                <span className="text-xs font-normal text-[#6F6A5B] tracking-normal font-sans">
                  / forever
                </span>
              </div>

              <ul className="space-y-3 text-xs text-[#171512] border-t-[0.8px] border-[#171512]/20 pt-6 mb-8 font-mono">
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>
                    <strong>50 requests</strong> / day
                  </span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>Instant API key generation</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>All 7 MCP tools unlocked</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>No credit card required</span>
                </li>
              </ul>
            </div>

            <button
              onClick={() => onOpenModal("starter")}
              className="w-full py-3.5 border-[0.8px] border-[#171512] bg-[#F1ECE0] hover:bg-[#E6DFC9] text-xs font-semibold uppercase tracking-[2px] transition-colors"
            >
              GENERATE STARTER KEY ↗
            </button>
          </div>

          {/* 2. Verified Developer Tier (Hero Card) */}
          <div className="border-[1.5px] border-[#E0447D] bg-[#171512] text-[#FAF6EE] p-8 flex flex-col justify-between relative shadow-lg">
            <div className="absolute -top-3 right-6 bg-[#E0447D] text-white text-[10px] font-mono font-bold tracking-widest uppercase px-3 py-1">
              RECOMMENDED
            </div>

            <div>
              <div className="text-[10px] font-mono tracking-widest text-[#E0447D] uppercase mb-2">
                TIER 02
              </div>
              <h3 className="text-2xl font-bold text-white mb-1">Developer</h3>
              <div className="text-xs text-[#D9D2BF] mb-6">
                Verified with 6-digit email OTP
              </div>

              <div className="text-4xl font-bold font-mono text-white mb-6">
                $0{" "}
                <span className="text-xs font-normal text-[#D9D2BF] tracking-normal font-sans">
                  / free with email
                </span>
              </div>

              <ul className="space-y-3 text-xs text-[#FAF6EE] border-t-[0.8px] border-white/20 pt-6 mb-8 font-mono">
                <li className="flex items-center gap-2">
                  <span className="text-[#E0447D]">✓</span>
                  <span>
                    <strong>1,000 requests</strong> / day
                  </span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#E0447D]">✓</span>
                  <span>Full repository scan priority</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#E0447D]">✓</span>
                  <span>GitHub automated clone &amp; ingest</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#E0447D]">✓</span>
                  <span>Persistent Supabase vector storage</span>
                </li>
              </ul>
            </div>

            <button
              onClick={() => onOpenModal("developer")}
              className="w-full py-3.5 bg-[#E0447D] hover:bg-[#c73467] text-white text-xs font-semibold uppercase tracking-[2px] transition-colors"
            >
              VERIFY EMAIL &amp; UNLOCK 1,000 REQ ↗
            </button>
          </div>

          {/* 3. Self-Hosted / Open Source */}
          <div className="border-[0.8px] border-[#171512] bg-[#F1ECE0] p-8 flex flex-col justify-between relative">
            <div>
              <div className="text-[10px] font-mono tracking-widest text-[#6F6A5B] uppercase mb-2">
                TIER 03
              </div>
              <h3 className="text-2xl font-bold text-[#171512] mb-1">
                Self-Hosted
              </h3>
              <div className="text-xs text-[#6F6A5B] mb-6">
                Run on your private infrastructure
              </div>

              <div className="text-4xl font-bold font-mono text-[#171512] mb-6">
                Open{" "}
                <span className="text-xs font-normal text-[#6F6A5B] tracking-normal font-sans">
                  Source
                </span>
              </div>

              <ul className="space-y-3 text-xs text-[#171512] border-t-[0.8px] border-[#171512]/20 pt-6 mb-8 font-mono">
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>
                    <strong>Unlimited</strong> local requests
                  </span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>Docker Compose (app + pgvector)</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>Air-gapped ONNX embeddings</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#0D8D9C]">✓</span>
                  <span>Zero data leaves your VPC</span>
                </li>
              </ul>
            </div>

            <a
              href="https://github.com/benpointdex/cinnamon"
              target="_blank"
              rel="noreferrer"
              className="w-full py-3.5 border-[0.8px] border-[#171512] bg-[#F1ECE0] hover:bg-[#E6DFC9] text-xs font-semibold uppercase tracking-[2px] text-center transition-colors inline-block"
            >
              VIEW ON GITHUB ↗
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}