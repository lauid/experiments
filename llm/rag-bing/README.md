# RAG-Bing：本地文档问答系统

基于 Ollama + LangChain + ChromaDB 的本地 RAG 问答系统，纯本地运行，数据不出本机。

## 项目概述

```
rag-bing/
├── README.md           ← 你在这里
├── config.py           ← 配置（模型、分块参数）
├── requirements.txt    ← Python 依赖
├── ingest.py           ← 文档 → 向量库
├── query.py            ← 交互式问答 CLI
├── data/               ← 放你的文档（支持 .pdf .txt .md .csv .docx）
└── .chroma/            ← 向量数据库（自动生成）
```

**技术栈**：Ollama + LangChain + ChromaDB + Qwen2.5 + nomic-embed-text

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 下载模型

```bash
# 嵌入模型（轻量，~137MB）
ollama pull nomic-embed-text

# 语言模型（推荐支持中文的）
ollama pull qwen2.5:3b       # 推荐，1.9GB，速度快
# 或：ollama pull qwen2.5:7b  # 更强但更慢，4.7GB
# 或：ollama pull llama3.1:8b
```

### 3. 放入文档

将你的文档放入 `data/` 目录，支持格式：
- `.txt` `.md` `.pdf` `.csv` `.docx`

**手动创建**：直接写文本文件即可，不依赖任何特定格式。

### 4. 导入文档（构建向量库）

```bash
python ingest.py
```

### 5. 开始问答

```bash
python query.py
```

## 配置

编辑 `config.py` 或设置环境变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `LLM_MODEL` | `qwen2.5:3b` | 语言模型 |
| `EMBED_MODEL` | `nomic-embed-text` | 嵌入模型 |
| `CHUNK_SIZE` | `500` | 文本分块大小 |
| `CHUNK_OVERLAP` | `50` | 分块重叠 |
| `TOP_K` | `4` | 检索返回的文档数 |

## 已完成的验证

系统已完成端到端测试验证：

1. 创建了 3 个测试文档（大模型基础 / RAG指南 / Agent入门）
2. 成功导入并构建 ChromaDB 向量库
3. 测试 3 个问题均准确回答：

```
Q: RAG有哪三个核心组件？
A: 文档加载与解析、向量存储与检索、生成模型 ✓

Q: 什么是 Agent 的 ReAct 模式？
A: 思考→行动→观察的循环框架 ✓

Q: 大模型训练分为哪几个阶段？
A: 预训练 → 监督微调 → 强化学习对齐 ✓
```

所有回答均基于本地文档内容，无幻觉。

## 后续进阶方向

- 换用更强模型：`qwen2.5:7b`、`llama3.1:8b`、`deepseek-r1:7b`
- 加 UI 界面：Streamlit / Gradio
- 加 Advanced RAG：混合检索 + 重排序
- 加评估：RAGAS 评测检索质量
- 加 Agent：LangGraph 多步推理
- 换 OpenAI：用 `ChatOpenAI` 替代 `ChatOllama`（速度快 10 倍+）
