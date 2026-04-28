# YAML Definitions Spec

This document describes the YAML language accepted by `ConfigurationLoader.load(...)`.
It is based on the Kotlin schema and converter code under `src/main/kotlin/pl/zarajczyk/huesetup/configuration`.

## Loading Rules
- The file is parsed as YAML into `RawDefinitions` with Jackson.
- Unknown properties are ignored.
- Jackson is configured with `ACCEPT_SINGLE_VALUE_AS_ARRAY`, so a scalar may be used where the schema expects a list.
- Scalar values are bound to the target Kotlin field type. In practice, plain YAML tokens such as `on` and `off` are accepted for string fields like `button` and `press-type` by this implementation.
- Bean validation runs before conversion. Validation errors are printed and the run fails.
- Some semantic errors are enforced later during conversion and also fail the run, for example unknown group references or invalid action combinations.

## Top-Level Shape

The YAML document must be a mapping with all of these keys:

```yaml
zones: []
rooms: []
lights: []
scenes: []
accessories: []
automations: []
```

All six keys are required by the Kotlin data class. Use empty lists if a section is unused.

## Shared Value Formats

### Group names
- Group references are plain strings.
- A referenced group must match the `name` of a defined room or zone exactly.

### Accessory names
- Accessory references are plain strings.
- A referenced accessory must match the `name` of a defined accessory exactly.

### MAC address
- Format: eight lowercase hex pairs separated by `:`.
- Regex: `[0-9a-f]{2}(:[0-9a-f]{2}){7}`
- Example: `ec:b5:fa:ff:fe:94:0f:1d`

### Brightness
- Format: `0%` to `100%`
- Regex: `[0-9]{1,3}%`
- Values outside `0..100` fail during conversion.

### Color temperature
- Format: Kelvin as `NNNNK`
- Regex: `[0-9]{4}K`
- Supported range: `2000K` to `6500K`

### Color
- Format: hex RGB
- Regex: `#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})`
- Example: `#ff8800`

### Time
- Format: Hue local time string
- Regex: `W127/T[0-9]{2}:[0-9]{2}:[0-9]{2}`
- Example: `W127/T07:30:00`

### Time period
- Format: `T..../T....`
- Regex: `T[0-9]{2}:[0-9]{2}:[0-9]{2}/T[0-9]{2}:[0-9]{2}:[0-9]{2}`
- Example: `T06:00:00/T08:30:00`

### Transition time
- Regex: `[0-9]{1,4}(min|s|ds)`
- Units:
  - `min`: minutes
  - `s`: seconds
  - `ds`: deciseconds
- Examples: `5s`, `30ds`, `2min`

### Timeout / delay
- Regex: `[0-9]{1,4}(h|min|s)`
- Units:
  - `h`: hours
  - `min`: minutes
  - `s`: seconds
- Examples: `30s`, `5min`, `1h`

## Sections

### `zones`

List of zone definitions.

```yaml
zones:
  - name: Kitchen
    class: kitchen
```

Fields:
- `name`: string, max length `32`
- `class`: required `GroupArchetype`

Allowed `class` values:
- `living_room`
- `kitchen`
- `dining`
- `bedroom`
- `kids_bedroom`
- `bathroom`
- `nursery`
- `recreation`
- `office`
- `gym`
- `hallway`
- `toilet`
- `front_door`
- `garage`
- `terrace`
- `garden`
- `driveway`
- `carport`
- `home`
- `downstairs`
- `upstairs`
- `top_floor`
- `attic`
- `guest_room`
- `staircase`
- `lounge`
- `man_cave`
- `computer`
- `studio`
- `music`
- `tv`
- `reading`
- `closet`
- `storage`
- `laundry_room`
- `balcony`
- `porch`
- `barbecue`
- `pool`
- `other`

### `rooms`

Same shape and allowed `class` values as `zones`.

```yaml
rooms:
  - name: Bedroom
    class: bedroom
```

### `lights`

List of light definitions.

```yaml
lights:
  - name: Ceiling
    mac:
      - 00:17:88:01:02:03:04:05
    group:
      - Kitchen
      - Downstairs
```

Fields:
- `name`: string, max length `20`
- `mac`: required list of MAC addresses
- `group`: optional list of room/zone names, default `[]`

Semantics:
- Every `group` entry must reference an existing room or zone.
- One YAML light entry expands into one internal light definition per MAC address.
- If `mac` contains more than one item, internal names become `name #1`, `name #2`, and so on.
- During reconciliation, room membership is applied through the device and zone membership through the light service.

### `scenes`

List of scene definitions.

```yaml
scenes:
  - name: Evening
    groups:
      - Living Room
      - Bedroom
    setup:
      - brightness: 30%
        color-temperature: 2700K
      - group: Bedroom
        turned-on: false
```

Fields:
- `name`: string, max length `32`
- `groups`: required list of room/zone names
- `setup`: required list of scene setup entries

Scene setup entry fields:
- `brightness`: optional brightness string
- `color-temperature`: optional color temperature string
- `color`: optional hex color
- `turned-on`: optional boolean
- `group`: optional room/zone name override

Semantics:
- Every `groups` value must reference an existing room or zone.
- `color` and `color-temperature` are mutually exclusive in the same setup entry.
- One YAML scene entry expands into one internal scene per value in `groups`.
- Each setup entry applies to all lights in `setup.group` when provided, otherwise to all lights in the scene's own group.
- If `turned-on` is omitted, scene actions default to `on: true`.

### `accessories`

List of accessory definitions.

```yaml
accessories:
  - name: Hall Dimmer
    mac: 00:17:88:01:02:03:04:05
```

Fields:
- `name`: string, max length `32`
- `mac`: required MAC address

Semantics:
- Accessory reconciliation currently targets supported remotes plus model `SML001`.
- Supported remote model ids are `RWL021`, `RWL022`, `ROM001`, and `RDM005`.

### `automations`

List of tagged automation objects. Each entry must include `type`.

```yaml
automations:
  - type: time
    name: Morning
    time: W127/T07:00:00
    actions: []
```

Supported automation types:
- `time`
- `button`
- `motion`

#### `type: time`

```yaml
- type: time
  name: Morning
  time: W127/T07:00:00
  actions:
    - type: turn_on
      group:
        - Kitchen
```

Fields:
- `name`: string, max length `12`
- `time`: required time string
- `actions`: required list of actions

Semantics:
- One YAML `time` automation expands into one internal automation per expanded action.
- Internal names are built as `"<name> <action description>"`.

#### `type: button`

```yaml
- type: button
  remote:
    - Hall Dimmer
  button: onoff
  press-type: initial_press
  actions:
    - type: scene
      group:
        - Hall
      scene: Bright
```

Fields:
- `remote`: required list of accessory names
- `button`: required string
- `press-type`: required string
- `actions`: required list of actions

Semantics:
- Every `remote` value must reference an existing accessory.
- One YAML `button` automation expands into one internal automation per remote.
- Valid `button` values depend on the physical remote model:
  - `RWL021` supports `on`, `up`, `down`, `off`
  - `RWL022` supports `onoff`, `up`, `down`, `hue`
  - `ROM001` and `RDM005` ignore the button name in code generation
- Supported `press-type` values are:
  - `initial_press`
  - `repeat`
  - `short_release`
  - `long_release`
  - `long_press`

#### `type: motion`

```yaml
- type: motion
  sensor: Hall Motion
  motion: true
  delay: 30s
  actions:
    - type: turn_on
      group:
        - Hall
```

Fields:
- `sensor`: required accessory name
- `motion`: required boolean
- `delay`: optional timeout, default `0s`
- `actions`: required list of actions

Semantics:
- `sensor` must reference an existing accessory.

## Automation Actions

Every action object must include `type`.

Supported action types:
- `turn_on`
- `turn_off`
- `color_temperature`
- `scene`
- `enable_sensor`
- `disable_sensor`
- `wait`

All action types may include optional `conditions`.

### Group-targeted actions

These action types target groups:
- `turn_on`
- `turn_off`
- `color_temperature`
- `scene`

They share these targeting rules:
- Exactly one of `group` or `group-per-remote` must be provided.
- `group` is a list of room/zone names.
- `group-per-remote` is a mapping from remote accessory name to a list of room/zone names.
- `group-per-remote` is only valid inside remote-related automations where a specific remote is being expanded.
- Every referenced group must exist.
- Each action expands into one internal action per targeted group.

### `type: turn_on`

```yaml
- type: turn_on
  group:
    - Living Room
  transition-time: 2ds
  conditions: []
```

Fields:
- `group`: optional list of groups
- `group-per-remote`: optional map of remote name to groups
- `transition-time`: optional transition time, default `2ds`
- `conditions`: optional list, default `[]`

### `type: turn_off`

Same shape as `turn_on`.

Default `transition-time`: `2ds`

### `type: color_temperature`

```yaml
- type: color_temperature
  group:
    - Bedroom
  color-temperature: 2200K
  transition-time: 3ds
```

Fields:
- `group`: optional list of groups
- `group-per-remote`: optional map of remote name to groups
- `color-temperature`: required color temperature
- `transition-time`: optional transition time, default `3ds`
- `conditions`: optional list, default `[]`

### `type: scene`

```yaml
- type: scene
  group:
    - Bedroom
  scene: Sleep
  transition-time: 3ds
```

Fields:
- `group`: optional list of groups
- `group-per-remote`: optional map of remote name to groups
- `scene`: required scene name string
- `transition-time`: optional transition time, default `3ds`
- `conditions`: optional list, default `[]`

Semantics:
- The referenced scene name is resolved within each targeted group.

### `type: enable_sensor`

```yaml
- type: enable_sensor
  sensor: Hall Motion
```

Fields:
- `sensor`: required accessory name
- `conditions`: optional list, default `[]`

### `type: disable_sensor`

Same shape as `enable_sensor`.

### `type: wait`

```yaml
- type: wait
  duration: 30s
  actions:
    - type: turn_off
      group:
        - Hall
```

Fields:
- `duration`: required timeout
- `actions`: required list of nested actions
- `conditions`: optional list, default `[]`

Semantics:
- Nested actions are recursively expanded using the same rules.

## Automation Conditions

Every condition object must include `type`.

Supported condition types:
- `any_on`
- `all_off`
- `time_of_day`
- `press_counter`

### `type: any_on`

```yaml
- type: any_on
  group: Living Room
```

Fields:
- `group`: optional group name

Semantics:
- If `group` is omitted, the condition uses the parent action's target group.
- If `group` is omitted and the parent action has no target group, conversion fails with `condition group is not set`.

### `type: all_off`

Same shape and fallback behavior as `any_on`.

### `type: time_of_day`

```yaml
- type: time_of_day
  time: T06:00:00/T09:00:00
```

Fields:
- `time`: required time period

### `type: press_counter`

```yaml
- type: press_counter
  if-press-number: 0
  next-press-number: 1
```

Fields:
- `if-press-number`: required integer
- `next-press-number`: required integer

Semantics:
- If any action in a button automation uses `press_counter`, the runtime creates a helper memory sensor for that automation.

## Fully Worked Example

```yaml
zones:
  - name: Downstairs
    class: downstairs

rooms:
  - name: Living Room
    class: living_room
  - name: Bedroom
    class: bedroom

lights:
  - name: Sofa Lamp
    mac: 00:17:88:01:02:03:04:05
    group:
      - Living Room
      - Downstairs

scenes:
  - name: Relax
    groups:
      - Living Room
      - Bedroom
    setup:
      - brightness: 25%
        color-temperature: 2200K
      - group: Bedroom
        turned-on: false

accessories:
  - name: Hall Dimmer
    mac: 00:17:88:0a:0b:0c:0d:0e
  - name: Hall Motion
    mac: 00:17:88:0f:10:11:12:13

automations:
  - type: time
    name: Morning
    time: W127/T07:00:00
    actions:
      - type: scene
        group:
          - Living Room
        scene: Relax

  - type: button
    remote:
      - Hall Dimmer
    button: onoff
    press-type: initial_press
    actions:
      - type: turn_on
        group:
          - Living Room
        conditions:
          - type: all_off

  - type: motion
    sensor: Hall Motion
    motion: true
    delay: 30s
    actions:
      - type: turn_on
        group:
          - Living Room
      - type: wait
        duration: 5min
        actions:
          - type: turn_off
            group:
              - Living Room
```
