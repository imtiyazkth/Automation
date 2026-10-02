# Design notes

The UI follows two sources: Apple's "Designing Fluid Interfaces" principles (via the
`apple-design` skill by emilkowalski) and the `baseline-ui` constraints from ibelick/ui-skills,
translated from web/Tailwind to Jetpack Compose.

## Principles and where they live

| Principle | Implementation |
|---|---|
| Respond on touch-down | `Modifier.tappable` / `pressScale` (ui/components/Motion.kt): scale to 0.97 on press, critically damped spring |
| Springs, no bounce by default | Every spring uses `dampingRatio = 1f`. Nothing in the app is flicked, so nothing overshoots |
| Reduced motion | `rememberReducedMotion()` makes springs `snap()`; feedback (colour, state) stays |
| Spatial consistency | Screens opened from More slide in from the right and leave to the right; tabs cross-fade |
| Fade edges, not hard dividers | `Modifier.fadeEdges()` on every scrolling area |
| Size-specific type | Large titles track at -0.022em, body at 0, small labels slightly positive. System font |
| Restraint | One accent (mint) per view. Warning/danger colours mean status only |

## Baseline-ui rules applied

- No gradients as decoration (the only gradient is an alpha mask in `fadeEdges`), no glow
- Empty states each have one clear next action
- Icon-only buttons have content descriptions
- Destructive actions (delete task, clear logs) use a confirmation dialog
- Numbers use tabular figures
- Sentence case everywhere; no emoji as icons; no ALL-CAPS labels
- Only `transform`/`alpha` animated for press and thumb feedback

## Tokens (ui/theme/Color.kt)

Dark and light, following the system setting.

| Token | Dark | Light |
|---|---|---|
| background | #0C0F0E | #F3F5F4 |
| card | #151A19 | #FFFFFF |
| raised | #1E2524 | #ECF0EF |
| hairline | #26302E | #DFE5E3 |
| text / muted | #EDF3F1 / #93A6A1 | #101413 / #5B6965 |
| accent | #3DDC97 | #0B7A50 |

Radii follow hierarchy: cards 20dp, controls 14dp, insets 10dp, bubbles 20dp with a 6dp tail corner.

## Navigation

Five tabs: Chat, Today, Approvals (with a count badge), Agents, More.
Tasks, Permissions, Activity and Diagnostics sit under More with a back button.

## Known limits

- The tab bar is opaque. Compose has no backdrop blur that works across minSdk 26.
- Not compiled or run on a device in the environment this was written in; build it before merging.
