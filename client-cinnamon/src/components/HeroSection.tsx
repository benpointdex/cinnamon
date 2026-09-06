"use client";

import React from "react";
import ShaderHeroBackground from "./ShaderHeroBackground";

interface HeroSectionProps {
  onOpenModal: () => void;
}

export default function HeroSection({ onOpenModal }: HeroSectionProps) {
  const scrollToMcp = () => {
    document
      .getElementById("mcp-setup")
      ?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <section
      id="hero"
      className="relative min-h-[100svh] flex flex-col justify-between overflow-hidden p-4 sm:p-5 md:p-6"
      style={{ minHeight: "100svh" }}
    >
      {/* WebGL Halftone Risograph Shader Background */}
      <ShaderHeroBackground />

      {/* ─── Top Plate Header Bar ─── */}
      <header className="relative z-10 w-full flex items-center justify-between pb-4 border-b-[0.8px] border-[#171512]/30">
        {/* Left: Brand + Tagline */}
        <div className="flex items-center gap-4 sm:gap-6 text-[12px] font-semibold tracking-[2.4px] uppercase">
          <span className="font-mono text-base text-[#171512]">+</span>
          <span className="text-[#171512] tracking-[3px]">CINNAMON</span>

        </div>

        {/* Center: Registration Crosshair */}
        <div className="absolute left-1/2 -translate-x-1/2 hidden sm:flex items-center justify-center">
          <div className="reg-target" title="Registration Target"></div>
        </div>

        {/* Right: Ink Swatches */}
        <div className="flex items-center gap-3 sm:gap-4 text-[12px] font-semibold tracking-[2.4px] uppercase">
          <div
            className="flex items-center gap-1.5"
            title="Ink Calibration Plates"
          >
            <span className="w-3.5 h-3.5 bg-[#0D8D9C] inline-block border-[0.8px] border-[#171512]"></span>
            <span className="w-3.5 h-3.5 bg-[#E0447D] inline-block border-[0.8px] border-[#171512]"></span>
          </div>
          <span className="font-mono text-base text-[#171512]">+</span>
        </div>
      </header>

      {/* ─── Hero Centerpiece ─── */}
      <div className="relative z-10 w-full max-w-7xl mx-auto my-auto py-8 sm:py-12 md:py-16 flex flex-col lg:flex-row lg:items-end justify-between gap-10">
        {/* Left Column: Display Heading + Body */}
        <div className="max-w-3xl">
          {/* Overline Kicker — label-md */}
          <div className="inline-block mb-4">
            <span className="text-[12px] font-semibold tracking-[2.4px] text-[#E0447D] uppercase leading-[16px]">
              MODEL CONTEXT PROTOCOL · V1.0
            </span>
            <div className="h-[1.5px] w-28 bg-[#E0447D] mt-1.5"></div>
          </div>

          {/* Display Headline — display-lg inspired, responsive */}
          <h1
            className="text-[#171512] uppercase leading-none tracking-[-0.05em] font-semibold mb-6"
            style={{
              fontFamily: "var(--font-inter), Inter, sans-serif",
              fontSize: "clamp(48px, 10vw, 120px)",
              lineHeight: "1",
            }}
          >
            Stop rewriting
            <br />
            code.
          </h1>

          {/* Accent Bar */}
          <div className="h-[3px] w-12 bg-[#E0447D] mb-5"></div>

          {/* Body — body-md: Inter 16px/24px weight 400 */}
          <p
            className="text-[#6F6A5B] max-w-lg"
            style={{
              fontFamily: "var(--font-inter), Inter, sans-serif",
              fontSize: "16px",
              fontWeight: 400,
              lineHeight: "24px",
            }}
          >
            We detect duplicate code before it runs because a third helper
            function has never once paid for itself.
          </p>
        </div>

        {/* Right Column: CTAs */}
        <div className="flex flex-col gap-3 w-full sm:w-auto lg:min-w-[260px]">
          <button onClick={onOpenModal} className="btn-primary-riso">
            <span>GET FREE API KEY</span>
            <span className="text-base">↗</span>
          </button>

          <button onClick={scrollToMcp} className="btn-secondary-riso">
            <span>ADD TO YOUR AGENT</span>

          </button>

          {/* Status */}
          <div className="text-[12px] tracking-[2.4px] uppercase font-semibold leading-[16px] text-[#6F6A5B] flex items-center gap-2 pt-1">


          </div>
        </div>
      </div>

      {/* ─── Bottom Plate Registration Footer ─── */}
      <footer className="relative z-10 w-full flex items-center justify-between pt-4 border-t-[0.8px] border-dashed border-[#171512]/30 text-[12px] font-semibold tracking-[2.4px] uppercase leading-[16px]">
        <div className="flex items-center gap-4 sm:gap-6">
          <span className="font-mono text-base text-[#171512]">+</span>
          <span className="text-[#E0447D]">
            TREE-SITTER &amp; PGVECTOR
          </span>
        </div>

        <div className="absolute left-1/2 -translate-x-1/2 hidden sm:flex">
          <div className="reg-target" title="Registration Target"></div>
        </div>

        <div className="flex items-center gap-4 sm:gap-6">
          <span className="text-[#171512]">OPEN SOURCE</span>
          <span className="font-mono text-base text-[#171512]">+</span>
        </div>
      </footer>
    </section>
  );
}