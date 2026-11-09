# Feature Specification: Local Workflow Test Preview

**Created**: 2026-11-05
**Status**: Implemented for local synthetic workflow testing
**Input**: Run Malik Oil here so the owner can test and analyze it.

## User Scenarios & Testing

### User Story 1 - Test the workflow on this laptop (Priority: P1)

The owner can try sample rate entry, approval, 3% alerts and business margin calculations locally.

**Independent Test**: Open the loopback URL, create/approve a sample quote and inspect alerts.

**Acceptance Scenarios**:
1. **Given** no connected Android device and virtualization disabled, **When** a local alternative
   is offered, **Then** it is clearly identified as a browser preview, not the running APK.
2. **Given** sample data, **When** a qualifying rise/fall is approved, **Then** alert history uses
   the existing database rules and repeated approval does not create another alert.
3. **Given** entered cost figures, **When** calculated, **Then** landed cost and gross margin are
   shown without claiming actual ledger/profit records.

### Edge Cases

Invalid amounts and cross-origin mutations are rejected. Missing native runtime does not imply
an Android test passed. No real credentials or production data are loaded into the preview.

## Requirements

- **FR-001**: Provide a running local test URL and a visible preview panel or clickable link.
- **FR-002**: Clearly label every preview as synthetic and separate from the APK/live services.
- **FR-003**: Reuse the existing SQL alert trigger without changing production auth or migrations.
- **FR-004**: Bind only to loopback, reject foreign Host/Origin mutations and cap request bodies.
- **FR-005**: Keep preview state out of Git/source backups and document restart/test instructions.

## Success Criteria

- **SC-001**: Five isolated workflow tests and local HTTP checks pass.
- **SC-002**: The owner can access a local URL for rates, review, alerts and margin scenarios.
- **SC-003**: Zero real accounts, cloud resources or native APK-runtime success claims are created.

## Assumptions

This fallback was chosen after observing virtualization firmware disabled, hypervisor absent and
no adb-connected device. Actual APK testing still requires a suitable Android runtime/device.
