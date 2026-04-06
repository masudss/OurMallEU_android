# OurMallEU (Android)

## Overview
OurMallEU is an Android multi-vendor ecommerce prototype built with Jetpack Compose. It features a searchable and filterable product catalog, multi-vendor cart management, checkout flows, and order history with item-level tracking and cancellation. `AppState` serves as the single source of truth for the application state.

## Technologies and Architecture
The project uses Kotlin and Jetpack Compose for a modern declarative UI. It follows an MVVM-inspired architecture where `AppState` (ViewModel) manages business logic and state. Networking is handled by Retrofit and GSON, with Coil for image loading.

## Structure
- **ui/**: Contains Compose screens and components (Catalog, Cart, Payment, etc.).
- **models/**: Domain models for Products, Cart, Orders, and Filtering.
- **viewmodels/**: `AppState` manages catalog loading, filtering, cart logic, and order persistence.
- **services/**: `CommerceAPIClient` handles Retrofit network communication.
- **test/**: JUnit tests for `AppState` logic and filtering.

## Product List Logic
Products are fetched via `AppState.refreshProducts()` from a Beeceptor endpoint. The UI includes a search bar and a dynamic filter bottom sheet. Filtering is performed in `AppState`, matching against names, vendors, and the `category: List<String>` field.

## Cart, Checkout, and Payment
The cart supports multiple vendors, grouping items by vendor to allow independent selection for checkout. Checkout totals include VAT and discount calculations. Payment submission simulates a network request, creates a grouped `Order`, and clears the purchased items from the cart.

## Orders and Cancellation
Orders are tracked via `vendorGroups`, enabling granular control. Users can cancel at three levels: individual item, vendor-specific sub-order, or the entire order. `AppState` calculates refunds based on the active items in the selected cancellation scope.

## API Summary
- **Base URL**: `https://mp160a575ce3a6471b72.free.beeceptor.com/`
- **Endpoints**: `/data` returns a `ProductListResponse`.
- **Product Model**: Includes `id`, `name`, `category` (array), `price`, `discountPercentage`, and vendor details.
- **Payment Payload**: Grouped by `vendorId`, containing product selections and a summary of totals.
