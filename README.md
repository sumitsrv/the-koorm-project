# 🧠 OCPD Assistant - Your Compassionate Productivity Companion

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a>

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-orange.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)](https://github.com/JetBrains/compose-multiplatform)
[![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-sa/4.0/)

OCPD Assistant is a specialized productivity application designed to help individuals with Obsessive-Compulsive Personality Disorder (OCPD) manage their perfectionism, time management challenges, and productivity patterns in a healthy, supportive way.

## 🌟 Features

Status Legend: ✅ Implemented (usable) • 🧪 Prototype/Partial • 📝 Planned

The project’s mission is explicitly OCPD-aware with AI-assisted behavioral support. Several OCPD/AI foundations exist in code (data models + service stubs) but are not yet production-grade. Below is an accurate breakdown.
- **Smart Task Management**: Create, organize, and track tasks with OCPD-aware features
### Core Productivity (✅ Implemented)
- ✅ Smart Task Management – create, organize, track tasks
- ✅ Intelligent Task Breakdown – heuristic pattern & generic step generation (TaskBreakdownService)
- ✅ Perfectionism Control – GOOD_ENOUGH task status & encouragement messaging
- ✅ Time Tracking (basic) – Pomodoro session creation & time blocks
- ✅ Priority Management – multi-level priorities
- ✅ Category Organization – Work / Personal / Health / Learning / Social
- **Procrastination Trigger Tracking**: Identify and manage procrastination patterns
### OCPD-Specific Support
- ✅ Procrastination Trigger Tracking – CognitiveInsight records with procrastination reasons
- 🧪 Behavioral Insights Engine – WeeklyInsightReport & recommendation heuristics (early logic only)
- ✅ Gentle Notifications – tone-adjusted reminders & celebrations (NotificationService)
- ✅ Progress Celebration – completion & “good enough” messaging
- ✅ Flexible Scheduling (basic) – time block generation + buffer-aware scheduling (TimeManagementService)
- **Cross-Platform Sync**: Available on Desktop, Web, Android, and iOS
### AI & Adaptive Intelligence
- 🧪 AI Assistant Integration – simulated breakdown, encouragement & productivity analysis (AIAssistantService stub)
- 🧪 Mood Tracking (data layer) – MoodEntry model + recording API (no UI/analytics yet)
- 📝 Adaptive Modeling Upgrade – real model integration & persistence-backed trends

### Data & Platform
- ✅ Local Data Persistence – current shared/common storage layer (local only)
- 📝 Cross-Platform Sync – planned optional cloud sync & conflict resolution
- 📝 Analytics Dashboard – visual trends (completion rate, mood, procrastination reasons)
- 📝 Professional Dashboard – export & therapeutic session summaries
- 📱 **Android** (API 24+)
### Therapeutic / Well‑Being Extensions
- 📝 Therapy Integration Tools – structured exports & cognitive reframes bundle
- 📝 Stress Management Micro-Interventions – contextual coping prompts

### Customization & UX
- ✅ Customizable Interface – adaptable UI components, tone preferences
- 📝 Advanced Personalization – AI-tuned nudging based on historical patterns

### Feature Status Summary
- Implemented: 12
- Prototype / Partial: 3
- Planned: 6
- Total Tracked: 21
- Overall Implemented Coverage: 12 / 21 (57%)

Breakdown Integrity Notes:
- “Prototype” means an internal heuristic or simulated implementation is present but not production-ready (e.g., no persistence, model inference, or UI polish).
- Planned items are mission-critical for achieving full OCPD + AI guidance vision.
- Update counts whenever changing any list item; keep Total Tracked accurate.

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

## 📋 Roadmap

### Phase 1: MVP (Current)
- ✅ Basic task management
- ✅ Task breakdown service
- ✅ Cross-platform UI
- ✅ Local data persistence

### Phase 2: Enhanced Features
- 🔄 Cloud synchronization
- 🔄 Advanced analytics
- 🔄 AI-powered insights
- 🔄 Social features (optional)

### Phase 3: Advanced OCPD Support
- 📅 Therapy integration tools
- 📅 Mood tracking
- 📅 Stress management features
- 📅 Professional dashboard

## 🤝 Community & Support

### For Users with OCPD
This app is designed **by and for** people who understand the unique challenges of OCPD. The features are carefully crafted to support, not exploit, perfectionist tendencies.

### Professional Integration
While this app provides valuable self-management tools, it's designed to complement, not replace, professional therapy and medical care.
- 🧪 Behavioral insights engine maturation (expand heuristics, add persistence)
### Privacy First
Your data is yours. All core features work offline, and any cloud features are optional with full user control.

## 🙏 Acknowledgments

- Built with love for the OCPD community
- Inspired by evidence-based approaches to perfectionism management
- 📝 Mood tracking UI & analytics (elevate from data layer)
- 📝 Stress management micro-interventions
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
