# TopUp — UI Design Specifications

**Document:** UI Design Specification  
**Project:** TopUp Android Application  
**Platform:** Native Android  
**UI Framework:** Jetpack Compose + Material 3  
**Typography:** Manrope  
**Status:** Agreed Design Decisions

---

## 1. Design Direction

### 1.1 Visual Style

Use **Modern Fintech** as the overall visual direction.

The interface should feel:

- Clean and modern.
- Minimal and uncluttered.
- Professional and trustworthy.
- Easy to understand and navigate.
- Focused on financial information and recharge actions.
- Consistent across light and dark themes.

Avoid unnecessary gradients, decorative elements, excessive shadows, and complicated animations.

### 1.2 Existing Design System

Use the existing TopUp design system.

Do not introduce a new color palette, typography system, or independent design language.

Existing design resources:

- `:core:designsystem`
- `AppColorScheme`
- Existing Material 3 theme implementation
- Manrope typography
- Existing adaptive spacing and shape definitions

The supplied `AppLightColorScheme` and `AppDarkColorScheme` are the source of truth for colors.

---

## 2. Color System

Use the existing application color scheme without changing its color values.

### 2.1 Brand Colors

| Color | Light Theme | Dark Theme | Usage |
|---|---|---|---|
| Primary | `#2970FF` | `#2970FF` | Primary actions, selection, emphasis |
| Secondary | `#9C6BFF` | `#B68CFF` | Secondary emphasis |
| Complementary | `#FFD600` | `#FFD600` | Ratings and meaningful highlights |

### 2.2 Semantic Colors

Use existing semantic colors:

- Success
- Warning
- Error
- Info

Do not use semantic colors as decorative brand accents.

### 2.3 Surface Colors

Use the existing theme values for:

- Background
- Surface
- Elevated surface
- Surface variant
- Outline
- Outline variant
- Primary and secondary content

All screens and components must support both light and dark themes.

### 2.4 Color Usage Guidelines

- Primary blue should be the main interaction color.
- Purple should be used sparingly.
- Gold should primarily represent ratings or meaningful highlights.
- Success green should indicate successful operations.
- Warning orange should indicate warnings.
- Error red should indicate errors.
- Maintain readable contrast in both themes.

---

## 3. Theme Behavior

The application supports three appearance options:

1. System default
2. Light
3. Dark

**Default appearance: System default.**

Requirements:

- Follow the Android system appearance by default.
- Allow users to manually select Light or Dark.
- Persist the selected preference using DataStore.
- Apply theme changes throughout the application.
- Components must use semantic theme colors instead of hardcoded light-theme or dark-theme colors.

---

## 4. Typography

Use the existing Manrope typography system.

General hierarchy:

| Element | Typography |
|---|---|
| Main screen title | Title style |
| Wallet balance | Large display/headline style |
| Package price | Prominent headline style |
| Section title | Title or semibold body style |
| Package information | Small body style |
| Supporting information | Secondary body style |
| Labels | Label or small body style |

Typography guidelines:

- Package prices must be visually prominent.
- Wallet balance must be easy to identify.
- Supporting information should not compete with important amounts.
- Avoid introducing arbitrary font sizes when existing typography styles are suitable.

---

## 5. General Component Styling

### 5.1 Cards

- Use rounded corners.
- Prefer approximately 16dp corner radius where appropriate.
- Use subtle outlines or surface separation.
- Avoid heavy shadows.
- Maintain consistent internal padding.
- Selected cards must have a clearly visible selected state.

### 5.2 Buttons

- Use the existing Material 3 button components and styles.
- Primary actions use the primary blue color.
- Use rounded corners consistent with the design system.
- Maintain clear enabled and disabled states.
- Use appropriate content padding and touch targets.

### 5.3 Text Fields

- Use Material 3 text field components.
- Support focused, unfocused, error, and disabled states.
- Use semantic content colors.
- Display validation messages clearly.

### 5.4 Icons

- Prefer consistent Material or existing design-system icons.
- Avoid mixing unrelated icon styles.
- Use semantic colors.

---

# 6. Top-up Screen

## 6.1 Screen Structure

The Top-up screen contains the following sections in order:

1. Top-up app bar
2. Wallet balance card
3. Mobile number and detected operator section
4. Recharge type tabs
5. Tab-specific content
6. Proceed to Recharge CTA

The screen must use a vertically scrollable layout when content exceeds the available screen height.

Support portrait and landscape orientations.

Preserve the current screen state during configuration changes.

## 6.2 App Bar

The Top-up screen app bar should contain:

- TopUp screen title.
- Settings action.

Use the existing application navigation and app-bar conventions.

## 6.3 Wallet Balance Card

Display the available wallet balance prominently near the top of the screen.

### Visual Design

- Primary blue background.
- White balance amount.
- White supporting text.
- Wallet icon.
- Rounded corners.
- Comfortable internal padding.

### Content

Display:

- Label: `Available balance`
- Wallet balance amount.
- Currency: MMK / Ks.

Example:

`500,000 Ks`

The example is illustrative. Actual balance must come from the backend.

### Behavior

- Fetch wallet balance whenever the Top-up screen opens.
- Show the cached balance immediately when available.
- Refresh the balance from Supabase.
- If refreshing fails, retain the cached balance and show a subtle warning.
- After successful recharge, update the displayed balance using the backend response.
- If offline, display the cached balance but disable recharge.

---

## 6.4 Mobile Number and Operator Section

Place the mobile number input below the wallet balance card.

### Mobile Number Input

- Accept Myanmar local mobile number format: `09xxxxxxxxx`.
- Allow users to type freely.
- Detect the operator when the number becomes valid.
- Show validation feedback for invalid numbers.
- Do not allow recharge when the number is invalid or unsupported.

### Operator Detection

Operator detection is performed locally using the supplied Myanmar mobile prefix mappings.

Supported operators:

- MPT
- ATOM
- Ooredoo
- Mytel

### Detected Operator Display

After successful detection, display:

- Operator logo.
- Operator name.

Use the existing operator logos stored in Supabase Storage.

The operator should be visually identifiable without taking too much screen space.

### Editing the Number

Users can edit the phone number from the Top-up screen.

When the number changes:

- Re-run local operator detection.
- If the operator remains the same, retain the current operator's package data.
- If the operator changes, display the new operator's cached catalog data and clear the previous selection.

---

# 7. Recharge Type Tabs

The Top-up screen contains two tabs:

1. Denominations
2. Packages

Use a Material 3 tab component or an existing design-system equivalent.

### Tab Design

- Two equally sized tabs.
- Clear selected and unselected states.
- Primary blue for selected indication.
- Smooth tab transition where appropriate.

### Tab Behavior

- Switching tabs must not reset the phone number.
- Switching tabs must preserve the detected operator.
- Denomination and package selections must remain independent only if explicitly supported; the active recharge selection must never combine both types.
- Clear the previous active selection when switching tabs to avoid accidental mixed selections.
- The Proceed to Recharge CTA must reflect the selection in the currently active tab.

---

# 8. Denominations Tab

## 8.1 Layout

Display predefined denomination cards in a **three-column grid**.

Use an adaptive grid where necessary to accommodate different screen widths.

### Predefined Denominations

The following values must be displayed:

| Row | Column 1 | Column 2 | Column 3 |
|---|---:|---:|---:|
| 1 | 1,000 Ks | 2,000 Ks | 3,000 Ks |
| 2 | 5,000 Ks | 10,000 Ks | 20,000 Ks |
| 3 | 30,000 Ks | | |

### Card Design

Each denomination card should:

- Display the denomination amount.
- Use a clean, minimal layout.
- Have rounded corners.
- Use subtle borders.
- Have consistent dimensions and spacing.
- Clearly indicate the selected state.

### Selection State

When a denomination is selected:

- Change its border to primary blue.
- Apply a subtle primary-tinted background.
- Display a checkmark in the top-right corner.
- Animate the selection change smoothly.

Only one denomination can be selected at a time.

---

## 8.2 Custom Amount

Display a dedicated CTA below the denomination grid:

`Enter custom amount`

Do not display the custom amount input by default.

### Interaction

When the user taps the CTA:

- Reveal the custom amount input.
- Use a smooth expand/fade animation.
- Allow the user to enter a custom recharge amount.
- Display the entered amount clearly.
- Provide validation feedback for invalid amounts.

### Selection Rules

- Selecting a predefined denomination clears the custom amount.
- Entering a custom amount clears any predefined denomination selection.
- Only one amount can be active at a time.
- The Proceed to Recharge CTA must use the currently selected valid amount.

### Animation

Use native Compose animation APIs.

Preferred behavior:

- Animated visibility.
- Vertical expansion.
- Fade-in transition.

Keep the animation subtle and quick.

---

# 9. Packages Tab

## 9.1 Layout

Display available data packages in a **two-column grid**.

Use an adaptive grid where necessary to accommodate different screen widths.

Each package must be represented by a card.

## 9.2 Package Card Content

Each package card must contain two main visual sections.

### Section 1: Package Price

Display the package price prominently using large, bold typography.

Example:

`20,000 Ks`

The price must be the most visually prominent information inside the card.

### Section 2: Package Information

Display all package benefits and validity information together in one horizontal row.

Example:

`YouTube + TikTok · 5 GB · 50 Minutes · 30 Days`

Information may include:

- Package name or supported services.
- Data allowance.
- Call minutes.
- Package validity days.

Only display information supplied by the package catalog.

Do not invent missing package benefits or validity information.

### Layout Rules

- Price appears first.
- Package information appears underneath the price.
- Use subtle dot separators (`·`) between information items.
- Keep package information compact.
- Allow natural text wrapping on narrow cards or compact screens.
- Maintain consistent spacing and card dimensions where practical.

## 9.3 Package Card Selection

Only one package can be selected at a time.

### Selected Appearance

A selected package card must have:

1. Primary blue border.
2. Subtle primary-tinted background.
3. Checkmark icon in the top-right corner.

Do not display an additional `Selected` badge or label.

### Unselected Appearance

- Normal surface background.
- Subtle outline.
- No checkmark.

### Animation

Use a subtle animation when selecting or deselecting a package.

Apply the same selection visual language to denomination cards and package cards.

---

# 10. Proceed to Recharge CTA

Display a prominent primary action below the active tab content.

Button label:

`Proceed to Recharge`

### Enabled State

Enable the button only when:

- Mobile number is valid.
- Operator is successfully detected.
- A valid denomination, custom amount, or package is selected.
- Required catalog data is available.
- The application has network connectivity.

### Disabled State

Disable the button when:

- Mobile number is invalid.
- Operator is unsupported.
- No selection has been made.
- Custom amount is invalid.
- Recharge is unavailable because the app is offline.

### Action

When tapped:

- Open the recharge confirmation flow.
- Show the selected phone number.
- Show the detected operator.
- Show the selected denomination or package.
- Show the recharge amount.
- Allow the user to confirm or cancel.

---

# 11. Selection and Interaction Consistency

Use consistent selection behavior throughout the Top-up screen.

| Component | Selection Appearance |
|---|---|
| Denomination card | Blue border + tinted background + top-right checkmark |
| Package card | Blue border + tinted background + top-right checkmark |
| Selected tab | Primary blue indicator |
| Primary CTA | Primary blue background |

Selection rules:

- Only one denomination can be selected at a time.
- Only one package can be selected at a time.
- A custom amount and predefined denomination cannot be active together.
- Denomination and package selections cannot be combined into one recharge.
- Switching tabs clears the previous active recharge selection.

---

# 12. Responsive Layout and Orientation

Support:

- Compact phone screens.
- Medium and expanded window sizes.
- Portrait orientation.
- Landscape orientation.

### Grid Layout

- Denominations: three columns.
- Packages: two columns.

Maintain the intended column count while adapting card dimensions and spacing to available width.

### Orientation Changes

- Do not reload catalog data.
- Preserve the entered phone number.
- Preserve the selected operator.
- Preserve the current tab.
- Preserve the current selection and custom amount input state.
- Avoid unnecessary API calls during configuration changes.

---

# 13. Data and UI State

The UI must observe application state from the ViewModel.

Use the existing architecture and project conventions.

### State to Preserve

The Top-up screen should maintain:

- Wallet balance.
- Phone number.
- Detected operator.
- Active tab.
- Selected denomination.
- Custom amount input visibility.
- Custom amount value.
- Selected package.
- Loading and error states.

Use stable state holders and lifecycle-aware state collection.

Do not place business logic inside composable UI components.

---

# 14. Implementation Guidelines

- Use Jetpack Compose.
- Use Material 3.
- Reuse existing design-system components.
- Reuse existing color and typography definitions.
- Use native Compose animations.
- Keep composables small and reusable.
- Separate UI state from business logic.
- Keep business logic in ViewModels and domain/use-case layers according to existing architecture.
- Avoid unnecessary dependencies.
- Avoid hardcoded operator logos, catalog data, wallet balances, and package details.
- Use Supabase and Room through the existing data architecture.
- Use Room as the catalog UI source of truth.
- Keep the implementation pragmatic for the 72-hour coding-test scope.

---

# 15. Agreed Design Decisions Summary

| Area | Decision |
|---|---|
| Visual direction | Modern Fintech |
| Brand palette | Existing TopUp Blue / Purple / Gold |
| Typography | Manrope |
| UI framework | Jetpack Compose + Material 3 |
| Default theme | System default |
| Manual theme options | Light / Dark / System |
| Wallet balance | Prominent blue card |
| Phone input | Below wallet card |
| Operator detection | Local prefix-based detection |
| Recharge types | Two tabs |
| Tab 1 | Denominations |
| Tab 2 | Packages |
| Denomination grid | Three columns |
| Package grid | Two columns |
| Predefined denominations | 1,000 / 2,000 / 3,000 / 5,000 / 10,000 / 20,000 / 30,000 Ks |
| Custom amount | Dedicated CTA below denomination grid |
| Custom input animation | Expand + fade |
| Package price | Large, bold text |
| Package information | One horizontal row below price |
| Package information separators | Dot separators |
| Selected card | Blue border + tinted background + checkmark |
| Selected badge | Not used |
| Selection animation | Subtle |
| Primary CTA | Proceed to Recharge |

**End of UI Design Specification.**
