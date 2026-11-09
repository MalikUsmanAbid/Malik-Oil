"""Local report preview. No phone connection, network calls or background scheduler."""
import hashlib,io,json,os,re,time,zipfile
from datetime import datetime,timedelta,timezone,date
from decimal import Decimal,ROUND_HALF_UP
from pathlib import Path
from xml.sax.saxutils import escape
from session_vault import protect,Conflict
PK=timezone(timedelta(hours=5))
KINDS=('sarson','canola','til','taramira','alsi')
PRIORITY=['Samundri','Faisalabad','Gojra','Toba Tek Singh','Tandlianwala','Jaranwala','Dijkot']
def today(): return datetime.now(PK).date().isoformat()
def timestamp(value):
 if not isinstance(value,str) or len(value)>50: raise ValueError('ISO observation date required')
 d=datetime.fromisoformat(value.replace('Z','+00:00'))
 if d.tzinfo is None or d.timestamp()>time.time() or d.year<2000: raise ValueError('Use an actual observation date with timezone, not a future date')
 return d.astimezone(PK).isoformat()
def label(value,limit):
 if not isinstance(value,str) or not value.strip() or len(value)>limit or any(ord(c)<32 for c in value): raise ValueError('Invalid or missing label')
 return value.strip()
def stream(r): return tuple(r[k] for k in ('commodity','market','grade','source'))
def amount40(r): return (Decimal(r['amount'])*{'kg':Decimal(40),'40kg':Decimal(1),'100kg':Decimal('.4')}[r['unit']]).quantize(Decimal('.01'),rounding=ROUND_HALF_UP)
def make_report(s,language,kind):
 day=today(); eligible=[r for r in s['rates'] if kind=='all' or r['commodity']==kind];latest={};prior={};history={}
 for r in sorted(eligible,key=lambda r:r['observedAt']):
  d=r['observedAt'][:10];key=stream(r)
  if d==day: latest[key]=r
  elif d<day: prior[key]=r
  if (date.fromisoformat(day)-timedelta(days=29)).isoformat()<=d<=day: history[(key,d)]=r
 rates=[]
 for key,r in latest.items():
  old=prior.get(key);prev=amount40(old) if old else None;now=amount40(r)
  rates.append({**r,'price40':str(now),'previous40':str(prev) if prev else None,'previousAt':old['observedAt'] if old else None,'change':str(((now-prev)*100/prev).quantize(Decimal('.01'),rounding=ROUND_HALF_UP)) if prev else None})
 rates.sort(key=lambda r:(s['priority'].index(r['market']) if r['market'] in s['priority'] else 999,r['market']))
 raw=[m for m in s['messages'] if m['capturedAt'][:10]==day and (kind!='sarson' or re.search(r'sarso[nou]*|mustard|سرسوں',m['text'],re.I))]
 headings={'en':'Daily report','roman':'Rozana report','ur':'روزانہ رپورٹ'}
 empty={'en':'No reviewed observations today. Missing rates are unknown.','roman':'Aaj reviewed rates nahi. Missing rate unknown hai.','ur':'آج جائزہ شدہ نرخ نہیں۔ نامعلوم نرخ نامعلوم ہیں۔'}
 counts={'en':('reviewed rates','unreviewed messages'),'roman':('reviewed rates','unreviewed messages'),'ur':('جائزہ شدہ نرخ','غیر جائزہ شدہ پیغامات')}[language]
 summary=headings[language]+': '+day+'\n'+str(len(rates))+' '+counts[0]+' | '+str(len(raw))+' '+counts[1]+'\n'
 summary+='\n'.join(r['commodity']+' | '+r['market']+' | PKR '+r['price40']+'/40kg'+(' | '+r['change']+'%' if r['change'] else '')+' | '+r['source'] for r in rates[:8]) if rates else empty[language]
 summary+='\n'+{'en':'Browser preview only. Group notifications and the 18:00 phone schedule are simulated/not connected.','roman':'Sirf browser preview. Group notifications aur 18:00 phone schedule simulated hain, connected nahi.','ur':'صرف براؤزر پیش منظر۔ گروپ نوٹیفکیشن اور شام ۶ بجے فون کا وقت تمثیلی ہیں، منسلک نہیں۔'}[language]
 return dict(date=day,generatedAt=datetime.now(PK).isoformat(),language=language,filter=kind,summary=summary,rates=rates,history=[{**r,'price40':str(amount40(r))} for r in history.values()],raw=raw)
class DailyPreview:
 def __init__(self,path): self.path=Path(path)
 def read(self):
  if not self.path.exists(): return dict(revision='empty',rates=[],messages=[],reports=[],group='',enabled=False,schedule=False,priority=PRIORITY)
  if self.path.stat().st_size>2200000: raise ValueError('Local report vault exceeds limit')
  encrypted=self.path.read_bytes();s=json.loads(protect(encrypted,True));s['revision']=hashlib.sha256(encrypted).hexdigest();return s
 def action(self,data):
  s=self.read()
  if data.get('revision')!=s['revision']: raise Conflict('Report data changed in another tab. Refresh before saving.')
  action=data.get('action')
  if action=='configure':
   s['group']=label(data.get('group'),100);s['enabled']=data.get('enabled') is True;s['schedule']=data.get('schedule') is True
   names=label(data.get('priority'),400).split(',');s['priority']=list(dict.fromkeys(n.strip() for n in names if n.strip()))[:15]
  elif action=='capture':
   if not s['enabled']: raise ValueError('Enable the simulated collector first')
   if label(data.get('group'),100)!=s['group']: raise ValueError('Exact selected group title does not match')
   text=data.get('text');
   if not isinstance(text,str) or not 1<=len(text.strip())<=5000: raise ValueError('Message must contain 1–5000 characters')
   ident=hashlib.sha256((s['group']+'\0'+text).encode()).hexdigest()
   if not any(m['id']==ident for m in s['messages']): s['messages']=(s['messages']+[dict(id=ident,group=s['group'],text=text,capturedAt=datetime.now(PK).isoformat())])[-200:]
  elif action=='approve':
   if data.get('reviewed') is not True: raise ValueError('Confirm review first')
   source=label(data.get('source'),200);text=data.get('text')
   if not isinstance(text,str) or len(text)>50000: raise ValueError('Invalid rate rows')
   lines=[line for line in text.splitlines() if line.strip()]
   if not 1<=len(lines)<=20: raise ValueError('Use 1–20 explicit rate rows')
   additions=[]
   for line in lines:
    f=[x.strip() for x in line.split('|')]
    if len(f)!=6 or f[0] not in KINDS or f[4] not in ('kg','40kg','100kg'): raise ValueError('Use commodity | mandi | grade | amount | unit | ISO date + timezone')
    if not re.fullmatch(r'[0-9]{1,9}(?:\.[0-9]{1,2})?',f[3]): raise ValueError('Price must have at most two decimals')
    value=Decimal(f[3]);
    if not 0<value<=100000000: raise ValueError('Price outside supported limits')
    r=dict(commodity=f[0],market=label(f[1],80),grade=label(f[2],80),amount=str(value),unit=f[4],observedAt=timestamp(f[5]),source=source)
    if amount40(r)<Decimal('.40'): raise ValueError('Price below one paisa/kg')
    r['id']=hashlib.sha256(json.dumps(r,sort_keys=True).encode()).hexdigest();additions.append(r)
   for r in additions:
    if not any(old['id']==r['id'] for old in s['rates']): s['rates'].append(r)
   if len(s['rates'])>2000: raise ValueError('Maximum 2,000 reviewed rows')
  elif action=='generate':
   lang=data.get('language','roman');kind=data.get('filter','sarson')
   if lang not in ('en','ur','roman') or kind not in (*KINDS,'all'): raise ValueError('Invalid report selection')
   report=make_report(s,lang,kind);s['reports']=([report]+[r for r in s['reports'] if r['date']!=report['date']])[:7]
  else: raise ValueError('Unknown report action')
  s.pop('revision');payload=json.dumps(s,ensure_ascii=False).encode()
  if len(payload)>2*1024*1024: raise ValueError('Report vault full; existing data preserved')
  encrypted=protect(payload);tmp=self.path.with_suffix('.tmp');tmp.write_bytes(encrypted);os.replace(tmp,self.path)
  return self.read()

def workbook(report):
 # Product exporter: standard-library OpenXML, literal source strings, no network or macros.
 ns='http://schemas.openxmlformats.org/spreadsheetml/2006/main';rel='http://schemas.openxmlformats.org/officeDocument/2006/relationships';pkg='http://schemas.openxmlformats.org/package/2006/relationships'
 def x(v): return escape(''.join(c for c in str(v) if (ord(c)>=32 and ord(c) not in (0xfffe,0xffff)) or c in '\n\t')).replace('"','&quot;')
 def col(i):
  result=''
  while i: i,n=divmod(i-1,26);result=chr(65+n)+result
  return result
 def sheet(rows,widths,chart=False):
  cells=[]
  for i,row in enumerate(rows,1):
   a=[]
   for j,v in enumerate(row,1):
    ref=col(j)+str(i);style='1' if i==1 else '0'
    a.append(f'<c r="{ref}" s="{style}"><v>{v}</v></c>' if isinstance(v,(int,Decimal)) else f'<c r="{ref}" s="{style}" t="inlineStr"><is><t xml:space="preserve">{x(v)}</t></is></c>')
   cells.append(f'<row r="{i}" ht="70" customHeight="1">'+''.join(a)+'</row>')
  return f'<worksheet xmlns="{ns}" xmlns:r="{rel}"><sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" state="frozen"/></sheetView></sheetViews><cols>'+''.join(f'<col min="{i}" max="{i}" width="{w}" customWidth="1"/>' for i,w in enumerate(widths,1))+'</cols><sheetData>'+''.join(cells)+'</sheetData>'+('<drawing r:id="rId1"/>' if chart else '')+'</worksheet>'
 summary=[['Malik Oil · BROWSER TEST REPORT',report['date']]]+[[s] for s in report['summary'].splitlines()]+[['Generated at (Pakistan)',report['generatedAt']]]
 rates=[['Commodity','Mandi','Grade','Original PKR','Unit','PKR/40kg','Previous PKR/40kg','Change %','Observed at','Previous observed','Source']]
 for r in report['rates']: rates.append([r['commodity'],r['market'],r['grade'],Decimal(r['amount']),r['unit'],Decimal(r['price40']),Decimal(r['previous40']) if r['previous40'] else '',Decimal(r['change']) if r['change'] else '',r['observedAt'],r['previousAt'] or '',r['source']])
 streams=list(dict.fromkeys(stream(r) for r in report['history']))[:4];days=[date.fromisoformat(report['date'])-timedelta(days=i) for i in range(29,-1,-1)];lookup={(stream(r),r['observedAt'][:10]):Decimal(r['price40']) for r in report['history']}
 trends=[['Date']+[' / '.join(key) for key in streams]]+[[str(d)]+[lookup.get((key,str(d)),'') for key in streams] for d in days]
 raw=[['Captured at (not observation date)','Group','Unreviewed text','Fingerprint']]+[[r['capturedAt'],r['group'],r['text'],r['id']] for r in report['raw']]
 parts={};names=['Summary','Rates','Trends','Unreviewed']
 for i,(rows,widths) in enumerate([(summary,[110,32]),(rates,[18,25,20,20,14,20,24,18,32,32,45]),(trends,[20]+[40]*len(streams)),(raw,[32,30,100,70])],1): parts[f'xl/worksheets/sheet{i}.xml']=sheet(rows,widths,i==3 and bool(streams))
 parts['xl/workbook.xml']=f'<workbook xmlns="{ns}" xmlns:r="{rel}"><sheets>'+''.join(f'<sheet name="{n}" sheetId="{i}" r:id="rId{i}"/>' for i,n in enumerate(names,1))+'</sheets></workbook>'
 parts['_rels/.rels']=f'<Relationships xmlns="{pkg}"><Relationship Id="rId1" Type="{rel}/officeDocument" Target="xl/workbook.xml"/></Relationships>'
 parts['xl/_rels/workbook.xml.rels']=f'<Relationships xmlns="{pkg}">'+''.join(f'<Relationship Id="rId{i}" Type="{rel}/worksheet" Target="worksheets/sheet{i}.xml"/>' for i in range(1,5))+f'<Relationship Id="rId5" Type="{rel}/styles" Target="styles.xml"/></Relationships>'
 parts['xl/styles.xml']=f'<styleSheet xmlns="{ns}"><fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF176548"/></patternFill></fill></fills><borders count="1"><border/></borders><cellStyleXfs count="1"><xf/></cellStyleXfs><cellXfs count="2"><xf fontId="0" fillId="0" borderId="0" numFmtId="0" applyAlignment="1"><alignment wrapText="1" vertical="top"/></xf><xf fontId="1" fillId="2" borderId="0" numFmtId="0" applyAlignment="1"><alignment wrapText="1" vertical="center"/></xf></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles></styleSheet>'
 if streams:
  parts['xl/worksheets/_rels/sheet3.xml.rels']=f'<Relationships xmlns="{pkg}"><Relationship Id="rId1" Type="{rel}/drawing" Target="../drawings/drawing1.xml"/></Relationships>'
  parts['xl/drawings/drawing1.xml']=f'<xdr:wsDr xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"><xdr:twoCellAnchor><xdr:from><xdr:col>0</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>34</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:from><xdr:to><xdr:col>8</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>55</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:to><xdr:graphicFrame><xdr:nvGraphicFramePr><xdr:cNvPr id="2" name="Reviewed PKR per 40kg history"/><xdr:cNvGraphicFramePr/></xdr:nvGraphicFramePr><xdr:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/></xdr:xfrm><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/chart"><c:chart xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:r="{rel}" r:id="rId1"/></a:graphicData></a:graphic></xdr:graphicFrame><xdr:clientData/></xdr:twoCellAnchor></xdr:wsDr>'
  parts['xl/drawings/_rels/drawing1.xml.rels']=f'<Relationships xmlns="{pkg}"><Relationship Id="rId1" Type="{rel}/chart" Target="../charts/chart1.xml"/></Relationships>'
  series=''
  for i,key in enumerate(streams):
   cats=''.join(f'<c:pt idx="{j}"><c:v>{d}</c:v></c:pt>' for j,d in enumerate(days));vals=''.join(f'<c:pt idx="{j}"><c:v>{lookup[(key,str(d))]}</c:v></c:pt>' for j,d in enumerate(days) if (key,str(d)) in lookup)
   series+=f'<c:ser><c:idx val="{i}"/><c:order val="{i}"/><c:tx><c:v>{x(" / ".join(key))}</c:v></c:tx><c:marker><c:symbol val="circle"/><c:size val="4"/></c:marker><c:cat><c:strRef><c:f>Trends!$A$2:$A$31</c:f><c:strCache><c:ptCount val="30"/>{cats}</c:strCache></c:strRef></c:cat><c:val><c:numRef><c:f>Trends!${col(i+2)}$2:${col(i+2)}$31</c:f><c:numCache><c:formatCode>0.00</c:formatCode><c:ptCount val="30"/>{vals}</c:numCache></c:numRef></c:val></c:ser>'
  parts['xl/charts/chart1.xml']=f'<c:chartSpace xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"><c:chart><c:autoTitleDeleted val="1"/><c:plotArea><c:layout/><c:lineChart><c:grouping val="standard"/>{series}<c:axId val="123"/><c:axId val="456"/></c:lineChart><c:catAx><c:axId val="123"/><c:scaling><c:orientation val="minMax"/></c:scaling><c:axPos val="b"/><c:tickLblPos val="nextTo"/><c:crossAx val="456"/></c:catAx><c:valAx><c:axId val="456"/><c:scaling><c:orientation val="minMax"/></c:scaling><c:axPos val="l"/><c:numFmt formatCode="0.00" sourceLinked="0"/><c:tickLblPos val="nextTo"/><c:crossAx val="123"/></c:valAx></c:plotArea><c:legend><c:legendPos val="b"/></c:legend><c:dispBlanksAs val="gap"/></c:chart></c:chartSpace>'
 overrides={'xl/workbook.xml':'sheet.main','xl/styles.xml':'styles',**{f'xl/worksheets/sheet{i}.xml':'worksheet' for i in range(1,5)}}
 parts['[Content_Types].xml']=f'<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>'+''.join(f'<Override PartName="/{n}" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.{kind}+xml"/>' for n,kind in overrides.items())+('<Override PartName="/xl/charts/chart1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawingml.chart+xml"/><Override PartName="/xl/drawings/drawing1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/>' if streams else '')+'</Types>'
 buffer=io.BytesIO()
 with zipfile.ZipFile(buffer,'w',zipfile.ZIP_DEFLATED) as z:
  for name,value in parts.items(): z.writestr(name,value.encode('utf-8'))
 return buffer.getvalue()
