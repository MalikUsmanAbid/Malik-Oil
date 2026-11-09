"""Windows-account encrypted local session vault; never fall back to plaintext."""
import ctypes, hashlib, json, os, uuid
from pathlib import Path
MAX_BYTES=2*1024*1024
class Conflict(ValueError): pass

def protect(data, decrypt=False):
 if os.name!='nt': raise ValueError('Encrypted session storage requires Windows DPAPI')
 from ctypes import wintypes
 class Blob(ctypes.Structure):
  _fields_=[('size',wintypes.DWORD),('data',ctypes.POINTER(ctypes.c_ubyte))]
 def blob(value):
  buffer=(ctypes.c_ubyte*len(value)).from_buffer_copy(value)
  return Blob(len(value),buffer),buffer
 incoming,keep=blob(data);entropy,keep_entropy=blob(b'MalikOil research sessions v1');output=Blob()
 crypt=ctypes.WinDLL('crypt32',use_last_error=True);kernel=ctypes.WinDLL('kernel32',use_last_error=True)
 function=crypt.CryptUnprotectData if decrypt else crypt.CryptProtectData
 function.argtypes=[ctypes.POINTER(Blob),ctypes.c_void_p,ctypes.POINTER(Blob),ctypes.c_void_p,ctypes.c_void_p,wintypes.DWORD,ctypes.POINTER(Blob)]
 function.restype=wintypes.BOOL;kernel.LocalFree.argtypes=[ctypes.c_void_p];kernel.LocalFree.restype=ctypes.c_void_p
 if not function(ctypes.byref(incoming),None,ctypes.byref(entropy),None,None,1,ctypes.byref(output)):
  raise ValueError('Session vault could not be decrypted or encrypted; existing file preserved')
 try: return ctypes.string_at(output.data,output.size)
 finally: kernel.LocalFree(ctypes.cast(output.data,ctypes.c_void_p))

def validate(sessions):
 if not isinstance(sessions,list) or len(sessions)>8: raise ValueError('Maximum 8 sessions')
 ids=set()
 for session in sessions:
  if not isinstance(session,dict) or set(session)-{'id','title','turns','scope'}: raise ValueError('Invalid session')
  try: uuid.UUID(session['id'])
  except (ValueError,KeyError,TypeError,AttributeError): raise ValueError('Invalid session ID')
  if session['id'] in ids: raise ValueError('Duplicate session ID')
  ids.add(session['id'])
  for key,limit in [('title',100),('scope',200)]:
   if not isinstance(session.get(key,''),str) or len(session.get(key,''))>limit: raise ValueError('Invalid session label')
  turns=session.get('turns')
  if not isinstance(turns,list) or len(turns)>12: raise ValueError('Maximum 12 answers per session')
  for turn in turns:
   if not isinstance(turn,dict) or not isinstance(turn.get('question',''),str) or len(turn.get('question',''))>300 or not isinstance(turn.get('text',''),str) or len(turn.get('text',''))>30000: raise ValueError('Invalid saved answer')
   if not isinstance(turn.get('sources'),list) or len(turn['sources'])>10: raise ValueError('Invalid saved sources')
   for source in turn['sources']:
    if not isinstance(source,dict): raise ValueError('Invalid saved source')
    for key,limit in [('title',300),('url',2048),('publisher',300),('publishedAt',80),('kind',40)]:
     if not isinstance(source.get(key,''),str) or len(source.get(key,''))>limit: raise ValueError('Invalid saved source field')
    if 'text' in source: raise ValueError('Full page bodies must not be stored in history')
   for key in ['findings','supports','sourceFailures','searchQueries','context']:
    values=turn.get(key,[])
    if not isinstance(values,list) or len(values)>12: raise ValueError('Invalid saved research details')
    if key in ['findings','supports']:
     for item in values:
      if not isinstance(item,dict) or not isinstance(item.get('sourceId'),int): raise ValueError('Invalid citation')
      for name in (['summary','evidenceQuote'] if key=='findings' else ['quote']):
       if not isinstance(item.get(name),str) or len(item[name])>1000: raise ValueError('Invalid citation text')
    elif any(not isinstance(item,str) or len(item)>1000 for item in values): raise ValueError('Invalid saved query')
 payload=json.dumps(sessions,ensure_ascii=False).encode('utf-8')
 if len(payload)>MAX_BYTES: raise ValueError('Session storage is full; export and delete an older session')
 return payload

class SessionVault:
 def __init__(self,path): self.path=Path(path)
 def read(self):
  if not self.path.exists(): return {'sessions':[],'revision':'empty'}
  if self.path.stat().st_size>MAX_BYTES+65536: raise ValueError('Session vault exceeds size limit')
  encrypted=self.path.read_bytes();sessions=json.loads(protect(encrypted,True));validate(sessions)
  return {'sessions':sessions,'revision':hashlib.sha256(encrypted).hexdigest()}
 def save(self,sessions,revision):
  current=self.read()
  if revision!=current['revision']: raise Conflict('Sessions changed in another tab. Reload before saving; existing file preserved.')
  encrypted=protect(validate(sessions));self.path.parent.mkdir(parents=True,exist_ok=True)
  temporary=self.path.with_suffix('.tmp');temporary.write_bytes(encrypted);os.replace(temporary,self.path)
  return {'revision':hashlib.sha256(encrypted).hexdigest()}
