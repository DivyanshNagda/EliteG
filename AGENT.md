# EliteG Android Game Booster - Agent Guidelines

## Build & Test Commands
- **Build debug**: `./gradlew assembleDebug`
- **Build release**: `./gradlew assembleRelease`
- **Run unit tests**: `./gradlew test`
- **Run instrumented tests**: `./gradlew connectedAndroidTest`
- **Single test**: `./gradlew :app:testDebugUnitTest --tests "ClassName.testMethodName"`
- **Lint check**: `./gradlew lint`
- **Clean build**: `./gradlew clean`

## Project Architecture
- **Android app** targeting API 21-34 using Java 11, ViewBinding enabled
- **Main package**: `com.dnagda.eliteG` - game performance optimization tool requiring ADB permissions
- **Core modules**: MainActivity (main UI), GameAppManager (game detection), SettingsManager (preferences), ExecuteADBCommands (ADB operations)
- **Utils package**: Logger (centralized logging), PerformanceUtils (calculations), UIUtils (UI helpers), Constants (app constants)
- **Features**: Resolution scaling, background app killing, performance optimizations, recent games management

## Code Style & Conventions
- **Imports**: Group standard Java, Android framework, then project imports with blank lines between
- **Naming**: CamelCase for classes, camelCase for methods/variables, UPPER_SNAKE_CASE for constants  
- **Error handling**: Use Logger.e() for errors with throwables, show user-friendly toasts via UIUtils.showToast()
- **Logging**: Use centralized Logger class with TAG constants, debug logs only in DEBUG builds
- **Documentation**: JavaDoc for public methods, inline comments for complex logic only
- **Resources**: String resources for all user-visible text, consistent naming with activity_component_purpose pattern
