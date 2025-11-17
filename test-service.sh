#!/bin/bash

# Quick test script for catalog-service
# Usage: ./test-service.sh

BASE_URL="http://localhost:8082"

echo "=== Testing Catalog Service ==="
echo ""

# Test 1: Health Check
echo "1. Testing health endpoint..."
HEALTH=$(curl -s "$BASE_URL/actuator/health")
if [[ $HEALTH == *"UP"* ]]; then
    echo "✅ Health check passed: $HEALTH"
else
    echo "❌ Health check failed: $HEALTH"
    echo "   Is the service running on port 8082?"
    exit 1
fi
echo ""

# Test 2: Create Category
echo "2. Creating a test category..."
CATEGORY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Electronics",
    "description": "Test category for electronics",
    "isActive": true
  }')

CATEGORY_ID=$(echo $CATEGORY_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [[ ! -z "$CATEGORY_ID" ]]; then
    echo "✅ Category created successfully (ID: $CATEGORY_ID)"
    echo "   Response: $CATEGORY_RESPONSE"
else
    echo "❌ Category creation failed: $CATEGORY_RESPONSE"
    exit 1
fi
echo ""

# Test 3: Get All Categories
echo "3. Fetching all categories..."
CATEGORIES=$(curl -s "$BASE_URL/api/categories")
if [[ $CATEGORIES == *"Test Electronics"* ]]; then
    echo "✅ Categories retrieved successfully"
    echo "   Response: $CATEGORIES" | head -c 200
    echo "..."
else
    echo "❌ Failed to retrieve categories: $CATEGORIES"
    exit 1
fi
echo ""

# Test 4: Create Product
echo "4. Creating a test product..."
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d "{
    \"sku\": \"TEST-$(date +%s)\",
    \"name\": \"Test Product\",
    \"description\": \"A test product\",
    \"price\": 99.99,
    \"categoryId\": $CATEGORY_ID,
    \"isActive\": true,
    \"isVisible\": true
  }")

PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [[ ! -z "$PRODUCT_ID" ]]; then
    echo "✅ Product created successfully (ID: $PRODUCT_ID)"
    echo "   Response: $PRODUCT_RESPONSE" | head -c 200
    echo "..."
else
    echo "❌ Product creation failed: $PRODUCT_RESPONSE"
    exit 1
fi
echo ""

# Test 5: Get All Products
echo "5. Fetching all products..."
PRODUCTS=$(curl -s "$BASE_URL/api/products?page=0&size=10")
if [[ $PRODUCTS == *"Test Product"* ]]; then
    echo "✅ Products retrieved successfully"
    echo "   Response: $PRODUCTS" | head -c 200
    echo "..."
else
    echo "❌ Failed to retrieve products: $PRODUCTS"
    exit 1
fi
echo ""

# Test 6: Get Product by ID
echo "6. Fetching product by ID..."
PRODUCT_BY_ID=$(curl -s "$BASE_URL/api/products/$PRODUCT_ID")
if [[ $PRODUCT_BY_ID == *"Test Product"* ]]; then
    echo "✅ Product by ID retrieved successfully"
    echo "   Response: $PRODUCT_BY_ID" | head -c 200
    echo "..."
else
    echo "❌ Failed to retrieve product by ID: $PRODUCT_BY_ID"
    exit 1
fi
echo ""

echo "=== All tests passed! ==="
echo ""
echo "The service is working correctly. You can now:"
echo "  - View all products: curl $BASE_URL/api/products"
echo "  - View all categories: curl $BASE_URL/api/categories"
echo "  - Check health: curl $BASE_URL/actuator/health"

