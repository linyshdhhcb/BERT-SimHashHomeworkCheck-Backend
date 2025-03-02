from transformers import BertTokenizer, BertModel
import torch
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
import sys
import os
from concurrent.futures import ThreadPoolExecutor

# 配置
sys.stdout.reconfigure(encoding='utf-8')
#model_path = 'C:/Intellij_IDEA/DEV/BERT-SimHashHomeworkCheck/src/main/resources/model/BERT-Base-Chinese'
# BERT模型路径
model_path = os.path.normpath(os.path.join(  # 使用normpath规范化路径
    os.path.dirname(__file__),
    '../../../../../../resources/model/BERT-Base-Chinese'  # 修正层级关系
)).replace('\\', '/')  # 统一使用Linux风格路径
MAX_CHUNK_LENGTH = 510
BATCH_SIZE = 512
USE_FP16 = True

# 初始化模型
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
tokenizer = BertTokenizer.from_pretrained(model_path)
model = BertModel.from_pretrained(model_path).to(device)

# 统一设置模型精度（不要在函数内部动态修改）
if USE_FP16 and torch.cuda.is_available():
    model = model.half()  # 整个模型统一用半精度
else:
    model = model.float()  # 整个模型统一用单精度

model.eval()

# 预定义特殊token ID
cls_token_id = tokenizer.cls_token_id
sep_token_id = tokenizer.sep_token_id
pad_token_id = tokenizer.pad_token_id

def chunk_text(text):
    """多线程分块处理（CPU密集型操作）"""
    input_ids = tokenizer.encode(text, add_special_tokens=False)
    return [
        input_ids[i:i + MAX_CHUNK_LENGTH]
        for i in range(0, len(input_ids), MAX_CHUNK_LENGTH)
    ]

def process_batch(batch_chunks):
    """关键修复：移除对model变量的修改"""
    # 添加特殊符号并填充
    batch_inputs, batch_masks = [], []
    for chunk in batch_chunks:
        chunk = [cls_token_id] + chunk + [sep_token_id]
        chunk = chunk[:512] + [pad_token_id]*(512 - len(chunk)) if len(chunk) < 512 else chunk[:512]
        mask = [1 if t != pad_token_id else 0 for t in chunk]
        batch_inputs.append(chunk)
        batch_masks.append(mask)

    # 根据模型精度设置数据类型
    dtype = torch.float16 if USE_FP16 and torch.cuda.is_available() else torch.float32
    input_tensor = torch.tensor(batch_inputs, dtype=torch.long).to(device)
    mask_tensor = torch.tensor(batch_masks, dtype=torch.long).to(device)

    # 统一使用已设置精度的模型进行推理
    with torch.no_grad():
        if USE_FP16 and torch.cuda.is_available():
            with torch.cuda.amp.autocast():
                outputs = model(input_ids=input_tensor, attention_mask=mask_tensor)
        else:
            outputs = model(input_ids=input_tensor, attention_mask=mask_tensor)

    cls_embeddings = outputs.last_hidden_state[:, 0, :]
    return torch.mean(cls_embeddings, dim=0).cpu().numpy()

def get_text_embedding(text):
    """并行分块+批量处理"""
    # Step 1: 多线程分块
    with ThreadPoolExecutor() as executor:
        chunk_future = executor.submit(chunk_text, text)
        chunks = chunk_future.result()

    # Step 2: 分批处理
    if not chunks:
        return np.zeros(768)

    # 根据BATCH_SIZE切分大块
    batch_embeddings = []
    for i in range(0, len(chunks), BATCH_SIZE):
        batch = chunks[i:i+BATCH_SIZE]
        batch_embedding = process_batch(batch)
        batch_embeddings.append(batch_embedding)

    # 合并批次结果
    return np.mean(batch_embeddings, axis=0)

def compute_similarity(text1, text2):
    """并行处理两个文本"""
    with ThreadPoolExecutor(max_workers=2) as executor:
        future1 = executor.submit(get_text_embedding, text1)
        future2 = executor.submit(get_text_embedding, text2)
        vec1 = future1.result()
        vec2 = future2.result()
    return cosine_similarity([vec1], [vec2])[0][0]

# 命令行处理部分保持不变...
if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Error: Expected 2 text arguments.")
        sys.exit(1)

    text1 = sys.argv[1]
    text2 = sys.argv[2]
    similarity = compute_similarity(text1, text2)
    print(f"文本相似度: {similarity:.4f}")

