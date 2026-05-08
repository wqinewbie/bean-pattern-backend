#!/bin/bash

# AI生成Mock接口测试脚本

BASE_URL="http://localhost:8080/api"
SESSION_ID="test-session-123"

echo "=========================================="
echo "AI生成Mock接口测试"
echo "=========================================="
echo ""

# 1. 测试创建任务
echo "1. 创建AI生成任务..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/ai/generate" \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: ${SESSION_ID}" \
  -d '{
    "prompt": "一只可爱的小猫",
    "style": "宠物毛发",
    "size": 64,
    "brand": "MARD",
    "colorCount": 48
  }')

echo "响应: $RESPONSE"
echo ""

# 提取taskId
TASK_ID=$(echo $RESPONSE | grep -o '"taskId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TASK_ID" ]; then
  echo "❌ 创建任务失败"
  exit 1
fi

echo "✅ 任务创建成功，taskId: $TASK_ID"
echo ""

# 2. 立即查询状态（应该是PENDING）
echo "2. 查询任务状态（立即）..."
curl -s "${BASE_URL}/ai/task/${TASK_ID}" | jq '.'
echo ""

# 3. 等待2秒
echo "3. 等待2秒..."
sleep 2
echo ""

# 4. 再次查询状态（应该是PENDING或PROCESSING）
echo "4. 查询任务状态（2秒后）..."
curl -s "${BASE_URL}/ai/task/${TASK_ID}" | jq '.'
echo ""

# 5. 等待2秒
echo "5. 等待2秒..."
sleep 2
echo ""

# 6. 最后查询状态（应该是SUCCESS）
echo "6. 查询任务状态（4秒后）..."
FINAL_RESPONSE=$(curl -s "${BASE_URL}/ai/task/${TASK_ID}")
echo $FINAL_RESPONSE | jq '.'
echo ""

# 检查是否成功
STATUS=$(echo $FINAL_RESPONSE | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$STATUS" = "SUCCESS" ]; then
  echo "✅ 测试通过！任务已成功完成"
  IMAGE_URL=$(echo $FINAL_RESPONSE | grep -o '"aiImageUrl":"[^"]*"' | cut -d'"' -f4)
  echo "图片URL: $IMAGE_URL"
else
  echo "⚠️  任务状态: $STATUS"
fi

echo ""
echo "=========================================="
echo "测试完成"
echo "=========================================="
