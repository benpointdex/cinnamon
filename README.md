<div align="center">

# Cinnamon

### AI Code Deduplication & Semantic Reuse Engine

Stop AI coding agents from reinventing logic that already exists in your codebase.

[![Live Portal](https://img.shields.io/badge/Live%20Portal-cinnamon--mcp.vercel.app-0D8D9C?style=for-the-badge)](https://cinnamon-mcp.vercel.app/)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI MCP](https://img.shields.io/badge/Protocol-Model%20Context%20Protocol-412991?style=for-the-badge)](https://modelcontextprotocol.io/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%20%2B%20pgvector-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://github.com/pgvector/pgvector)
[![License: MIT](https://img.shields.io/badge/License-MIT-gray.svg?style=for-the-badge)](LICENSE)

<br/>

<a href="https://cinnamon-mcp.vercel.app/">
  <img src="assets/cinnamon-preview.png" alt="Cinnamon Developer Portal" width="850" style="border-radius: 8px; border: 1px solid #d0d7de; box-shadow: 0 8px 24px rgba(149, 157, 165, 0.2);" />
</a>

<br/><br/>

[Live Portal](https://cinnamon-mcp.vercel.app/) • [Overview](#overview) • [How It Works](#how-it-works) • [Capabilities](#key-capabilities) • [Architecture](#architecture) • [MCP Tools](#mcp-tools) • [Quickstart](#quickstart) • [Privacy](#privacy--compliance)

</div>

---

## Overview

AI coding assistants (Claude Code, Cursor, Windsurf) generate code rapidly, but lack global awareness across large repositories. When prompted to implement a feature, agents frequently author new helper functions, validators, or route handlers without realizing equivalent logic already exists in the project.

### The Impact

| Without Cinnamon | With Cinnamon |
|:---|:---|
| **Blind Generation** — Agents recreate existing utility logic under arbitrary variable names. | **Proactive Reuse** — Agent queries Cinnamon before writing code and imports existing functions. |
| **Silent Code Bloat** — Monorepos accumulate dozens of slightly different helper implementations. | **Graph Clustering** — Whole-repo scans detect and group duplicate clusters with line savings. |
| **Vulnerability Sprawl** — Security patches applied in one file miss duplicated variants elsewhere. | **Centralized Logic** — Single canonical implementation maintained across services and modules. |
| **Token Inefficiency** — Agents waste context window and output tokens re-deriving logic. | **Sub-millisecond Checks** — Instant Tier-1 hash matches and local vector similarity. |

---

## How It Works

Cinnamon is a multi-tenant **Model Context Protocol (MCP)** server that hooks directly into an agent's reasoning loop. Before generating new code, the agent asks Cinnamon:
> *"Does a function with this behavior already exist in the repository?"*

Cinnamon evaluates incoming code through a two-tier cascade:

```text
Incoming Code Probe
       │
       ▼
[ Tier 1: AST Normalization & Hash Match ]
  • Tree-sitter parses Concrete Syntax Tree (9 languages)
  • Strips comments, canonicalizes local identifiers to VAR_1, VAR_2
  • Computes SHA-256 hash of canonical token stream
       │
       ├─► Match Found? ──► Return 1.0 confidence match in < 1ms
       │
       ▼ (No exact match)
[ Tier 2: In-Process ONNX Embedding + pgvector HNSW ]
  • Generates 384-dimensional dense vector via local quantized transformer
  • Queries PostgreSQL HNSW index using cosine distance (<=>)
  • Filters candidates with similarity >= 0.85
       │
       └─► Return candidates with line savings & file paths in ~15ms
```

If a match is found, the agent simply imports the existing implementation.

---

## Key Capabilities

### 1. Model Context Protocol (MCP) Native
Exposes 7 specialized tools over Streamable HTTP. Compatible out-of-the-box with Cursor, Claude Code, Windsurf, and custom agent runtimes.

### 2. Multi-Language Tree-sitter Parsing
Deep syntactic analysis across **9 programming languages**:
`Java` • `TypeScript` • `JavaScript` • `Python` • `Go` • `C#` • `Dart` • `Kotlin` • `Rust` • `PHP`

### 3. Canonical AST Normalization
A two-pass symbol-table normalizer removes comments, normalizes signatures to `FUNC`, and maps variables to canonical tokens (`VAR_n`). Catches copy-pasted logic even when every variable and parameter name has been renamed.

### 4. Local In-Process Vector Embeddings
Runs a 22MB quantized INT8 `all-MiniLM-L6-v2` model directly inside the Java process via Deep Java Library (DJL) and ONNX Runtime. Generates 384-dimensional vectors on the CPU in 1–3ms with zero Python runtime and zero third-party API costs.

### 5. Whole-Repository Graph Clustering
An in-database vector self-join query ($< 50\text{ms}$) coupled with Disjoint Set Union (Union-Find) with path compression groups pairwise duplicates into $N$-way clusters, calculating total lines of code saved and synthesizing refactoring recommendations (`EXTRACT_SHARED_UTIL`, `MERGE_ENDPOINTS`).

### 6. Automated Git Ingestion
Clones and indexes public and private repositories in one click via Eclipse JGit (`depth=1`), with smart filtering that automatically prunes vendor folders, build outputs, test files, and trivial getters/setters.

### 7. Zero-Source Storage Policy
Source code is analyzed in volatile memory and discarded immediately. PostgreSQL stores only cryptographic hashes, dense vector embeddings, and file path pointers—ensuring complete enterprise code privacy.

### 8. Multi-Tenant Isolation & Security
Enforces SHA-256 hashed API keys, `ThreadLocal` tenant context isolation, SLF4J MDC distributed tracing, and Bucket4j token-bucket rate limiting with dynamic HTTP 429 `Retry-After` headers.

---

## Architecture

```text
 ┌────────────────────────────────────────────────────────┐
 │        AI Coding Agents (Claude Code, Cursor, etc.)    │
 └───────────────────────────┬────────────────────────────┘
                             │ Streamable HTTP (MCP Protocol)
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │             Cinnamon Backend (Spring Boot 4)           │
 │                                                        │
 │  [Security & Auth]                                     │
 │  ├── ApiKeyAuthFilter (SHA-256 Hashing, ThreadLocal)   │
 │  └── RateLimitFilter (Bucket4j Token Bucket Quotas)    │
 │                                                        │
 │  [Parsing & Normalization Engine]                      │
 │  ├── Tree-sitter AST Parser (9 Language Grammars)      │
 │  ├── IdentifierNormalizer (Symbol Table + Canonical)   │
 │  └── In-Process DJL ONNX Engine (all-MiniLM-L6-v2)     │
 │                                                        │
 │  [Graph Analytics]                                     │
 │  └── Disjoint Set Union (Union-Find + Path Compression)│
 └───────────────────────────┬────────────────────────────┘
                             │ JDBC / Cosine Distance (<=>)
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │            PostgreSQL 16 + pgvector (HNSW)             │
 │                                                        │
 │  ├── tenants (Quotas, verification, hashed credentials)│
 │  ├── code_units (HNSW cosine index, 384-d vectors)     │
 │  ├── duplicate_findings (Confirmed duplicate logs)     │
 │  └── ingestion_jobs (Async job tracking)               │
 └────────────────────────────────────────────────────────┘
```

---

## MCP Tools

| Tool Name | Purpose | Key Parameters |
|:---|:---|:---|
| `find_similar_functions` | Checks if a proposed function already exists prior to code generation. | `repository`, `sourceCode`, `filePath` |
| `scan_repository_duplicates` | Whole-repo vector self-join scan returning $N$-way clusters and line savings. | `repository`, `minSimilarity`, `pathPrefix`, `limit` |
| `ingest_github_repository` | Clones (depth 1) and indexes a public/private Git repository on the server. | `repoUrl`, `repository`, `branch`, `githubToken`, `sourceDirs` |
| `ingest_files` | Asynchronously indexes an array of source files supplied by an IDE agent. | `repository`, `files: [{path, content}]` |
| `get_ingestion_status` | Polling endpoint for real-time progress of an ingestion job. | `jobId` |
| `record_duplicate` | Records an agent-verified duplicate finding with technical reasoning. | `repository`, `newFilePath`, `matchedFilePath`, `similarityScore`, `reasoning` |
| `get_duplicate_report` | Summarizes repository duplicate counts, total lines affected, and top offending files. | `repository`, `pathPrefix`, `minSimilarity` |

---

## Quickstart

### 1. Connect Your Agent

Add Cinnamon to your agent's MCP configuration (`mcp_config.json` in Cursor, Claude Code, or Windsurf):

```json
{
  "mcpServers": {
    "cinnamon": {
      "url": "https://cinnamon-l7jf.onrender.com/mcp",
      "headers": {
        "X-Api-Key": "YOUR_CINNAMON_API_KEY"
      }
    }
  }
}
```

> Obtain a free API key at the [Cinnamon Developer Portal](https://cinnamon-mcp.vercel.app/).

---

### 2. Self-Host via Docker Compose

```bash
# Clone the repository
git clone https://github.com/benpointdex/cinnamon.git
cd cinnamon

# Start PostgreSQL with pgvector and Cinnamon
docker compose up -d
```

The server automatically executes Flyway migrations, initializes the local ONNX engine, and exposes the MCP endpoint at `http://localhost:8080/mcp`.

---

## Privacy & Compliance

Cinnamon enforces an enterprise-grade privacy boundary:
* **Zero Source Code Retained:** Raw source text is processed in volatile memory and discarded immediately after AST extraction.
* **Mathematical Representations Only:** Database records store solely SHA-256 hashes, 384-dimensional vector embeddings, and file path pointers.
* **No Third-Party AI APIs:** Vector generation runs 100% locally via ONNX Runtime. Source code is never transmitted to external model providers.

---

## Tech Stack

| Layer | Component |
|:---|:---|
| **Core Runtime** | Java 21 (LTS), Spring Boot 4.1.0, Spring Data JPA, Hibernate |
| **Protocol** | Spring AI MCP Server (`spring-ai-starter-mcp-server-webmvc`) |
| **AST Parsing** | Tree-sitter (`io.github.bonede:tree-sitter:0.26.6`) across 9 languages |
| **AI / Embeddings** | Deep Java Library (DJL) ONNX Runtime, `all-MiniLM-L6-v2` (384 dimensions) |
| **Database** | PostgreSQL 16 + pgvector (HNSW Indexing), Flyway |
| **Git Operations** | Eclipse JGit (`depth=1` shallow cloning) |
| **Rate Limiting & Auth** | Bucket4j Token Bucket, SHA-256 API Key Hashing |

---

## License

This project is licensed under the [MIT License](LICENSE).
