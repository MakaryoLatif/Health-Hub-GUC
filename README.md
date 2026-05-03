# HealthHub GUC: Telemedicine & Pharmacy Platform

> **Video Demonstrations:** Full video walkthroughs of the application in action are available in the `Videos.zip` folder included in this repository.

## Overview
HealthHub is a comprehensive, full-stack Android healthcare application designed to bridge the gap between patients and medical professionals. Developed in Kotlin, the platform serves as a dual-portal system offering real-time VoIP telemedicine consultations, geolocation services, and an integrated e-pharmacy.

The application relies on a robust Firebase backend for secure user authentication, database management, and real-time state synchronization (`google-services.json`).

## Core Features

### 1. Role-Based Portals (Doctor & Patient)
* **Custom Interfaces:** Distinct UI flows and dashboards depending on the authenticated user's role (`Doctor.kt`, `Patient.kt`, `DocProfFragment.kt`).
* **Review & Rating System:** Patients can leave detailed reviews for medical professionals post-consultation (`ReviewFragment.kt`).

### 2. Telehealth & VoIP Consultations
* **Real-Time Voice Calls:** Integrated scheduling and secure **VoIP (Voice over IP)** calling capabilities, allowing patients to consult with doctors seamlessly in real-time over the network (`Call.kt`, `Medical_consult.kt`).

### 3. Integrated E-Pharmacy
* **Medication Browsing:** A dynamic catalog of available medications and healthcare products (`Medication.kt`, `MedicationsAdapter.kt`).
* **Shopping Cart System:** Full e-commerce functionality allowing users to add items, review their cart, and process orders (`Cart.kt`, `CartAdapter.kt`).

### 4. Geolocation & Routing
* **Interactive Maps:** Utilizes mapping APIs to provide location-based services, helping users locate nearby clinics, doctors, or pharmacies (`mapActivity.kt`).

## 🛠️ Architecture & Tech Stack
* **Language:** Kotlin
* **Telecommunication:** VoIP (Voice over IP) for real-time audio consulting.
* **UI Design:** XML Layouts with ViewBinding, utilizing custom drawables and Material Design components (`rounded_edit_text.xml`, `baseline_add_shopping_cart_24.xml`).
* **Architecture:** MVVM patterns utilizing ViewModels (`MedicationsViewModel.kt`) and modular Fragments for a smooth, single-activity-driven user experience.
* **Backend:** Firebase (Authentication, Realtime Database/Firestore).
