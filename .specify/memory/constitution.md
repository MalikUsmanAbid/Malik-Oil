<!-- Sync Impact Report: template -> 1.0.0. Initial adoption of owner access, provenance,
consent/cost, verified delivery and modularity principles. Added Security and Delivery Constraints
and Development Workflow sections. Removed no adopted principles. Deferred placeholders: none.
Remove this temporary report before committing an amendment. -->
# Malik Oil Constitution

## Core Principles

### I. Private Owner Access

Every business-data endpoint MUST authorize the exact configured owner. Secrets MUST remain
server-side or in ignored local configuration; Android artifacts MUST NOT contain server private
keys or the owner's password. Rationale: a private-owner app must remain private even when copied.

### II. Traceable Rates and Deterministic Alerts

Approved rates MUST retain source, observation time, commodity, market, grade and explicit units.
Unavailable or stale data MUST be labeled. The 3% bidirectional alert rule MUST use deterministic
math with durable deduplication. AI MUST NOT invent or approve rates or place orders.

### III. Owner Consent and No-Cost First

Local installation, builds and fixes are authorized. Cloud deployment, remote mutations,
production messaging and publication MUST remain on hold until the owner requests them.
Free-tier limits and any required future cost MUST be disclosed; no billing or credentials may
be fabricated. Rationale: account access and deployment are distinct from preparing source code.

### IV. Evidence Before Completion Claims

Buildable changes MUST pass relevant compilation and checks. Reports MUST distinguish artifact
verification, device testing and live-provider integration. Pending credentials MUST NOT be
reported as a working connected service. Reversible presentation/configuration changes do not
require redundant tests that merely mirror implementation.

### V. Modular and Recoverable Delivery

The app/backend MUST remain separable from optional laptop-local inference. Commodities and
future adapters MUST use explicit contracts. Source, specifications and reproducible build steps
MUST stay inside the permanent project; cache, signing material and private configuration MUST
remain outside source archives. JARVICS transport MUST await a real agreed endpoint/authority.

## Security and Delivery Constraints

Use authenticated HTTPS for business data. Offline snapshots MUST retain original observation
and save times and be encrypted in app-private storage. Initial APKs without account settings
MUST show setup-required status. A debug build MUST be identified as a test build. No production
uptime, complete Punjab coverage or WhatsApp-history entitlement may be inferred from a scaffold.

## Development Workflow

Use Spec Kit constitution, specification, plan, tasks, implementation and convergence artifacts
for substantive features. Existing features resume their current stage. Keep requirements and
plans in this repository, apply meaningful verification, and record material limitations. User
instructions and actual tool results take precedence over stale project assumptions.

## Governance

This is the initial project constitution. Amendments MUST describe the changed requirement,
rationale and affected checks. Breaking principle changes increment the major version; additional
principles increment the minor version; clarifications increment the patch version. Review each
feature against these principles without treating the document as authorization for deployment.

**Version**: 1.0.0 | **Ratified**: 2026-10-19 | **Last Amended**: 2026-10-19
