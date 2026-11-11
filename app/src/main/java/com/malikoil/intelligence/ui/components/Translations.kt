package com.malikoil.intelligence.ui.components

object Translations {
    fun t(key: String, lang: String): String {
        val entry = DICTIONARY[key] ?: return key
        return when (lang) {
            "ur" -> entry.ur
            "roman" -> entry.roman
            else -> entry.en
        }
    }

    data class Translation(val en: String, val ur: String, val roman: String)

    private val DICTIONARY = mapOf(
        "app_name" to Translation("Malik Oil", "ملک آئل", "Malik Oil"),
        "home_sub" to Translation("Samundri · Punjab", "سمندری · پنجاب", "Samundri · Punjab"),
        "private_workspace" to Translation("PRIVATE OWNER WORKSPACE", "نجی مالک ورک سپیس", "PRIVATE OWNER WORKSPACE"),
        "market" to Translation("Market", "مارکیٹ", "Market"),
        "research" to Translation("Research", "تحقیق", "Research"),
        "alerts" to Translation("Alerts", "الرٹس", "Alerts"),
        "review" to Translation("Review", "جائزہ", "Review"),
        "business" to Translation("Business", "کاروبار", "Business"),
        "reports" to Translation("Daily reports", "روزانہ رپورٹس", "Daily reports"),
        "demo_badge" to Translation("SAMPLE RATES", "نمونہ نرخ", "SAMPLE RATES"),
        "preview_notice" to Translation(
            "Rates are verified observations; 3% bidirectional alert policy active.",
            "نرخ تصدیق شدہ مشاہدات ہیں؛ ۳٪ دونوں سمت الرٹ فعال ہے۔",
            "Rates verified observations hain; 3% dono taraf alert policy active hai."
        ),
        "watchlist_title" to Translation("YOUR MARKET WATCHLIST", "آپ کی مارکیٹ واچ لسٹ", "AAPKI MARKET WATCHLIST"),
        "overview_heading" to Translation("A clearer view of the market.", "مارکیٹ کا واضح جائزہ۔", "Market ka saaf view."),
        "refresh" to Translation("Refresh observations", "مشاہدات تازہ کریں", "Refresh observations"),
        "alerts_quick" to Translation("3% · both directions", "۳٪ · دونوں سمت", "3% · dono taraf"),
        "pending_review" to Translation("Pending review", "زیرِ جائزہ", "Pending review"),
        "owner_approval" to Translation("Owner approval required", "مالک کی منظوری درکار", "Owner approval zaroori"),
        "price_history" to Translation("PRICE HISTORY", "قیمت کی تاریخ", "PRICE HISTORY"),
        "stream_label" to Translation("Commodity · mandi · source", "جنس · منڈی · ذریعہ", "Commodity · mandi · source"),
        "last_observation" to Translation("Last observation", "آخری مشاہدہ", "Aakhri observation"),
        "range_change" to Translation("Range change", "مدت کی تبدیلی", "Range change"),
        "range_high" to Translation("Range high", "سب سے زیادہ", "Range high"),
        "range_low" to Translation("Range low", "سب سے کم", "Range low"),
        "not_anchor" to Translation("not the alert baseline", "الرٹ بنیاد نہیں", "alert baseline nahi"),
        "chart_note" to Translation(
            "Matching grade and source only. Observation time → PKR/kg. No invented candles.",
            "یکساں معیار اور ذریعہ۔ مشاہدے کا وقت → روپے فی کلو۔ فرضی کینڈلز نہیں۔",
            "Matching grade aur source. Observation time → PKR/kg. Fake candles nahi."
        ),
        "export_csv" to Translation("Export CSV", "ایکسپورٹ CSV", "Export CSV"),
        "toggle_demo" to Translation("Toggle sample chart", "نمونہ گراف بدلیں", "Toggle sample chart"),
        "exact_observations" to Translation("Exact observations", "اصل مشاہدات", "Exact observations"),
        "add_sample_rate" to Translation("Add rate observation", "نیا مشاہدہ شامل کریں", "Add rate observation"),
        "commodity" to Translation("Commodity", "جنس", "Commodity"),
        "mandi" to Translation("Mandi", "منڈی", "Mandi"),
        "amount_pkr" to Translation("Amount · PKR", "رقم · روپے", "Amount · PKR"),
        "quoted_unit" to Translation("Quoted unit", "اکائی", "Quoted unit"),
        "submit_review" to Translation("Send for review", "جائزے کے لیے بھیجیں", "Send for review"),
        "quick_test" to Translation("Quick 3% alert test", "فوری ۳٪ الرٹ ٹیسٹ", "Quick 3% alert test"),
        "quick_test_sub" to Translation(
            "Create a sample observation, approve it, and inspect the ±3% alert trigger.",
            "نمونہ اندراج بنائیں، منظور کریں اور ۳٪ الرٹ ٹرگر چیک کریں۔",
            "Sample observation banayein, approve karein aur 3% alert check karein."
        ),
        "alert_rules" to Translation("Your alert rules", "الرٹ کے قواعد", "Alert ke rules"),
        "rules_note" to Translation(
            "±3% movement in both directions. Matching commodity, mandi, grade and source.",
            "دونوں سمت میں ±۳٪ تبدیلی۔ یکساں جنس، منڈی، معیار اور ذریعہ۔",
            "Dono taraf ±3% movement. Matching commodity, mandi, grade aur source."
        ),
        "dedupe_pill" to Translation("Duplicates suppressed", "ڈپلیکیٹ خارج", "Duplicates suppressed"),
        "no_orders_pill" to Translation("No auto trading", "خودکار ٹریڈنگ نہیں", "No auto trading"),
        "research_heading" to Translation("What would you like to understand?", "آپ کیا سمجھنا چاہتے ہیں؟", "Aap kya samajhna chahte hain?"),
        "research_sub" to Translation(
            "Research your market. See the evidence. Decide with context.",
            "مارکیٹ کی تحقیق کریں۔ ثبوت دیکھیں۔ فیصلے کے لیے سیاق و سباق پائیں۔",
            "Market research karein. Evidence dekhein. Context ke sath decide karein."
        ),
        "new_research" to Translation("New research", "نئی تحقیق", "New research"),
        "search_sessions" to Translation("Search saved sessions", "محفوظ گفتگو تلاش کریں", "Search saved sessions"),
        "sessions" to Translation("Sessions", "گفتگو", "Sessions"),
        "plugins" to Translation("Plugins", "پلگ اِنز", "Plugins"),
        "export_research" to Translation("Export session", "گفتگو ایکسپورٹ", "Export session"),
        "rate_converter" to Translation("Rate converter", "نرخ کی تبدیلی", "Rate converter"),
        "whatsapp_row_checker" to Translation("WhatsApp row checker", "واٹس ایپ سطروں کی جانچ", "WhatsApp row checker"),
        "check_rows" to Translation("Check rows", "سطریں چیک کریں", "Check rows"),
        "copy_rows" to Translation("Copy checked rows", "جانچی سطریں کاپی کریں", "Copy checked rows"),
        "ask_question" to Translation("Your public market question", "عوامی مارکیٹ کا سوال", "Aapka public market sawal"),
        "research_web" to Translation("Research the web", "ویب تحقیق کریں", "Research the web"),
        "prompt_news" to Translation("Latest oilseed news", "تیل دار بیجوں کی تازہ خبریں", "Taza oilseed news"),
        "prompt_supply" to Translation("Supply & production", "رسد اور پیداوار", "Supply aur production"),
        "prompt_verify" to Translation("What should I verify?", "مجھے کیا تصدیق کرنی چاہیے؟", "Mujhe kya verify karna chahiye?"),
        "landed_cost_heading" to Translation("Know your landed cost.", "اپنی کل لاگت جانیے۔", "Apni landed cost janiye."),
        "quantity_kg" to Translation("Quantity · kg", "مقدار · کلو", "Quantity · kg"),
        "purchase_pkr" to Translation("Purchase · PKR/kg", "خرید · روپے فی کلو", "Purchase · PKR/kg"),
        "sale_pkr" to Translation("Sale · PKR/kg", "فروخت · روپے فی کلو", "Sale · PKR/kg"),
        "transport_pkr" to Translation("Transport · PKR", "ٹرانسپورٹ · روپے", "Transport · PKR"),
        "other_costs_pkr" to Translation("Other costs · PKR", "دیگر اخراجات · روپے", "Other costs · PKR"),
        "calculate_margin" to Translation("Calculate margin", "منافع کا حساب کریں", "Calculate margin"),
        "landed_cost" to Translation("Landed cost", "کل لاگت", "Landed cost"),
        "revenue" to Translation("Revenue", "آمدنی", "Revenue"),
        "gross_margin" to Translation("Gross margin", "خام منافع", "Gross margin"),
        "break_even" to Translation("Break-even", "نفع نقصان برابر", "Break-even"),
        "margin_percent" to Translation("Margin %", "منافع کا تناسب", "Margin %"),
        "daily_schedule_title" to Translation("18:00 · Asia/Karachi", "شام ۶:۰۰ · پاکستان وقت", "18:00 · Asia/Karachi"),
        "generate_daily" to Translation("Generate today's report", "آج کی رپورٹ بنائیں", "Aaj ki report generate karein"),
        "saved_daily" to Translation("Saved reports", "محفوظ رپورٹس", "Saved reports"),
        "download_report" to Translation("Download Report (.csv)", "رپورٹ ڈاؤن لوڈ کریں", "Download Report (.csv)"),
        "approve_daily_rows" to Translation("Approve rows for report", "رپورٹ کے لیے سطریں منظور کریں", "Report ke liye rows approve karein"),
        "confirm_reviewed" to Translation("I checked the rate, source and observation date.", "میں نے نرخ، ذریعہ اور مشاہدے کی تاریخ چیک کی ہے۔", "Main ne rate, source aur observation date check ki hai."),
        "whatsapp_preview_note" to Translation(
            "Local test inbox. Filter notifications by Sarson and nearby Punjab towns.",
            "مقامی ٹیسٹ اِن باکس۔ نوٹیفکیشن کو سرسوں اور قریبی پنجاب کے شہروں کے لحاظ سے فلٹر کریں۔",
            "Local test inbox. Notifications ko Sarson aur Punjab ke qareebi shehron se filter karein."
        ),
        "simulate_msg" to Translation("Simulate incoming notification", "موصولہ نوٹیفکیشن ٹیسٹ کریں", "Simulate incoming notification"),
        "receive_sim" to Translation("Receive notification", "نوٹیفکیشن وصول کریں", "Receive notification"),
        "maund_eq" to Translation("PKR %s / 40kg · 40kg eq.", "روپے %s فی ۴۰ کلو (من)", "PKR %s / 40kg · 40kg eq."),
        "oil_khali_calculator" to Translation("Oil & Khali Yield", "تیل اور کھل پیداوار", "Oil & Khali Yield"),
        "moisture_calculator" to Translation("Moisture & Deduction", "نمی اور کاٹ کا حساب", "Moisture & Deduction"),
        "freight_matrix" to Translation("Mandi Freight & Distance", "منڈی کرایہ اور فاصلہ", "Mandi Freight & Distance"),
        "rate_card_share" to Translation("Share WhatsApp Rate Card", "واٹس ایپ ریٹ کارڈ شیئر کریں", "Share WhatsApp Rate Card"),
        "break_even_seed_price" to Translation("Max Seed Purchase Price", "زیادہ سے زیادہ خرید ریٹ", "Max Seed Purchase Price"),
        "net_profit" to Translation("Net Crushing Profit", "خالص منافع", "Net Crushing Profit"),
        "profit_per_maund" to Translation("Profit / 40kg Maund", "منافع فی من", "Profit / 40kg Maund"),
        "oil_revenue" to Translation("Oil Revenue", "تیل کی آمدنی", "Oil Revenue"),
        "khali_revenue" to Translation("Khali (Cake) Revenue", "کھل کی آمدنی", "Khali (Cake) Revenue"),
        "net_billable" to Translation("Net Billable Weight", "صاف وزنی مال", "Net Billable Weight"),
        "effective_rate" to Translation("Effective Cost / Maund", "اصل لاگت فی من", "Effective Cost / Maund"),
        "total_savings" to Translation("Deduction Cut / Savings", "کاٹ کی کل رقم", "Deduction Cut / Savings"),
        "insights" to Translation("Market Insights", "مارکیٹ بصیرت و خبریں", "Market Insights"),
        "insights_heading" to Translation("Punjab Oilseed Intelligence", "پنجاب تیل دار اجناس انٹیلیجنس", "Punjab Oilseed Intelligence"),
        "insights_sub" to Translation(
            "Daily news aggregator for Punjab oilseed mandis, expeller crushing demand & regulatory policies.",
            "پنجاب کی غلہ منڈیوں، تیل ایکسپیلر ملوں اور حکومتی ضوابط کی روزانہ تصدیق شدہ خبریں۔",
            "Punjab mandiyon, expeller mills aur mandi policies ki daily verified news."
        ),
        "market_sentiment" to Translation("Mandi Sentiment", "منڈی کا مجموعی رجحان", "Mandi Sentiment"),
        "sentiment_bullish" to Translation("Bullish Demand", "تیزی / طلب مضبوط", "Bullish Demand"),
        "sentiment_stable" to Translation("Stable / Balanced", "مستحکم / متوازن", "Stable / Balanced"),
        "sentiment_bearish" to Translation("Bearish / Softening", "مندی / سست روی", "Bearish / Softening"),
        "all_news" to Translation("All News", "تمام خبریں", "Tamam News"),
        "cat_sarson" to Translation("Sarson & Mustard", "سرسوں و رایا", "Sarson & Mustard"),
        "cat_canola" to Translation("Canola & Taramira", "کینولا و تارامیرا", "Canola & Taramira"),
        "cat_crushing" to Translation("Mill Crushing & Ghee", "ملیں اور گھی صنعت", "Mill Crushing & Ghee"),
        "cat_policy" to Translation("Govt & Mandi Rules", "حکومتی پالیسی و ضوابط", "Govt & Mandi Rules"),
        "search_news_placeholder" to Translation("Search Punjab oilseed news...", "پنجاب کی تیل بیج خبریں تلاش کریں...", "Punjab oilseed news search karein..."),
        "read_source" to Translation("Read Full Source", "مکمل خبر کا ذریعہ", "Read Full Source"),
        "share_news" to Translation("Share News", "خبر شیئر کریں", "Share News"),
        "key_takeaways" to Translation("Key Takeaways for Millers:", "مل مالکان و تاجروں کے لیے اہم نکات:", "Millers ke liye zaroori points:"),
        "live_aggregator" to Translation("Live Aggregator Active", "لائیو نیوز ایگریگیٹر فعال", "Live Aggregator Active"),
        "refresh_news" to Translation("Refresh News", "خبریں تازہ کریں", "News Refresh Karein"),
        "back_to_rates" to Translation("Back to Rates", "واپس ریٹس پر جائیں", "Rates par wapas"),
        "trend_7d_title" to Translation("7-Day Price Trend", "۷ روزہ قیمت کا رجحان", "7-Day Price Trend"),
        "trend_7d_sub" to Translation(
            "Sarson & Canola daily mandi price movement & spread",
            "سرسوں اور کینولا کی روزانہ منڈی قیمتوں اور فرق کا تقابلی جائزہ",
            "Sarson aur Canola ka daily mandi rate movement aur spread"
        ),
        "trend_both" to Translation("Both (Sarson & Canola)", "دونوں (سرسوں و کینولا)", "Both (Sarson & Canola)"),
        "sarson_trend" to Translation("Sarson (Mustard)", "سرسوں (Mustard)", "Sarson (Mustard)"),
        "canola_trend" to Translation("Canola", "کینولا", "Canola"),
        "price_spread" to Translation("Price Spread", "قیمت کا فرق", "Price Spread"),
        "canola_premium" to Translation("Canola Premium", "کینولا پریمیم", "Canola Premium"),
        "touch_to_inspect" to Translation(
            "Touch or drag across chart to inspect daily rates & spread",
            "روزانہ ریٹس اور فرق جانچنے کے لیے چارٹ پر ٹچ کریں",
            "Daily rates aur spread check karne ke liye chart par touch karein"
        ),
        "peak_label" to Translation("7D Peak", "۷ روزہ بلند ترین ریٹ", "7D Peak"),
        "dip_label" to Translation("7D Dip", "۷ روزہ کم ترین ریٹ", "7D Dip"),
        "peak_marker" to Translation("PEAK", "بلند ترین", "PEAK"),
        "dip_marker" to Translation("DIP", "کم ترین", "DIP"),
        "key_extremes" to Translation("7-Day Key Price Extremes", "۷ دن کے اہم ترین ریٹ (بلند و کم ترین)", "7-Day Key Price Extremes"),
        "market_live_badge" to Translation("Mandi Live", "منڈی لائیو", "Mandi Live"),
        "quick_tools_title" to Translation("MILL & EXPELER UTILITIES", "مل و ایکسپیلر کے فوری ٹولز", "MILL & EXPELLER UTILITIES"),
        "target_alerts" to Translation("Target Purchase Alerts", "ہدف خرید ریٹ الرٹس", "Target Purchase Alerts"),
        "target_alerts_sub" to Translation(
            "Set purchase thresholds for Sarson & Canola and receive push notifications when market hits the price.",
            "سرسوں اور کینولا کا ہدف ریٹ سیٹ کریں اور منڈی ریٹ پہنچنے پر پش نوٹیفکیشن پائیں۔",
            "Sarson aur Canola ka target purchase rate set karein aur push notification payein."
        ),
        "set_target_alert" to Translation("Set Target Alert", "ہدف خرید ریٹ مقرر کریں", "Target Alert Set Karein"),
        "target_price" to Translation("Target Price", "ہدف قیمت", "Target Price"),
        "condition_drop_below" to Translation("Drop to / below target (Buy)", "ہدف تک یا نیچے گرے (خرید)", "Target tak ya neechay aye (Buy)"),
        "condition_rise_above" to Translation("Rise to / above target (Sell)", "ہدف تک یا اوپر چڑھے (فروخت)", "Target tak ya ooper jaye (Sell)"),
        "active_target_alerts" to Translation("Active Target Alerts", "فعال ہدف الرٹس", "Active Target Alerts"),
        "movement_history" to Translation("3% Movement History", "۳٪ موومنٹ ہسٹری", "3% Movement History"),
        "simulate_mandi_hit" to Translation("Test Push Notification", "پش نوٹیفکیشن ٹیسٹ کریں", "Push Notification Test Karein"),
        "threshold_hit" to Translation("Threshold Hit", "ہدف ریٹ آ گیا", "Threshold Hit"),
        "quick_target_shortcut" to Translation("Set Target Price Alert", "ہدف ریٹ الرٹ لگائیں", "Target Alert Lagayein"),
        "news_feed_title" to Translation("Oilseed Market News Feed", "تیل دار اجناس منڈی خبریں", "Oilseed Market News Feed"),
        "news_feed_sub" to Translation(
            "Latest agricultural headlines grounded with verified public sources & search queries",
            "تیل دار بیجوں کی تازہ ترین زرعی خبریں، تصدیق شدہ سرچ گراؤنڈنگ اور پبلک ذرائع کے ساتھ",
            "Taza oilseed agri headlines, verified search grounding aur public sources ke sath"
        ),
        "search_grounding_active" to Translation("Search Grounding Active", "گوگل سرچ گراؤنڈنگ فعال", "Search Grounding Active"),
        "grounded_query" to Translation("Grounded Query", "سرچ گراؤنڈنگ کیوری", "Grounded Query"),
        "grounded_sources" to Translation("Grounded Sources", "گراؤنڈ ذرائع", "Grounded Sources"),
        "view_all_news" to Translation("View Full Insights & News", "مکمل خبریں اور انسائٹس دیکھیں", "Poori News & Insights Dekhein"),
        "grounded_badge" to Translation("Search Grounded", "سرچ سے تصدیق شدہ", "Search Grounded")
    )
}
