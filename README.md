# Catalog Service

E-commerce Catalog Microservice built with Java 22, Spring Boot 3.2, and microservices architecture.

## Overview

The Catalog Service is responsible for managing product catalogs, categories, and product information. It integrates with the Inventory Service to fetch real-time stock quantities.

## Features

- **Product Management**: CRUD operations for products
- **Category Management**: Hierarchical category structure with parent-child relationships
- **Product Search**: Advanced search with filters (name, category, price range)
- **Inventory Integration**: Real-time inventory data via Feign client
- **Product Attributes**: Flexible attribute system for product variations
- **Product Images**: Multiple images per product with primary image support
- **Pagination**: Efficient pagination for large product catalogs

## Technology Stack

- **Java 22**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Cloud OpenFeign** (for Inventory Service integration)
- **PostgreSQL** (production) / **H2** (development)
- **MapStruct** (DTO mapping)
- **Lombok** (boilerplate reduction)
- **Maven** (build tool)

## Prerequisites

- Java 22
- Maven 3.6+
- PostgreSQL 12+ (for production)
- Inventory Service running (for full functionality)

## Getting Started

### 1. Clone and Build

```bash
cd catalog-service
mvn clean install
```

### 2. Configure Database

Update `application.yml` with your PostgreSQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_catalog
    username: your_username
    password: your_password
```

For development, you can use H2 in-memory database by using the `dev` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Configure Inventory Service

Set the Inventory Service URL in `application.yml` or as environment variable:

```yaml
inventory:
  service:
    url: http://localhost:8081
```

Or via environment variable:
```bash
export INVENTORY_SERVICE_URL=http://localhost:8081
```

### 4. Run the Service

```bash
mvn spring-boot:run
```

The service will start on port `8082` by default.

## API Endpoints

### Products

- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `GET /api/products/search` - Search products with filters
- `GET /api/products/category/{categoryId}` - Get products by category
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `PATCH /api/products/{id}/deactivate` - Deactivate product

### Categories

- `GET /api/categories` - Get all categories
- `GET /api/categories/roots` - Get root categories (with tree structure)
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/slug/{slug}` - Get category by slug
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category
- `PATCH /api/categories/{id}/deactivate` - Deactivate category

## Example API Calls

### Create a Category

```bash
curl -X POST http://localhost:8082/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "description": "Electronic products",
    "isActive": true
  }'
```

### Create a Product

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ELEC-001",
    "name": "Smartphone",
    "description": "Latest smartphone model",
    "price": 599.99,
    "categoryId": 1,
    "inventoryId": 1,
    "isActive": true,
    "isVisible": true,
    "attributes": [
      {
        "name": "Color",
        "value": "Black",
        "displayOrder": 1
      }
    ],
    "images": [
      {
        "imageUrl": "https://example.com/image.jpg",
        "altText": "Smartphone image",
        "isPrimary": true,
        "displayOrder": 1
      }
    ]
  }'
```

### Search Products

```bash
curl "http://localhost:8082/api/products/search?name=phone&minPrice=100&maxPrice=1000&page=0&size=20"
```

## Database Schema

### Products Table
- `id` (Primary Key)
- `sku` (Unique)
- `name`
- `description`
- `price`
- `category_id` (Foreign Key)
- `inventory_id` (Reference to Inventory Service)
- `is_active`
- `is_visible`
- `created_at`
- `updated_at`

### Categories Table
- `id` (Primary Key)
- `name` (Unique)
- `description`
- `slug` (Unique)
- `parent_category_id` (Self-referencing Foreign Key)
- `is_active`
- `display_order`
- `created_at`
- `updated_at`

### Product Attributes Table
- `id` (Primary Key)
- `product_id` (Foreign Key)
- `name`
- `value`
- `display_order`

### Product Images Table
- `id` (Primary Key)
- `product_id` (Foreign Key)
- `image_url`
- `alt_text`
- `is_primary`
- `display_order`

## Integration with Inventory Service

The Catalog Service uses Spring Cloud OpenFeign to communicate with the Inventory Service. When fetching products, it automatically enriches them with inventory data (available quantity).

The Inventory Service should expose the following endpoints:
- `GET /api/inventory/{inventoryId}` - Get inventory by ID
- `GET /api/inventory/sku/{sku}` - Get inventory by SKU
- `GET /api/inventory/batch?inventoryIds=...` - Get multiple inventories

## Health Check

The service exposes health check endpoints via Spring Boot Actuator:

```bash
curl http://localhost:8082/actuator/health
```

## Development

### Running Tests

```bash
mvn test
```

### Building Docker Image

```bash
docker build -t catalog-service:1.0.0 .
```

## Configuration

Key configuration properties:

- `server.port`: Service port (default: 8082)
- `spring.datasource.*`: Database configuration
- `inventory.service.url`: Inventory Service URL
- `logging.level.*`: Logging levels

## Future Enhancements

- Caching layer (Redis) for frequently accessed products
- Elasticsearch integration for advanced search
- Product recommendations
- Product reviews and ratings
- Multi-language support
- Product variants (size, color, etc.)

## License

This project is part of an e-commerce microservices architecture.

# catalog-service
