# Helicopter & Pong Games

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project contains two games:
1. **Helicopter Game** - Avoid the gun and its bullets
2. **Pong** - Classic two-player (or vs AI) paddle game

## How to Run

### Desktop (Windows)
```bash
.\gradlew.bat :lwjgl3:run
```

### Desktop (Linux/Mac)
```bash
./gradlew :lwjgl3:run
```

### Android
```bash
.\gradlew.bat :android:installDebug
```

## Main Menu

When the game starts, you'll see a menu with options:
- **1. Helicopter Game** - Play the helicopter game
- **2. Pong Game** - Play Pong
- **3. Exit** - Quit the application

Use **UP/DOWN** arrows to navigate and **ENTER** to select.

## Helicopter Game Controls

| Key/Action | Description |
|------------|-------------|
| **Mouse Click/Touch** | Move helicopter towards click position |
| **R** | Restart the game |
| **ESC** | Return to main menu |

### Helicopter Gameplay
- The helicopter automatically bounces around the screen
- Click/touch to guide the helicopter to a new position
- Avoid the gun at the bottom - crash into it and you explode!
- The gun fires periodically - if the bullet hits you, you explode!
- When exploded, the helicopter falls to the ground

## Pong Controls

| Key/Action | Description |
|------------|-------------|
| **W/S** | Move left paddle up/down |
| **UP/DOWN** | Move right paddle up/down (multiplayer only) |
| **T** | Toggle single player / multiplayer mode |
| **R** | Restart the game |
| **ESC** | Return to main menu |

### Pong Gameplay
- Ball bounces off paddles and top/bottom walls
- Score a point when the ball passes your opponent's paddle
- Ball speed increases each time it hits a paddle
- First to 21 points wins
- In single player mode, AI controls the right paddle

## Credits

- Graphics generated at [artlist.io](https://artlist.io)

## Build Release

To build a release package with the runnable JAR and source code:

### Windows
```bash
.\build-release.bat
```

### Linux/Mac
```bash
chmod +x build-release.sh
./build-release.sh
```

This creates a `dist/` folder containing:
- `Helicopter-1.0.0.jar` - Runnable JAR file
- `Helicopter-delivery.zip` - ZIP with source code and JAR

To run the JAR:
```bash
java -jar dist/Helicopter-1.0.0.jar
```

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
