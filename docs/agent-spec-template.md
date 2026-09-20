# Agent Spec Template

Fill this out before writing agent.json or the Kotlin Agent
implementation for a new capability. The goal is the same one the
LangGraph course this was adapted from has: force every design decision
(what it does, what it needs, how it fails, what it must never do) onto
paper first, so the code is a direct translation of a spec instead of
a set of decisions made ad hoc while typing.

Every section maps to something real in this codebase - by the time this
is filled in, agent.json and the Agent.execute() skeleton mostly write
themselves.

---

## 1. Identity

- id (kebab-case, matches the JSON filename): ___-agent
- name: ___
- category: one of the existing categories (hr, jobs, documents,
  growth, communication, security, media, ...) or a new one
- Role, in one paragraph: What does this agent do, in plain language?
  Who or what is it acting on behalf of?
- Explicitly NOT this agent's job: What might someone assume it does,
  that it actually doesn't?

## 2. Objective and Success Criteria

- One-sentence objective:
- How you'd know it's working (2-4 concrete, checkable bullets)

## 3. Capabilities and Actions

List every TaskStep.action string this agent will handle in its
when (step.action) block. One row per action.

| action | what it does | maps to capabilities in agent.json |
|---|---|---|
| ___ | ___ | ___ |

## 4. Inputs and Outputs

For each action above, the step.params keys it reads and what it
returns via ExecutionReport.

| action | param key | type | required? | example value |
|---|---|---|---|---|
| ___ | ___ | String | yes/no | ___ |

- On success, what does ExecutionReport.Success.message say, and
  what (if anything) goes in its data map?

## 5. Tools and Dependencies

What gets passed into this agent's constructor? Every dependency here is
something AutomationOsApp.kt will need to construct and pass in.

- Kotlin classes / clients needed: ___
- Existing DAOs needed (if it persists anything): ___
- New Room entity/DAO needed? If yes, note it here and design the entity
  before writing the agent.
- Android APIs touched (Intents, Context, permissions): ___

## 6. Permissions and Risk

- permissions list for agent.json: only list a permission if the
  code genuinely checks/needs it through PermissionManager - don't
  copy another agent's list by default.
- risk: low / medium / high / critical - what's the worst
  plausible outcome of this agent acting on bad input or a bug?
- Default automation_mode: informational only right now (the real
  control is AutomationModeStore, set via the Agents tab).

## 7. Data Residency and Privacy

- Does any part of this agent's input ever reach AiRouter.generate()
  with a cloud-routed intentType? If yes, which exact fields go in the
  payload map, and does PrivacyGateway's allowlist already cover them?
- Does this agent write anything to Room (audit log, a new entity,
  memory)? If it's personal/sensitive data, say so here.

## 8. Approval Rules

- Which actions, if any, go in requires_approval_for regardless of
  automation mode? What's irreversible or high-consequence enough to
  demand a human look first?

## 9. Failure Behavior

For every way this can fail, name the exact ExecutionReport type and
draft the message text now.

| Failure case | Report type | Message |
|---|---|---|
| missing required param | RequiresUserAction | reason "missing field" |
| dependency not configured | Failed | ___ |
| dependency call throws | Failed | ___ |
| ambiguous/unparseable input | RequiresUserAction | ___ |

Rule this codebase already learned the hard way: never return
Success unless something you can point to actually happened.

## 10. Conversation and Clarification Behavior

- Which RequiresUserAction reasons should be resumable by HeadAgent's
  clarification state machine? The reason string must follow the
  convention: "missing field1/field2".
- List the fields here, in the order you want them asked:
  1. ___
  2. ___
- Does this agent only ever run as a single-step plan?

## 11. Example Runs

Write 2-3 concrete conversations, start to finish.

Example 1:
User: "___"
Head Agent: "___"
User: "___"
Head Agent: "___"

Example 2 (a failure path):
User: "___"
Head Agent: "___"

## 12. Out of Scope (for now)

What did you deliberately leave out, and why?
