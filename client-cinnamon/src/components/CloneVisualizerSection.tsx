"use client";

import React, { useState } from "react";

interface CloneExample {
  title: string;
  badge: string;
  similarity: string;
  plateColor: string;
  description: string;
  codeOriginal: string;
  codeDuplicate: string;
}

const CLONE_EXAMPLES: Record<string, CloneExample> = {
  type1: {
    title: "Type 1: Exact Clone",
    badge: "SHA-256 HASH MATCH",
    similarity: "100%",
    plateColor: "#0D8D9C",
    description:
      "Identical functions copied and pasted across packages. Detected in sub-milliseconds via SHA-256 AST body hashing without hitting vector inference.",
    codeOriginal: `// auth/token_service.ts
export function generateAuthToken(userId: string, salt: string): string {
  const secret = process.env.JWT_SECRET || "default_dev_key";
  const payload = Buffer.from(\`\${userId}:\${salt}\`);
  return crypto.createHmac("sha256", secret)
               .update(payload)
               .digest("hex");
}`,
    codeDuplicate: `// api/handlers/session.ts
export function generateAuthToken(userId: string, salt: string): string {
  const secret = process.env.JWT_SECRET || "default_dev_key";
  const payload = Buffer.from(\`\${userId}:\${salt}\`);
  return crypto.createHmac("sha256", secret)
               .update(payload)
               .digest("hex");
}`,
  },
  type2: {
    title: "Type 2: Renamed Identifiers",
    badge: "AST NORMALIZED MATCH",
    similarity: "96.8%",
    plateColor: "#E0447D",
    description:
      "Variables, parameters, and method names were modified to fit a new context, but the internal syntax tree and logical structure are identical.",
    codeOriginal: `// repositories/UserRepository.java
public Optional<User> findActiveById(UUID userId) {
    return jpaQueryFactory
        .selectFrom(user)
        .where(user.id.eq(userId)
            .and(user.status.eq(Status.ACTIVE)))
        .fetchFirst();
}`,
    codeDuplicate: `// services/AccountFinder.java
public Optional<Account> lookupExistingAccount(UUID accountId) {
    return queryBuilder
        .selectFrom(account)
        .where(account.id.eq(accountId)
            .and(account.state.eq(State.ACTIVE)))
        .fetchFirst();
}`,
  },
  type3: {
    title: "Type 3: Modified Syntax & Control Flow",
    badge: "SYNTACTIC VARIATION",
    similarity: "92.4%",
    plateColor: "#0D8D9C",
    description:
      "Statements were reordered, extra diagnostic logging was added, or minor error branches were altered, yet the core transformation remains duplicated.",
    codeOriginal: `# utils/retry.py
def execute_with_backoff(action, max_retries=3, delay=1.0):
    for attempt in range(max_retries):
        try:
            return action()
        except TransientNetworkError as err:
            time.sleep(delay * (2 ** attempt))
    raise MaxRetriesExceeded("Action failed after retries")`,
    codeDuplicate: `# client/http_worker.py
def retry_http_call(task_fn, limit=3, initial_wait=1.0):
    logger.info("Initiating request with exponential backoff")
    for step in range(limit):
        try:
            result = task_fn()
            logger.debug(f"Succeeded on attempt {step}")
            return result
        except ConnectionError as ex:
            backoff = initial_wait * (2 ** step)
            logger.warn(f"Transient error: {ex}. Sleeping {backoff}s")
            time.sleep(backoff)
    raise ExecutionFailed("Exceeded attempt limit")`,
  },
  type4: {
    title: "Type 4: Semantic Equivalence",
    badge: "384-DIM ONNX VECTOR MATCH",
    similarity: "89.5%",
    plateColor: "#E0447D",
    description:
      "Completely different algorithmic implementation solving the exact same business logic (e.g. imperative procedural loop vs. functional stream reduction).",
    codeOriginal: `// math/aggregator.ts (Imperative)
export function calculateTotals(items: CartItem[]): OrderSummary {
  let subtotal = 0;
  let tax = 0;
  for (let i = 0; i < items.length; i++) {
    if (items[i].active) {
      subtotal += items[i].price * items[i].qty;
      tax += items[i].taxRate * items[i].price;
    }
  }
  return { subtotal, tax, grandTotal: subtotal + tax };
}`,
    codeDuplicate: `// checkout/summary.ts (Functional Stream)
export const computeCartSummary = (cart: CartItem[]): OrderSummary => {
  return cart
    .filter(item => item.active)
    .reduce((acc, { price, qty, taxRate }) => {
      const itemSub = price * qty;
      const itemTax = price * taxRate;
      acc.subtotal += itemSub;
      acc.tax += itemTax;
      acc.grandTotal = acc.subtotal + acc.tax;
      return acc;
    }, { subtotal: 0, tax: 0, grandTotal: 0 });
};`,
  },
};

export default function CloneVisualizerSection() {
  const [activeTab, setActiveTab] = useState<string>("type4");
  const activeExample = CLONE_EXAMPLES[activeTab];

  return (
    <section
      id="visualizer"
      className="relative px-4 sm:px-8 md:px-12 py-20 border-b-[0.8px] border-[#171512] bg-[#FAF6EE]"
    >
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-12 pb-6 border-b-[0.8px] border-[#171512]/30">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-sm text-[#171512]">+</span>
              <span className="text-xs font-semibold tracking-[2.4px] text-[#E0447D] uppercase">
                INTERACTIVE DEMO · FOUR CLONE PLATES
              </span>
            </div>
            <h2 className="text-3xl sm:text-5xl font-[family-name:var(--font-newsreader)] text-[#171512] tracking-[-0.02em]">
              Detecting clones traditional linters completely miss.
            </h2>
          </div>
          <p className="text-sm text-[#6F6A5B] max-w-md mt-4 md:mt-0 font-[family-name:var(--font-inter)] leading-relaxed">
            From identical copy-pastes to advanced semantic clones written in
            completely different paradigms, Cinnamon surfaces duplicates
            directly inside your editor.
          </p>
        </div>

        {/* Tab Selection Bar (Letterpress Style) */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mb-6">
          {(["type1", "type2", "type3", "type4"] as const).map((tabKey) => {
            const item = CLONE_EXAMPLES[tabKey];
            const isSelected = activeTab === tabKey;
            return (
              <button
                key={tabKey}
                onClick={() => setActiveTab(tabKey)}
                className={`text-left p-4 transition-all duration-200 border-[0.8px] ${
                  isSelected
                    ? "bg-[#171512] text-[#FAF6EE] border-[#171512]"
                    : "bg-[#F1ECE0] text-[#171512] border-[#171512]/40 hover:border-[#171512]"
                }`}
              >
                <div className="text-[10px] uppercase tracking-[2px] opacity-75 font-mono mb-1">
                  PLATE {tabKey.replace("type", "0")}
                </div>
                <div className="text-sm font-semibold tracking-wide flex items-center justify-between">
                  <span>{item.title}</span>
                  {isSelected && (
                    <span className="text-[#E0447D]">●</span>
                  )}
                </div>
              </button>
            );
          })}
        </div>

        {/* Selected Clone Details Banner */}
        <div className="p-5 border-[0.8px] border-[#171512] bg-[#F1ECE0] mb-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span
              className="text-xs uppercase tracking-[2px] font-semibold px-2.5 py-1 text-white"
              style={{ backgroundColor: activeExample.plateColor }}
            >
              {activeExample.badge}
            </span>
            <span className="text-xs text-[#6F6A5B] font-[family-name:var(--font-inter)]">
              {activeExample.description}
            </span>
          </div>

          <div className="flex items-center gap-2 font-mono text-sm self-end sm:self-auto">
            <span className="text-xs text-[#6F6A5B] tracking-wider uppercase">
              Cosine Score:
            </span>
            <span
              className="px-2 py-0.5 font-bold text-xs text-white"
              style={{ backgroundColor: activeExample.plateColor }}
            >
              {activeExample.similarity}
            </span>
          </div>
        </div>

        {/* Side-by-Side Code Diff Display */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Original Code Plate */}
          <div className="border-[0.8px] border-[#171512] bg-[#171512] text-[#FAF6EE] shadow-md flex flex-col">
            <div className="px-4 py-3 border-b-[0.8px] border-white/10 flex items-center justify-between text-xs font-mono tracking-wider">
              <span className="text-[#0D8D9C]">ORIGINAL IMPLEMENTATION</span>
              <span className="text-white/40">BRANCH: MAIN</span>
            </div>
            <pre className="p-5 text-xs sm:text-sm font-mono overflow-x-auto leading-relaxed text-[#D9D2BF] flex-1">
              <code>{activeExample.codeOriginal}</code>
            </pre>
          </div>

          {/* Duplicate Code Plate */}
          <div className="border-[0.8px] border-[#171512] bg-[#171512] text-[#FAF6EE] shadow-md flex flex-col">
            <div className="px-4 py-3 border-b-[0.8px] border-white/10 flex items-center justify-between text-xs font-mono tracking-wider">
              <span className="text-[#E0447D]">
                DUPLICATE FUNCTION DETECTED
              </span>
              <span className="text-[#E0447D] font-bold">
                MATCH: {activeExample.similarity}
              </span>
            </div>
            <pre className="p-5 text-xs sm:text-sm font-mono overflow-x-auto leading-relaxed text-[#D9D2BF] flex-1">
              <code>{activeExample.codeDuplicate}</code>
            </pre>
          </div>
        </div>
      </div>
    </section>
  );
}