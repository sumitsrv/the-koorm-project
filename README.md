# 🧠 OCPD Assistant - Your Compassionate Productivity Companion

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a>

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-orange.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)](https://github.com/JetBrains/compose-multiplatform)
[![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-sa/4.0/)

OCPD Assistant is a specialized productivity application designed to help individuals with Obsessive-Compulsive Personality Disorder (OCPD) manage their perfectionism, time management challenges, and productivity patterns in a healthy, supportive way.

## 🌟 Features

Status Legend: ✅ Implemented (usable) • 🧪 Prototype/Partial • 📝 Planned

The project's mission is OCPD-aware productivity with AI-assisted behavioral support, designed to ship to the App Store and Google Play. Several OCPD/AI foundations exist in code (data models + service stubs); the list below is the working backlog toward a production v1.0.

### Core Productivity
- ✅ Smart Task Management – create, organize, track tasks
- ✅ Intelligent Task Breakdown – heuristic pattern & generic step generation (`TaskBreakdownService`)
- ✅ Perfectionism Control – `GOOD_ENOUGH` task status & encouragement messaging
- ✅ Time Tracking (basic) – Pomodoro session creation & time blocks
- ✅ Priority Management – multi-level priorities
- ✅ Category Organization – Work / Personal / Health / Learning / Social
- 📝 Recurring tasks & natural-language quick input
- 📝 Tags, filters, and full-text search
- 📝 Calendar integration (Google Calendar, Apple Calendar)
- 📝 Quick capture via Android share target & iOS share extension

### OCPD-Specific Support
- ✅ Procrastination Trigger Tracking – `CognitiveInsight` records with procrastination reasons
- ✅ Gentle Notifications – tone-adjusted reminders & celebrations (`NotificationService`)
- ✅ Progress Celebration – completion & "good enough" messaging
- ✅ Flexible Scheduling (basic) – buffer-aware time block generation (`TimeManagementService`)
- 🧪 Behavioral Insights Engine – `WeeklyInsightReport` & recommendation heuristics (early logic only)
- 📝 CBT-inspired thought records & cognitive-reframe library
- 📝 Self-compassion micro-exercises
- 📝 Body-doubling focus mode with ambient soundscapes
- 📝 Region-aware crisis resources & safety links

### AI & Adaptive Intelligence
- 🧪 AI Assistant Integration – simulated breakdown, encouragement & productivity analysis (`AIAssistantService` stub)
- 🧪 Mood Tracking (data layer) – `MoodEntry` model + recording API (no UI/analytics yet)
- 📝 Real LLM integration (Claude API) for task breakdown, reframes, encouragement
- 📝 On-device fallback for sensitive prompts (rule engine / small instruct model)
- 📝 Mood ↔ productivity correlation analytics
- 📝 Adaptive nudge tone & timing personalization from historical patterns

### Data, Sync & Platform
- ✅ Local Data Persistence – shared/common storage layer (local only)
- 📝 SQLDelight-backed local store with versioned migrations
- 📝 End-to-end encrypted optional cloud sync (per-user key, no plaintext at rest)
- 📝 Multi-device conflict resolution
- 📝 Encrypted local backup, export & restore
- 📝 Offline-first architecture with deferred sync queue

### Mobile-Native Experience
- 📝 Android home-screen widget (today's focus + Pomodoro)
- 📝 iOS WidgetKit widgets + Live Activity for active sessions
- 📝 Apple Watch & Wear OS companion (Pomodoro, mood check-in)
- 📝 Voice journaling using platform speech recognition
- 📝 Rich notifications with snooze, do-not-disturb windows & quick actions

### Therapeutic / Well-Being
- 📝 Therapy session export (PDF / encrypted bundle)
- 📝 Stress management micro-interventions (contextual coping prompts)
- 📝 Sleep & energy logging with productivity correlation
- 📝 Shame-free habit streaks tuned for OCPD

### Customization & UX
- ✅ Customizable Interface – adaptable components & tone preferences
- 📝 Material 3 dynamic theming on Android, native look on iOS
- 📝 Full Dynamic Type / font scaling parity
- 📝 Localization: en, es, fr, de, pt-BR, ja (expandable)
- 📝 Full accessibility pass: TalkBack, VoiceOver, contrast, reduced motion

### Quality, Compliance & Distribution
- 📝 Crash reporting (Sentry) + opt-in privacy-respecting analytics
- 📝 GitHub Actions CI/CD: Android AAB → internal track, iOS `.ipa` → TestFlight
- 📝 iOS Privacy Manifest + Play Data Safety disclosures
- 📝 GDPR/CCPA: in-app data export, deletion, and consent flows
- 📝 In-app account deletion (App Store + Play requirement)
- 📝 Hosted privacy policy, terms, and age-rating questionnaires
- 📝 Localized store listings, screenshots & preview videos
- 📝 Beta program: TestFlight + Play Closed Testing cohorts

### Feature Status Summary
- Implemented: 12
- Prototype / Partial: 3
- Planned: 38
- Total Tracked: 53
- Overall Implemented Coverage: 12 / 53 (≈23%)

Notes:
- "Prototype" means an internal heuristic or simulated implementation exists but is not production-ready (no persistence, model inference, or UI polish).
- The Planned set is the full backlog for a polished v1.0 on the App Store and Google Play; not every item is in scope for the first submission — see the Roadmap below for sequencing.
- Update counts whenever any list item changes; keep `Total Tracked` accurate.

> Next incremental milestone: graduate Behavioral Insights + AI Assistant from 🧪 to ✅ by adding persistence, UI surfacing, and evaluation hooks.

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Compose Multiplatform
- **Serialization**: Kotlinx Serialization
- **Coroutines**: Kotlinx Coroutines
- **Date/Time**: Kotlinx DateTime
- **Build System**: Gradle with Kotlin DSL

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or higher
- **Android SDK** (for Android builds)
- **Xcode** (for iOS builds, macOS only)
- **Git**

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd TheKoormProject
   ```

2. **Run on Desktop**
   ```bash
   ./gradlew desktopRun
   ```

3. **Run on Web**
   ```bash
   ./gradlew jsBrowserDevelopmentRun
   ```

4. **Build for Android**
   ```bash
   ./gradlew assembleDebug
   ```

### Platform-Specific Setup

#### Desktop
No additional setup required. The desktop application will be packaged as:
- `.dmg` for macOS
- `.msi` for Windows  
- `.deb` for Linux

#### Web/Browser
The web version runs in any modern browser with JavaScript + WebGL. A diagnostics loader now:
- Detects WebGL2/WebGL support
- Distinguishes bundle load failures vs. initialization timeouts vs. runtime errors
- Provides a Retry button and error details (expandable)

Web troubleshooting:
1. Run dev server: `./gradlew jsBrowserDevelopmentRun` and open printed URL (not the raw file:// index.html).
2. If you see an error panel:
   - "WebGL Context Unavailable": Enable hardware acceleration (Chrome Settings > System) and update GPU drivers.
   - "Bundle Load Failed": Ensure `TheKoormProject.js` is served (check Network tab 200 status).
   - "Application Initialization Timeout": Check console for exceptions preventing `main()` execution.
   - "Runtime Error Before Start": Expand details; likely an exception during Compose initialization.
3. In DevTools console, run:
   `(function(){let c=document.createElement('canvas');return ['webgl2','webgl','experimental-webgl'].map(k=>[k,!!c.getContext(k)]);})()` to verify context support.
4. Retry after fixing environment by pressing the Retry button.

#### Android
1. Install Android Studio or Android SDK
2. Set `ANDROID_HOME` environment variable
3. See [ANDROID_SETUP.md](ANDROID_SETUP.md) for detailed instructions

#### iOS
1. Requires macOS with Xcode installed
2. iOS development certificates and provisioning profiles
3. Run from Xcode or use Kotlin Multiplatform Mobile plugin

## 📁 Project Structure

```
src/
├── commonMain/           # Shared code across all platforms
│   └── kotlin/
│       └── org/koorm/ocpd/
│           ├── core/                # Core business logic
│           │   └── OCPDAssistantManager.kt
│           ├── models/              # Data models
│           │   ├── Task.kt
│           │   ├── TimeManagement.kt
│           │   └── BehavioralInsights.kt
│           ├── services/            # Business services
│           │   ├── TaskBreakdownService.kt
│           │   ├── TimeManagementService.kt
│           │   ├── NotificationService.kt
│           │   ├── BehavioralInsightsService.kt
│           │   └── AIAssistantService.kt
│           └── ui/                  # Compose UI
│               └── OCPDAssistantApp.kt
├── androidMain/          # Android-specific code
├── desktopMain/          # Desktop-specific code
├── iosMain/              # iOS-specific code
└── jsMain/               # Web-specific code
```

## 🎯 Key Concepts

### Task Management
- **Tasks** with subtasks, priorities, and categories
- **"Good Enough" Thresholds**: Combat perfectionism by defining completion criteria
- **Smart Breakdown**: Large tasks automatically broken into manageable pieces
- **Progress Tracking**: Visual progress indicators and completion celebrations

### Time Management
- **Pomodoro Sessions**: Focused work periods with breaks
- **Flexible Scheduling**: Structure without rigidity
- **Time Estimates vs. Reality**: Learn from actual time spent vs. estimates

### Behavioral Insights
- **Pattern Recognition**: AI identifies productivity patterns and potential issues
- **Procrastination Triggers**: Track what causes delays and avoidance
- **Gentle Feedback**: Compassionate insights that encourage rather than criticize

## 🛠️ Development

### Building the Project

```bash
# Clean build
./gradlew clean

# Build all targets
./gradlew build

# Run tests
./gradlew test

# Desktop application
./gradlew desktopRun

# Web application  
./gradlew jsRun

# Package desktop app
./gradlew packageDistributionForCurrentOS
```

### Code Style
This project follows Kotlin coding conventions and uses:
- Ktlint for code formatting
- Detekt for static analysis
- Compose guidelines for UI code

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Submit a pull request

## 📋 Roadmap to App Store & Google Play

The path from today's prototype to a polished v1.0 in both stores. Milestones are sized in approximate calendar weeks, not engineer weeks; they're sequenced so each unlocks the next.

### Milestone 0 — Engineering Foundations (≈2 weeks)
- Consolidate `src/main` into `commonMain` so KMP is the single source of truth
- Adopt SQLDelight (or Room KMP) with versioned migrations
- Introduce Koin DI and a clean repository layer
- Settings persistence + theming primitives shared across platforms
- GitHub Actions CI: Android assemble, iOS build, lint, unit tests
- Crash reporting (Sentry) wired with opt-in toggle

### Milestone 1 — OCPD Core MVP (≈4 weeks)
- Mood tracking UI with history charts
- Behavioral Insights Engine: persisted insights surfaced in a dashboard
- Cognitive reframe library (CBT thought records)
- Self-compassion micro-exercises
- Pomodoro polish: stats, history, gentle break prompts, do-not-disturb
- Journaling with voice input on mobile

### Milestone 2 — Mobile-Native Experience (≈4 weeks)
- Android: Material 3 theming, predictive back, adaptive icon, home-screen widget, share target
- iOS: native look, WidgetKit widgets, Live Activity for the active Pomodoro, Share Extension
- Rich local notifications with snooze and quick actions
- Accessibility pass: VoiceOver, TalkBack, Dynamic Type, contrast, reduced motion

### Milestone 3 — AI & Adaptive Intelligence (≈4 weeks)
- Real LLM integration via Claude API for task breakdown, reframes, encouragement
- Backend proxy for key safety, response caching, and rate limiting
- On-device fallback for sensitive prompts (rule engine or small instruct model)
- Mood ↔ productivity correlation analytics
- Adaptive nudge tone & timing tuned per user

### Milestone 4 — Sync, Backup & Account (≈3 weeks)
- Account system with Sign in with Apple (App Store requirement) + Google Sign-In
- End-to-end encrypted optional cloud sync (per-user key, no plaintext at rest)
- Multi-device conflict resolution
- Encrypted local backup, restore, and full data export
- In-app account deletion (App Store + Play required)

### Milestone 5 — Localization & Polish (≈2 weeks)
- Localization: en, es, fr, de, pt-BR, ja
- Region-aware crisis resource directory
- Onboarding flow with consent and tone preference
- Empty states, loading skeletons, error-recovery polish
- Performance: <1.5s cold start on mid-tier Android, smooth 60fps lists

### Milestone 6 — Compliance, Beta & Store Submission (≈3 weeks)
- Hosted privacy policy and terms; in-app links
- iOS Privacy Manifest + Play Data Safety form
- Age rating and content rating questionnaires
- App Store Connect + Google Play Console setup, signing, store assets
- Localized listings, screenshots, preview videos
- TestFlight + Play Closed Testing with structured feedback loop
- Address reviewer feedback; submit for production review

### Post-Launch (v1.x and beyond)
- Apple Watch + Wear OS companion (Pomodoro, mood check-in)
- Google Calendar / Apple Calendar two-way sync
- Therapy session export (PDF / encrypted bundle)
- Shame-free habit streaks
- Sleep & energy correlations
- Community-translated locales
- Optional research opt-in: de-identified data for OCPD efficacy studies

### Distribution & Licensing Decision (blocker before submission)
The current CC BY-NC-SA 4.0 license restricts commercial use. Pick a path before submitting:
- (a) Keep non-commercial: free on both stores, no IAPs, no paid tiers
- (b) Dual-license a commercial edition for paid tiers / in-app purchases
- (c) Move to a permissive or hybrid license aligned with the project's mission

## 🤝 Community & Support

### For Users with OCPD
This app is designed **by and for** people who understand the unique challenges of OCPD. The features are carefully crafted to support, not exploit, perfectionist tendencies.

### Professional Integration
While this app provides valuable self-management tools, it's designed to complement, not replace, professional therapy and medical care.

### Privacy First
Your data is yours. All core features work offline, and any cloud features are optional with full user control.

## 🙏 Acknowledgments

- Built with love for the OCPD community
- Inspired by evidence-based approaches to perfectionism management

## 📄 License

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License - see the [LICENSE](LICENSE) file for details.

**In summary, you are free to:**
- Share — copy and redistribute the material in any medium or format
- Adapt — remix, transform, and build upon the material

**Under the following terms:**
- Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made
- NonCommercial — You may not use the material for commercial purposes
- ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license

## 📞 Contact & Support

- **Issues**: [GitHub Issues](<repository-url>/issues)
- **Discussions**: [GitHub Discussions](<repository-url>/discussions)
- **Email**: [your-email@example.com]

---

**Remember**: Progress over perfection. You've got this! 💪

---

*Built with ❤️ by Koorm • © 2025 OCPD Assistant*
