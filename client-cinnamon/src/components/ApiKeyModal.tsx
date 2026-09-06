"use client";

import React, { useState, useEffect } from "react";

interface ApiKeyModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialTier?: "starter" | "developer";
}

export default function ApiKeyModal({
  isOpen,
  onClose,
  initialTier = "developer",
}: ApiKeyModalProps) {
  const [step, setStep] = useState<"form" | "result">("form");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [tenantId, setTenantId] = useState("");
  const [apiKey, setApiKey] = useState("");
  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [verified, setVerified] = useState(false);
  const [verifyLoading, setVerifyLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [resendMsg, setResendMsg] = useState("");

  useEffect(() => {
    if (!isOpen) {
      // Reset non-persisted states when closing
      setError("");
      setResendMsg("");
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setResendMsg("");
    setLoading(true);

    try {
      const res = await fetch("/api/tenants", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: name.trim(), email: email.trim() }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(
          data.error || data.message || `Failed to create key (HTTP ${res.status})`
        );
      }

      const generatedKey =
        data.rawApiKey ||
        data.apiKey ||
        data.key ||
        "cin_live_" + Math.random().toString(36).substring(2);

      setApiKey(generatedKey);
      if (data.tenantId) {
        setTenantId(data.tenantId);
      }
      setStep("result");
    } catch (err: unknown) {
      const e = err as Error;
      setError(e.message || "Failed to generate key. Please check your connection.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp.trim()) {
      setError("Please enter the 6-digit verification code.");
      return;
    }

    setError("");
    setResendMsg("");
    setVerifyLoading(true);

    try {
      const res = await fetch("/api/tenants/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          tenantId,
          email: email.trim(),
          code: otp.trim(),
        }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(
          data.error || data.message || "Invalid or expired verification code."
        );
      }

      setVerified(true);
    } catch (err: unknown) {
      const e = err as Error;
      setError(e.message || "Invalid OTP code. Please check your email.");
    } finally {
      setVerifyLoading(false);
    }
  };

  const handleResendOtp = async () => {
    setError("");
    setResendMsg("");
    setResendLoading(true);

    try {
      const res = await fetch("/api/tenants/resend", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          tenantId,
          email: email.trim(),
        }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.error || data.message || "Failed to resend code.");
      }

      setResendMsg("A fresh 6-digit OTP code has been sent to your email!");
    } catch (err: unknown) {
      const e = err as Error;
      setError(e.message || "Failed to resend code. Please try again later.");
    } finally {
      setResendLoading(false);
    }
  };

  const copyKey = () => {
    navigator.clipboard.writeText(apiKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const isDeveloperTier = initialTier === "developer";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#171512]/75 backdrop-blur-sm">
      <div className="bg-[#FAF6EE] border-[1.5px] border-[#171512] w-full max-w-lg shadow-2xl p-6 sm:p-8 relative">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-xs font-mono font-bold text-[#171512] hover:text-[#E0447D] p-1 transition-colors"
        >
          [ESC / CLOSE]
        </button>

        {/* Modal Header */}
        <div className="flex items-center gap-2 mb-2">
          <span className="font-mono text-sm text-[#171512]">+</span>
          <span className="text-[11px] font-semibold tracking-[2px] text-[#E0447D] uppercase">
            {isDeveloperTier
              ? "DEVELOPER TIER · 6-DIGIT EMAIL OTP"
              : "CINNAMON ACCESS KEY"}
          </span>
        </div>

        <h3 className="text-2xl font-[family-name:var(--font-newsreader)] text-[#171512] mb-6">
          {step === "form"
            ? isDeveloperTier
              ? "Unlock Developer Tier (1,000 req/day)"
              : "Generate Your Free Developer Key"
            : "Your API Key is Ready"}
        </h3>

        {step === "form" ? (
          <form onSubmit={handleGenerate} className="space-y-4">
            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-[#6F6A5B] mb-1">
                Developer / Organization Name
              </label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Alex Mercer"
                className="w-full px-4 py-3 border-[0.8px] border-[#171512] bg-[#F1ECE0] text-sm text-[#171512] focus:outline-none focus:border-[#E0447D] font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-[#6F6A5B] mb-1">
                Work or Personal Email
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="alex@company.com"
                className="w-full px-4 py-3 border-[0.8px] border-[#171512] bg-[#F1ECE0] text-sm text-[#171512] focus:outline-none focus:border-[#E0447D] font-mono"
              />
              <p className="text-[11px] text-[#6F6A5B] mt-1 font-sans">
                We will send a real 6-digit OTP code to verify your inbox and
                upgrade your limit to 1,000 requests/day.
              </p>
            </div>

            {error && (
              <div className="text-xs font-mono text-[#E0447D] bg-[#E0447D]/10 p-2 border-[0.8px] border-[#E0447D]/30">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full btn-primary-riso mt-4"
            >
              {loading
                ? "SENDING CODE & GENERATING KEY..."
                : isDeveloperTier
                ? "SEND 6-DIGIT OTP & GET KEY ↗"
                : "GENERATE API KEY ↗"}
            </button>
          </form>
        ) : (
          <div className="space-y-6">
            {/* Generated Key Display */}
            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-[#6F6A5B] mb-1">
                Your Secret API Key (Keep Safe)
              </label>
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  readOnly
                  value={apiKey}
                  className="w-full px-4 py-3 border-[0.8px] border-[#171512] bg-[#171512] text-[#0D8D9C] text-sm font-mono focus:outline-none select-all"
                />
                <button
                  type="button"
                  onClick={copyKey}
                  className="px-4 py-3 bg-[#E0447D] text-white text-xs font-mono uppercase tracking-wider font-semibold hover:bg-[#c73467] transition-colors whitespace-nowrap"
                >
                  {copied ? "COPIED!" : "COPY"}
                </button>
              </div>
            </div>

            {/* Email OTP Verification Card */}
            <div className="p-5 border-[1px] border-[#171512] bg-[#F1ECE0]">
              <div className="flex items-center justify-between mb-3 pb-2 border-b-[0.8px] border-[#171512]/20">
                <span className="text-xs font-bold text-[#171512] uppercase tracking-wider">
                  DEVELOPER VERIFICATION
                </span>
                {verified ? (
                  <span className="text-xs font-mono text-[#10B981] font-bold">
                    ✓ 1,000 REQ / DAY UNLOCKED
                  </span>
                ) : (
                  <span className="text-[11px] font-mono text-[#E0447D] font-bold">
                    CURRENT: 50 REQ/DAY
                  </span>
                )}
              </div>

              {!verified ? (
                <form onSubmit={handleVerifyOtp} className="space-y-3">
                  <p className="text-xs text-[#6F6A5B] leading-relaxed">
                    A 6-digit verification code was emailed to{" "}
                    <strong className="text-[#171512]">{email}</strong>. Enter it
                    below to unlock the full 1,000 requests/day quota:
                  </p>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      maxLength={6}
                      value={otp}
                      onChange={(e) =>
                        setOtp(e.target.value.replace(/[^0-9]/g, ""))
                      }
                      placeholder="000000"
                      className="w-36 px-3 py-2.5 border-[0.8px] border-[#171512] bg-white text-base font-mono text-center tracking-[4px] text-[#171512] focus:outline-none focus:border-[#0D8D9C]"
                    />
                    <button
                      type="submit"
                      disabled={verifyLoading}
                      className="flex-1 px-4 py-2.5 border-[0.8px] border-[#171512] bg-[#171512] text-white text-xs font-mono uppercase tracking-wider hover:bg-[#333] transition-colors"
                    >
                      {verifyLoading ? "VERIFYING..." : "VERIFY OTP ↗"}
                    </button>
                  </div>

                  <div className="flex items-center justify-between pt-2">
                    <button
                      type="button"
                      onClick={handleResendOtp}
                      disabled={resendLoading}
                      className="text-[11px] font-mono text-[#0D8D9C] hover:underline uppercase tracking-wider disabled:opacity-50"
                    >
                      {resendLoading ? "RESENDING..." : "DIDN'T RECEIVE? RESEND CODE ↗"}
                    </button>
                  </div>

                  {resendMsg && (
                    <div className="text-xs font-mono text-[#0D8D9C] bg-[#0D8D9C]/10 p-2 border-[0.8px] border-[#0D8D9C]/30">
                      ✓ {resendMsg}
                    </div>
                  )}

                  {error && (
                    <div className="text-xs font-mono text-[#E0447D] bg-[#E0447D]/10 p-2 border-[0.8px] border-[#E0447D]/30">
                      {error}
                    </div>
                  )}
                </form>
              ) : (
                <div className="space-y-2">
                  <p className="text-xs text-[#10B981] font-mono font-bold">
                    ✓ Account verified! Your daily request limit has been upgraded to 1,000 requests/day.
                  </p>
                  <p className="text-xs text-[#6F6A5B] font-sans">
                    You can now run deep repository cluster scans and continuous MCP vector indexing across your agents.
                  </p>
                </div>
              )}
            </div>

            <button
              onClick={onClose}
              className="w-full py-3 border-[0.8px] border-[#171512] bg-[#F1ECE0] hover:bg-[#E6DFC9] text-xs font-semibold uppercase tracking-[2px] transition-colors"
            >
              DONE / CLOSE
            </button>
          </div>
        )}
      </div>
    </div>
  );
}