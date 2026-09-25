<div align="center">

# Cinnamon
### AI Code Deduplication & Semantic Reuse Engine

Stop AI coding agents from reinventing logic that already exists in your codebase.

[![Live Portal](https://img.shields.io/badge/Live%20Portal-cinnamon--mcp.vercel.app-0D8D9C?style=for-the-badge)](https://cinnamon-mcp.vercel.app/)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI MCP](https://img.shields.io/badge/Model%20Context%20Protocol-MCP%20v1.0-412991?style=for-the-badge)](https://modelcontextprotocol.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://github.com/pgvector/pgvector)
[![License: MIT](https://img.shields.io/badge/License-MIT-gray.svg?style=for-the-badge)](LICENSE)

<br/>

<a href="https://cinnamon-mcp.vercel.app/">
  <img src="assets/cinnamon-preview.png" alt="Cinnamon Developer Portal" width="850" style="border-radius: 6px; border: 1px solid #e1e4e8;" />
</a>

<p align="center">
  <a href="https://cinnamon-mcp.vercel.app/"><strong>Visit Developer Portal</strong></a> •
  <a href="#quickstart">Quickstart</a> •
  <a href="#mcp-tools">MCP Tools</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#privacy--compliance">Privacy</a>
</p>

</div>

---

## Overview

AI coding assistants (Claude Code, Cursor, Windsurf) frequently generate redundant helper functions, validators, or route handlers because they lack holistic visibility over large codebases. 

This causes:
* **Codebase bloat:** Proliferation of near-identical utility functions across directories.
* **Bug fragmentation:** Security patches applied in one file miss duplicated variants elsewhere.
* **Wasted context:** Agents waste generation tokens re-deriving logic that could simply be imported.

**Cinnamon** is a multi-tenant **Model Context Protocol (MCP)** server that hooks directly into an agent's reasoning loop. Before writing new code, the agent queries Cinnamon:
> *"Does a function with this behavior already exist in the repository?"*

Cinnamon evaluates a two-tier cascade:
1. **Tier 1 (Sub-millisecond SHA-256):** Tree-sitter AST normalization strips comments, renames local identifiers to canonical placeholders (`VAR_1`, `VAR_2`), and hashes the structure for instant exact matches ($< 1\text{ms}$).
2. **Tier 2 (In-Process ONNX + pgvector HNSW):** Encodes function semantics into 384-dimensional dense vectors using a local `all-MiniLM-L6-v2` transformer and queries PostgreSQL HNSW cosine indexes for near-duplicates ($\ge 85\%$ similarity).

If found, the agent reuses the existing implementation instead of authoring redundant code.

---

## Key Capabilities

* **MCP Native:** Exposes 7 tools over Streamable HTTP for seamless integration with Cursor, Claude Code, and Windsurf.
* **9-Language AST Parsing:** Powered by Tree-sitter C bindings for Java, TypeScript, JavaScript, Python, Go, C#, Dart, Kotlin, Rust, and PHP.
* **Two-Pass Identifier Normalizer:** Normalizes variable names, removes comments, and preserves external API calls to catch copy-pasted code even when variables are renamed.
* **In-Process ONNX Embeddings:** Employs an embedded 22MB quantized INT8 transformer via Deep Java Library (DJL) on the CPU—zero Python runtime and zero external API fees.
* **Graph-Based Duplicate Clustering:** An in-database vector self-join ($< 50\text{ms}$) coupled with Disjoint Set Union (Union-Find) with path compression clusters pairwise duplicates into $N$-way refactoring groups with computed line savings.
* **Native Shallow Git Ingestion:** Pure-Java repository cloning via Eclipse JGit (`depth=1`), with smart filtering to skip vendor directories, test suites, and generated boilerplate.
* **Zero-Source Storage Policy:** Source code is analyzed in memory and immediately discarded. PostgreSQL stores only cryptographic hashes, vectors, and file path pointers.
* **Multi-Tenant Security:** Enforces SHA-256 hashed API keys, `ThreadLocal` tenant context isolation, SLF4J MDC distributed tracing, and Bucket4j token-bucket rate limiting.

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
 │  [Parsing & Processing Engine]                         │
 │  ├── Tree-sitter AST Multi-Language Parser (9 Grammars)│
 │  ├── IdentifierNormalizer (Symbol Table + Canonical)   │
 │  └── In-Process DJL ONNX Embedding (all-MiniLM-L6-v2)  │
 │                                                        │
 │  [Graph Clustering Engine]                             │
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

| Tool Name | Description | Key Parameters |
|---|---|---|
| `find_similar_functions` | Checks if a proposed function already exists prior to code generation. | `repository`, `sourceCode`, `filePath` |
| `scan_repository_duplicates` | Performs a whole-repo vector self-join scan, returning $N$-way clusters and line savings. | `repository`, `minSimilarity`, `pathPrefix`, `limit` |
| `ingest_github_repository` | Clones (depth 1) and indexes a public/private Git repository directly on the server. | `repoUrl`, `repository`, `branch`, `githubToken`, `sourceDirs` |
| `ingest_files` | Asynchronously indexes an array of source files supplied by an IDE agent. | `repository`, `files: [{path, content}]` |
| `get_ingestion_status` | Polling endpoint for real-time progress of an ingestion job. | `jobId` |
| `record_duplicate` | Records an agent-confirmed duplicate finding with technical reasoning. | `repository`, `newFilePath`, `matchedFilePath`, `similarityScore`, `reasoning` |
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

> Get your free API key at the [Cinnamon Developer Portal](https://cinnamon-mcp.vercel.app/).

### 2. Self-Host via Docker Compose

```bash
git clone https://github.com/benpointdex/cinnamon.git
cd cinnamon
docker compose up -d
```
The server will boot PostgreSQL with pgvector, run Flyway migrations, initialize the local ONNX engine, and expose the MCP endpoint at `http://localhost:8080/mcp`.

---

## Privacy & Compliance

Cinnamon is built for enterprise privacy:
* **Zero Source Code Retained:** Raw code is inspected in volatile memory and discarded immediately.
* **Mathematical Representations Only:** Database records hold only SHA-256 hashes, 384-dimensional vector embeddings, and file path pointers.
* **No Third-Party AI APIs:** Vector generation runs 100% locally via ONNX Runtime. Source code is never sent to external LLM providers.

---

## Tech Stack

* **Language & Framework:** Java 21 (LTS), Spring Boot 4.1.0, Spring Data JPA, Hibernate
* **Protocol:** Spring AI MCP Server (`spring-ai-starter-mcp-server-webmvc`)
* **AST Parsing:** Tree-sitter (`io.github.bonede:tree-sitter:0.26.6`) across 9 languages
* **Embedding Model:** In-process ONNX Runtime via Deep Java Library (`all-MiniLM-L6-v2`)
* **Database & Search:** PostgreSQL 16 + pgvector (HNSW Indexing), Flyway
* **Git Operations:** Eclipse JGit (`depth=1` shallow cloning)
* **Rate Limiting & Auth:** Bucket4j Token Bucket, SHA-256 API Key Hashing

---

## License

This project is licensed under the [MIT License](LICENSE).
