# Example: Media Agent (filled retroactively)

This is the actual, already-shipped Media Agent, written up against the
template so you can see what a filled spec looks like next to the real
code (core/agents/MediaAgent.kt, assets/agents/media-agent.json).

---

## 1. Identity

- id: media-agent
- name: Media Agent
- category: media
- Role: Opens a YouTube search for the user via an Android Intent, so
  a spoken or typed "play X on YouTube" actually does something instead
  of hitting "no agent wired up."
- Explicitly NOT this agent's job: does not control playback (pause/
  seek/volume) of the standalone YouTube app once it's open. Does not
  use the YouTube Data API or rank results.

## 2. Objective and Success Criteria

- One-sentence objective: Turn "play/search X on YouTube" into an
  actually-opened YouTube search for X.
- Success criteria: query extracted and search opens; missing query asks
  first; works with or without the YouTube app installed.

## 3. Capabilities and Actions

| action | what it does | maps to capabilities |
|---|---|---|
| search_and_open | Builds a YouTube search URL and launches it via ACTION_VIEW | media_search |

## 4. Inputs and Outputs

| action | param key | type | required? | example |
|---|---|---|---|---|
| search_and_open | query | String | yes | "arijit singh new song" |

- On success: ExecutionReport.Success("Opened YouTube search for query"), no data payload needed.

## 5. Tools and Dependencies

- android.content.Context (application context) - needed to call startActivity() from a non-Activity class
- No DAOs, no new Room entity
- Android API touched: Intent(ACTION_VIEW, uri) with FLAG_ACTIVITY_NEW_TASK

## 6. Permissions and Risk

- permissions: [] - ACTION_VIEW with a URL needs no dangerous Android permission
- risk: low
- Default automation_mode: full

## 7. Data Residency and Privacy

- No AiRouter call at all - the query goes straight into a URL, never through Gemini.
- Nothing persisted to Room.

## 8. Approval Rules

- None. requires_approval_for: [] - opening a search is fully reversible.

## 9. Failure Behavior

| Failure case | Report type | Message |
|---|---|---|
| blank/missing query | RequiresUserAction | "What should I search for on YouTube?", reason "missing query" |
| startActivity throws | Failed | "Couldn't open YouTube" |

## 10. Conversation and Clarification Behavior

- One resumable field: query. Reason string is exactly "missing query".
- Single-step plan only - eligible for clarification resumption.

## 11. Example Runs

Example 1:
User: "YouTube and search Arijit Singh song"
Head Agent: "Opened YouTube search for Arijit Singh song"

Example 2 (clarification path):
User: "play"
Head Agent: "What should I search for on YouTube?"
User: "lofi beats"
Head Agent: "Opened YouTube search for lofi beats"

## 12. Out of Scope (for now)

- No playback control
- No YouTube Data API integration or result ranking
- No "play the first result automatically" mode
