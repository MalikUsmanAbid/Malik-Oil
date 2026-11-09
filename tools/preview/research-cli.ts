// Loopback preview invokes the same adapter as the authenticated cloud route.
import {research} from '../../backend/src/research';
async function main(){
 let input='';
 for await (const chunk of process.stdin)input+=chunk;
 try{console.log(JSON.stringify(await research(JSON.parse(input),{TAVILY_API_KEY:process.env.TAVILY_API_KEY,TAVILY_FREE_TIER_CONFIRMED:process.env.TAVILY_FREE_TIER_CONFIRMED,ENABLE_RESEARCH:process.env.ENABLE_RESEARCH,GEMINI_API_KEY:process.env.GEMINI_API_KEY})))}
 catch(e){console.log(JSON.stringify({error:e instanceof Response?await e.text():e instanceof Error&&e.name==='ZodError'?'Invalid public market question or filters; do not include credentials or contact details.':'Research unavailable',status:e instanceof Response?e.status:e instanceof Error&&e.name==='ZodError'?400:502}));process.exitCode=1}
}
main();
