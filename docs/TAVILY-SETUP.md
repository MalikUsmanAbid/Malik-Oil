# Tavily free setup
1. Open https://app.tavily.com/ and sign in/create an account.
2. Keep the free Researcher plan. Do not add a card, upgrade, or enable pay-as-you-go. Official allowance checked 7 November 2026: 1,000 credits/month; basic search costs 1 credit. Other apps using the same account also consume credits.
3. In the dashboard API Keys area, create a key if needed, name it Malik Oil, and copy it. Never paste it in chat, Android files or source control.
4. Run in PowerShell:

```powershell
& "C:\Users\IDEAL COMPUTERS\Documents\Codex\Projects\Malik-Oil\tools\setup-tavily-key.ps1"
```

Type FREE only after checking pay-as-you-go is OFF, then paste the key at the hidden prompt and press Enter. It is normal that no key characters appear. The script saves Windows-account encrypted credentials in .preview/tavily.credential.xml and restarts the preview, then automatically runs one live Tavily + Gemini research check (normally 1 basic-search credit). Refresh http://127.0.0.1:8765/ and use Research. Existing encrypted Gemini key is still needed for synthesis.

No billing-setting API is called. The application cannot independently enforce the account's billing plan: leave pay-as-you-go OFF in Tavily. Quick uses one basic search; Deeper uses up to three parallel basic searches and requests provider page text. No automatic retries or upgrades. Existing 20-attempt/day limit remains; new atomic 900-search-credit/month reservation cap applies per database, including failed attempts. Other clients or another database can consume the same account credits; pay-as-you-go must remain OFF. Cloud requires migration 0003_search_budget.sql before enabling research; no deployment performed.

Research now uses question + up to two earlier public questions, regional commodity context, search snippets and evidence-checked Gemini findings with numbered sources. Browser New research starts a separate in-memory session. Up to 8 sessions and 12 answers/session are stored encrypted locally. Browser refresh and Android restart retain them. Native sign out clears cached history. Laptop/phone cloud synchronization is not configured. Changing commodity/market starts new context. Conversation history is encrypted locally; it is not stored in browser localStorage or synchronized to cloud. Search snippets and provider-extracted page text are separately labeled; pages are not independently verified, unknown dates remain unknown, and results never become approved mandi rates automatically. This is a focused cited researcher, not full Perplexity product parity or autonomous deep research.

Cloud: TAVILY_API_KEY must be a server secret and TAVILY_FREE_TIER_CONFIRMED=true after account verification. Do not put either provider's key in the Android APK. No cloud deployment performed. The setup-mode APK still needs Firebase/backend configuration to access production APIs.

Official sources:
https://help.tavily.com/articles/8298987937-billing-credits-usage-limits
https://docs.tavily.com/documentation/api-reference/endpoint/search

## One-click launcher
Double-click Setup-Tavily.cmd in the project root. It opens the same hidden-prompt setup script; no key is passed on the command line. After saving, the script now automatically asks the exact Samundri sarson rate question in Deeper mode (up to 3 basic-search credits). A successful search is not proof of a verified current local rate; check the returned source, observation date, unit and market.
