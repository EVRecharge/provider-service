# Provider Service - Independent Microservice

## Overview
Provider Service manages EV charging provider registration, profile management, and location-based queries.

## Service Details
- **Port**: 8002
- **Database**: PostgreSQL (ev_provider_db)
- **Version**: 1.0.0

## Quick Start
```bash
cd provider-service
docker-compose up -d
```

## API Endpoints
- POST /api/providers/register - Register new provider
- GET /api/providers - Get all providers
- GET /api/providers/{id} - Get provider by ID
- GET /api/providers/city/{city} - Get providers by city
- PUT /api/providers/{id} - Update provider
- DELETE /api/providers/{id} - Delete provider

## Database Schema
- providers table with company info, location, rates, charger details
- Indexes on email, city, location coordinates

## Configuration
Edit .env file to configure:
- DB_HOST, DB_PORT, DB_NAME
- DB_USER, DB_PASSWORD
- EUREKA_SERVER
- JWT_SECRET

## Features
- Provider registration with OAuth
- Location-based search
- Rate and charger information
- Soft delete (mark inactive)
- Service discovery via Eureka

## Testing
```bash
curl -X POST http://localhost:8002/api/providers/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "FastCharge",
    "contactPersonName": "John Doe",
    "email": "john@fastcharge.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94102",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "ratePerHour": 50.0,
    "chargerType": "DC Fast"
  }'
```

## Deployment
```bash
docker build -t provider-service:1.0.0 .
docker push provider-service:1.0.0
docker-compose up -d
```

## Monitoring
- Health: http://localhost:8002/actuator/health
- Metrics: http://localhost:8002/actuator/metrics
- Logs: logs/provider-service.log
