# OurMallEU (Android)

## Overview
OurMallEU is a Jetpack Compose multi-vendor ecommerce app prototype. It includes a splash screen, a searchable and filterable product catalog, configurable product detail pages, a multi-vendor cart, checkout and payment flows, and an order history experience with tracking, cancellation, and refund feedback. The app follows a modular architecture where domain-specific logic is encapsulated in dedicated ViewModels, with `AppState` serving as a Facade and orchestrator for the entire application state.

## Technologies and Architecture
The project is built with Kotlin and Jetpack Compose, and uses JUnit for unit tests. Backend work is handled with Coroutines, and the app follows a clean MVVM-style structure:
- **Models**: Domain-specific data structures (Product, Cart, Order, Payment) located in `eu.ourmall.models.[domain]`.
- **ViewModels**: Business logic decomposed into domain-focused units (`ProductViewModel`, `CartViewModel`, `OrderViewModel`, `PaymentViewModel`) under `eu.ourmall.viewmodels.[domain]`.
- **AppState**: An orchestrator/facade that delegates domain logic to sub-ViewModels while managing navigation and high-level application state.
- **Service Layer**: `CommerceAPIClient` handles network communication, with client-side pagination for the product catalog.

## Structure
`MainActivity.kt` bootstraps the app and manages navigation using a stack-based approach. 
- `eu.ourmall.models`: Contains the core domain types, now partitioned by domain.
- `eu.ourmall.viewmodels`: Contains the business logic. Decomposed from a monolithic state into domain-specific ViewModels.
- `eu.ourmall.ui`: Partitioned into `screens` and reusable `components` for each domain.
- `src/test`: Contains a comprehensive suite of unit tests, also modularized to match the production code structure.

## Product List Logic
The product list screen loads catalog data through `ProductViewModel` (accessed via `AppState`). It supports client-side pagination via `loadNextPageIfNeeded`. At the top of the screen, users can search by keyword and open filters for category, price range, and stock availability. Below that, the screen displays a rotating carousel and then a two-column product grid. Filtering logic matches against product name, vendor name, summary, and categories.

## Cart, Multi-Vendor Checkout, and Payment
Cart items are managed by `CartViewModel`. Items are keyed by product ID plus selected options. The cart groups items into vendor-based sections so that each vendor can be selected independently for checkout. Prices are strictly formatted in Naira (₦) using `CurrencyUtils`. When payment is submitted, `AppState` orchestrates the process by building a vendor-grouped request, triggering `PaymentViewModel` for processing, persisting the result via `OrderViewModel`, and clearing the cart.

## Orders, Vendor Split, and Cancellation
Orders are managed by `OrderViewModel` and remain grouped by vendor through `vendorGroups`. This supports vendor-based summaries, vendor-level cancellation, and vendor-specific refund handling. The orders screen separates orders into `In progress` and `Settled`. Cancellation is supported at item level, vendor level, and full-order level. Refund amounts are calculated dynamically for the affected active items.

## Getting Started

### Prerequisites
- **Android Studio** (Koala or later recommended)
- **JDK 17**
- **Android SDK 34**

### Cloning and Building
1. Clone the repository:
2. Open the project in Android Studio.
3. Wait for Gradle sync to complete.
4. Build the project:
   - Go to `Build > Make Project` or run `./gradlew assembleDebug` in the terminal.

### Running the App
- **Emulator/Device**: Select your device from the dropdown in the toolbar and click the **Run** (green play) button.
- **Unit Tests**: Right-click the `src/test` folder and select `Run 'Tests in eu.ourmall'` or run `./gradlew test`.

### Generating an APK
To generate a debug APK for manual installation:
1. In Android Studio, go to `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
2. Once finished, a notification will appear. Click **locate** to find the `app-debug.apk` file.
3. Alternatively, run the following command in the terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

## API Summary
The product endpoint uses the base URL `https://mp160a575ce3a6471b72.free.beeceptor.com` and fetches products from `/data`. The response contains a `products` array with full metadata including `id`, `name`, `category`, `vendor`, `price`, and `options`.

```json
{
  "products": [
    {
      "id": "shoe-1",
      "name": "Aero Runner",
      "category": ["clothing"],
      "imageURL": "https://...",
      "vendor": { "id": "vendor-a", "name": "BluePeak Sports" },
      "price": 120,
      "discountPercentage": 15,
      "offerEndsAt": "2026-04-08T13:00:00Z",
      "quantityRemaining": 8,
      "summary": "Lightweight running shoes...",
      "options": [
        { "name": "size", "values": ["S", "M", "L"] }
      ],
      "status": "PENDING"
    }
  ]
}
```

Payment submission sends a vendor-grouped payload containing product details and a summary section for subtotal, discount, VAT, and grand total.

```json
{
  "vendors": {
    "vendor-a": [
      {
        "productId": "shoe-1",
        "quantity": 2,
        "unitPrice": 102.0,
        "selectedOptions": { "size": "M", "color": "Red" }
      }
    ]
  },
  "summary": {
    "subtotal": 204.0,
    "discount": 36.0,
    "vat": 15.3,
    "grandTotal": 219.3
  }
}
```

Cancellation logic is handled within the `OrderViewModel`, supporting three scopes: item, vendor, and full order.
