# Product Requirements Document (PRD): DejaCode Landing Page & Developer Portal

**Product Name:** DejaCode (Backend: Cinnamon)  
**Document Version:** 1.0.0  
**Target:** Single-Page Marketing & Developer Website  
**Status:** Approved for Build  

---

## 1. Executive Summary & Vision

**DejaCode** is an AI-native Model Context Protocol (MCP) service designed to eliminate duplicate logic, code bloat, and redundant utility functions across modern codebases. Powered by Tree-Sitter AST parsers, ONNX vector embeddings (`all-MiniLM-L6-v2`), and Supabase `pgvector`, DejaCode identifies:
- **Type 1:** Exact duplicate code blocks (SHA-256 hash detection).
- **Type 2:** Renamed identifiers and parameters.
- **Type 3:** Modified syntax, reordered statements, and added error checks.
- **Type 4:** Semantic equivalents (different algorithms performing the exact same business logic).

### Website Objective
A high-converting, single-page developer portal that explains the product in under 5 seconds, provides interactive duplicate detection demos, offers 1-click MCP setup for Cursor/Claude/Windsurf, and allows instant API key generation with email verification.

---

## 2. Target Audience & Personas

| Persona | Core Pain Point | Primary Action on Website |
| :--- | :--- | :--- |
| **AI Pair Programmers** (Cursor / Claude users) | AI assistants hallucinate and generate duplicate helper functions that already exist elsewhere in the repo. | Copy MCP server JSON config into claude_desktop_config.json or .cursor/mcp.json. |
| **Engineering Leads & Architects** | Sprawling monorepos suffering from copy-paste rot and inconsistent business logic. | Evaluate semantic benchmarks, API rate limits, and language coverage. |
| **Open Source Maintainers** | Pull requests that reinvent existing internal library functions. | Integrate GitHub repository scanning via CI/CD or MCP. |

---

## 3. Page Architecture & Section Breakdown

`mermaid
graph TD
    A[Navbar & Live Health Status] --> B[Hero: Value Proposition & Terminal Preview]
    B --> C[Interactive Side-by-Side Clone Visualizer]
    C --> D[How It Works: 4-Stage Detection Pipeline]
    D --> E[1-Click MCP Setup Guide: Cursor / Claude / Windsurf]
    E --> F[Feature & Language Ecosystem Grid: 12+ Languages]
    F --> G[Developer Pricing: Free vs. Verified 1,000 req/day]
    G --> H[Footer & Instant API Key Generation Modal]
`

---

### Section 1: Navbar & Hero
* **Navbar:**
  * **Brand Logo:** DejaCode (with subtle cinnamon-amber glow).
  * **Navigation Links:** *Features*, *How It Works*, *MCP Setup*, *Pricing*, *Docs*.
  * **Live Uptime Badge:** ● 99.9% Live (points to /actuator/health).
  * **CTA Button:** Get API Key (triggers signup modal).
* **Hero Content:**
  * **Badge:** 🚀 Built on the Model Context Protocol (MCP)
  * **Headline:** *Stop Writing Code You Already Wrote.*
  * **Subheadline:** *The AI-native MCP server that detects exact and semantic duplicate functions in real-time — directly inside Cursor, Claude, and your terminal.*
  * **Action Buttons:**
    * Primary: Get Free API Key (50 Free Calls/Day) $\rightarrow$ Opens API key generator modal.
    * Secondary: Add to Cursor / Claude $\rightarrow$ Smooth scroll to MCP installation config.
  * **Hero Visual:** Interactive simulated IDE Terminal preview showing a live repository scan with AST function extraction and a 96.4% semantic similarity match found in utils/formatting.ts.

---

### Section 2: Interactive Duplicate Code Visualizer
An interactive before-and-after code viewer demonstrating the 4 clone types DejaCode detects:
1. **Type 1 (Exact Match):** Identical code blocks (SHA-256 hash detection).
2. **Type 2 (Renamed Identifiers):** Variables and parameters renamed (userList $\rightarrow$ ccountCollection).
3. **Type 3 (Reordered Statements):** Extra logging, error handling, or swapped statement order.
4. **Type 4 (Semantic Equivalence):** Different algorithms doing the exact same task (e.g., iterative or-loop vs. functional stream().filter().map()).
* **Interactive Element:** Tab switchers with syntax highlighting and live cosine similarity confidence scores.

---

### Section 3: The 4-Stage Cascading Pipeline
Visual breakdown showing how DejaCode delivers sub-second responses while running deep vector searches:
1. **Tree-Sitter AST Parsing:** Strips comments, normalizes syntax, and extracts function boundaries across 12+ languages.
2. **SHA-256 Exact Hash Filter:** Microsecond exact match check before computing expensive vectors.
3. **ONNX Vector Embedding:** Generates 384-dimensional dense vectors asynchronously in batches of 50.
4. **pgvector Cosine Clustering:** Executes HNSW vector index lookups and self-joins for fast nearest-neighbor retrieval.

---

### Section 4: 1-Click MCP Setup Guide
Tabbed copy-paste configuration snippets for every major AI coding tool:
* **Cursor:** Configuration for .cursor/mcp.json.
* **Claude Desktop:** Configuration for claude_desktop_config.json:
  `json
  {
    mcpServers: {
      dejacode: {
        command: npx,
        args: [-y, mcp-remote, https://cinnamon-l7jf.onrender.com/sse],
        env: { DEJACODE_API_KEY: YOUR_API_KEY }
      }
    }
  }
  `
* **Windsurf / Antigravity:** Remote SSE endpoint and tool schemas.

---

### Section 5: Supported Languages & MCP Tools Grid
* **12+ Supported Languages:** TypeScript, JavaScript, Python, Java, Go, Rust, Dart, C, C++, C#, Kotlin, PHP, Ruby.
* **The 7 Native MCP Tools:**
  1. scan_repository_duplicates (Full repository cluster scan)
  2. ind_similar_functions (Look up duplicates for a code snippet)
  3. ingest_files (Direct multi-file vectorization)
  4. ingest_github_repository (Clone and vectorize any public repo)
  5. get_duplicate_report (Formatted markdown analysis)
  6. get_ingestion_status (Async job progress tracking)
  7. ecord_duplicate (Manual review feedback loop)

---

### Section 6: Developer Pricing & Tiers
Simple, transparent  pricing model:

| Plan | Daily Limit | Verification Required | Cost |
| :--- | :--- | :--- | :--- |
| **Starter (Unverified)** | 50 requests / day | None (Instant API key generation) | ** Free** |
| **Developer (Verified)** | 1,000 requests / day | 6-Digit Email OTP verification | ** Free** |
| **Self-Hosted / Team** | Unlimited | Docker Compose (docker compose up) | **Open Source** |

---

### Section 7: Instant API Key Generation Modal
* **2-Step Flow:**
  1. Input: **Name** + **Email** $\rightarrow$ Click Generate API Key.
  2. Output: Generates cin_live_... API key immediately with a 1-click copy button.
  3. Verification Prompt: *We sent a 6-digit OTP code to your email. Enter it below to unlock 1,000 requests/day*.
* **Live Integration:** Calls your live Render backend (POST /api/tenants and POST /api/tenants/verify).

---

## 4. Visual Design System & Aesthetics

* **Theme:** Deep Engineering Dark Mode (Sleek, modern, high-contrast).
* **Color Palette:**
  * Background: #0A0D14 (Deep obsidian)
  * Surface Cards: #111622 (Card surface with subtle gba(255,255,255,0.06) border)
  * Primary Accent: #F59E0B (Warm Cinnamon Amber)
  * Secondary Accent: #06B6D4 (Electric Vector Cyan)
  * Success/Uptime: #10B981 (Emerald Green)
* **Typography:**
  * Headings: Plus Jakarta Sans or Outfit
  * Body: Inter
  * Code/Telemetry: JetBrains Mono
* **Micro-Interactions:**
  * Glowing gradient hover borders on cards.
  * 1-click clipboard copy buttons with instant checkmark feedback.
  * Smooth tab switching on the code visualizer.

---

## 5. Technical Stack Recommendation

* **Architecture:** Static Single-Page Application (Zero server cost, instant load).
* **Technologies:** Modern HTML5 / CSS3 / Vanilla JS (or lightweight Vite).
* **Hosting:** Cloudflare Pages or Render Static Site (, free global CDN, unlimited bandwidth).
* **Backend API:** Connects directly to https://cinnamon-l7jf.onrender.com for tenant creation and verification.