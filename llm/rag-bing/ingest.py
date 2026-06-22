from pathlib import Path
from langchain_community.document_loaders import (
    PyPDFLoader,
    TextLoader,
    UnstructuredMarkdownLoader,
    CSVLoader,
    Docx2txtLoader,
)
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_ollama import OllamaEmbeddings
from config import DATA_DIR, CHROMA_DIR, OLLAMA_BASE_URL, EMBED_MODEL, CHUNK_SIZE, CHUNK_OVERLAP


def load_documents(data_dir: Path):
    docs = []
    loaders = {
        ".pdf": PyPDFLoader,
        ".txt": TextLoader,
        ".md": UnstructuredMarkdownLoader,
        ".csv": CSVLoader,
        ".docx": Docx2txtLoader,
    }
    for ext, loader_cls in loaders.items():
        for fpath in data_dir.rglob(f"*{ext}"):
            if fpath.name.startswith("."):
                continue
            loader = loader_cls(str(fpath))
            docs.extend(loader.load())
            print(f"  Loaded: {fpath.name}")
    return docs


def main():
    print("Documents loading...")
    docs = load_documents(DATA_DIR)
    if not docs:
        print("No documents found in data/ directory.")
        return

    print(f"Loaded {len(docs)} document chunks, splitting...")
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP,
    )
    chunks = splitter.split_documents(docs)
    print(f"Split into {len(chunks)} chunks")

    print("Creating embeddings and storing in ChromaDB...")
    embeddings = OllamaEmbeddings(
        model=EMBED_MODEL,
        base_url=OLLAMA_BASE_URL,
    )
    Chroma.from_documents(
        documents=chunks,
        embedding=embeddings,
        persist_directory=str(CHROMA_DIR),
    )
    print(f"Done! Vector store saved to {CHROMA_DIR}")


if __name__ == "__main__":
    main()
