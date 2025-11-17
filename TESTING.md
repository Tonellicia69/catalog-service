# Testing Guide for Catalog Service

This guide provides step-by-step instructions to test if the catalog-service is working properly.

## Prerequisites

- Java 22 installed
- Maven 3.6+ installed (or Docker if using Docker Compose)
- PostgreSQL 15+ (for production testing) or use H2 in-memory DB for quick testing

## Option 1: Quick Test with H2 In-Memory Database (Recommended for First Test)

### Step 1: Build the Application

```bash
cd catalog-service
mvn clean install -DskipTests
```

**Note:** If you see errors about `<n>` tag in pom.xml, first fix line 18 in pom.xml from `<n>` to `<name>`.

### Step 2: Run with Dev Profile (H2 Database)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service should start on port 8082 with an H2 in-memory database.

### Step 3: Test Health Endpoint

Open a new terminal and test the health endpoint:

```bash
curl http://localhost:8082/actuator/health
```

You should see:
```json
{"status":"UP"}
```

### Step 4: Test API Endpoints

#### Create a Category

```bash
curl -X POST http://localhost:8082/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "description": "Electronic products",
    "isActive": true
  }'
```

Save the `id` from the response (e.g., `"id": 1`).

#### Get All Categories

```bash
curl http://localhost:8082/api/categories
```

#### Create a Product

Replace `categoryId` with the ID from the category you created:

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ELEC-001",
    "name": "Smartphone",
    "description": "Latest smartphone model",
    "price": 599.99,
    "categoryId": 1,
    "isActive": true,
    "isVisible": true
  }'
```

#### Get All Products

```bash
curl http://localhost:8082/api/products
```

#### Get Product by ID

Replace `1` with the product ID from the create response:

```bash
curl http://localhost:8082/api/products/1
```

#### Search Products

```bash
curl "http://localhost:8082/api/products/search?name=phone"
```

## Option 2: Test with Docker Compose (PostgreSQL)

### Step 1: Build and Start Services

```bash
cd catalog-service
docker-compose up --build
```

This will:
- Start PostgreSQL container
- Build and start the catalog-service container
- Wait for PostgreSQL to be healthy before starting the service

### Step 2: Check Logs

```bash
docker-compose logs -f catalog-service
```

Look for:
```
Started CatalogServiceApplication in X.XXX seconds
```

### Step 3: Test Health Endpoint

```bash
curl http://localhost:8082/actuator/health
```

### Step 4: Test API Endpoints

Use the same curl commands from Option 1, Step 4.

## Option 3: Manual Local Setup with PostgreSQL

### Step 1: Start PostgreSQL

```bash
# Using Docker
docker run --name catalog-postgres \
  -e POSTGRES_DB=ecommerce_catalog \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15-alpine
```

### Step 2: Update application.yml (if needed)

The default configuration should work:
- URL: `jdbc:postgresql://localhost:5432/ecommerce_catalog`
- Username: `postgres`
- Password: `postgres`

### Step 3: Build and Run

```bash
cd catalog-service
mvn clean install -DskipTests
mvn spring-boot:run
```

### Step 4: Test

Use the same test commands from Option 1, Step 4.

## Expected Test Results

### ✅ Success Indicators

1. **Application Starts**: No errors in logs, service starts on port 8082
2. **Health Check**: `http://localhost:8082/actuator/health` returns `{"status":"UP"}`
3. **Category Creation**: Returns 201 Created with category data
4. **Product Creation**: Returns 201 Created with product data (without inventory quantity)
5. **Product Retrieval**: Returns product data successfully
6. **Search**: Returns filtered products

### ❌ Common Issues

1. **Port Already in Use**: Another service is using port 8082
   - Solution: Change port in `application.yml` or stop the other service

2. **Database Connection Error**: Can't connect to PostgreSQL
   - Solution: Check if PostgreSQL is running and credentials are correct

3. **Build Fails**: Maven build errors
   - Check Java version: `java -version` should show Java 22
   - Check Maven version: `mvn -version` should show Maven 3.6+
   - Fix pom.xml line 18 if you see `<n>` tag errors

4. **Feign Client Errors**: If inventory service is not running, you'll see warnings when fetching products with inventory IDs
   - This is expected and logged as warnings, not errors

## Quick Verification Checklist

- [ ] Service starts without errors
- [ ] Health endpoint returns UP
- [ ] Can create a category
- [ ] Can retrieve all categories
- [ ] Can create a product
- [ ] Can retrieve all products
- [ ] Can search products
- [ ] Can get product by ID
- [ ] Can get product by SKU

## Advanced Testing

### Test Pagination

```bash
curl "http://localhost:8082/api/products?page=0&size=10&sortBy=name&direction=ASC"
```

### Test with Multiple Categories and Products

Create a category hierarchy:

```bash
# Create parent category
curl -X POST http://localhost:8082/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics", "isActive": true}'

# Create child category (replace PARENT_ID)
curl -X POST http://localhost:8082/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Smartphones", "parentCategoryId": PARENT_ID, "isActive": true}'
```

### Test Error Handling

Try creating a duplicate SKU:

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ELEC-001",
    "name": "Duplicate Product",
    "price": 100.00,
    "isActive": true
  }'
```

Should return 400 Bad Request with an error message.

## Stopping the Service

### If running with Maven:
Press `Ctrl+C`

### If running with Docker Compose:
```bash
docker-compose down
```

### If running PostgreSQL separately:
```bash
docker stop catalog-postgres
docker rm catalog-postgres
```

