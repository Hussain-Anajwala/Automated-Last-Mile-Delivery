# UI/UX Design Specification

**Project:** Automated Last-Mile Delivery Management Platform  
**Document status:** Approved code-first UI specification  
**Version:** 1.1

## 1. Design goals

The product should feel like a credible logistics operations console: clear, dependable, data-dense without being cluttered, and usable during repeated operational tasks. The product label **LastMile Ops** is a working label for UI exploration; the official project title remains *Automated Last-Mile Delivery Management Platform*.

We will not depend on Google Stitch or another external design generator. Claude Code should implement and refine the interface directly in Thymeleaf, Bootstrap and CSS from this specification. It must inspect the running application at laptop and narrow viewports, use Selenium for critical interactions, and correct visual and functional defects. The implementation—not a mockup—is the source of truth.

## 2. Information architecture and navigation

### Global shell

- Header: product label/logo, current page title or breadcrumb, signed-in user identity and logout.
- Side navigation on desktop; collapsible navigation on narrower screens.
- Main content area with consistent page heading, brief description and primary action.
- Inline feedback for success, validation failures, authorization errors and server errors.
- Footer/secondary area only if it provides useful operational context; avoid decorative noise.

### Role-aware navigation

| Navigation item | Admin | Dispatcher | Agent |
|---|---|---|---|
| Dashboard | Global | Operational scope | Own assigned-delivery scope |
| Deliveries | All records | All operational records | Assigned records only |
| Create delivery | Yes | Yes | No |
| User list / minimal account administration | If approved and implemented | No | No |
| Logout | Yes | Yes | Yes |

The server is the authority for permissions. Removing a menu item does not enforce security.

## 3. Proposed visual system

Use shared CSS variables so changes can be made consistently rather than styling each page independently.

### Color direction

- Primary text: dark navy/charcoal.
- Main surface: white or near-white neutral.
- Background: soft cool gray.
- Primary action: restrained blue.
- Success: green; in-progress/warning: amber; failure/error: red; neutral: gray.
- Do not communicate delivery status using color alone; every status badge includes readable text and a suitable accessible contrast ratio.

### Typography and spacing

- Use a highly legible system font stack or a documented, locally available web font.
- Clear hierarchy: page title, section heading, field label, body text and secondary metadata.
- Consistent spacing scale (for example 4/8/12/16/24/32 px) stored as tokens.
- Reuse border, radius, shadow and focus-ring tokens.
- Use tabular numerals for dashboard metrics if supported by the chosen font.

### Component patterns

- Primary and secondary buttons with consistent states.
- Status badges for `CREATED`, `ASSIGNED`, `OUT_FOR_DELIVERY`, `DELIVERED` and `FAILED`.
- Search/filter toolbar with clear/reset behavior.
- Data table with readable headers, row actions, empty state and pagination.
- Labeled form controls, inline validation and error summary for longer forms.
- Confirmation dialog for sensitive reassignment/deactivation actions.
- Timeline/activity list for delivery status history.
- Dashboard metric card with a label, a number and optional concise definition.
- Accessible alert/toast messages that do not disappear before they can be read.

## 4. Screens and requirements

### 4.1 Login

Fields: email/username and password. Include submit/loading feedback, validation and a useful generic invalid-credentials message. Do not reveal whether an account exists. No public self-registration or email-based password reset is required for the MVP. Seeded credentials must be development-only and documented as such.

### 4.2 Dashboard

Display total deliveries and counts for created/assigned, out for delivery, delivered and failed states. Choose the exact card set after reviewing readability, but all values must be derived from the persisted data. The agent dashboard must be scoped to that agent's deliveries; admin/dispatcher counts may cover all operational records. Optional charting is lower priority than accurate numbers, clear labels and fast load time.

Include a small recent-delivery list or a clear link to filtered deliveries. Avoid fake live maps, stock analytics or fabricated performance metrics.

### 4.3 Delivery list

Required elements:

- Search by delivery code, recipient name or reference/order number.
- Filters for status and assigned agent where the role permits.
- Reset filters, result count and pagination.
- Columns such as delivery code, recipient, city, agent, status, created date and available actions.
- Primary **Create delivery** button for admin/dispatcher roles.
- Empty state for no records and a distinct state for a failed request.
- Role-aware record visibility and actions.

On mobile/narrow windows, allow deliberate horizontal scrolling or switch to compact record cards; never allow controls to overlap.

### 4.4 Create/edit delivery

Group fields into recipient/contact, delivery address, reference and assignment. Required fields: recipient name, phone, delivery address and city. Delivery code, initial status, timestamps and history actors are system managed. Assignment can be empty until a dispatcher assigns an agent.

On edit, show only editable fields for the current role/status. Provide Save and Cancel, inline field messages, preserve valid input after validation errors, and avoid duplicate submissions. Never allow an ordinary form to directly set system-managed status/timestamp/actor fields.

### 4.5 Delivery detail

Show the delivery code, recipient/contact information according to role, address, reference number, assigned agent, current status and timestamps. Show only actions the current user may perform. Display status history chronologically with old/new status, actor, timestamp and reason where present. Failure requires a reason. Do not make terminal delivered records editable through unrestricted state-changing controls.

### 4.6 Minimal user administration (conditional)

If retained after scope review, provide an admin-only table of seeded/demo accounts with name, email, role and active state. Role changes/deactivation require authorization and a confirmation. Do not implement registration, invitations, password reset emails, identity federation or an elaborate access-management subsystem in this MVP.

### 4.7 Operational/health diagnostics

Health endpoints are primarily for automation and do not need a public navigation item. Avoid exposing dependency names, environment variables, secrets or internal stack traces on an unauthenticated screen.

## 5. Interaction and feedback rules

- All visible actions must work and have a defined outcome.
- Primary action is visually distinct; destructive actions use a confirmation when appropriate.
- Show success only after the server confirms persistence.
- Distinguish field validation (`400`), authentication (`401`/login redirect), authorization (`403`), missing record (`404`), conflict/invalid state (`409`) and generic server failure (`5xx`).
- For search, handle empty query, no matches, loading and failure states.
- Preserve search/filter state when navigating back from a record if feasible within the chosen architecture.
- Use stable `data-testid` or equivalent selectors for critical Selenium interactions rather than brittle CSS class chains or translated visible text alone.
- Do not rely on color alone for statuses, errors or selected states.

## 6. Accessibility and responsive behavior

- Semantic headings and landmarks.
- Explicit labels and accessible names for inputs/buttons.
- Keyboard-operable navigation, dialogs and controls.
- Visible keyboard focus and sensible tab order.
- Contrast that meets WCAG AA targets for text and controls where practical.
- Error text connected to its input and understandable without color.
- Responsive layouts tested at common laptop and tablet widths; basic mobile usability is required even though mobile is not a separate native product.

## 7. Code-first UI design workflow

1. Translate the requirements in this document into a page/component inventory before coding.
2. Define shared tokens for color, typography, spacing, cards, tables, badges, inputs and buttons in CSS.
3. Implement the dashboard, delivery list, forms and details in Thymeleaf/Bootstrap using reusable fragments where appropriate.
4. Run the application with real database-backed data; do not substitute hard-coded metrics or placeholder actions.
5. Inspect browser-rendered pages at laptop and narrow viewports when browser inspection is available.
6. Use Selenium selectors and assertions for critical interactions, and correct functional and visual defects.
7. Store screenshots under `docs/evidence/` only after redacting credentials and personal data.

## 8. UI acceptance checklist

- Correct role-specific navigation and record visibility.
- No dead buttons or mocked save operations.
- Create/edit forms display server validation errors clearly.
- Search, filter, reset and pagination work against real data.
- Every workflow state is textually identified.
- Dashboard metrics reflect database queries.
- Status history is readable and chronological.
- Empty, loading, success and error cases are represented.
- Layout works at the agreed laptop viewport and a narrower viewport.
- Critical interactive controls have stable automated-test selectors.
