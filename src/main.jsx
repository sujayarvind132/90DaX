import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { CalendarDays, Check, ChevronRight, Dumbbell, Flame, Home, Mic, Moon, MoreHorizontal, Play, Plus, Settings, Target, Trophy, Brain, X } from 'lucide-react';
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

const nav = [
  ['Today', Home], ['Goals', Target], ['Plan', CalendarDays], ['Progress', Trophy], ['Settings', Settings]
];

function minutes(hhmm) { const [h, m] = hhmm.split(':').map(Number); return h * 60 + m; }
function nowMinutes() { const d = new Date(); return d.getHours() * 60 + d.getMinutes() + d.getSeconds() / 60; }
function statusFor(a, now, completed) {
  if (completed) return 'done';
  const start = minutes(a.time); const end = minutes(a.end);
  if (a.title === 'Sleep') return now >= 23.5 * 60 || now < 7.5 * 60 ? 'current' : now > 7.5 * 60 ? 'done' : 'upcoming';
  if (now >= start && now < end) return 'current';
  if (now >= end) return 'missed';
  return 'upcoming';
}

function App() {
  const [page, setPage] = useState('Today');
  const [activities, setActivities] = useState(() => JSON.parse(localStorage.getItem('90dax-activities') || 'null') || initialActivities);
  const [completed, setCompleted] = useState(() => JSON.parse(localStorage.getItem('90dax-completed') || '{}'));
  const [now, setNow] = useState(nowMinutes());
  const [selected, setSelected] = useState(null);
  const [showAdd, setShowAdd] = useState(false);

  useEffect(() => { const id = setInterval(() => setNow(nowMinutes()), 1000); return () => clearInterval(id); }, []);
  useEffect(() => localStorage.setItem('90dax-activities', JSON.stringify(activities)), [activities]);
  useEffect(() => localStorage.setItem('90dax-completed', JSON.stringify(completed)), [completed]);

  const statuses = useMemo(() => activities.map(a => ({ ...a, status: statusFor(a, now, !!completed[a.id]) })), [activities, completed, now]);
  const doneCount = statuses.filter(a => a.status === 'done').length;
  const current = statuses.find(a => a.status === 'current') || statuses.find(a => a.status === 'upcoming');
  const percent = Math.round((doneCount / activities.length) * 100);

  const complete = id => setCompleted(v => ({ ...v, [id]: true }));
  const addActivity = data => { setActivities(v => [...v, { ...data, id: Date.now() }].sort((a,b) => minutes(a.time)-minutes(b.time))); setShowAdd(false); };

  return <div className="app-shell">
    <main className="phone">
      <header className="topbar"><div><div className="brand">90DaX</div><div className="eyebrow">90 DAY EXECUTION SYSTEM</div></div><button className="icon-btn"><MoreHorizontal size={21}/></button></header>
      {page === 'Today' && <Today statuses={statuses} current={current} doneCount={doneCount} percent={percent} onComplete={complete} onSelect={setSelected} />}
      {page === 'Goals' && <Goals />}
      {page === 'Plan' && <Plan activities={activities} onAdd={() => setShowAdd(true)} />}
      {page === 'Progress' && <Progress doneCount={doneCount} percent={percent} />}
      {page === 'Settings' && <SettingsPage />}
      <nav className="bottom-nav">{nav.map(([label, Icon]) => <button key={label} className={page===label?'active':''} onClick={()=>setPage(label)}><Icon size={20}/><span>{label}</span></button>)}</nav>
      {selected && <Detail activity={selected} onClose={()=>setSelected(null)} onComplete={()=>{complete(selected.id);setSelected(null)}} />}
      {showAdd && <AddActivity onClose={()=>setShowAdd(false)} onAdd={addActivity} />}
    </main>
  </div>;
}

function Today({ statuses, current, doneCount, percent, onComplete, onSelect }) {
  const d = new Date(); const day = d.toLocaleDateString('en-IN',{weekday:'long'}); const date = d.getDate();
  return <section className="content">
    <div className="today-head"><div><h1>Today</h1><p>{day}, {date} September</p></div><div className="progress-ring" style={{'--p':`${percent*3.6}deg`}}><strong>{percent}%</strong><small>{doneCount}/{statuses.length}</small></div></div>
    <div className="week"><span>Mon<br/><b>14</b></span><span className="selected">Tue<br/><b>15</b></span><span>Wed<br/><b>16</b></span><span>Thu<br/><b>17</b></span><span>Fri<br/><b>18</b></span><span>Sat<br/><b>19</b></span><span>Sun<br/><b>20</b></span></div>
    <div className="mission"><Flame size={16}/><span>Keep the line green.</span><b>{90 - Math.min(89,date % 90)} DAYS</b></div>
    <div className="timeline">{statuses.map((a,i)=><Activity key={a.id} activity={a} isCurrent={current?.id===a.id} onComplete={onComplete} onSelect={onSelect} first={i===0} last={i===statuses.length-1}/>)}</div>
  </section>;
}

function Activity({activity:a,isCurrent,onComplete,onSelect,first,last}) {
  const state=a.status; return <div className={`timeline-row ${state}`}>
    <div className="rail"><div className="dot">{state==='done' && <Check size={13} strokeWidth={3}/>} {state==='current' && <span/>}</div>{!last && <div className="line"/>}</div>
    <button className={`activity-card ${isCurrent?'focus':''}`} onClick={()=>onSelect(a)}>
      <div className="card-top"><span>{a.time}</span><span>{a.end}</span><em>{a.category}</em></div>
      <h3>{a.title}</h3>
      {isCurrent && <><div className="now-label">NOW</div><div className="live-bar"><i/></div><div className="card-bottom"><span>In progress</span><button className="complete" onClick={e=>{e.stopPropagation();onComplete(a.id)}}>COMPLETE</button></div></>}
      {state==='done' && <div className="done-text">Completed</div>}
      {state==='missed' && <div className="done-text">Missed</div>}
    </button>
  </div>;
}

function Detail({activity,onClose,onComplete}) { return <div className="overlay"><div className="sheet"><button className="close" onClick={onClose}><X/></button><span className="pill">{activity.category}</span><h2>{activity.title}</h2><p>{activity.time} — {activity.end}</p><div className="detail-box"><Brain/><strong>Today's target</strong><span>{activity.detail || 'Execute this block with full focus.'}</span></div><button className="primary" onClick={onComplete}><Check/> Complete activity</button></div></div> }

function Goals(){return <section className="content"><h1>90 Day Mission</h1><p className="subtitle">Build the body. Build the skills.</p><div className="days-left"><strong>90</strong><span>DAYS<br/>TO EXECUTE</span></div><Goal icon={Dumbbell} title="Fitness" value="93 → 78 kg" text="Lose 15 kg with consistent training, nutrition and 8h sleep."/><Goal icon={Flame} title="Google Preparation" value="DSA · System Design · Coding" text="Build interview-ready problem solving and design skills."/><Goal icon={Mic} title="English" value="Speak with confidence" text="Daily listening, technical speaking and mock interview practice."/></section>}
function Goal({icon:Icon,title,value,text}){return <div className="goal-card"><div className="goal-icon"><Icon/></div><div><span>{title}</span><h3>{value}</h3><p>{text}</p><div className="bar"><i/></div></div><ChevronRight/></div>}
function Plan({activities,onAdd}){return <section className="content"><div className="section-head"><div><h1>Plan</h1><p className="subtitle">Your day, deliberately designed.</p></div><button className="add" onClick={onAdd}><Plus size={18}/></button></div><div className="plan-list">{activities.map(a=><div className="plan-item" key={a.id}><time>{a.time}</time><div><strong>{a.title}</strong><small>{a.category} · {a.end}</small></div><ChevronRight size={16}/></div>)}</div></section>}
function Progress({doneCount,percent}){return <section className="content"><h1>Progress</h1><p className="subtitle">Small wins compound.</p><div className="score"><span>Today's execution</span><strong>{percent}%</strong><div className="bar big"><i style={{width:`${percent}%`}}/></div><small>{doneCount} activities completed</small></div><div className="stats"><Stat label="Fitness" value="On track"/><Stat label="DSA" value="Daily"/><Stat label="System Design" value="Daily"/><Stat label="English" value="Daily"/></div><div className="streak"><Flame/><div><strong>7 day consistency</strong><span>Keep the streak alive.</span></div><b>7</b></div></section>}
function Stat({label,value}){return <div><span>{label}</span><strong>{value}</strong><div className="mini-bar"><i/></div></div>}
function SettingsPage(){return <section className="content"><h1>Settings</h1><p className="subtitle">Make 90DaX fit your life.</p><div className="settings-card"><Setting icon={Moon} label="Dark mode"/><Setting icon={CalendarDays} label="Daily notifications"/><Setting icon={Target} label="8 hour sleep target"/><Setting icon={Home} label="Execution widget"/></div></section>}
function Setting({icon:Icon,label}){return <div className="setting"><Icon size={19}/><span>{label}</span><div className="toggle on"><i/></div></div>}
function AddActivity({onClose,onAdd}){const [title,setTitle]=useState('');const [time,setTime]=useState('21:00');const [end,setEnd]=useState('22:00');const [category,setCategory]=useState('Google');return <div className="overlay"><div className="sheet"><button className="close" onClick={onClose}><X/></button><h2>Add activity</h2><label>Activity name<input value={title} onChange={e=>setTitle(e.target.value)} placeholder="e.g. DSA practice"/></label><div className="two"><label>Start<input type="time" value={time} onChange={e=>setTime(e.target.value)}/></label><label>End<input type="time" value={end} onChange={e=>setEnd(e.target.value)}/></label></div><label>Category<select value={category} onChange={e=>setCategory(e.target.value)}><option>Fitness</option><option>Google</option><option>English</option><option>Work</option><option>Personal</option></select></label><button className="primary" disabled={!title.trim()} onClick={()=>onAdd({title,time,end,category})}><Plus/> Add activity</button></div></div>}

createRoot(document.getElementById('root')).render(<App/>);
