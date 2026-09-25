<div align="center">

# 🌿 Cinnamon
### AI Code Deduplication & Semantic Reuse Engine
**Stop AI agents from reinventing logic that already exists in your codebase.**

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI MCP](https://img.shields.io/badge/Model%20Context%20Protocol-MCP%20v1.0-412991?style=for-the-badge&logo=anthropic&logoColor=white)](https://modelcontextprotocol.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2016-pgvector-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://github.com/pgvector/pgvector)
[![ONNX Runtime](https://img.shields.io/badge/ONNX%20Runtime-In--Process%20SIMD-005CED?style=for-the-badge&logo=onnx&logoColor=white)](https://onnxruntime.ai/)
[![Tree-sitter](https://img.shields.io/badge/Tree--sitter-9%20Languages-1E1E1E?style=for-the-badge&logo=tree-sitter&logoColor=white)](https://tree-sitter.github.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

<br/>

<img src="assets/cinnamon-preview.png" alt="Cinnamon Developer Portal & Clone Visualizer" width="900" style="border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.15);" />

<br/><br/>

[Features](#-key-features) • [Architecture](#-architecture) • [MCP Tools](#-mcp-tools-reference) • [Quickstart](#-quickstart) • [Security & Privacy](#-zero-source-storage--privacy) • [Tech Stack](#-technology-stack)

</div>

---

## 💡 The Problem

AI coding assistants (**Claude Code**, **Cursor**, **Windsurf**, **Copilot**) write code at superhuman speed. However, they lack complete visibility across large repositories. When asked to implement a feature, agents often write brand new helper functions, validators, or endpoint handlers—completely unaware that identical logic already exists elsewhere in the project.

This creates:
* 📉 **Silent Technical Debt:** Bloated codebases with dozens of duplicate implementations.
* 🐛 **Bug Desynchronization:** Fixing a vulnerability in one utility leaves duplicated copies vulnerable.
* 💸 **Wasted Tokens & Context:** Agents spend time and tokens re-deriving logic they could have simply imported.

---

## ✨ The Solution: Cinnamon

**Cinnamon** is an enterprise-grade, multi-tenant **Model Context Protocol (MCP)** server that connects directly into your AI coding agent's reasoning loop. 

Before generating new code, the agent queries Cinnamon:
> *"Is there already a function that computes sales tax or verifies JWT tokens?"*

Cinnamon parses the repository using **Tree-sitter ASTs**, eliminates variable naming variance via **canonical symbol-table normalization**, and evaluates a **two-tier cascade**:
1. **Tier 1 (Sub-millisecond SHA-256):** Instant exact structural match (< 1ms).
2. **Tier 2 (pgvector HNSW + In-Process ONNX Embeddings):** Dense semantic similarity search ($\ge 85\%$ cosine similarity) running 100% locally with **zero external AI API fees**.

If a match is found, the agent simply imports the existing function!

---

## ⚡ Key Features

* **🔌 Model Context Protocol (MCP) Native:** Exposes 7 tools over Streamable HTTP for Claude Code, Cursor, and Windsurf.
* **🌲 9-Language Concrete Syntax Trees:** Powered by **Tree-sitter** C native bindings supporting:
  * `Java`, `TypeScript`, `JavaScript`, `Python`, `Go`, `C#`, `Dart`, `Kotlin`, `Rust`, and `PHP`.
* **🎯 Two-Pass AST Normalizer:** Automatically strips docstrings and comments, normalizes the function signature to `FUNC`, and maps local variables/parameters to incremental canonical tokens (`VAR_1`, `VAR_2`). Catches copy-pasted code even if every variable name was changed.
* **⚡ In-Process ONNX Embeddings:** Employs a pre-bundled 22MB quantized INT8 `all-MiniLM-L6-v2` transformer via **Deep Java Library (DJL)**. Generates 384-dimensional vectors in 1–3ms with **no Python runtime and no OpenAI API bills**.
* **🕸️ Whole-Repo Graph Clustering (Union-Find):** Performs an in-database vector self-join query ($< 50\text{ms}$) and aggregates pairwise matches into $N$-way clusters using **Disjoint Set Union (DSU)** with path compression, calculating estimated lines of code saved.
* **🐙 Pure-Java Git Shallow Ingestion:** Clones and indexes public/private repositories in one click via **Eclipse JGit** (`depth=1`), skipping test files, minified bundles, and boilerplate getters/setters.
* **🔒 Zero-Source Storage Policy:** **Never stores raw code** in the database. Only cryptographic SHA-256 hashes, 384-d vectors, and metadata pointers (`file_path`, `line_count`) are persisted.
* **🛡️ Multi-Tenant Security & Rate Limiting:** Enforces SHA-256 hashed API keys, `ThreadLocal` tenant context isolation, SLF4J MDC distributed tracing, and **Bucket4j** token-bucket request quotas with dynamic HTTP 429 `Retry-After` headers.

---

## 🏗️ Architecture

```text
 ┌────────────────────────────────────────────────────────┐
 │        AI Coding Agents (Claude Code, Cursor, etc.)    │
 └───────────────────────────┬────────────────────────────┘
                             │ Streamable HTTP (MCP Protocol)
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │             Cinnamon Backend (Spring Boot 4)           │
 │                                                        │
 │  [Security & Multi-Tenancy]                            │
 │  ├── ApiKeyAuthFilter (SHA-256 API Keys, ThreadLocal)  │
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
 │  ├── duplicate_findings (Agent-confirmed findings)     │
 │  └── ingestion_jobs (Async job tracking & metrics)     │
 └────────────────────────────────────────────────────────┘
```

---

## 🛠️ MCP Tools Reference

Cinnamon exposes 7 specialized tools to connected LLM agents:

| Tool Name | Purpose | Key Parameters |
|---|---|---|
| `find_similar_functions` | Probe for existing duplicate/similar functions before authoring new code. | `repository`, `sourceCode`, `filePath` |
| `scan_repository_duplicates` | Executes a whole-repository vector self-join scan, returning $N$-way clusters and lines saved. | `repository`, `minSimilarity`, `pathPrefix`, `limit` |
| `ingest_github_repository` | Clones (depth 1) and indexes a public or private GitHub repository directly on the server. | `repoUrl`, `repository`, `branch`, `githubToken`, `sourceDirs` |
| `ingest_files` | Background indexing of an array of source files sent directly from the IDE. | `repository`, `files: [{path, content}]` |
| `get_ingestion_status` | Polling endpoint for real-time indexing progress and function counts. | `jobId` |
| `record_duplicate` | Logs an agent-verified duplicate finding with reasoning and commit SHA. | `repository`, `newFilePath`, `matchedFilePath`, `similarityScore`, `reasoning` |
| `get_duplicate_report` | Generates repo-level duplication metrics, line savings, and top offending files. | `repository`, `pathPrefix`, `minSimilarity` |

---

## 🚀 Quickstart

### 1. Connecting Your AI Agent

Add Cinnamon to your MCP client configuration (e.g. `mcp_config.json` in **Cursor**, **Claude Code**, or **Windsurf**):

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

> 💡 *Need an API key? Generate a free key instantly at the Cinnamon developer portal or via the `/api/tenants` endpoint.*

---

### 2. Self-Hosting via Docker Compose

Run Cinnamon locally with a single command:

```bash
# 1. Clone the repository
git clone https://github.com/your-username/cinnamon.git
cd cinnamon

# 2. Start PostgreSQL with pgvector and Cinnamon
docker compose up -d
```

The server will automatically:
1. Boot PostgreSQL 16 with the `pgvector` extension.
2. Execute Flyway database migrations (`V1__init.sql`) to set up HNSW indexes.
3. Download the quantized 22MB ONNX transformer.
4. Expose the MCP server at `http://localhost:8080/mcp`.

---

### 3. Local Development (Maven)

**Prerequisites:** Java 21 (JDK) and Docker (for PostgreSQL).

```bash
# Start PostgreSQL with pgvector
docker run -d --name cinnamon-pg -p 5432:5432 \
  -e POSTGRES_DB=cinnamon_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root123 \
  pgvector/pgvector:pg16

# Build and run the Spring Boot application
./mvnw clean spring-boot:run
```

Verify the health check:
```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

## 🔒 Zero-Source Storage & Privacy

Cinnamon was designed from day one for enterprise privacy compliance (SOC2 / GDPR):

1. **Zero Raw Code Persisted:** Source code is analyzed in volatile memory to compute the AST and vector embedding, and is **immediately discarded**.
2. **Mathematical Fingerprints Only:** The database stores solely SHA-256 hashes, 384-dimensional floating point vectors, and file path pointers (`src/utils/math.ts`).
3. **No External AI APIs:** All neural embeddings run locally in-process via ONNX Runtime. Your proprietary source code is **never transmitted to OpenAI, Anthropic, or any third party**.

---

## 📊 Performance & Specifications

* **AST Parsing Throughput:** ~1,200 functions/sec across 9 languages via Tree-sitter.
* **Tier-1 Hash Lookup:** $< 1\text{ms}$ via composite index `(tenant_id, repository, content_hash)`.
* **Tier-2 Vector Search:** $\sim 15\text{ms}$ for top-5 nearest neighbors via PostgreSQL HNSW cosine indexing.
* **Whole-Repository Self-Join:** $< 50\text{ms}$ for full-repo duplicate scans.
* **Quantized Model Size:** 22MB INT8 ONNX (75% smaller than FP32).

---

## 🧰 Technology Stack

| Layer | Technologies |
|---|---|
| **Core Runtime** | Java 21 (LTS), Spring Boot 4.1.0, Spring MVC, Spring Data JPA |
| **Protocol** | Spring AI MCP Server (`spring-ai-starter-mcp-server-webmvc`) |
| **AST Parsing** | Tree-sitter (`io.github.bonede:tree-sitter:0.26.6`) with 9 language grammars |
| **AI / Embeddings** | Deep Java Library (DJL) ONNX Runtime, `all-MiniLM-L6-v2` (384 dimensions) |
| **Database** | PostgreSQL 16, pgvector (HNSW Indexing with `vector_cosine_ops`), Flyway |
| **Git Engine** | Eclipse JGit (`org.eclipse.jgit:6.9.0`) |
| **Security & Limits** | Bucket4j Token Bucket Rate Limiting, SHA-256 API Key Hashing |
| **Observability** | Spring Boot Actuator, Micrometer, Prometheus (`/actuator/prometheus`) |
| **Email Service** | Brevo HTTPS REST API (Port 443) + Spring Mail SMTP fallback |

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
