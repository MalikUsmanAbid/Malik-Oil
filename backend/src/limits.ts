// Count bytes before parsing untrusted upstream responses.
export async function boundedText(response:Response,limit:number){
 const declared=Number(response.headers.get('Content-Length'));
 if(Number.isFinite(declared)&&declared>limit){await response.body?.cancel();throw Error('Response too large')}
 const reader=response.body?.getReader();if(!reader)throw Error('Empty response');
 const decoder=new TextDecoder();let text='',size=0;
 try{while(true){const {done,value}=await reader.read();if(done)break;size+=value.byteLength;if(size>limit)throw Error('Response too large');text+=decoder.decode(value,{stream:true})}return text+decoder.decode()}
 catch(e){await reader.cancel();throw e}
}
