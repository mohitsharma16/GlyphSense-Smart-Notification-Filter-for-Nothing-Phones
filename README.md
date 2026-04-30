# GlyphSense – Smart Notification Filtering for Nothing Glyph

GlyphSense is an intelligent notification system that uses the Nothing Glyph interface to help users decide whether to check their phone or ignore notifications — without unlocking the screen.

## 🚀 Problem

Modern smartphones overwhelm users with notifications. Most of them are not important, but users still feel compelled to check their phones.

## 💡 Solution

GlyphSense classifies notifications and converts them into meaningful Glyph light patterns:

* 🔴 Important → Strong pulse (check immediately)
* 🟡 Normal → Soft glow (can wait)
* ⚫ Silent → No light (ignore)

## ⚙️ How it Works

Notification → Classification → Glyph Output

* Uses Android NotificationListenerService
* App-based classification system
* Real-time Glyph rendering using Nothing Glyph SDK

## 📱 Features

* Works passively in the background
* No need to unlock phone
* Reduces distraction
* Smart default classification

## 🧠 Tech Stack

* Kotlin
* Android Services
* Nothing Glyph Developer Kit (SDK)
* Coroutines

## 📦 APK

(Add link here)

## 🔧 Future Improvements

* User-defined app priorities
* Keyword-based classification
* AI-based importance detection

## 👨‍💻 Author

Mohit Sharma
