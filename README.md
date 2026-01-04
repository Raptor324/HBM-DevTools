# HBM DevTools

Development tools for HBM Modernized mod. Provides runtime model editing and other dev utilities.

## Features

- **JSON Transform Editor**: Edit item model transforms in real-time without restarting the client
- **Live Preview**: See changes instantly as you adjust sliders
- **Undo/Redo**: Full history support for all changes
- **Hotkeys**: Quick access via F8 (open editor) and F9 (open editor for held item)

## Installation

1. Build the mod:
   ```bash
   ./gradlew build
   ```

2. In HBM-Modernized's `build.gradle`, uncomment the DevTools dependency:
   ```gradle
   runtimeOnly fg.deobf("com.hbm_devtools:hbm_devtools:1.0.0-dev")
   ```

3. Run the game - DevTools will only work in development environment (not in production builds)

## Usage

### Transform Editor

1. Press **F8** to open the transform editor, or **F9** to open it for the item you're holding
2. Select a display mode (GUI, Ground, Fixed, 3rd Person, 1st Person)
3. Adjust sliders or type values directly
4. Changes are saved automatically to config
5. Click "Save" to write changes to the original JSON model files

### Features

- **Sliders**: Visual controls for all transform values
- **Text Input**: Direct value entry for precise control
- **Copy/Paste**: Copy transforms between display modes
- **Reset**: Restore original values for current mode
- **Undo/Redo**: Full history support (up to 50 steps)

## Architecture

The mod uses a feature-based architecture:

- `core/`: Core infrastructure (registry, config)
- `features/`: Individual features (json_editor, etc.)
- `integration/`: Integration with HBM mod

New features can be added by implementing `IDevToolFeature` and registering in `FeatureRegistry`.

## Development

This mod is designed to work only in development environments. It will not load in production builds.

## License

GNU General Public License v3.0

