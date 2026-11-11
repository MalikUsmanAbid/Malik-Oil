package com.malikoil.intelligence
import com.malikoil.intelligence.model.*
data class OwnerSnapshot(
 val commodities:List<Commodity>,val rates:List<RateObservation>,
 val candidates:List<RateCandidate>,val notifications:List<NotificationCaptureItem>,
 val reports:List<DailyReportSnapshot>,val sessions:List<ResearchSession>,val alerts:List<MovementAlert>
)
