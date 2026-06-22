# 大模型经典技术栈 & 入门最佳实践

## 技术栈分层

| 层次 | 技术/工具 |
|------|----------|
| **模型架构** | Transformer, MoE, MLA, Dense, Hybrid Retention |
| **训练/微调** | PyTorch 3.0, LoRA/QLoRA, DeepSpeed, TRL, Unsloth |
| **推理部署** | vLLM, SGLang, TGI (HuggingFace), Ollama, TensorRT-LLM |
| **应用框架** | LangChain, LangGraph, LlamaIndex, CrewAI |
| **向量数据库** | ChromaDB, Pinecone, FAISS, Weaviate, Milvus |
| **RAG** | Naive RAG → Advanced RAG → GraphRAG, RAGAS(评估) |
| **Agent** | LangGraph, AutoGen, MCP (Model Context Protocol), ReAct |
| **评估/可观测** | LangSmith, Ragas, Phoenix, Guardrails |
| **工程化** | FastAPI, Docker, Redis, ONNX Runtime, LiteLLM |
| **多模态** | GPT-4o, Claude, Gemini, 开源多模态模型 |
| **云服务** | AWS Bedrock, Azure ML, GCP Vertex AI |

## 最佳学习路径（3-6个月）

```
阶段1: 认知建立 + Prompt工程 + API调用
       核心概念(Prompt/Token/RAG/Agent/MCP/Embedding)
       结构化输出 (JSON Schema)
       Function Calling → Agent地基
       ↓
阶段2: RAG 检索增强生成
       文档加载 → 分割 → Embedding → 向量库 → 检索 → 生成
       混合检索 + 重排序
       ↓
阶段3: Agent 智能体
       LangGraph (Nodes/Edges/State/Conditional Routing)
       ReAct 框架 → 工具调用
       多Agent协作 (CrewAI)
       MCP 协议
       ↓
阶段4: 微调选型
       LoRA/QLoRA/DPO 懂边界不亲手训
       判断何时微调 vs Prompt+RAG
       ↓
阶段5: 部署工程化
       vLLM/Docker/Ollama
       监控/评估/成本控制
       CI/CD + LLMOps
```

## 最佳方向（2026）

范式转移：**"模型中心" → "Agentic System中心"**

| 方向 | 价值 | 核心技能 |
|------|------|----------|
| RAG | 大模型应用第一入口 | 向量库、检索优化、GraphRAG |
| Agent | 当前最高价值 | LangGraph、MCP、多Agent编排 |
| 多模态 | 下一代应用形态 | Text+Image+Audio 统一处理 |
| 评估/安全 | 生产级必备 | Guardrails、Eval、LLMOps |
| 模型微调 | 面试必问，实际按需 | LoRA、DPO、量化 |

**核心原则**：绝大多数场景用 Prompt + RAG 解决，微调是最后手段。

## 最佳入门项目

| # | 项目 | 覆盖知识点 |
|---|------|-----------|
| 1 | **Chat With Your PDF** | RAG 全流程：文档加载→分割→向量→检索→生成 |
| 2 | **知识库问答系统** | LangChain + ChromaDB + Embedding |
| 3 | **多Agent研究助手** | LangGraph + 多工具调用 + 状态管理 |
| 4 | **本地聊天机器人** | Ollama/vLLM + FastAPI + Docker |
| 5 | **简历筛选系统** | 结构化输出 + RAG + 评测 |
| 6 | **AI SaaS 产品** | 全栈：Next.js + FastAPI + LangChain + 向量库 |

**项目要求**：公开部署 + README + Demo演示 > 证书

## 学习原则

1. **不要试图自己训练基础模型** — 用现成 API 做应用开发
2. **不要过早深入 CUDA/底层** — 框架封装已够 95% 场景
3. **Ship > 读论文** — 每个概念对应一个可演示项目
4. **先搭认知框架** — 理解概念关系再动手，避免术语满天飞
5. **评估是一等公民** — 有 Eval 的系统才是生产级
6. **学会选型决策** — Prompt vs RAG vs Fine-tuning vs Agent

## 资源

- 框架: LangChain / LangGraph / LlamaIndex / CrewAI / AutoGen
- 模型: OpenAI / Anthropic / Gemini / DeepSeek / LLaMA
- 学习: llm-stacks.com / HuggingFace Transformers
- 部署: vLLM / Ollama / SGLang / TGI
