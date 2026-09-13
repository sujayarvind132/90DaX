import React, { useEffect, useMemo, useState } from 'react';
import { CalendarDays, Check, ChevronRight, Dumbbell, Flame, Home, Mic, Moon, MoreHorizontal, Plus, Settings, Target, Trophy, Brain, X } from 'lucide-react';
import './styles.css';

const initialActivities = [
  { id: 1, time: '06:00', end: '06:30', title: 'Wake up & freshen up', category: 'Health' },
  { id: 2, time: '06:30', end: '08:00', title: 'Workout', category: 'Fitness' },
  { id: 3, time: '08:00', end: '09:00', title: 'Get ready for office', category: 'Personal' },
  { id: 4, time: '09:00', end: '10:00', title: 'Commute + English', category: 'English' },
  { id: 5, time: '10:00', end: '13:00', title: 'Office', category: 'Work' },
  { id: 6, time: '13:00', end: '13:30', title: 'Lunch + walk', category: 'Fitness' },
  { id: 7, time: '13:30', end: '20:00', title: 'Office', category: 'Work' },
  { id: 8, time: '20:00', end: '21:00', title: 'Commute + Google audio', category: 'Google' },
  { id: 9, time: '21:00', end: '22:00', title: 'DSA practice', category: 'Google', detail: 'Solve 2 problems and explain complexity.' },
  { id: 10, time: '22:00', end: '22:40', title: 'System design', category: 'Google', detail: 'One distributed-systems concept.' },
  { id: 11, time: '22:40', end: '23:00', title: 'English speaking', category: 'English', detail: 'Explain a technical topic aloud.' },
  { id: 12, time: '23:00', end: '23:30', title: 'Daily review', category: 'Review' },
  { id: 13, time: '23:30', end: '07:30', title: 'Sleep', category: 'Health' }
];
const nav = [['Today', Home], ['Goals', Target], ['Plan', CalendarDays], ['Progress', Trophy], ['Settings', Settings]];

function minutes(hhmm) { const [h,m] = hhmm.split(':').map(Number); return h*60+m; }
function nowMinutes() { const d=new Date(); return d.getHours()*60+d.getMinutes()+d.getSeconds()/60; }
function dateKey(d) { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
function dayOnly(d) { const x=new Date(d); x.setHours(0,0,0,0); return x; }
function readCompletedForDate(d) { try { const all=JSON.parse(localStorage.getItem('90dax-completed-by-date')||'{}'); return all[dateKey(d)] || {}; } catch (_) { return {}; } }
function writeCompletedForDate(d,value) { try { const all=JSON.parse(localStorage.getItem('90dax-completed-by-date')||'{}'); all[dateKey(d)]=value; localStorage.setItem('90dax-completed-by-date',JSON.stringify(all)); localStorage.setItem('90dax-completed',JSON.stringify(value)); } catch (_) {} }
function statusFor(a,now,completed) {
  if (completed) return 'done';
  if (a.title==='Sleep') return now>=1410 || now<450 ? 'current' : 'missed';
  const start=minutes(a.time), end=minutes(a.end);
  if (now>=start && now<end) return 'current';
  if (now>=end) return 'missed';
  return 'upcoming';
}
function statusForDate(a,selectedDate,now,completed) {
  const today=dayOnly(new Date()), selected=dayOnly(selectedDate);
  if (dateKey(selected)===dateKey(today)) return statusFor(a,now,completed);
  if (selected<today) return completed ? 'done' : 'missed';
  return completed ? 'done' : 'upcoming';
}

function App() {
  const [page,setPage]=useState('Today');
  const [activities,setActivities]=useState(()=>{try{return JSON.parse(localStorage.getItem('90dax-activities')||'null')||initialActivities;}catch(_){return initialActivities;}});
  const [selectedDate,setSelectedDate]=useState(new Date());
  const [completed,setCompleted]=useState(()=>readCompletedForDate(new Date()));
  const [now,setNow]=useState(nowMinutes());
  const [selected,setSelected]=useState(null);
  const [selectedGoal,setSelectedGoal]=useState(null);
  const [showAdd,setShowAdd]=useState(false);
  const [showMore,setShowMore]=useState(false);
  const [settings,setSettings]=useState(()=>{try{return JSON.parse(localStorage.getItem('90dax-settings')||'null')||{dark:true,notifications:true,sleep:true,widget:true};}catch(_){return {dark:true,notifications:true,sleep:true,widget:true};}});
  useEffect(()=>{const id=setInterval(()=>setNow(nowMinutes()),1000);return()=>clearInterval(id);},[]);
  useEffect(()=>localStorage.setItem('90dax-activities',JSON.stringify(activities)),[activities]);
  useEffect(()=>writeCompletedForDate(selectedDate,completed),[completed,selectedDate]);
  useEffect(()=>localStorage.setItem('90dax-settings',JSON.stringify(settings)),[settings]);
  const isToday=dateKey(selectedDate)===dateKey(new Date());
  const statuses=useMemo(()=>activities.map(a=>({...a,status:statusForDate(a,selectedDate,now,!!completed[a.id])})),[activities,selectedDate,now,completed]);
  const doneCount=statuses.filter(a=>a.status==='done').length;
  const current=isToday?statuses.find(a=>a.status==='current'):null;
  const percent=Math.round(doneCount/Math.max(1,activities.length)*100);
  const selectDate=d=>{setSelectedDate(d);setCompleted(readCompletedForDate(d));setSelected(null);};
  const complete=id=>setCompleted(v=>({...v,[id]:true}));
  const resetToday=()=>{setCompleted({});setSelectedDate(new Date());setShowMore(false);};
  const resetAll=()=>{localStorage.removeItem('90dax-completed-by-date');localStorage.removeItem('90dax-completed');setCompleted({});setSelectedDate(new Date());setShowMore(false);};
  const addActivity=data=>{setActivities(v=>[...v,{...data,id:Date.now()}].sort((a,b)=>minutes(a.time)-minutes(b.time)));setShowAdd(false);};
  return <div className="app-shell"><main className="phone">
    <header className="topbar"><div><div className="brand">90DaX</div><div className="eyebrow">90 DAY EXECUTION SYSTEM</div></div><button className="icon-btn" onClick={()=>setShowMore(true)}><MoreHorizontal size={21}/></button></header>
    {page==='Today'&&<Today statuses={statuses} current={current} doneCount={doneCount} percent={percent} selectedDate={selectedDate} onDateSelect={selectDate} onComplete={complete} onSelect={setSelected}/>} 
    {page==='Goals'&&<Goals onSelect={setSelectedGoal}/>} 
    {page==='Plan'&&<Plan activities={activities} onAdd={()=>setShowAdd(true)} onSelect={setSelected}/>} 
    {page==='Progress'&&<Progress activities={activities} statuses={statuses} doneCount={doneCount} percent={percent}/>} 
    {page==='Settings'&&<SettingsPage settings={settings} onChange={key=>setSettings(s=>({...s,[key]:!s[key]}))}/>} 
    <nav className="bottom-nav">{nav.map(([label,Icon])=><button key={label} className={page===label?'active':''} onClick={()=>{setPage(label);setSelected(null);}}><Icon size={20}/><span>{label}</span></button>)}</nav>
    {selected&&<Detail activity={selected} onClose={()=>setSelected(null)} onComplete={()=>{complete(selected.id);setSelected(null)}}/>}
    {selectedGoal&&<GoalDetail goal={selectedGoal} onClose={()=>setSelectedGoal(null)}/>} {showAdd&&<AddActivity onClose={()=>setShowAdd(false)} onAdd={addActivity}/>} {showMore&&<MoreSheet onClose={()=>setShowMore(false)} onResetToday={resetToday} onResetAll={resetAll} onRefresh={()=>{setNow(nowMinutes());setShowMore(false);}}/>}
  </main></div>;
}

function Today({statuses,current,doneCount,percent,selectedDate,onDateSelect,onComplete,onSelect}) {
  const d=selectedDate,today=dateKey(d)===dateKey(new Date()),day=d.toLocaleDateString('en-IN',{weekday:'long'}),month=d.toLocaleDateString('en-IN',{month:'long'});
  const monday=new Date(d),dayIndex=(monday.getDay()+6)%7;monday.setDate(monday.getDate()-dayIndex);
  const week=Array.from({length:7},(_,i)=>{const x=new Date(monday);x.setDate(monday.getDate()+i);return x;});
  return <section className="content"><div className="today-head"><div><h1>{today?'Today':day}</h1><p>{day}, {d.getDate()} {month}</p></div><div className="progress-ring" style={{'--p':`${percent*3.6}deg`}}><strong>{percent}%</strong><small>{doneCount}/{statuses.length}</small></div></div>
    <div className="week">{week.map(x=><span key={dateKey(x)} className={dateKey(x)===dateKey(d)?'selected':''} role="button" tabIndex="0" onClick={()=>onDateSelect(x)} onKeyDown={e=>{if(e.key==='Enter'||e.key===' ')onDateSelect(x)}}><span>{x.toLocaleDateString('en-IN',{weekday:'short'})}</span><br/><b>{x.getDate()}</b></span>)}</div>
    <div className="mission"><Flame size={16}/><span>Keep the line green.</span><b>90 DAYS</b></div>
    <div className="timeline">{statuses.map((a,i)=><Activity key={a.id} activity={a} isCurrent={current?.id===a.id} onComplete={onComplete} onSelect={onSelect} last={i===statuses.length-1}/>)}</div></section>;
}

function Activity({activity:a,isCurrent,onComplete,onSelect,last}) { const state=a.status; return <div className={`timeline-row ${state}`}><div className="rail"><div className="dot">{state==='done'&&<Check size={13} strokeWidth={3}/>} {state==='current'&&<span/>}</div>{!last&&<div className="line"/>}</div><div className={`activity-card ${isCurrent?'focus':''}`} role="button" tabIndex="0" onClick={()=>onSelect(a)} onKeyDown={e=>{if(e.key==='Enter'||e.key===' ')onSelect(a)}}><div className="card-top"><span>{a.time}</span><span>{a.end}</span><em>{a.category}</em></div><h3>{a.title}</h3>{isCurrent&&<><div className="now-label">NOW</div><div className="live-bar"><i/></div><div className="card-bottom"><span>In progress</span><button className="complete" onClick={e=>{e.stopPropagation();onComplete(a.id)}}>COMPLETE</button></div></>}{state==='done'&&<div className="done-text">Completed</div>}{state==='missed'&&<div className="done-text">Missed</div>}</div></div>; }
function Detail({activity,onClose,onComplete}){return <div className="overlay"><div className="sheet"><button className="close" onClick={onClose}><X/></button><span className="pill">{activity.category}</span><h2>{activity.title}</h2><p>{activity.time} — {activity.end}</p><div className="detail-box"><Brain/><strong>Today's target</strong><span>{activity.detail||'Execute this block with full focus.'}</span></div><button className="primary" onClick={onComplete}><Check/> Complete activity</button></div></div>}
function GoalDetail({goal,onClose}){return <div className="overlay"><div className="sheet"><button className="close" onClick={onClose}><X/></button><span className="pill">90 DAY GOAL</span><h2>{goal.title}</h2><p>{goal.value}</p><div className="detail-box"><Target/><strong>Execution focus</strong><span>{goal.text}</span></div><button className="primary" onClick={onClose}>Got it</button></div></div>}
function Goals({onSelect}){const goals=[{title:'Fitness',value:'93 → 78 kg',text:'Lose 15 kg with consistent training, nutrition and 8h sleep.',icon:Dumbbell},{title:'Google Preparation',value:'DSA · System Design · Coding',text:'Build interview-ready problem solving and design skills.',icon:Flame},{title:'English',value:'Speak with confidence',text:'Daily listening, technical speaking and mock interview practice.',icon:Mic}];return <section className="content"><h1>90 Day Mission</h1><p className="subtitle">Build the body. Build the skills.</p><div className="days-left"><strong>90</strong><span>DAYS<br/>TO EXECUTE</span></div>{goals.map(g=><Goal key={g.title} {...g} onClick={()=>onSelect(g)}/>)}</section>}
function Goal({icon:Icon,title,value,text,onClick}){return <div className="goal-card" role="button" tabIndex="0" onClick={onClick} onKeyDown={e=>{if(e.key==='Enter'||e.key===' ')onClick()}}><div className="goal-icon"><Icon/></div><div><span>{title}</span><h3>{value}</h3><p>{text}</p><div className="bar"><i/></div></div><ChevronRight/></div>}
function Plan({activities,onAdd,onSelect}){return <section className="content"><div className="section-head"><div><h1>Plan</h1><p className="subtitle">Your day, deliberately designed.</p></div><button className="add" onClick={onAdd}><Plus size={18}/></button></div><div className="plan-list">{activities.map(a=><div className="plan-item" key={a.id} role="button" tabIndex="0" onClick={()=>onSelect(a)} onKeyDown={e=>{if(e.key==='Enter'||e.key===' ')onSelect(a)}}><time>{a.time}</time><div><strong>{a.title}</strong><small>{a.category} · {a.end}</small></div><ChevronRight size={16}/></div>)}</div></section>}
function Progress({activities,statuses,doneCount,percent}){const cat=n=>{const rows=statuses.filter(a=>a.category===n);return rows.length?Math.round(rows.filter(a=>a.status==='done').length/rows.length*100):0;};const system=statuses.find(a=>a.title==='System design');return <section className="content"><h1>Progress</h1><p className="subtitle">Small wins compound.</p><div className="score"><span>Today's execution</span><strong>{percent}%</strong><div className="bar big"><i style={{width:`${percent}%`}}/></div><small>{doneCount} activities completed</small></div><div className="stats"><Stat label="Fitness" value={`${cat('Fitness')}%`} percent={cat('Fitness')}/><Stat label="Google" value={`${cat('Google')}%`} percent={cat('Google')}/><Stat label="System Design" value={system?.status==='done'?'Complete':'Daily'} percent={system?.status==='done'?100:0}/><Stat label="English" value={`${cat('English')}%`} percent={cat('English')}/></div><div className="streak"><Flame/><div><strong>7 day consistency</strong><span>Keep the streak alive.</span></div><b>7</b></div></section>}
function Stat({label,value,percent=0}){return <div><span>{label}</span><strong>{value}</strong><div className="mini-bar"><i style={{width:`${percent}%`}}/></div></div>}
function SettingsPage({settings,onChange}){return <section className="content"><h1>Settings</h1><p className="subtitle">Make 90DaX fit your life.</p><div className="settings-card"><Setting icon={Moon} label="Dark mode" value={settings.dark} onClick={()=>onChange('dark')}/><Setting icon={CalendarDays} label="Daily notifications" value={settings.notifications} onClick={()=>onChange('notifications')}/><Setting icon={Target} label="8 hour sleep target" value={settings.sleep} onClick={()=>onChange('sleep')}/><Setting icon={Home} label="Execution widget" value={settings.widget} onClick={()=>onChange('widget')}/></div></section>}
function Setting({icon:Icon,label,value,onClick}){return <div className="setting" role="button" tabIndex="0" onClick={onClick} onKeyDown={e=>{if(e.key==='Enter'||e.key===' ')onClick()}}><Icon size={19}/><span>{label}</span><div className={`toggle ${value?'on':''}`}><i/></div></div>}
function MoreSheet({onClose,onResetToday,onResetAll,onRefresh}){return <div className="overlay"><div className="sheet"><button className="close" onClick={onClose}><X/></button><span className="pill">90DAX</span><h2>Quick actions</h2><p>Manage today's execution without changing the main design.</p><button className="primary" onClick={onRefresh}>Refresh current time</button><button className="primary" style={{marginTop:8}} onClick={onResetToday}>Reset today's completion</button><button className="primary" style={{marginTop:8}} onClick={onResetAll}>Reset all completion history</button></div></div>}
function AddActivity({onClose,onAdd}){const [title,setTitle]=useState('');const [time,setTime]=useState('21:00');const [end,setEnd]=useState('22:00');const [category,setCategory]=useState('Google');return <div className="overlay"><div className="sheet"><button className="close" onClick={onClose}><X/></button><h2>Add activity</h2><label>Activity name<input value={title} onChange={e=>setTitle(e.target.value)} placeholder="e.g. DSA practice"/></label><div className="two"><label>Start<input type="time" value={time} onChange={e=>setTime(e.target.value)}/></label><label>End<input type="time" value={end} onChange={e=>setEnd(e.target.value)}/></label></div><label>Category<select value={category} onChange={e=>setCategory(e.target.value)}><option>Fitness</option><option>Google</option><option>English</option><option>Work</option><option>Personal</option><option>Health</option><option>Review</option></select></label><button className="primary" disabled={!title.trim()} onClick={()=>onAdd({title,time,end,category})}><Plus/> Add activity</button></div></div>}
createRoot(document.getElementById('root')).render(<App/>);
