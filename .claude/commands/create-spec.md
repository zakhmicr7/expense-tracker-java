---
description: Create a spec file and feature branch for the next expense tracker step
argument-hint: "Step number and feature name e.g. 2 category-management"
allowed-tools: Read, Write, Glob, Bash(git:*)
---

You are a senior Java developer building the expense tracker described in CLAUDE.md.
Always follow the rules in CLAUDE.md — especially: no JPA/ORM, no Lombok, JdbcTemplate only,
SQL in Repository layer only, vanilla JS frontend.

User input: $ARGUMENTS

## Step 1 — Check working directory is clean

Run `git status` and check for uncommitted, unstaged, or untracked files.
If any exist, stop immediately and tell the user to commit or stash changes before proceeding.
DO NOT CONTINUE until the working directory is clean.

## Step 2 — Parse the arguments

From $ARGUMENTS extract:

1. `step_number` — zero-padded to 2 digits: 2 → 02, 11 → 11
2. `feature_title` — human readable title in Title Case
   - Example: "Category Management" or "Monthly Summary"
3. `feature_slug` — git and file safe slug
   - Lowercase, kebab-case
   - Only a-z, 0-9 and -
   - Maximum 40 characters
   - Example: category-management, monthly-summary
4. `branch_name` — format: `feature/<feature_slug>`
   - Example: `feature/category-management`

If you cannot infer these from $ARGUMENTS, ask the user to clarify before proceeding.

## Step 3 — Check branch name is not taken

Run `git branch` to list existing branches.
If `branch_name` is already taken, append a number:
`feature/category-management-01`, `feature/category-management-02` etc.

## Step 4 — Switch to main and pull latest

Run:
```
git checkout main
git pull origin main
```

## Step 5 — Create and switch to the feature branch

Run:
```
git checkout -b <branch_name>
```

## Step 6 — Research the codebase

Read these files before writing the spec:

- `CLAUDE.md` — tech stack, conventions, schema, rules
- `src/main/java/com/tracker/controller/ExpenseController.java` — existing endpoints
- `src/main/java/com/tracker/service/ExpenseService.java` — existing business logic
- `src/main/java/com/tracker/repository/ExpenseRepository.java` — existing SQL and schema
- `src/main/resources/schema.sql` — current database schema
- `src/main/resources/application.properties` — current config
- `frontend/index.html` — existing UI structure and JS fetch calls
- All files in `.claude/specs/` — avoid duplicating work already specced

Check `CLAUDE.md` to confirm the requested step is not already marked complete.
If it is, warn the user and stop.

## Step 7 — Write the spec

Generate a spec document with this exact structure:

---
# Spec: <feature_title>

## Overview
One paragraph describing what this feature does and why it exists
at this stage of the expense tracker roadmap.

## Depends on
Which previous steps/specs this feature requires to be complete.

## API changes
Every new or modified REST endpoint:
- `METHOD /api/path` — description — request body fields (if any)

If no new endpoints: state "No new endpoints".

## Database changes
Any new tables, new columns, or modified constraints.
Always verify against `src/main/resources/schema.sql` before writing this.
Reference the exact `ALTER TABLE` or `CREATE TABLE` SQL needed.
If none: state "No database changes".

## Backend files to create
Every new Java file, with its full path and one-line purpose.

## Backend files to modify
Every existing Java file that changes, and what specifically changes in it.

## Frontend changes
What changes in `frontend/index.html`:
- New form fields or UI sections
- New fetch() calls and which endpoints they hit
- New JS functions
- CSS additions

## New Maven dependencies
Any new `<dependency>` blocks needed in `pom.xml`.
If none: state "No new dependencies".

## Rules for implementation
Specific constraints Claude must follow for this feature. Always include:
- No JPA, Hibernate, or any ORM — use JdbcTemplate only
- No Lombok — plain Java POJOs with explicit getters/setters
- SQL lives only in the Repository layer
- All input validation in the Service layer; return 400 with a clear message on failure
- Return 404 if a resource ID does not exist
- Use ResponseEntity<?> for all controller return types
- No JS frameworks or CDN libraries in the frontend
- No inline styles in HTML — use CSS classes only
Plus any feature-specific constraints.

## Definition of done
A specific, testable checklist. Each item must be verifiable by
running the app or hitting an endpoint manually with curl/browser.

---

## Step 8 — Save the spec

Save to: `.claude/specs/<step_number>-<feature_slug>.md`

Create the `.claude/specs/` directory if it does not exist.

## Step 9 — Report to the user

Print a short summary in this exact format:
```
Branch:    <branch_name>
Spec file: .claude/specs/<step_number>-<feature_slug>.md
Title:     <feature_title>
```

Then tell the user:
"Review the spec at `.claude/specs/<step_number>-<feature_slug>.md`
then enter Plan Mode with Shift+Tab twice to begin implementation."

Do not print the full spec in chat unless explicitly asked.