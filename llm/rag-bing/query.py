from langchain_chroma import Chroma
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_classic.chains import create_retrieval_chain
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_core.prompts import ChatPromptTemplate
from config import CHROMA_DIR, OLLAMA_BASE_URL, EMBED_MODEL, LLM_MODEL, TOP_K


def create_rag_chain():
    embeddings = OllamaEmbeddings(
        model=EMBED_MODEL,
        base_url=OLLAMA_BASE_URL,
    )
    vector_store = Chroma(
        embedding_function=embeddings,
        persist_directory=str(CHROMA_DIR),
    )
    retriever = vector_store.as_retriever(
        search_kwargs={"k": TOP_K}
    )

    llm = ChatOllama(
        model=LLM_MODEL,
        base_url=OLLAMA_BASE_URL,
        temperature=0.1,
    )

    prompt = ChatPromptTemplate.from_template(
        "You are an AI assistant. Answer based on the context.\n\n"
        "Context:\n{context}\n\n"
        "Question:\n{input}"
    )

    combine_docs_chain = create_stuff_documents_chain(llm, prompt)
    return create_retrieval_chain(retriever, combine_docs_chain)


def main():
    print(f"Loading RAG system... (LLM: {LLM_MODEL}, Embed: {EMBED_MODEL})")
    chain = create_rag_chain()

    print("\nReady! Type your question (or 'quit' to exit)\n")
    while True:
        try:
            query = input(">>> ").strip()
        except (EOFError, KeyboardInterrupt):
            break
        if not query:
            continue
        if query.lower() in ("quit", "exit", "q"):
            break

        result = chain.invoke({"input": query})
        print(f"\n{result['answer']}\n")


if __name__ == "__main__":
    main()
