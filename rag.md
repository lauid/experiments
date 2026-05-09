# 使用一个小模型，RAG 完整示例代码
  RAG的核心组件：
   1. 文档加载和分块
   2. 向量化（Embedding）
   3. 向量存储（Vector Store）
   4. 检索（Retrieval）
   5. 生成（Generation）

  对于小模型，我可以使用：
   - Embedding模型：sentence-transformers（轻量级）
   - 文本生成模型：可以选择轻量级的如Qwen-0.5B, Phi-2, TinyLlama等，或者使用API方式

  让我创建一个完整的Python RAG示例，包括：
   1. 环境准备说明
   2. 完整的代码实现
   3. 使用示例

  现在让我创建一个完整的RAG示例代码。我会使用：
   - sentence-transformers 用于 embedding
   - Chroma 作为向量数据库
   - 可以选择本地小模型或API方式
   
  快速开始

   1 # 1. 安装依赖
   2 cd ~/rag
   3 pip install -r requirements.txt
   4
   5 # 2. 运行演示 (推荐先试这个)
   6 python simple_rag.py --mode demo


  RAG 核心组件

   1. 文档加载与分块 - 将长文本分割成小块
   2. 向量化 (Embedding) - 使用 sentence-transformers/all-MiniLM-L6-v2
   3. 向量存储 - 支持 Chroma / FAISS
   4. 相似度检索 - Top-K 检索相关文档
   5. 生成回答 - 使用 LLM 结合上下文生成

  推荐的小模型


  ┌────────────────┬────────┬──────────┐
  │ 模型           │ 参数量 │ 显存需求 │
  ├────────────────┼────────┼──────────┤
  │ Qwen2-0.5B     │ 0.5B   │ ~2GB     │
  │ TinyLlama-1.1B │ 1.1B   │ ~4GB     │
  │ phi-2          │ 2.7B   │ ~6GB     │
  └────────────────┴────────┴──────────┘






  ```python
  #!/usr/bin/env python3
"""
RAG (检索增强生成) 完整示例 - 使用小模型
============================================

核心组件:
1. 文档加载与分块 (Document Loading & Chunking)
2. 向量化 (Embedding)
3. 向量存储 (Vector Store)
4. 相似度检索 (Retrieval)
5. 生成回答 (Generation)

依赖安装:
    pip install langchain langchain-community langchain-huggingface \
                sentence-transformers chromadb faiss-cpu \
                transformers huggingface-hub accelerate

或使用简化版本:
    pip install chromadb sentence-transformers
"""

import os
import sys
from pathlib import Path
from typing import List, Optional

# 优先使用本地缓存的模型，避免网络不通时反复重试
os.environ.setdefault("HF_HUB_OFFLINE", "1")

# ================== 配置 ==================
# 可以根据需要切换不同的模型
CONFIG = {
    # Embedding 模型 - 轻量级
    "embedding_model": "sentence-transformers/all-MiniLM-L6-v2",  # ~80MB, 384维

    # 文本生成模型选项:
    # 1. "microsoft/Phi-3-mini-4k-instruct" - 约3.8B参数，需要8GB+显存
    # 2. "microsoft/phi-2" - 约2.7B参数，需要6GB+显存
    # 3. "TinyLlama/TinyLlama-1.1B-Chat-v1.0" - 约1.1B参数，需要4GB+显存
    # 4. "Qwen/Qwen2-0.5B-Instruct" - 约0.5B参数，需要2GB+显存
    "generation_model": "microsoft/phi-2",

    # 向量数据库类型: "chroma" | "faiss"
    "vector_store": "chroma",

    # 分块参数
    "chunk_size": 500,
    "chunk_overlap": 50,
}


class Document:
    """简单的文档类"""
    def __init__(self, page_content: str, metadata: dict = None):
        self.page_content = page_content
        self.metadata = metadata or {}


class SimpleRAG:
    """
    简化的 RAG 系统实现

    使用本地小模型，无需API密钥
    """

    def __init__(
        self,
        embedding_model: str = None,
        generation_model: str = None,
        vector_store_type: str = "chroma",
        device: str = "cpu"
    ):
        self.embedding_model = embedding_model or CONFIG["embedding_model"]
        self.generation_model = generation_model or CONFIG["generation_model"]
        self.vector_store_type = vector_store_type
        self.device = device

        self.embedding_fn = None
        self.llm = None
        self.vector_store = None
        self.documents = []

        print(f"🤖 RAG 配置:")
        print(f"   Embedding: {self.embedding_model}")
        print(f"   生成模型: {self.generation_model}")
        print(f"   向量存储: {self.vector_store_type}")
        print(f"   设备: {self.device}")

    def load_embedding_model(self):
        """加载 Embedding 模型"""
        print(f"\n📥 加载 Embedding 模型...")
        try:
            from sentence_transformers import SentenceTransformer
            self.embedding_fn = SentenceTransformer(self.embedding_model, device=self.device)
            print(f"   ✅ Embedding 模型加载成功!")
            print(f"   向量维度: {self.embedding_fn.get_embedding_dimension()}")
        except ImportError:
            print("❌ 请安装 sentence-transformers: pip install sentence-transformers")
            sys.exit(1)

    def load_llm(self):
        """加载文本生成模型"""
        print(f"\n📥 加载 LLM 模型 (可能需要几分钟)...")
        try:
            from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline
            import torch

            # 加载分词器和模型
            tokenizer = AutoTokenizer.from_pretrained(self.generation_model)
            model = AutoModelForCausalLM.from_pretrained(
                self.generation_model,
                torch_dtype=torch.float32 if self.device == "cpu" else torch.float16,
                device_map="auto" if self.device != "cpu" else None,
                low_cpu_mem_usage=True
            )

            # 创建 pipeline
            self.llm = pipeline(
                "text-generation",
                model=model,
                tokenizer=tokenizer,
                max_new_tokens=512,
                temperature=0.7,
                top_p=0.9,
                do_sample=True,
            )
            print(f"   ✅ LLM 模型加载成功!")

        except ImportError:
            print("❌ 请安装 transformers: pip install transformers")
            sys.exit(1)
        except Exception as e:
            print(f"❌ LLM 加载失败: {e}")
            print("💡 建议: 降低生成模型配置，或使用更小的模型")
            sys.exit(1)

    def load_vector_store(self):
        """加载向量存储"""
        print(f"\n📥 初始化向量存储 ({self.vector_store_type})...")
        try:
            if self.vector_store_type == "chroma":
                import chromadb
                self.chroma_client = chromadb.Client()  # 新版 chromadb 默认内存模式
                self.collection = self.chroma_client.create_collection("rag_docs")
                self.vector_store = "chroma"
            elif self.vector_store_type == "faiss":
                import faiss
                import numpy as np
                self.dimension = self.embedding_fn.get_embedding_dimension()
                self.index = faiss.IndexFlatLIP(self.dimension)
                self.doc_embeddings = []
                self.vector_store = "faiss"
            print(f"   ✅ 向量存储初始化成功!")
        except ImportError:
            print(f"❌ 请安装 {self.vector_store_type}: pip install {self.vector_store_type}")
            sys.exit(1)

    def load_documents(self, documents: List[Document]):
        """加载文档到向量存储"""
        print(f"\n📚 加载 {len(documents)} 个文档...")

        # 1. 分块处理
        chunks = []
        for doc in documents:
            doc_chunks = self._split_text(doc.page_content)
            for chunk in doc_chunks:
                chunks.append(Document(chunk, doc.metadata))

        print(f"   文档分块后: {len(chunks)} 个块")

        # 2. 生成 Embedding
        texts = [chunk.page_content for chunk in chunks]
        embeddings = self.embedding_fn.encode(texts, convert_to_numpy=True)

        # 3. 存入向量数据库
        if self.vector_store == "chroma":
            ids = [f"doc_{i}" for i in range(len(chunks))]
            self.collection.add(
                ids=ids,
                embeddings=embeddings.tolist(),
                documents=texts,
                metadatas=[chunk.metadata for chunk in chunks]
            )
        elif self.vector_store == "faiss":
            self.index.add(embeddings)
            self.documents = chunks

        print(f"   ✅ 文档加载完成!")

    def _split_text(self, text: str) -> List[str]:
        """将文本分割成块"""
        chunk_size = CONFIG["chunk_size"]
        overlap = CONFIG["chunk_overlap"]

        chunks = []
        start = 0
        text_len = len(text)

        while start < text_len:
            end = start + chunk_size
            chunk = text[start:end]
            chunks.append(chunk)
            start = end - overlap

        return chunks

    def retrieve(self, query: str, top_k: int = 3) -> List[Document]:
        """检索相关文档"""
        # 1. 将查询向量化
        query_embedding = self.embedding_fn.encode([query], convert_to_numpy=True)

        # 2. 相似度搜索
        if self.vector_store == "chroma":
            results = self.collection.query(
                query_embeddings=query_embedding.tolist(),
                n_results=top_k
            )
            retrieved_docs = []
            for i, doc in enumerate(results["documents"][0]):
                metadata = results["metadatas"][0][i] if results.get("metadatas") else {}
                retrieved_docs.append(Document(doc, metadata))

        elif self.vector_store == "faiss":
            scores, indices = self.index.search(query_embedding, top_k)
            retrieved_docs = [self.documents[i] for i in indices[0] if i < len(self.documents)]

        return retrieved_docs

    def generate(self, query: str, retrieved_docs: List[Document]) -> str:
        """生成回答"""
        # 构建 prompt
        context = "\n\n".join([doc.page_content for doc in retrieved_docs])

        # 如果没有加载 LLM，使用模拟回答（demo 模式）
        if self.llm is None:
            return self._mock_generate(query, context)

        prompt = f"""你是一个智能助手，请根据以下参考文档回答用户的问题。

参考文档:
{context}

用户问题: {query}

请根据参考文档回答，如果文档中没有相关信息，请说明无法从文档中找到答案。
"""

        # 调用 LLM 生成
        response = self.llm(prompt)[0]["generated_text"]

        # 提取回答部分（去除 prompt）
        answer = response[len(prompt):].strip()

        # 清理回答
        answer = answer.split("用户问题:")[0] if "用户问题:" in answer else answer
        answer = answer.split("参考文档:")[0] if "参考文档:" in answer else answer

        return answer if answer else "抱歉，无法生成回答。"

    def _mock_generate(self, query: str, context: str) -> str:
        """模拟 LLM 生成回答（demo 模式，无需真实 LLM）"""
        return f"[模拟回答] 根据检索到的文档内容，以下是关于「{query}」的相关信息：\n\n{context.strip()}"

    def query(self, question: str, top_k: int = 3) -> dict:
        """完整的 RAG 查询流程"""
        print(f"\n🔍 问题: {question}")

        # 1. 检索
        print(f"   📥 检索相关文档...")
        retrieved_docs = self.retrieve(question, top_k)
        print(f"   ✅ 找到 {len(retrieved_docs)} 个相关文档")

        # 2. 生成
        print(f"   🤖 生成回答...")
        answer = self.generate(question, retrieved_docs)

        return {
            "question": question,
            "answer": answer,
            "retrieved_docs": retrieved_docs
        }


def create_sample_documents() -> List[Document]:
    """创建示例文档"""
    documents = [
        Document(
            page_content="""
            Python 是一种高级编程语言，由 Guido van Rossum 于 1991 年首次发布。
            Python 语法简洁清晰，注重代码可读性。它支持多种编程范式，包括结构化、
            过程式、反射式、面向对象和函数式编程。Python 拥有丰富而强大的标准库，
            被称为"内置电池"的理念。
            """,
            metadata={"source": "python_intro", "topic": "python"}
        ),
        Document(
            page_content="""
            机器学习是人工智能的一个分支，专门研究计算机怎样模拟或实现人类的学习行为，
            以获取新的知识或技能，重新组织已有的知识结构使之不断改善自身的性能。
            机器学习算法包括监督学习、无监督学习和强化学习等类型。
            """,
            metadata={"source": "ml_intro", "topic": "machine_learning"}
        ),
        Document(
            page_content="""
            深度学习是机器学习的一个分支，它是一种以人工神经网络为架构，
            对数据进行表征学习的算法。深度学习在计算机视觉、语音识别、
            自然语言处理等领域取得了突破性进展。常见的深度学习模型包括
            卷积神经网络(CNN)、循环神经网络(RNN)和Transformer等。
            """,
            metadata={"source": "dl_intro", "topic": "deep_learning"}
        ),
        Document(
            page_content="""
            RAG (Retrieval-Augmented Generation) 检索增强生成，是一种结合检索系统和
            生成模型的技术。RAG 可以让生成模型访问外部知识库，从而生成更准确、
            更具时效性的回答。RAG 系统通常包括文档加载、向量化、向量存储、
            相似度检索和生成回答等步骤。
            """,
            metadata={"source": "rag_intro", "topic": "rag"}
        ),
        Document(
            page_content="""
            大语言模型(Large Language Model, LLM) 是基于大规模文本数据训练的
            深度学习模型，具有强大的语言理解和生成能力。常见的 LLM 包括 GPT、
            Claude、LLaMA 等。LLM 可以用于问答、文本生成、代码编写等多种任务。
            """,
            metadata={"source": "llm_intro", "topic": "llm"}
        ),
    ]
    return documents


def demo_with_mock_llm():
    """
    使用模拟 LLM 的演示版本
    (不需要下载大模型，快速体验 RAG 流程)
    """
    print("=" * 60)
    print("🎯 RAG 演示 - 使用模拟 LLM")
    print("=" * 60)

    # 1. 初始化 RAG
    print("\n📦 初始化 RAG 系统...")
    rag = SimpleRAG(
        embedding_model="sentence-transformers/all-MiniLM-L6-v2",
        vector_store_type="chroma"
    )

    # 2. 加载模型
    rag.load_embedding_model()
    rag.load_vector_store()

    # 3. 加载文档
    documents = create_sample_documents()
    rag.load_documents(documents)

    # 4. 演示查询
    print("\n" + "=" * 60)
    print("💬 开始问答演示")
    print("=" * 60)

    questions = [
        "什么是 Python?",
        "机器学习和深度学习有什么区别?",
        "RAG 技术是什么?",
    ]

    for q in questions:
        result = rag.query(q)
        print(f"\n❓ 问题: {result['question']}")
        print(f"📝 回答: {result['answer']}")
        print(f"📚 参考文档数: {len(result['retrieved_docs'])}")
        print("-" * 40)


def demo_with_real_llm():
    """
    使用真实 LLM 的完整版本
    (需要下载模型，首次运行较慢)
    """
    print("=" * 60)
    print("🎯 RAG 完整版 - 使用真实 LLM")
    print("=" * 60)

    # 1. 初始化 RAG
    rag = SimpleRAG(
        embedding_model="sentence-transformers/all-MiniLM-L6-v2",
        generation_model="microsoft/phi-2",  # 可选: TinyLlama/TinyLlama-1.1B-Chat-v1.0
        vector_store_type="chroma",
        device="cpu"  # 或 "cuda" 如果有 GPU
    )

    # 2. 加载所有模型
    rag.load_embedding_model()
    rag.load_llm()
    rag.load_vector_store()

    # 3. 加载文档
    documents = create_sample_documents()
    rag.load_documents(documents)

    # 4. 交互式问答
    print("\n" + "=" * 60)
    print("💬 问答系统已就绪 (输入 'quit' 退出)")
    print("=" * 60)

    while True:
        question = input("\n❓ 请输入问题: ").strip()
        if question.lower() in ['quit', 'q', 'exit']:
            break
        if not question:
            continue

        result = rag.query(question)
        print(f"\n📝 回答:\n{result['answer']}")


# ================== 主程序 ==================
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="RAG 完整示例")
    parser.add_argument(
        "--mode",
        choices=["demo", "full"],
        default="demo",
        help="demo: 使用模拟 LLM 快速体验; full: 使用真实 LLM"
    )
    parser.add_argument(
        "--model",
        default="microsoft/phi-2",
        help="LLM 模型名称 (full 模式有效)"
    )
    args = parser.parse_args()

    if args.mode == "demo":
        demo_with_mock_llm()
    else:
        demo_with_real_llm()%
  ```
