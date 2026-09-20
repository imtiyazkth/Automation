# Agent Spec Template

Fill this out **before** writing `agent.json` or the Kotlin `Agent`
implementation for a new capability. The goal is the same one the
LangGraph course this was adapted from has: force every design decision
(what it does, what it needs, how it fails, what it must never do) onto
paper first, so the code is a direct translation of a spec instead of
a set of decisions made ad hoc while typing.

Every section maps to something real in this codebase - by the time this
is filled in, `agent.json` and the `Agent.execute()` skeleton mostly write
themselves. Delete this instruction line and the bracketed hints as you go.

---

## 1. Identity

- **id** (kebab-case, matches the JSON filename): `___-agent`
- **name**: `___`
- **category**: one of the existing categories (`hr`, `jobs`, `documents`,
  `growth`, `communication`, `security`, `media`, ...) or a new one
- **Role, in one paragraph**: What does this agent do, in plain language?
  Who or what is it acting on behalf of?
- **Explicitly NOT this agent's job**: What might someone assume it does,
  that it actually doesn't? (e.g. "evaluates job postings, does not
  search for them" / "opens a pre-filled WhatsApp chat, does not send
  the Business API message")

## 2. Objective & Success Criteria

- **One-sentence objective**:
- **How you'd know it's working** (2-4 concrete, checkable bullets - not
  vibes): e.g. "a pasted job description produces a report with a
  Score: X/5 line", "a phone-number recipient opens WhatsApp with the
  message pre-filled"

## 3. Capabilities & Actions

List every `TaskStep.action` string this agent will handle in its
`when (step.action) { ... }` block. One row per action.

| action | what it does | maps to `capabilities` in agent.json |
|---|---|---|
| `___` | ___ | `___` |

## 4. Inputs / Outputs

For each action above, the `step.params` keys it reads and what it
returns via `ExecutionReport`.

| action | param key | type | required? | example value |
|---|---|---|---|---|
| `___` | `___` | String | yes/no | `"___"` |

- **On success**, what does `ExecutionReport.Success.message` say, and
  what (if anything) goes in its `data` map?

## 5. Tools & Dependencies

What gets passed into this agent's constructor? Every dependency here is
something `AutomationOsApp.kt` will need to construct and pass in.

- Kotlin classes / clients needed: `___`
- Existing DAOs needed (if it persists anything): `___`
- New Room entity/DAO needed? If yes, note it here and design the entity
  before writing the agent.
- Android APIs touched (Intents, Context, permissions): `___`

## 6. Permissions & Risk

- **`permissions` list for agent.json**: only list a permission if the
  code genuinely checks/needs it through `PermissionManager` - don't
  copy another agent's list by default (see the CommunicationAgent fix:
  it used to declare `read_contacts`/`send_sms`/`read_notifications` it
  never actually touched).
- **`risk`**: `low` / `medium` / `high` / `critical` - what's the worst
  plausible outcome of this agent acting on bad input or a bug?
- **Default `automation_mode`**: informational only right now (the real
  control is `AutomationModeStore`, set via the Agents tab) - still
  record your intent here.

## 7. Data Residency & Privacy

- Does any part of this agent's input ever reach `AiRouter.generate()`
  with a cloud-routed `intentType`? If yes:
  - Which exact fields go in the `payload` map?
  - Does `PrivacyGateway`'s allowlist for that `intentType` already cover
    them, or does it need updating (and is that update actually
    justified - see the `job_description`/`candidate_skills` precedent)?
  - Is there anything in the free-text `prompt` string itself that
    shouldn't leave the device? (Note: today `PrivacyGateway` only gates
    the `payload` side-channel, not the prompt string itself - flag it
    here if this agent's prompt could carry something sensitive, since
    that's a known gap, not a solved one.)
- Does this agent write anything to Room (audit log, a new entity, memory)?
  If it's personal/sensitive data, say so here even if scrubbing isn't
  built yet - the record should exist even before the fix does.

## 8. Approval Rules

- Which actions, if any, go in `requires_approval_for` regardless of
  automation mode? What's irreversible or high-consequence enough to
  demand a human look first?
- Is there a class of input (e.g. "recipient not seen before") that
  should force approval even under Full automation? Note it even though
  `PolicyEngine` doesn't fully wire this up yet (`isNewOrUnknownRecipient`
  exists as a parameter but nothing calls it with `true` yet).

## 9. Failure Behavior

For every way this can fail, name the exact `ExecutionReport` type and
draft the message text now, in your own words - not "TODO handle error."

| Failure case | Report type | Message |
|---|---|---|
| missing required param | `RequiresUserAction` | `"..."`, reason `"missing <field>"` |
| dependency not configured (API key, etc.) | `Failed` | `"..."` |
| dependency call throws | `Failed` | `"..."` (wrap in `runCatching`) |
| ambiguous/unparseable input | `RequiresUserAction` | `"..."` |

**Rule this codebase already learned the hard way**: never return
`Success` unless something you can point to actually happened. If a
client call returns a placeholder/error string, check its content -
don't assume the call worked just because it didn't throw.

## 10. Conversation / Clarification Behavior

- Which of this agent's `RequiresUserAction` reasons should be
  resumable by `HeadAgent`'s clarification state machine? To qualify,
  the `reason` string MUST follow the convention:
  `"missing field1/field2"` (exact prefix `"missing "`, fields joined
  by `/`).
- List the fields here, in the order you want them asked (first missing
  field is asked first):
  1. `___`
  2. `___`
- Does this agent only ever run as a single-step plan? (Multi-step plans
  aren't eligible for clarification resumption today - see `HeadAgent.handle()`.)

## 11. Example Runs

Write 2-3 concrete conversations, start to finish, including a
clarification round-trip if this agent has one.

**Example 1:**
cat > docs/example-spec-media-agent.md << 'EOF'
# Example: Media Agent (filled retroactively)

This is the actual, already-shipped Media Agent, written up against the
template so you can see what a filled spec looks like next to the real
code (`core/agents/MediaAgent.kt`, `assets/agents/media-agent.json`).

---

## 1. Identity

- **id**: `media-agent`
- **name**: Media Agent
- **category**: `media`
- **Role**: Opens a YouTube search for the user via an Android Intent, so
  a spoken or typed "play X on YouTube" actually does something instead
  of hitting "no agent wired up."
- **Explicitly NOT this agent's job**: does not control playback (pause/
  seek/volume) of the standalone YouTube app once it's open - that's not
  reliably automatable on Android (see blueprint Part 11/18). Does not
  use the YouTube Data API or rank results - it hands the query straight
  to YouTube's own search.

## 2. Objective & Success Criteria

- **One-sentence objective**: Turn "play/search X on YouTube" into an
  actually-opened YouTube search for X.
- **Success criteria**:
  - `"play arijit singh"` opens YouTube search results for "arijit singh"
  - Missing/blank query asks the user what to search for instead of
    opening an empty search
  - Works whether or not the YouTube app is installed (falls back to
    browser via the same `https://` URL)

## 3. Capabilities & Actions

| action | what it does | maps to `capabilities` |
|---|---|---|
| `search_and_open` | Builds a YouTube search URL and launches it via `ACTION_VIEW` | `media_search` |

## 4. Inputs / Outputs

| action | param key | type | required? | example |
|---|---|---|---|---|
| `search_and_open` | `query` | String | yes | `"arijit singh new song"` |

- **On success**: `ExecutionReport.Success("Opened YouTube search for \"$query\"")`, no `data` payload needed.

## 5. Tools & Dependencies

- `android.content.Context` (application context, injected via constructor) - needed to call `startActivity()` from a non-Activity class
- No DAOs, no new Room entity
- Android API touched: `Intent(Intent.ACTION_VIEW, uri)` with `FLAG_ACTIVITY_NEW_TASK`

## 6. Permissions & Risk

- **`permissions`**: `[]` - `ACTION_VIEW` with a URL needs no dangerous
  Android permission and nothing in `PermissionManager`
- **`risk`**: `low` - worst case is opening an unwanted search, fully
  visible and undoable by the user
- **Default `automation_mode`**: `full` - no reason to gate a search

## 7. Data Residency & Privacy

- No `AiRouter` call at all - the query goes straight into a URL, never
  through Gemini. Nothing to allowlist.
- Nothing persisted to Room.

## 8. Approval Rules

- None. `requires_approval_for: []` - opening a search is fully
  reversible and low-consequence.

## 9. Failure Behavior

| Failure case | Report type | Message |
|---|---|---|
| blank/missing query | `RequiresUserAction` | `"What should I search for on YouTube?"`, reason `"missing query"` |
| `startActivity` throws (no browser/YouTube capable of handling the intent) | `Failed` | `"Couldn't open YouTube"` (wraps the exception) |

## 10. Conversation / Clarification Behavior

- One resumable field: `query`. Reason string is exactly
  `"missing query"`, matching the `"missing "` prefix convention, so if
  the user just says "play" with nothing else, `HeadAgent` will ask
  "what should I search for?" and resume with whatever they say next.
- Single-step plan only (`TaskPlanner`'s `"media_search"` case has one
  `TaskStep`) - eligible for clarification resumption.

## 11. Example Runs

**Example 1:**-spec-media-agent.md
cat >> README.md << 'EOF'

## Adding a new agent

Before writing `agent.json` or the Kotlin implementation for a new
capability, fill out `docs/agent-spec-template.md`. See
`docs/example-spec-media-agent.md` for what a completed one looks like,
written up against the actual Media Agent.
