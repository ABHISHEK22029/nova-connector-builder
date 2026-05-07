import chromadb
client = chromadb.PersistentClient(path="./chroma_db")
print("Collections:")
for col in client.list_collections():
    print(f"- {col.name}: {col.count()} chunks")
