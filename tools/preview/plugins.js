(function(root){
'use strict';
function convert(amount,unit){
 if(!/^(?:0|[1-9]\d{0,8})(?:\.\d{1,2})?$/.test(String(amount)))throw Error('Enter a positive PKR amount with at most 2 decimal places');
 const [whole,fraction='']=String(amount).split('.'),paisa=BigInt(whole)*100n+BigInt(fraction.padEnd(2,'0'));
 if(paisa<=0n||paisa>10000000000n)throw Error('Amount must be above zero and at most PKR 100 million');
 const base={kg:1n,'40kg':40n,'100kg':100n}[unit];if(!base)throw Error('Choose kg, 40kg or 100kg');
 const format=n=>(n/100n)+'.'+(n%100n).toString().padStart(2,'0');
 return Object.fromEntries([['kg',1n],['40kg',40n],['100kg',100n]].map(([name,kg])=>[name,format((paisa*kg+base/2n)/base)]));
}
function checkRows(text,now=Date.now()){
 if(typeof text!=='string'||text.length>50000)throw Error('Maximum 50,000 characters');
 const lines=text.split(/\r?\n/).map(x=>x.trim()).filter(Boolean);if(!lines.length||lines.length>20)throw Error('Paste 1 to 20 explicit rate rows');
 return lines.map((line,index)=>{const fields=line.split('|').map(x=>x.trim());if(fields.length!==6)throw Error('Row '+(index+1)+': commodity | market | grade | amount | unit | ISO date with timezone');const [commodity,market,grade,amount,unit,date]=fields;
 if(!['sarson','canola','til','taramira','alsi'].includes(commodity)||!market||market.length>80||!grade||grade.length>80)throw Error('Row '+(index+1)+': invalid commodity, market or grade');convert(amount,unit);
 const parts=date.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.\d{1,9})?)?(Z|[+-]\d{2}:\d{2})$/);if(!parts)throw Error('Row '+(index+1)+': ISO timestamp with timezone required');const calendar=new Date(0);calendar.setUTCFullYear(+parts[1],+parts[2],0);if(+parts[2]<1||+parts[2]>12||+parts[3]<1||+parts[3]>calendar.getUTCDate()||+parts[4]>23||+parts[5]>59||+(parts[6]||0)>59)throw Error('Row '+(index+1)+': invalid calendar date');
 if(!/^\d{4}-\d{2}-\d{2}T.*(?:Z|[+-]\d{2}:\d{2})$/.test(date)||!Number.isFinite(Date.parse(date))||Date.parse(date)>now+300000)throw Error('Row '+(index+1)+': dated observation with timezone required; future dates rejected');
 return {commodity,market,grade,amount,unit,date};});
}
const api={convert,checkRows};if(typeof module==='object'&&module.exports)module.exports=api;else root.MalikPlugins=api;
})(globalThis);
