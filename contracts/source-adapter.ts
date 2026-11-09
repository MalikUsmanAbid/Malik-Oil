// Planned ingestion contract; no unverified source fetcher is enabled.
export interface SourceAdapter {
 readonly id:string;
 readonly allowedHosts:readonly string[];
 readonly pollIntervalMinutes:number;
 collect(input:{since:number; signal:AbortSignal}):Promise<{
  status:'ok'|'unavailable'|'schema_changed'|'not_authorized';
  observations:Array<{
   commodity:string;market:string;grade:string;source:string;sourceUrl:string;
   amount:number;unit:'kg'|'40kg'|'100kg';observedAt:number;sourceEventId:string;
  }>;
  fetchedAt:number; checksum:string;
 }>;
}
// Reject unknown unit, missing source timestamp, ambiguous range/grade or changed markup.
// Never use fetch time as observation time. Never substitute AI-invented prices.
