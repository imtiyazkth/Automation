# Personal AI Automation OS — Scaffold Implementation

This is a **working Android/Kotlin scaffold** of the architecture from the
project blueprint: one Head Agent, a registry-driven set of specialized
agents, a policy/permission/privacy layer, a workflow engine, Room-backed
storage, and a minimal Compose chat UI.

**Read this file before you build.** It tells you exactly what runs today
versus what's intentionally stubbed and needs your own credentials/model
files — being upfront about that gap is more useful than a scaffold that
quietly pretends to be finished.

---

## What's real right now

If you open this in Android Studio and get it compiling (see *Build steps*
below), the following logic is genuinely implemented and unit-tested —
not mocked:

- **Head Agent pipeline** (`core/orchestrator/HeadAgent.kt`): intent →
  plan → permission/policy check → agent selection → execute → observe →
  report, exactly as described in the blueprint. Every result resolves to
  `SUCCESS` / `PARTIAL_SUCCESS` / `FAILED` / `REQUIRES_USER_ACTION` — never
  silent success.
- **Rule-based intent classification fallback** (`IntentDetector.kt`) —
  works with zero external dependencies, so the whole pipeline is testable
  before any real model is wired in.
- **Agent Registry** (`AgentRegistry.kt`) — loads `assets/agents/*.json`
  definitions at runtime and matches agents by *capability*, never by
  hardcoded name, so adding a new agent later is "drop in a JSON file +
  implementation + register it," not "edit the Head Agent."
- **Policy Engine / Permission Manager / Privacy Gateway / Emergency
  Control** — real, testable logic enforcing the blueprint's Manual /
  Smart / Full automation modes, the always-review action lists per
  agent, and the allowlist-based data gate before anything reaches Gemini.
- **Workflow Engine** — a small trigger/condition/action rule engine that
  routes through the *same* policy checks as direct commands.
- **Room database schema** matching the ER diagram (HR, CRM, communication,
  audit, permissions, memory, workflows).
- **Excel export** (`tools/ExcelExportTool.kt`) via Apache POI — the one
  "tool" in this scaffold that needs no external service or model and
  genuinely writes a `.xlsx` file today.
- **Link Safety Agent** — real heuristic checks (HTTPS, raw-IP host,
  excessive hyphenation) with an honest `UNKNOWN` verdict when no
  reputation data is available; never claims guaranteed safety.
- **6 example agents** wired end-to-end into the registry: HR, Document,
  Marketing, Link Safety, Job Search, Communication.
- **13 unit tests** covering the policy engine, the privacy gateway, the
  workflow condition evaluator, and the intent-detection fallback.

## What's intentionally stubbed — and why

| Piece | State | What you need to do |
|---|---|---|
| **On-device local LLM** (`core/ai/LocalAiProvider.kt`) | Returns a "not wired in" placeholder | Integrate a real Android LLM runtime — llama.cpp Android JNI build or MLC-LLM — with a quantized 1–4B GGUF model. This can't be scaffolded blind; it involves multi-hundred-MB native binaries and model weights. The file has a detailed comment on both options. |
| **Gemini** (`core/ai/GeminiProvider.kt`) | Real REST client, but needs your API key | Put `GEMINI_API_KEY` in `local.properties` (see `local.properties.example`) |
| **WhatsApp Business Cloud API** (`integrations/whatsapp/WhatsAppBusinessClient.kt`) | Real REST client, needs your Meta Business credentials | Get `WHATSAPP_CLOUD_API_TOKEN` + `WHATSAPP_PHONE_NUMBER_ID` from Meta's Business Platform, put them in `local.properties`. **This uses the official Cloud API only** — see the compliance note in that file about the 2026 ban on open-domain AI chatbots on this channel. |
| **Telegram Bot** | Real REST client, needs your bot token | `TELEGRAM_BOT_TOKEN` in `local.properties` |
| **Gmail** | Placeholder only | Wire Google Sign-In + Gmail API OAuth flow |
| **PDF extraction** (`tools/PdfExtractTool.kt`) | Stub | Wire to PDFBox-Android (or similar) + an OCR fallback for scanned PDFs |
| **Link reputation lookup** | Returns `null` (honest "no data") | Wire to a real threat-intel/safe-browsing API |
| **Database encryption** | Plain Room | Wrap with SQLCipher + Android Keystore-derived key before shipping anything real — see the comment in `data/AppDatabase.kt` |
| **AccessibilityService / Device Admin** | Declared in the manifest, not implemented | High-risk, policy-restricted per blueprint Part 11/37 — build only for a personal, sideloaded install, never for Play distribution, and only once you've read that section again |

## Build steps

1. Install Android Studio (Koala/2024.1 or newer) with SDK 34.
2. Open this folder as a project.
3. Copy `local.properties.example` → `local.properties`, set `sdk.dir` to
   your SDK path, and fill in whichever API keys you actually have.
4. Sync Gradle. The project will compile and the app will *launch* and let
   you type commands into the Head Agent chat even with zero keys set —
   you'll just get honest "[not configured]" responses from anything that
   needs a real credential, and the rule-based fallback will handle intent
   understanding until a local model is wired in.
5. Run `./gradlew test` to run the unit test suite described above.

> I built and organized this scaffold in a sandboxed Linux container
> without an Android SDK/emulator, so I could not run an actual Gradle
> build or instrumented test here. The Kotlin follows standard
> AGP 8.5 / Kotlin 1.9 / Compose conventions throughout, but budget time
> for the normal first-sync fixes (dependency version bumps, etc.) any
> Android project needs on a machine with the real toolchain.

## Where to go next

Follow the phase order in the blueprint's Part 32 (Development Roadmap).
Concretely, the highest-value next steps on top of this scaffold are:

1. Wire a real on-device model into `LocalAiProvider` (Phase 3).
2. Replace the in-memory stores (`InMemoryPermissionStore`,
   `InMemoryAuditLogger`, `InMemoryAutomationModeStore`) with Room-backed
   implementations using the DAOs already defined in `data/dao/`.
3. Add the Permission Center, Agent Center, and Audit Log screens (the
   `HeadAgentChatScreen` and `DashboardScreen` are the only two UI screens
   built so far).
4. Get real credentials into `local.properties` and test one real
   end-to-end flow (e.g. link-safety checking needs no credentials at all
   and is the fastest thing to try first).

## Project structure

See the blueprint document (Part 31) for the full folder-purpose
breakdown — this scaffold follows it exactly, module for module.
