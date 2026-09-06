"use client";

import React, { useState } from "react";
import HeroSection from "@/components/HeroSection";
import CloneVisualizerSection from "@/components/CloneVisualizerSection";
import PipelineSection from "@/components/PipelineSection";
import McpSetupSection from "@/components/McpSetupSection";
import EcosystemSection from "@/components/EcosystemSection";
import PricingSection from "@/components/PricingSection";
import Footer from "@/components/Footer";
import ApiKeyModal from "@/components/ApiKeyModal";

export default function Home() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalTier, setModalTier] = useState<"starter" | "developer">("developer");

  const openModal = (tier: "starter" | "developer" = "developer") => {
    setModalTier(tier);
    setIsModalOpen(true);
  };

  return (
    <main>
      {/* Section 1: Hero with WebGL Shader Gradient Background */}
      <HeroSection onOpenModal={() => openModal("developer")} />

      {/* Section 2: Interactive Duplicate Code Clone Visualizer */}
      <CloneVisualizerSection />

      {/* Section 3: 4-Stage Cascading Detection Pipeline */}
      <PipelineSection />

      {/* Section 4: 1-Click MCP Setup for Cursor / Claude / Windsurf */}
      <McpSetupSection />

      {/* Section 5: Supported Languages & MCP Tools Ecosystem */}
      <EcosystemSection />

      {/* Section 6: Developer Pricing Tiers */}
      <PricingSection onOpenModal={openModal} />

      {/* Footer with Registration Marks */}
      <Footer />

      {/* API Key Generation Modal Overlay */}
      <ApiKeyModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        initialTier={modalTier}
      />
    </main>
  );
}
