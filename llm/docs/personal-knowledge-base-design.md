# 个人知识库系统设计文档

> 基于 LLM 搭建可增长的企业级个人知识库
> 2026-06-22

---

## 1. 设计目标

### 1.1 核心目标

搭建一个从零手写的个人知识库系统，覆盖企业级 RAG 知识库的完整链路，同时具备**可增长**能力——从小规模平滑扩展到大规模。

### 1.2 约束条件

| 维度 | 当前状态 |
|------|---------|
| 硬件 | MacBook Pro 2015 (i5, 8GB RAM, 无 GPU) |
| 数据规模 | 起步 <100 份文档，目标支持 10,000+ |
| 数据类型 | PDF、Word、网页、Markdown（混合类型） |
| 数据隐私 | 文档完全本地，LLM 推理调用云端 API |
| 使用方式 | CLI + Web UI + API（三者都支持） |
| 技术角色 | 专业开发者，从零搭建学习 |

---

## 2. 整体架构

### 2.1 架构分层

```
┌─────────────────────────────────────────────────┐
│                  Interface 层                    │
│     CLI (click/rich)    Web UI (FastAPI+Vue)    │
│           REST API (FastAPI)                    │
├─────────────────────────────────────────────────┤
│               Agent / Orchestrator 层            │
│     查询路由 → Wiki 优先 → Vector 兜底           │
│     多轮对话管理、意图识别、工具调用               │
├─────────────────────────────────────────────────┤
│               Retrieval 层                       │
│   向量检索 (ChromaDB/FAISS)  关键词检索 (BM25)   │
│   混合召回 → Rerank (bge-reranker)               │
├─────────────────────────────────────────────────┤
│               Indexing 层                        │
│   文档解析 → 分块 → Embedding → 向量库          │
│   LLM 摘要 → Wiki 页面生成                       │
├─────────────────────────────────────────────────┤
│               Storage 层                         │
│   原始文档 (本地文件系统)                         │
│   Wiki 页面 (Markdown + Git)                     │
│   向量库 (ChromaDB/FAISS)                        │
│   元数据 (SQLite / DuckDB)                       │
├─────────────────────────────────────────────────┤
│               Ingestion 层                       │
│   PDF / Word / HTML / Markdown / 网页抓取         │
│   统一解析 → 文本提取 → 元数据提取               │
└─────────────────────────────────────────────────┘
```

### 2.2 核心数据流

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  源文档   │ →  │  解析器   │ →  │  分块器   │ →  │ Embedding│
│ (PDF/WEB) │    │          │    │          │    │  模型     │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                                     ↓
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  回答     │ ←  │  LLM     │ ←  │  Rerank  │ ←  │  向量库   │
│  用户     │    │  生成     │    │  排序     │    │          │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### 2.3 Wiki + Vector 双轨设计

为支持知识复利与大规模检索，采用**双存储 + 分级查询**策略：

```
新文档进入
  ↓
┌──────────────────────┐
│ LLM 提取摘要 + 生成   │
│ Wiki 页面            │ ←── 知识编译（一次成本）
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ 写入 Wiki 目录        │ ←── `_index.md` + 分类 MD 文件
│ (Markdown + Git)      │     人机可读、版本管理
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ 同时全文 Chunk +      │ ←── 向量化兜底
│ Embedding → 向量库    │     长尾检索不遗漏
└──────────────────────┘
```

**查询流程：**

```
用户提问
  ↓
Agent 先读 Wiki 索引 (`_index.md`)
  ├─ 命中相关 Wiki 页面 → 读取 Wiki 内容 → LLM 合成回答
  │   (快速、高质量、知识已编译)
  └─ 不明确/需细节 → 向量检索 Top-K → Rerank → LLM 合成回答
      (兜底覆盖、不遗漏)
```

---

## 3. 组件详细设计

### 3.1 Ingestion 层 — 文档解析

支持文件类型与解析方案：

| 类型 | 解析方案 | 说明 |
|------|---------|------|
| PDF | `PyMuPDF` (fitz) / `pdfplumber` | 文本提取 + 表格识别 |
| Word | `python-docx` | 段落 + 表格提取 |
| HTML / 网页 | `trafilatura` / `BeautifulSoup` | 正文提取，去除广告/导航 |
| Markdown | 原生解析 | 保留标题层级结构 |
| 图片 PDF | OCR 降级（`paddleocr` / `tesseract`） | 可选，非初期必需 |

解析输出统一格式：

```python
@dataclass
class ParsedDocument:
    source_path: str          # 源文件路径
    source_type: str          # pdf / docx / html / md
    title: str                # 文档标题
    content: str              # 纯文本内容
    metadata: dict            # 作者、日期、标签等
    sections: list[Section]   # 按标题分节的结构
```

### 3.2 Indexing 层 — 索引构建

#### 3.2.1 分块策略 (Chunking)

采用**层级分块**策略，按文档结构递归分割：

```
文档
 ├─ 标题1 → Chunk (保留标题作为前缀)
 │   ├─ 子标题1.1 → Chunk
 │   └─ 子标题1.2 → Chunk
 ├─ 标题2 → Chunk
 └─ ...
```

- **默认规则**：chunk_size = 500 tokens, overlap = 50 tokens
- **结构感知**：按 Markdown 标题 / PDF 章节分割，保留标题层级作为 metadata
- **语义感知**：可选的，用 LLM 检测语义边界（非初期必需）

#### 3.2.2 Embedding

- **模型选择**：`BAAI/bge-small-zh-v1.5` (CPU 可运行，384 维，~30MB)
- **进阶**：`BAAI/bge-large-zh-v1.5` (1024 维，需更多内存)
- **本地 CPU 推理**：使用 `sentence-transformers` 库

#### 3.2.3 Wiki 编译

新文档入库时，LLM 负责：

1. 生成文档摘要（100-200 字）
2. 提取关键术语和实体
3. 与已有 Wiki 页面建立交叉引用
4. 更新 `_index.md` 索引文件

Wiki 目录结构：

```
wiki/
├── _index.md              # 总索引（所有页面的目录 + 一句话摘要）
├── ai/
│   ├── _index.md          # AI 分类索引
│   ├── machine-learning.md
│   ├── deep-learning.md
│   └── rag.md
├── programming/
│   ├── _index.md
│   ├── python.md
│   └── go.md
└── ... 
```

`_index.md` 格式示例：

```markdown
# Wiki 索引

## AI
- [[machine-learning.md]] — 机器学习基础概念与算法分类
- [[deep-learning.md]] — 深度学习架构：CNN、RNN、Transformer
- [[rag.md]] — RAG 检索增强生成流程与实现

## Programming
- [[python.md]] — Python 语法、标准库、最佳实践
```

### 3.3 Storage 层 — 存储设计

| 存储对象 | 技术选型 | 用途 |
|---------|---------|------|
| 原始文档 | 本地文件系统 | 不可变源文件 |
| Wiki 页面 | Markdown 文件 + Git | 编译后的结构化知识 |
| 向量索引 | ChromaDB (起步) → FAISS (扩展) | 语义检索 |
| 元数据 | SQLite (起步) → DuckDB (扩展) | 文档/Chunk 关系、标签、状态 |
| 对话记录 | SQLite | 多轮对话历史 |

### 3.4 Retrieval 层 — 检索

#### 3.4.1 检索流程

```
用户 Query
  ↓
Query Rewrite (可选：LLM 改写/扩展)
  ↓
┌─────────────────────────────┐
│ 多路召回                     │
│  ├─ 向量检索 (语义相似度)     │
│  ├─ BM25 (关键词精确匹配)    │
│  └─ Wiki 精确匹配 (标题/标签) │
└──────────┬──────────────────┘
           ↓
      合并去重
           ↓
      Rerank (bge-reranker)
           ↓
      Top-K (3-5) → Prompt 组装
```

#### 3.4.2 检索参数

| 参数 | 默认值 | 说明 |
|------|-------|------|
| 向量检索 Top-K | 30 | 粗召回 |
| BM25 Top-K | 10 | 关键词召回 |
| 最终 K（Rerank 后） | 5 | 送入 LLM 的片段数 |
| 相似度阈值 | 0.6 | 低于此值的丢弃 |
| 混合权重 | 向量 0.7 / BM25 0.3 | 可配置 |

### 3.5 Generation 层 — 回答生成

#### 3.5.1 Prompt 模板

采用分层模板结构：

```
System Prompt:
  角色定位：你是知识库助手
  行为规则：只基于参考资料回答
  不知道就说不知道
  引用来源编号

Context Block:
  [1] 来源：xxx.docx  内容：...
  [2] 来源：xxx.pdf  内容：...

User Query:
  用户的问题
```

#### 3.5.2 LLM 调用

- **API 调用**：DeepSeek / OpenAI / SiliconFlow 等
- **参数**：temperature=0.1, max_tokens=1024
- **流式返回**：SSE 流式输出
- **备用模型**：主模型失败时自动切换

### 3.6 Interface 层 — 交互

| 交互方式 | 技术方案 | 优先级 |
|---------|---------|--------|
| **CLI** | `click` + `rich`（终端交互式问答） | P0 |
| **Web UI** | FastAPI + 简单前端 (HTMX 或 Vue) | P1 |
| **REST API** | FastAPI 提供完整 API | P0 |
| **Agent 集成** | MCP Server / Function Calling 工具 | P2 |

### 3.7 Agent 层 — 查询路由

查询路由 Agent 负责：

```
用户输入
  ↓
意图分类
  ├─ 知识问答 → 走 RAG 流程
  ├─ Wiki 查询 → 走 Wiki 检索
  ├─ 文档管理 → 列出/搜索文档
  └─ 闲聊/不相关 → 礼貌拒绝
```

构建方式：先手写规则路由，后续演进为 LLM-based router。

---

## 4. 技术选型

| 组件 | 技术选型 | 选型理由 |
|------|---------|---------|
| 编程语言 | Python 3.11+ | AI 生态最丰富 |
| Web 框架 | FastAPI | 异步、类型安全、自动文档 |
| 向量库 | ChromaDB → FAISS | 起步简单，后续可换 |
| Embedding | sentence-transformers | CPU 可跑，模型丰富 |
| 文档解析 | PyMuPDF + python-docx + trafilatura | 覆盖主流格式 |
| 全文检索 | `rank-bm25` | 纯 Python，无外部依赖 |
| Rerank | `FlagEmbedding` (bge-reranker) | 本地可运行 |
| 元数据存储 | SQLite | 零配置，足够用 |
| Wiki 版本管理 | Git | 原生支持 |
| CLI | click + rich | Python 生态最成熟 |
| LLM API | DeepSeek / OpenAI | 性价比高，兼容性好 |
| 任务编排 | asyncio + 简单队列 | 轻量，避免引入消息队列 |

### 为什么不选某些技术

| 技术 | 排除原因 |
|------|---------|
| LangChain / LlamaIndex | 框架封装太多细节，不利于学习底层；调试困难 |
| Milvus / Elasticsearch | 初期不需要分布式，运维成本高 |
| PostgreSQL + pgvector | 超出当前需求，SQLite 足够 |
| Docker / Kubernetes | 增加复杂度，等需要部署时再加 |
| Redis | 初期 QPS 低，不需要缓存层 |

---

## 5. 规模演进路径

```
Phase 1 (起步)        Phase 2 (增长)         Phase 3 (企业级)
< 100 份文档         100 - 5,000 份          5,000 - 100,000+
══════════════       ═══════════════         ══════════════════

Storage:
ChromaDB(内存)  →    ChromaDB(持久化)    →   FAISS + SQL → DuckDB
Wiki(MD+Git)         Wiki(MD+Git)            Wiki(MD+Git)

Retrieval:
向量检索              + BM25 混合             + Rerank
                     + 查询改写              + 多路召回加权

Generation:
API 直连              + Prompt 模板           + 缓存命中
                     + 流式输出              + 模型路由

Interface:
CLI                   + Web UI               + API + Agent
                     + 流式 SSE              + MCP Server

Ingestion:
手动放入目录          自动监听目录            + 网页抓取
                     + 增量更新              + 定时同步
```

---

## 6. 项目目录结构

```
kb/                         # 项目根目录
├── README.md
├── requirements.txt
├── pyproject.toml
│
├── kb/                     # 核心包
│   ├── __init__.py
│   ├── config.py           # 全局配置
│   │
│   ├── ingest/             # Ingestion 层
│   │   ├── __init__.py
│   │   ├── loader.py       # 文件加载器
│   │   ├── parser.py       # 文档解析器
│   │   └── crawler.py      # 网页抓取
│   │
│   ├── index/              # Indexing 层
│   │   ├── __init__.py
│   │   ├── chunker.py      # 分块策略
│   │   ├── embedder.py     # 向量化
│   │   └── wiki_builder.py # Wiki 编译
│   │
│   ├── storage/            # Storage 层
│   │   ├── __init__.py
│   │   ├── vector_store.py # 向量库接口
│   │   ├── metadata_store.py # 元数据存储
│   │   └── wiki_store.py   # Wiki 存储
│   │
│   ├── retrieval/          # Retrieval 层
│   │   ├── __init__.py
│   │   ├── vector_search.py  # 向量检索
│   │   ├── bm25_search.py    # 关键词检索
│   │   ├── hybrid_search.py  # 混合检索
│   │   └── reranker.py       # 重排序
│   │
│   ├── generation/         # Generation 层
│   │   ├── __init__.py
│   │   ├── llm.py          # LLM 调用封装
│   │   ├── prompt.py       # Prompt 模板管理
│   │   └── streaming.py    # 流式输出
│   │
│   ├── agent/              # Agent/Orchestrator 层
│   │   ├── __init__.py
│   │   ├── router.py       # 查询路由
│   │   └── session.py      # 对话管理
│   │
│   ├── interface/          # Interface 层
│   │   ├── __init__.py
│   │   ├── cli.py          # 命令行界面
│   │   ├── api.py          # REST API
│   │   └── web/            # Web UI
│   │       ├── app.py
│   │       └── templates/
│   │
│   └── core/               # 核心工具
│       ├── __init__.py
│       ├── types.py        # 公共类型定义
│       └── utils.py        # 工具函数
│
├── data/                   # 数据目录
│   ├── sources/            # 原始文档
│   ├── wiki/               # Wiki 页面
│   ├── chroma/             # ChromaDB 持久化
│   └── db/                 # SQLite 数据库
│
├── tests/                  # 测试
│   ├── test_ingest.py
│   ├── test_retrieval.py
│   └── test_generation.py
│
└── scripts/                # 脚本
    ├── ingest.py           # 文档入库
    ├── rebuild_index.py    # 重建索引
    └── query.py            # 命令行查询
```

---

## 7. 实施路线

### Phase 1: 核心管道（2-3 周）

| 步骤 | 内容 | 产出 |
|------|------|------|
| 1 | Storage 层：ChromaDB + SQLite 封装 | 可读写向量库和元数据 |
| 2 | Ingestion + Indexing：解析 PDF/MD → 分块 → 嵌入 | 文档可入库 |
| 3 | Retrieval：向量检索 | 可检索相关片段 |
| 4 | Generation：LLM API 封装 + Prompt 模板 | 可回答简单问题 |
| 5 | CLI 界面：交互式问答 | 终端可用 |

### Phase 2: 进阶优化（1-2 周）

| 步骤 | 内容 |
|------|------|
| 6 | BM25 混合检索 + 混合召回加权 |
| 7 | Rerank 集成 |
| 8 | Wiki 编译（LLM 摘要 + 索引） |
| 9 | 查询路由（Wiki 优先 + Vector 兜底） |

### Phase 3: 全功能（1-2 周）

| 步骤 | 内容 |
|------|------|
| 10 | Web UI（FastAPI + 前端） |
| 11 | 流式输出（SSE） |
| 12 | 网页抓取（trafilatura） |
| 13 | 多轮对话 |
| 14 | MCP Server / Agent 集成 |

---

## 8. 企业级知识点覆盖

通过此项目可系统掌握以下企业级知识库核心技术：

| 领域 | 具体知识点 |
|------|-----------|
| **文档处理** | 多格式解析、OCR 降级、结构化提取、分块策略 |
| **向量化** | Embedding 模型对比、向量维度选择、批量嵌入优化 |
| **向量存储** | HNSW/IVF 索引原理、内存 vs 持久化、Filter 过滤 |
| **检索策略** | 向量检索、BM25、混合召回加权、Query Rewrite |
| **排序** | Rerank 原理、Cross-Encoder vs Bi-Encoder |
| **Prompt 工程** | 模板管理、Few-shot、结构化输出、注入防护 |
| **LLM 调用** | API 封装、流式 SSE、重试/降级、模型路由 |
| **系统设计** | 分层架构、增量更新、缓存策略、性能优化 |

---

## 9. 非目标（明确不做）

- ❌ 多用户/权限系统（个人使用不需要）
- ❌ 分布式部署（单机够用，未来再升级）
- ❌ 模型微调（RAG 场景不需要）
- ❌ 知识图谱（复杂度高，收益有限）
- ❌ 实时协同编辑（超出个人知识库范围）

---

## 10. 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 本地 CPU Embedding 慢 | 索引构建慢 | 用轻量模型 (bge-small)；批量处理 |
| 8GB RAM 内存不足 | 系统卡顿 | 避免加载大模型；ChromaDB 用内存映射 |
| API 调用成本 | 持续费用 | 控制 token；缓存命中；选便宜模型 |
| 规模增长后检索退化 | 回答质量下降 | 引入 Rerank；阶段性迁移到 FAISS |
| 文档解析错误 | 知识丢失 | 失败重试 + 人工标记 |

---

## 11. 附录：硬件适配说明

当前硬件（Intel MacBook Pro 2015, 8GB RAM, 无 GPU）：

| 操作 | 可行性 | 性能预期 |
|------|--------|---------|
| 文档解析 (PDF/Word) | ✅ 顺畅 | 毫秒级 |
| Embedding (bge-small) | ✅ 可运行 | 100 份文档 ~30 秒 |
| ChromaDB 检索 | ✅ 顺畅 | 毫秒级 |
| BM25 检索 | ✅ 顺畅 | 毫秒级 |
| Rerank (bge-reranker) | ⚠️ 较慢 | 每次 ~1-2 秒 |
| LLM 推理 (本地) | ❌ 不可行 | 7B 模型需要 16GB+ |
| LLM 推理 (API) | ✅ 顺畅 | 取决于网络 |

升级建议：更换为 Apple Silicon Mac (M 系列) 后，可改用本地 Ollama 推理，无需修改架构。

---

## 12. 相关资源

- [Karpathy's LLM Wiki Gist](https://gist.github.com/karpathy/442a6bf555914893e9891c11519de94f)
- [BGE Embedding Models](https://github.com/FlagOpen/FlagEmbedding)
- [ChromaDB](https://www.trychroma.com/)
- [FAISS](https://github.com/facebookresearch/faiss)
